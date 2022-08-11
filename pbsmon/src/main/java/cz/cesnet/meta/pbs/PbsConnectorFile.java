package cz.cesnet.meta.pbs;

import cz.cesnet.meta.pbs.Node.NodeResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Loads data from PBS server by executing an external command and parsing its output.
 * The main idea is to isolate the calling of the PBSPro IFL library to a separate process
 * that dies after the call, so no resource leaks are possible.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class PbsConnectorFile implements PbsConnector {

    private final static Logger log = LoggerFactory.getLogger(PbsConnectorFile.class);


    @Override
    public PBS loadData(PbsServerConfig serverConfig) {
        String serverHost = serverConfig.getHost();
        log.debug("loadData({})", serverHost);

        //run pbsprocaller
        try {
            File tempStdoutFile = File.createTempFile("stdout", ".txt");
            File tempStderrFile = File.createTempFile("stderr", ".txt");
            File tmpDirectory = Paths.get(System.getProperty("java.io.tmpdir", "/tmp")).toFile();

            Process p = new ProcessBuilder("pbsprocaller", serverHost)
                    .inheritIO()
                    .redirectOutput(tempStdoutFile)
                    .redirectError(tempStderrFile)
                    .directory(tmpDirectory)
                    .start();
            int exit = p.waitFor();
            if (exit != 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("pbsprocaller exited with nonzero status ").append(exit).append("\n");
                for (String line : Files.readAllLines(tempStderrFile.toPath(), Charset.defaultCharset())) {
                    sb.append("stderr: ").append(line).append("\n");
                }
                if (!tempStdoutFile.delete()) log.warn("cannot delete file {}", tempStdoutFile);
                if (!tempStderrFile.delete()) log.warn("cannot delete file {}", tempStderrFile);
                throw new RuntimeException(sb.toString());
            }
            PBS pbs = loadFileToMemory(serverConfig, tempStdoutFile);
            if (!tempStdoutFile.delete()) log.warn("cannot delete file {}", tempStdoutFile);
            if (!tempStderrFile.delete()) log.warn("cannot delete file {}", tempStderrFile);
            return pbs;
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException("cannot run pbsprocaller", e);
        }
    }

    /**
     * Replacement for BufferedReader that uses US character as line separator.
     * Enables lines to contain CR and LF characters.
     */
    public class RecordReader implements Closeable {

        private final BufferedInputStream is;
        private final StringBuilder sb = new StringBuilder(4000);

        public RecordReader(BufferedInputStream is) {
            this.is = is;
        }

        public String readLine() throws IOException {
            int ch;
            while ((ch = is.read()) != -1) {
                if (ch == '\u001F') {
                    String line = sb.toString();
                    sb.setLength(0);
                    return line;
                } else {
                    sb.append((char) ch);
                }
            }
            return null;
        }

        @Override
        public void close() throws IOException {
            is.close();
        }
    }

    private PBS loadFileToMemory(PbsServerConfig serverConfig, File file) throws IOException {
        PBS pbs = new PBS(serverConfig);
        PbsServer server = null;
        Map<String, Node> nodes = new HashMap<>();
        Map<String, Queue> queues = new HashMap<>();
        Map<String, Job> jobs = new HashMap<>();
        Map<String, Reservation> reservations = new HashMap<>();
        Map<String, PbsResource> resources = new HashMap<>();
        Map<String, Scheduler> schedulers = new HashMap<>();
        Map<String, Hook> hooks = new HashMap<>();

        try (RecordReader in = new RecordReader(new BufferedInputStream(new FileInputStream(file)))) {
            String objectType = "";
            PbsInfoObject pbsInfoObject = new PbsInfoObject(pbs, "unknown");
            String line;
            while ((line = in.readLine()) != null) {
                //object type separator
                if (line.charAt(0) == '\u001D') {
                    String[] split = line.substring(1).split(",");
                    objectType = split[0];
                    int num = Integer.parseInt(split[1]);
                    switch (objectType) {
                        case "nodes":
                            nodes = new HashMap<>((int) (num * 1.5));
                            break;
                        case "queues":
                            queues = new HashMap<>((int) (num * 1.5));
                            break;
                        case "jobs":
                            jobs = new HashMap<>((int) (num * 1.5));
                            break;
                        case "reservations":
                            reservations = new HashMap<>((int) (num * 1.5));
                            break;
                        case "resources":
                            resources = new TreeMap<>();
                            break;
                        case "schedulers":
                            schedulers = new HashMap<>((int) (num * 1.5));
                            break;
                        case "hooks":
                            hooks = new HashMap<>((int) (num * 1.5));
                            break;
                    }
                    continue;
                }
                //object separator
                if (line.charAt(0) == '\u001E') {
                    String name = line.substring(1);
                    switch (objectType) {
                        case "servers":
                            server = new PbsServer(pbs, name);
                            server.setServerConfig(serverConfig);
                            pbsInfoObject = server;
                            break;
                        case "nodes":
                            Node node = new Node(pbs, name);
                            nodes.put(name, node);
                            pbsInfoObject = node;
                            break;
                        case "queues":
                            Queue queue = new Queue(pbs, name);
                            queues.put(name, queue);
                            pbsInfoObject = queue;
                            break;
                        case "jobs":
                            Job job = new Job(pbs, name);
                            jobs.put(name, job);
                            pbsInfoObject = job;
                            break;
                        case "reservations":
                            Reservation reservation = new Reservation(pbs, name);
                            reservations.put(name, reservation);
                            pbsInfoObject = reservation;
                            break;
                        case "resources":
                            PbsResource resource = new PbsResource(pbs, name);
                            resources.put(name, resource);
                            pbsInfoObject = resource;
                            break;
                        case "schedulers":
                            Scheduler scheduler = new Scheduler(pbs, name);
                            schedulers.put(name, scheduler);
                            pbsInfoObject = scheduler;
                            break;
                        case "hooks":
                            Hook hook = new Hook(pbs, name);
                            hooks.put(name, hook);
                            pbsInfoObject = hook;
                            break;
                        default:
                            pbsInfoObject = new PbsInfoObject(pbs, name);
                            log.warn("unrecognized object type " + objectType + " name " + name);
                    }
                    continue;
                }
                //attribute line
                String[] split = line.split("=", 2);
                if (split.length != 2) {
                    log.warn("line {} cannot be split at =; objectType={} pbsInfoObject.name={}", line, objectType, pbsInfoObject.getName());
                } else {
                    pbsInfoObject.attrs.put(split[0].intern(), split[1]);
                }
            }
        }
        pbs.setServer(server);
        pbs.setNodes(nodes);
        pbs.setJobs(jobs);
        pbs.setQueues(queues);
        pbs.setReservations(reservations);
        pbs.setResources(resources);
        pbs.setSchedulers(schedulers);
        pbs.setHooks(hooks);
        return pbs;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("start");
        log.debug("main");
        PbsConnectorFile p = new PbsConnectorFile();
        PbsServerConfig serverConfig = new PbsServerConfig("meta", "meta-pbs.metacentrum.cz", false, false, false, Collections.emptyList());
        PBS pbs = p.loadFileToMemory(serverConfig, new File("meta.txt"));
        pbs.uprav();

        HashSet<Float> specvalues = new HashSet<>();
        List<Node> nodes = pbs.getNodesByName();
        for (Node node : nodes) {
                specvalues.add(node.getSpec());
        }
        System.out.println("specvalues = " + new TreeSet<>(specvalues));

//        for (Queue queue : pbs.getQueuesByPriority()) {
//            System.out.println("queue = " + queue);
//        }
//        for(String nodeName : Arrays.asList("perian50", "hildor27")) {
//            Node node = arien.getNodes().get(nodeName);
//            System.out.println(arien.getServer().getShortName() + " node.name: " + node.getName());
//            System.out.println(arien.getServer().getShortName() + " node.scratch: " + node.getScratch());
//            System.out.println(arien.getServer().getShortName() + " node.scratch.available: " + node.getScratch().getAnyAvailableInHumanUnits());
//        }
//        for (int i = 0; i < 10; i++) {
//            PBS arien = call(p, arienServerConfig);
//            System.out.println(arien.getServer().getShortName() + " jobs: " + arien.getJobsById().size());
//            PBS wagap = call(p, new PbsServerConfig("wagap-pro.cerit-sc.cz", false, false, true, Collections.emptyList()));
//            System.out.println(wagap.getServer().getShortName() + " jobs: " + wagap.getJobsById().size());
//        }
    }

    static PBS call(PbsConnector p, PbsServerConfig server) {
        long start = System.currentTimeMillis();
        PBS pbs = p.loadData(server);
        long end = System.currentTimeMillis();
        pbs.uprav();
        System.out.println();
        System.out.println();
        System.out.println("time " + (end - start));
        System.out.println("pbs.getServer().getShortName() = " + pbs.getServer().getShortName());
        System.out.println("pbs.getServer().getVersion() = " + pbs.getServer().getVersion());
        int jobsNum = pbs.getJobsById().size();
        System.out.println("jobsNum = " + jobsNum);

        return pbs;
    }
}
