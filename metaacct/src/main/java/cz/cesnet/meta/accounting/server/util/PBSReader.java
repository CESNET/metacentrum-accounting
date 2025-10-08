package cz.cesnet.meta.accounting.server.util;

import cz.cesnet.meta.accounting.server.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PBSReader {

    private final static Logger log = LoggerFactory.getLogger(PBSReader.class);

    public static List<PBSRecord> readPBSFile(InputStream is, Date limitForStartedJobs, boolean pbspro) throws IOException, ParseException {
        log.debug("parsing pbspro={}",pbspro);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        List<PBSRecord> records = new ArrayList<>();

        final long limit = limitForStartedJobs.getTime();

        String line;
        DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        while (true) {
            line = br.readLine();
            if (line == null) {
                break;
            }
            if ("".equals(line.trim())) {
                continue;
            }
            PBSRecord record = new PBSRecord();
            String[] entryParts = line.split(";");

            switch (entryParts[1]) {
                case "E":
                    record.setRecordType(PBSRecordType.ENDED);
                    break;
                case "S":
                    record.setRecordType(PBSRecordType.STARTED);
                    break;
                case "D":
                    record.setRecordType(PBSRecordType.DELETED);
                    break;
                case "R":
                    record.setRecordType(PBSRecordType.RERUN);
                    entryParts = Arrays.copyOf(entryParts, 4);
                    entryParts[3] = "";
                    break;
                case "A":
                    record.setRecordType(PBSRecordType.ABORTED);
                    entryParts = Arrays.copyOf(entryParts, 4);
                    entryParts[3] = "";
                    break;
                case "G":
                    record.setRecordType(PBSRecordType.G);
                    break;
                default:
                    continue;
            }

            record.setDateTime(formatter.parse(entryParts[0]).getTime());

            //u jobu starsich nez 30 dnu nema smysl zaznamenavat
            if (record.getDateTime() < limit && record.getRecordType() != PBSRecordType.ENDED && record.getRecordType() != PBSRecordType.G) {
                continue;
            }

            record.setIdString(entryParts[2]);

            String[] messageParts = entryParts[3].split(" ");
            PBSMessage pbsMessage = new PBSMessage();


            for (String s : messageParts) {
                if (s.startsWith("user=")) {
                    pbsMessage.setUser(s.substring(s.indexOf('=') + 1));
                } else if (s.startsWith("group=")) {
                    pbsMessage.setGroup(s.substring(s.indexOf('=') + 1));
                } else if (s.startsWith("jobname=")) {
                    pbsMessage.setJobname(s.substring(s.indexOf('=') + 1));
                } else if (s.startsWith("queue=")) {
                    pbsMessage.setQueue(s.substring(s.indexOf('=') + 1));
                } else if (s.startsWith("ctime=")) {
                    pbsMessage.setCreateTime(Long.parseLong(s.substring(s.indexOf('=') + 1)));
                } else if (s.startsWith("start=")) {
                    pbsMessage.setStartTime(Long.parseLong(s.substring(s.indexOf('=') + 1)));
                } else if (s.startsWith("end=")) {
                    pbsMessage.setEndTime(Long.parseLong(s.substring(s.indexOf('=') + 1)));
                } else if (s.startsWith("Exit_status=")) {
                    pbsMessage.setExitStatus(Integer.parseInt(s.substring(s.indexOf('=') + 1)));
                } else if (s.startsWith("resources_used.walltime=")) {
                    String walltime = s.substring(s.indexOf('=') + 1);
                    pbsMessage.setUsedWalltime(StringUtils.parsePbsLogTimeFormat(walltime));
                } else if (s.startsWith("resources_used.cput=")) {
                    String cputime = s.substring(s.indexOf('=') + 1);
                    pbsMessage.setUsedCputime(StringUtils.parsePbsLogTimeFormat(cputime));
                } else if (s.startsWith("resources_used.ncpus=")) {
                    String ncpus = s.substring(s.indexOf('=') + 1);
                    pbsMessage.setUsedNcpus(Integer.parseInt(ncpus));
                } else if (s.startsWith("resources_used.mem=")) {
                    String mem = s.substring(s.indexOf('=') + 1);
                    pbsMessage.setUsedMem(StringUtils.parsePbsLogSizeFormat(mem));
                } else if (s.startsWith("resources_used.vmem=")) {
                    String vmem = s.substring(s.indexOf('=') + 1);
                    pbsMessage.setUsedVmem(StringUtils.parsePbsLogSizeFormat(vmem));
                } else if (s.startsWith("resources_used.cpupercent=")) {
                    String cpupercent = s.substring(s.indexOf('=') + 1);
                    pbsMessage.setUsedCpupercent(Integer.parseInt(cpupercent));
                } else if (s.startsWith("resources_used.gpupercent=")) {
                    String gpupercent = s.substring(s.indexOf('=') + 1);
                    pbsMessage.setUsedGpupercent(Integer.parseInt(gpupercent));
                } else if (s.startsWith("resources_used.gpumemmaxpercent=")) {
                    String gpumemmaxpercent = s.substring(s.indexOf('=') + 1);
                    pbsMessage.setUsedGpuMemMaxPercent(Integer.parseInt(gpumemmaxpercent));
                } else if (s.startsWith("resources_used.gpupowerusage=")) {
                    String gpupowerusage = s.substring(s.indexOf('=') + 1);
                    pbsMessage.setUsedGpuPowerUsage(Float.parseFloat(gpupowerusage));
                } else if (s.startsWith("Resource_List.walltime=")) {
                    String walltime = s.substring(s.indexOf('=') + 1);
                    pbsMessage.setReqWalltime(StringUtils.parsePbsLogTimeFormat(walltime));
                } else if (s.startsWith("Resource_List.soft_walltime=")) {
                    String walltime = s.substring(s.indexOf('=') + 1);
                    pbsMessage.setSoftWalltime(StringUtils.parsePbsLogTimeFormat(walltime));
                } else if (s.startsWith("resc_req_total.mem=")||s.startsWith("Resource_List.mem=")) {
                    String mem = s.substring(s.indexOf('=') + 1);
                    pbsMessage.setReqMem(StringUtils.parsePbsLogSizeFormat(mem));
                } else if (s.startsWith("resc_req_total.procs=")||s.startsWith("Resource_List.ncpus=")) {
                    pbsMessage.setReqNcpus(Long.parseLong(s.substring(s.indexOf('=') + 1)));
                } else if (s.startsWith("resc_req_total.gpu=")||s.startsWith("Resource_List.ngpus")) {
                    pbsMessage.setReqGpus(Integer.valueOf(s.substring(s.indexOf('=') + 1)));
                } else if (s.startsWith("Resource_List.nodes=")||s.startsWith("Resource_List.select=")) {
                    pbsMessage.setReqNodes(s.substring(s.indexOf('=') + 1));
                } else if (s.startsWith("Resource_List.nodect=")) {
                    String ncpus = s.substring(s.indexOf('=') + 1);
                    pbsMessage.setReqNodect(Integer.parseInt(ncpus));
                } else if (s.startsWith("exec_host=")) {
                    if(pbspro) {
                        // PBSPro může mít vnodeA/1+vnodeB/3*4  kde první číslo je číslo rezervace a druhé počet CPU
                        // čísla CPU tedy nejsou k dispozici
                        int cpuCounter = 0;
                        String[] hosts = substringAfter(s,'=').split("\\+");
                        List<PBSHost> pbsHosts = new ArrayList<>();
                        for (String h : hosts) {
                            int slashIndex = h.indexOf('/');
                            String vnode = h.substring(0, slashIndex);
                            String cpus = h.substring(slashIndex + 1);
                            if (cpus.indexOf('*') > -1) {
                                // vnode/P*C where P is a unique index and C is the number of CPUs assigned to the reservation
                                int cpuNum = Integer.parseInt(substringAfter(cpus,'*'));
                                for(int i=0;i<cpuNum;i++) {
                                    PBSHost onePbsHost = new PBSHost();
                                    onePbsHost.setHostName(vnode);
                                    onePbsHost.setProcessorNumber(cpuCounter++);
                                    pbsHosts.add(onePbsHost);
                                }
                            } else {
                                // vnode/rezervace ... implicitně 1 CPU
                                PBSHost onePbsHost = new PBSHost();
                                onePbsHost.setHostName(vnode);
                                onePbsHost.setProcessorNumber(cpuCounter++);
                                pbsHosts.add(onePbsHost);
                            }
                        }
                        pbsMessage.setExecHosts(pbsHosts);
                    } else {
                        //toto je jedina realna vec = skutecny pocet pridelenych CPU - proto jim nahradime parameter *.ncpus
                        // oddelit predek az po '=' a pak rozdelit podle + na jedntlive stroje
                        String[] hosts = s.substring(s.indexOf('=') + 1).split("\\+");
                        List<PBSHost> pbsHosts = new ArrayList<>();
                        int usedCPUs = 0;

                        for (String h : hosts) {
                            int slashIndex = h.indexOf('/');

                            String cpuNumber = h.substring(slashIndex + 1);
                            String hostname = h.substring(0, slashIndex);
                            // exec_host=eru2.ruk.cuni.cz/0*16  ===> Resource_List.neednodes=eru2.ruk.cuni.cz:cpp=16
                            // je nutne doplnit idcka CPU od napr. 0 .. 16
                            if (cpuNumber.indexOf('*') > -1) {
                                String[] cpuNumberParts = cpuNumber.split("\\*");
                                for (int cpu = Integer.parseInt(cpuNumberParts[0]); cpu < Integer.parseInt(cpuNumberParts[1]); cpu++) {
                                    PBSHost onePbsHost = new PBSHost();
                                    onePbsHost.setHostName(hostname);
                                    onePbsHost.setProcessorNumber(cpu);
                                    pbsHosts.add(onePbsHost);
                                    usedCPUs++;
                                }
                            } else {
                                // obycejny pripad = hostname/CPU_number
                                PBSHost onePbsHost = new PBSHost();
                                onePbsHost.setHostName(hostname);
                                onePbsHost.setProcessorNumber(Integer.parseInt(cpuNumber));
                                pbsHosts.add(onePbsHost);
                                usedCPUs++;
                            }
                        }
                        pbsMessage.setExecHosts(pbsHosts);
                        // this method sets PBS-specific variables using Torque accounting data
                        applyFixForTorque(pbsMessage, usedCPUs);
                    }
                } else if (s.startsWith("sched_nodespec=")) {
                    //specialita Torque od Šimona
                    String sched_nodespec = s.substring(s.indexOf('=') + 1);
                    String[] split = sched_nodespec.split("\\+");
                    List<NodeSpec> nodeSpecs = new ArrayList<>(split.length);
                    for (String spec : split) {
                        nodeSpecs.add(new NodeSpec(spec));
                    }
                    pbsMessage.setNodesSpecs(nodeSpecs);
                } else if (s.startsWith("exec_vnode=")) {
                    //specialita PBSPro
                    String exec_vnode = substringAfter(s,'=');
                    pbsMessage.setNodesSpecs(NodeSpec.parseExecVnodePart(exec_vnode));
                }
            }
            record.setMessageText(pbsMessage);

            if (record.getRecordType() == PBSRecordType.G && record.getMessageText().getStartTime() == 0) {
                continue;
            }
            if (record.getRecordType() == PBSRecordType.ENDED && record.getMessageText().getStartTime() == 0) {
                continue;
            }

            records.add(record);
        }
        return records;
    }

    /**
     * This method sets PBS-specific variables - that are no longer available in new file format - using Torque accounting data.
     * It sets resources_used.ncpus and Resource_List.ncpus parameter equal to the number of actually used CPUs.
     * The number of used CPUs is parsed from exec_host parameter. Finally, resources_used.cpupercent parameter is set
     * to 0, as it cannot be reconstructed.
     *
     * @param pbsMessage an object representing all important parameters of one logged job
     * @param usedCPUs   a number of actually used CPUs by the job
     */

    private static void applyFixForTorque(PBSMessage pbsMessage, int usedCPUs) {
        // a fix for resources_used.ncpus parameter
        pbsMessage.setUsedNcpus(usedCPUs);
        // a fix for resources_used.cpupercent parameter
        pbsMessage.setUsedCpupercent(0);
        //a fix for Resource_List.ncpus parameter (now equals to resources_used.ncpus)
        if (pbsMessage.getReqNcpus() == 0L) pbsMessage.setReqNcpus(usedCPUs);
    }

    private static String substringAfter(String s,char ch) {
        return s.substring(s.indexOf(ch) + 1);
    }
}
