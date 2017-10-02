package cz.cesnet.meta.pbs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;

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

    private PBS loadFileToMemory(PbsServerConfig serverConfig, File file) throws IOException {
        PBS pbs = new PBS(serverConfig);
        PbsServer server = null;
        HashMap<String, Node> nodes = new HashMap<>();
        HashMap<String, Queue> queues = new HashMap<>();
        HashMap<String, Job> jobs = new HashMap<>();
        try (FileReader fr = new FileReader(file)) {
            try (BufferedReader in = new BufferedReader(fr)) {
                String objectType = "";
                PbsInfoObject pbsInfoObject = new PbsInfoObject();
                String line;
                while ((line = in.readLine()) != null) {
                    //object type separator
                    if (line.charAt(0) == '\u001E') {
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
                        }
                        continue;
                    }
                    //object separator
                    if (line.charAt(0) == '\u001F') {
                        String name = line.substring(1);
                        switch (objectType) {
                            case "servers":
                                server = new PbsServer(name);
                                server.setServerConfig(serverConfig);
                                pbsInfoObject = server;
                                break;
                            case "nodes":
                                Node node = new Node(name);
                                nodes.put(name, node);
                                pbsInfoObject = node;
                                break;
                            case "queues":
                                Queue queue = new Queue(name);
                                queues.put(name, queue);
                                pbsInfoObject = queue;
                                break;
                            case "jobs":
                                Job job = new Job(name);
                                jobs.put(name, job);
                                pbsInfoObject = job;
                                break;
                            default:
                                pbsInfoObject = new PbsInfoObject(name);
                                log.warn("unrecognized object type " + objectType + " name " + name);
                        }
                        continue;
                    }
                    //attribute line
                    String[] split = line.split("=", 2);
                    pbsInfoObject.attrs.put(split[0].intern(), split[1]);
                }
            }
        }
        pbs.setServer(server);
        pbs.setNodes(nodes);
        pbs.setJobs(jobs);
        pbs.setQueues(queues);
        return pbs;
    }

    public static void main(String[] args) {
        System.out.println("start");
        log.debug("main");
        PbsConnectorFile p = new PbsConnectorFile();
        for (int i = 0; i < 10; i++) {
            PBS arien = call(p, new PbsServerConfig("arien-pro.ics.muni.cz", true, false, true, Collections.emptyList()));
            System.out.println(arien.getServer().getShortName() + " jobs: " + arien.getJobsById().size());
            PBS wagap = call(p, new PbsServerConfig("wagap-pro.cerit-sc.cz", false, false, true, Collections.emptyList()));
            System.out.println(wagap.getServer().getShortName() + " jobs: " + wagap.getJobsById().size());
        }
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
