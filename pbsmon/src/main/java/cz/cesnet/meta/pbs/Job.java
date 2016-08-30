package cz.cesnet.meta.pbs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Holds information about a job.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class Job extends PbsInfoObject {

    final static Logger log = LoggerFactory.getLogger(Job.class);

    //used attribute names
    public static final String ATTRIBUTE_TIME_CREATED = "ctime";
    public static final String ATTRIBUTE_TIME_ELIGIBLE = "etime";
    public static final String ATTRIBUTE_TIME_MODIFIED = "mtime";
    public static final String ATTRIBUTE_TIME_STARTED = "start_time";
    public static final String ATTRIBUTE_TIME_COMPLETED = "comp_time";
    public static final String ATTRIBUTE_PLANNED_START = "planned_start";
    public static final String ATTRIBUTE_PLANNED_NODES = "planned_nodes";
    public static final String ATTRIBUTE_USED_GPUS = "resc_req_total.gpu";
    public static final String ATTRIBUTE_USED_CPUS = "resc_req_total.procs";
    public static final String ATTRIBUTE_USED_MEMORY = "resources_used.mem";
    public static final String ATTRIBUTE_USED_CPUTIME = "resources_used.cput";
    public static final String ATTRIBUTE_USED_WALLTIME = "resources_used.walltime";
    public static final String ATTRIBUTE_RESERVED_MEMORY = "resc_req_total.mem";
    public static final String ATTRIBUTE_NODE_COUNT = "Resource_List.nodect";
    public static final String ATTRIBUTE_NODES = "Resource_List.nodes";
    public static final String ATTRIBUTE_VARIABLE_LIST = "Variable_List";
    public static final String ATTRIBUTE_JOB_OWNER = "Job_Owner";
    public static final String ATTRIBUTE_JOB_NAME = "Job_Name";
    public static final String ATTRIBUTE_WALLTIME_REMAINING = "Walltime.Remaining";
    public static final String ATTRIBUTE_JOB_STATE = "job_state";
    public static final String ATTRIBUTE_QUEUE = "queue";
    public static final String ATTRIBUTE_COMMENT = "comment";
    public static final String ATTRIBUTE_EXEC_HOST = "exec_host";
    public static final String ATTRIBUTE_SCHEDULED_NODES_SPECS = "sched_nodespec";
    public static final String ATTRIBUTE_EXECUTION_TIME = "Execution_Time";
    public static final String ATTRIBUTE_EXIT_STATUS = "exit_status";

    public static final Comparator<Job> PLANNED_START_JOB_COMPARATOR = (j1, j2) -> {
        Date t1 = j1.getPlannedStart();
        Date t2 = j2.getPlannedStart();
        if (t1 == null && t2 == null) return 0;
        if (t1 == null) return +1;
        if (t2 == null) return -1;
        return t1.compareTo(t2);
    };

    public static final Comparator<Job> TIME_STARTED_JOB_COMPARATOR = (j1, j2) -> {
        Date t1 = j1.getTimeStarted();
        Date t2 = j2.getTimeStarted();
        if (t1 == null && t2 == null) return 0;
        if (t1 == null) return +1;
        if (t2 == null) return -1;
        return t1.compareTo(t2);
    };

    public static final Comparator<Job> FAIRSHARE_JOB_COMPARATOR = (j1, j2) -> {
        int c = j2.getFairshareRank() - j1.getFairshareRank();
        if (c != 0) return c;
        //stejny fairshare,porovnat joby
        return j1.getId().compareTo(j2.getId());
    };

    public static final Comparator<Job> PRIORITY_FAIRSHARE_JOB_COMPARATOR = (j1, j2) -> {
        int c = j2.getQueue().getPriority() - j1.getQueue().getPriority();
        if (c != 0) return c;
        c = j2.getFairshareRank() - j1.getFairshareRank();
        if (c != 0) return c;
        //stejny fairshare,porovnat joby
        return j1.getId().compareTo(j2.getId());
    };


    public Job() {
    }

    public Job(String name) {
        super(name);
    }
    //primary data are inherited from PBSInfoObject

    private int idNum = -1;
    private int idSubNum = -1;
    private int noOfUsedCPU = -1;
    private int noOfUsedGPU = -1;
    private String user = null;
    private String submitDir = null;
    private String workDir = null;
    private long cpuTimeUsedSec = -1L;
    private long wallTimeUsedSec = -1L;
    private String execHostFirst = null;
    private String[] execHostMore = null;
    private Date ctime = null;
    private Date etime = null;
    private Date mtime = null;
    private Date start_time = null;
    private Date comp_time = null;
    private Date executionTime = null;
    private Date planned_start = null;
    private Queue queue = null;

    private Map<String, String> orderedAttributes = null;

    @Override
    public void clear() {
        super.clear();
        user = null;
        submitDir = null;
        execHostFirst = null;
        execHostMore = null;
        ctime = etime = mtime = null;
        queue = null;
    }


    /**
     * Defines order of attributes.
     */
    static String[] orderedAttributeNames = new String[]{
            "submit_args", ATTRIBUTE_COMMENT, ATTRIBUTE_JOB_NAME, ATTRIBUTE_JOB_OWNER, ATTRIBUTE_JOB_STATE, ATTRIBUTE_QUEUE,
            ATTRIBUTE_EXEC_HOST, "Output_Path", "Error_Path", "Join_Path",
            ATTRIBUTE_TIME_CREATED, "qtime", ATTRIBUTE_TIME_ELIGIBLE, ATTRIBUTE_PLANNED_START, ATTRIBUTE_TIME_MODIFIED, "stime", ATTRIBUTE_TIME_STARTED, ATTRIBUTE_TIME_COMPLETED, ATTRIBUTE_WALLTIME_REMAINING,
            ATTRIBUTE_PLANNED_NODES,
            "resc_req_total.nodect", ATTRIBUTE_USED_CPUS, ATTRIBUTE_USED_GPUS,
            ATTRIBUTE_RESERVED_MEMORY, "resc_req_total.vmem", "resc_req_total.walltime",
            "resc_req_total.scratch", "resc_req_total.scratch_volume",
            "Resource_List.ncpus", "Resource_List.neednodes", ATTRIBUTE_NODE_COUNT, ATTRIBUTE_NODES, "Resource_List.gpu",
            "Resource_List.mem", "Resource_List.vmem", "Resource_List.walltime", "Resource_List.scratch",
            "Resource_List.ansys", "Resource_List.matlab", "Resource_List.processed_nodes", ATTRIBUTE_SCHEDULED_NODES_SPECS,
            "resources_used.cpupercent", ATTRIBUTE_USED_CPUTIME, "resources_used.ncpus", ATTRIBUTE_USED_MEMORY,
            "resources_used.vmem", ATTRIBUTE_USED_WALLTIME, "resources_used.fairshare",
            "Keep_Files", "Mail_Points", "Priority", "Rerunable", ATTRIBUTE_VARIABLE_LIST, "Checkpoint", "Hold_Types",
            "server", "depend", "session_id"
    };

    public String getId() {
        return name;
    }

    public int getIdNum() {
        if (idNum == -1) {
            int tecka = name.indexOf('.');
            String jobid = name.substring(0, tecka);
            int pomlcka = jobid.indexOf('-');
            if (pomlcka >= 0) {
                //job from job array
                idNum = Integer.parseInt(jobid.substring(0, pomlcka));
                idSubNum = Integer.parseInt(jobid.substring(pomlcka + 1));
            } else {
                idNum = Integer.parseInt(jobid);
            }
        }
        return idNum;
    }

    public int getIdSubNum() {
        return idSubNum;
    }

    /**
     * Provides a Map of attributes ordered by order specified
     * in orderedAttributeNames.
     *
     * @return Map of attributes
     */
    public Map<String, String> getOrderedAttributes() {
        if (orderedAttributes == null) {
            Map<String, String> ordered = new LinkedHashMap<>((int) (attrs.size() * 1.5));
            Set<String> atKeysCopy = new HashSet<>(attrs.keySet());
            //first move entries from order list
            for (String key : orderedAttributeNames) {
                if (atKeysCopy.contains(key)) {
                    ordered.put(key, attrs.get(key));
                    atKeysCopy.remove(key);
                }
            }
            //move remaining
            List<String> sorted = new ArrayList<>(atKeysCopy);
            Collections.sort(sorted);
            for (String key : sorted) {
                if ("krb_ticket".equals(key)) continue;
                ordered.put(key, attrs.get(key));
            }
            //store it
            orderedAttributes = ordered;
        }
        return orderedAttributes;
    }

    public Date getTimeCreated() {
        //  ctime The time that the job was created.
        if (ctime == null) {
            ctime = PbsUtils.getJavaTime(attrs.get(ATTRIBUTE_TIME_CREATED));
        }
        return ctime;
    }

    public Date getTimeStarted() {
        if (start_time == null) {
            start_time = PbsUtils.getJavaTime(attrs.get(ATTRIBUTE_TIME_STARTED));
        }
        return start_time;
    }

    public Date getTimeCompleted() {
        if (comp_time == null) {
            comp_time = PbsUtils.getJavaTime(attrs.get(ATTRIBUTE_TIME_COMPLETED));
        }
        return comp_time;
    }


    public Date getPlannedStart() {
        if (planned_start == null) {
            planned_start = PbsUtils.getJavaTime(attrs.get(ATTRIBUTE_PLANNED_START));
        }
        return planned_start;
    }

    public String getPlannedNodes() {
        return attrs.get(ATTRIBUTE_PLANNED_NODES);
    }

    private String[] nodeNames;

    public String[] getPlannedNodesNames() {
        if (nodeNames == null) {
            String plannedNodes = getPlannedNodes();
            nodeNames = plannedNodes != null ? plannedNodes.split(", *") : new String[0];
        }
        return nodeNames;
    }

    /**
     * Gets first hostname, optionally followed by triple dot.
     *
     * @return hostname
     */
    public String getPlannedNodesShort() {
        String[] nodeNames = getPlannedNodesNames();
        if (nodeNames.length == 1) return nodeNames[0];
        return nodeNames[0] + " ...";
    }

    /**
     * Get etime - the time that the job became eligible to run.
     *
     * @return The time that the job became eligible to run, i.e. in a queued state while residing in an execution queue
     */
    public Date getTimeEligible() {
        //  etime The time that the job became eligible to run, i.e. in a queued
        //  state while residing in an execution queue.
        if (etime == null) {
            //ulohy ve stavu H nemaji etime
            String es = attrs.get(ATTRIBUTE_TIME_ELIGIBLE);
            if (es != null) {
                etime = PbsUtils.getJavaTime(es);
            }
        }
        return etime;
    }

    public Date getTimeModified() {
        // mtime The time that the job was last modified, changed state, or changed locations.
        if (mtime == null) {
            mtime = PbsUtils.getJavaTime(attrs.get(ATTRIBUTE_TIME_MODIFIED));
        }
        return mtime;
    }

    public Date getExecutionTime() {
        // executionTime - job waits in state W until that time instant
        if (executionTime == null) {
            executionTime = PbsUtils.getJavaTime(attrs.get(ATTRIBUTE_EXECUTION_TIME));
        }
        return executionTime;
    }

    public String getUsedGPU() {
        return attrs.get(ATTRIBUTE_USED_GPUS);
    }

    public int getNoOfUsedGPU() {
        if (noOfUsedGPU == -1) {
            String ngpu = getUsedGPU();
            if (ngpu == null) {
                this.noOfUsedGPU = 0;
            } else {
                this.noOfUsedGPU = Integer.parseInt(ngpu);
            }

        }
        return noOfUsedGPU;
    }

    public int getNoOfUsedCPU() {
        if (noOfUsedCPU == -1) {
            String ncpu = attrs.get(ATTRIBUTE_USED_CPUS);
            if (ncpu == null) {
                this.noOfUsedCPU = 0;
            } else {
                this.noOfUsedCPU = Integer.parseInt(ncpu);
            }

        }
        return noOfUsedCPU;
    }

    private long reservedMemoryTotal = -1;
    private long usedMemory = -1;

    public long getUsedMemoryNum() {
        if (usedMemory == -1) {
            usedMemory = PbsUtils.parsePbsBytes(attrs.get(ATTRIBUTE_USED_MEMORY));
        }
        return usedMemory;
    }

    public String getUsedMemory() {
        long mem = getUsedMemoryNum();
        return PbsUtils.formatInPbsUnits(mem);
    }

    public long getReservedMemoryTotalNum() {
        if (reservedMemoryTotal == -1) {
            reservedMemoryTotal = PbsUtils.parsePbsBytes(attrs.get(ATTRIBUTE_RESERVED_MEMORY));
        }
        return reservedMemoryTotal;
    }

    public String getReservedMemoryTotal() {
        return PbsUtils.formatInPbsUnits(getReservedMemoryTotalNum());
    }

    public boolean getMemoryExceeded() {
        return getUsedMemoryNum() > getReservedMemoryTotalNum();
    }

    public Integer getExitStatus() {
        String s = attrs.get(ATTRIBUTE_EXIT_STATUS);
        if (s != null) return new Integer(s);
        return null;
    }

    public boolean isKilledBySignal() {
        Integer exitStatus = getExitStatus();
        return exitStatus != null && (exitStatus > 256);
    }

    /**
     * If job was killed, returns number of signal that killed it, if job exited itself returns exit value.
     * http://www.eresearchsa.edu.au/pbs_exitcodes
     * If exit_status is negative, it is a special value.
     * If exit_status is greater than 256, the job was killed by signal, otherwise it exited itself.
     * In both cases the lower byte holds the value of the signal or the exit status.
     *
     * @return value
     */
    public int getExitValueOrSignal() {
        Integer exitStatus = getExitStatus();
        return exitStatus == null ? 0 : exitStatus < 0 ? exitStatus : (exitStatus & 255);
    }

//    #define JOB_EXEC_OK    0 /* job exec successful */
//    #define JOB_EXEC_FAIL1   -1 /* job exec failed, before files, no retry */
//    #define JOB_EXEC_FAIL2   -2 /* job exec failed, after files, no retry  */
//    #define JOB_EXEC_RETRY   -3 /* job execution failed, do retry    */
//    #define JOB_EXEC_INITABT  -4 /* job aborted on MOM initialization */
//    #define JOB_EXEC_INITRST  -5 /* job aborted on MOM init, checkpoint, no migrate */
//    #define JOB_EXEC_INITRMG  -6 /* job aborted on MOM init, checkpoint, ok migrate */
//    #define JOB_EXEC_BADRESRT -7 /* job restart failed */
//    #define JOB_EXEC_CMDFAIL  -8 /* exec() of user command failed */
//    #define JOB_EXEC_STDOUTFAIL -9 /* could not create/open stdout stderr files */
//    #define JOB_EXEC_OVERLIMIT -10 /* resources over limit */

    public boolean getKilledForOverLimit() {
        Integer exitStatus = getExitStatus();
        return exitStatus != null && exitStatus == -10;
    }

    private int nodeCount = -1;

    @SuppressWarnings("UnusedDeclaration")
    public int getNodeCount() {
        if (nodeCount == -1) {
            String nct = attrs.get(ATTRIBUTE_NODE_COUNT);
            if (nct != null) {
                nodeCount = Integer.parseInt(nct);
            } else {
                nodeCount = 1;
            }
        }
        return nodeCount;
    }

    public String getResourceNodes() {
        return attrs.get(ATTRIBUTE_NODES);
    }

    /**
     * Gets directory PBS_O_WORKDIR, which is the directory where qsub was submitted and where stdin and stdout will be put.
     *
     * @return $PBS_O_WORKDIR
     */
    public String getSubmitDir() {
        if (submitDir == null) {
            String vl = attrs.get(ATTRIBUTE_VARIABLE_LIST);
            if (vl != null) {
                int position = vl.indexOf("PBS_O_WORKDIR=");
                if (position != -1) {
                    String end = vl.substring(position + "PBS_O_WORKDIR=".length());
                    int comma = end.indexOf(',');
                    if (comma == -1) {
                        submitDir = end;
                    } else {
                        submitDir = end.substring(0, comma);
                    }
                }
            }
        }
        return submitDir;
    }

    public String getWorkDir() {
        if (workDir == null) {
            String vl = attrs.get(ATTRIBUTE_VARIABLE_LIST);
            if (vl != null) {
                int poz = vl.indexOf("PBS_O_INITDIR=");
                if (poz != -1) {
                    String konec = vl.substring(poz + "PBS_O_INITDIR=".length());
                    int carka = konec.indexOf(',');
                    if (carka == -1) {
                        workDir = konec;
                    } else {
                        workDir = konec.substring(0, carka);
                    }
                } else {
                    workDir = "$HOME";
                }
            }
        }
        return workDir;
    }

    public String getUser() {
        if (user == null) {
            String owner = attrs.get(ATTRIBUTE_JOB_OWNER);
            this.user = owner.substring(0, owner.indexOf('@'));
        }
        return user;
    }

    public String getJobName() {
        return attrs.get(ATTRIBUTE_JOB_NAME);
    }

    /**
     * Returns string representation of used CPU time.
     *
     * @return null or string "hours:minutes:seconds"
     */
    public String getCPUTimeUsed() {
        String s = attrs.get(ATTRIBUTE_USED_CPUTIME);
        return s == null ? "" : s;
    }

    /**
     * Report jobs using less than 3/4 of allocated CPUs.
     * Only for running or completed jobs, which run more than 5 minutes.
     *
     * @return true for jobs wasting CPUs
     */
    @SuppressWarnings("SimplifiableIfStatement")
    public boolean isUnderusingCPUs() {
        String state = getState();
        if (!("R".equals(state) || "C".equals(state))) return false;
        long walltimeUsedSec = getWallTimeUsedSec();
        if (walltimeUsedSec < 5 * 60) return false;
        return getCPUTimeUsedSec() < getNoOfUsedCPU() * walltimeUsedSec / 4 * 3;
    }

    /**
     * Returns used CPU time in seconds.
     *
     * @return used CPU time in seconds
     */
    public long getCPUTimeUsedSec() {
        if (cpuTimeUsedSec == -1L) {
            String stime = getCPUTimeUsed();
            if (stime == null || stime.isEmpty()) {
                cpuTimeUsedSec = 0L;
                return 0L;
            }
            String[] c = stime.split(":");
            cpuTimeUsedSec = Long.parseLong(c[0]) * 3600 + Long.parseLong(c[1]) * 60 + Long.parseLong(c[2]);
        }
        return cpuTimeUsedSec;
    }


    /**
     * Returns string representation of used wall clock time.
     *
     * @return null or string "hours:minutes:seconds"
     */
    public String getWallTimeUsed() {
        String s = attrs.get(ATTRIBUTE_USED_WALLTIME);
        return s == null ? "" : s;
    }

    public Long getWalltimeRemaining() {
        String s = attrs.get(ATTRIBUTE_WALLTIME_REMAINING);
        if (s == null) return null;
        try {
            return Long.parseLong(s) * 1000;
        } catch (NumberFormatException ex) {
            log.warn("Job {} has non-parseable value for Walltime.Remaining={}", getId(), s);
            return null;
        }
    }

    private Date expectedEndTime;

    public Date getTimeExpectedEnd() {
        if (expectedEndTime == null) {
            if (!"R".equals(this.getState())) return null;
            Long walltimeRemaining = getWalltimeRemaining();
            if (walltimeRemaining == null) return null;
            expectedEndTime = new Date(getPbs().getTimeLoaded().getTime() + walltimeRemaining);
        }
        return expectedEndTime;
    }


    private Date plannedEnd;

    public Date getPlannedEnd() {
        if (plannedEnd == null) {
            if (!this.getPbs().getServerConfig().isPlanbased()) return null;
            if (!"Q".equals(this.getState())) return null;
            Date plannedStart = getPlannedStart();
            if (plannedStart == null) return null;
            Long walltimeRemaining = getWalltimeRemaining();
            if (walltimeRemaining == null) return null;
            plannedEnd = new Date(plannedStart.getTime() + walltimeRemaining);
        }
        return plannedEnd;
    }


    public boolean isOverRun() {
        Date time = getTimeExpectedEnd();
        return time != null && time.getTime() < System.currentTimeMillis();
    }

    /**
     * Returns used wall clock time in seconds.
     *
     * @return used wall clock time in seconds
     */
    public long getWallTimeUsedSec() {
        if (wallTimeUsedSec == -1L) {
            String stime = getWallTimeUsed();
            if (stime == null || stime.isEmpty()) {
                wallTimeUsedSec = 0L;
                return 0L;
            }
            String[] c = stime.split(":");
            wallTimeUsedSec = Long.parseLong(c[0]) * 3600 + Long.parseLong(c[1]) * 60 + Long.parseLong(c[2]);
        }
        return wallTimeUsedSec;
    }

    public String getState() {
        return attrs.get(ATTRIBUTE_JOB_STATE);
    }

    public Queue getQueue() {
        if (queue == null) queue = getPbs().getQueues().get(attrs.get(ATTRIBUTE_QUEUE));
        return queue;
    }

    public String getQueueName() {
        return getQueue().getName();
    }

    public String getComment() {
        return attrs.get(ATTRIBUTE_COMMENT);
    }

    /**
     * Returns first exec-host param.
     *
     * @return first
     */
    public String getExecHostFirst() {
        if (execHostFirst == null) {
            String hosts = attrs.get(ATTRIBUTE_EXEC_HOST);
            if (hosts != null) {
                String sh[] = hosts.split("\\+");
                execHostFirst = sh[0];
            } else execHostFirst = null;
        }
        return this.execHostFirst;
    }

    public String getExecHostFirstName() {
        String s = getExecHostFirst();
        if (s == null) return "";
        return PbsUtils.substringBefore(s, '/');
    }

    public String getExecHostFirstCPU() {
        String s = getExecHostFirst();
        if (s == null) return "";
        return PbsUtils.substringAfter(s, '/');
    }

    /**
     * Returns rest of exec-host param
     *
     * @return rest
     */
    public String[] getExecHostMore() {
        if (execHostMore == null) {
            String hosts = attrs.get(ATTRIBUTE_EXEC_HOST);
            if (hosts != null) {
                String sh[] = hosts.split("\\+");
                if (sh.length > 1) {
                    execHostMore = new String[sh.length - 1];
                    System.arraycopy(sh, 1, this.execHostMore, 0, (sh.length - 1));
                } else {
                    this.execHostMore = new String[0];
                }
            } else {
                this.execHostMore = new String[0];
            }
        }
        return this.execHostMore;
    }

    List<NodeSpec> nodeSpecs;

    // sched_nodespec	host=gram9.zcu.cz:ppn=2:mem=716800KB:vmem=137438953472KB:gpu=2:cl_gram:scratch_type=ssd:scratch_volume=400mb:scratch_ssd=400mb+host=doom7.metacentrum.cz:ppn=1:mem=614400KB:vmem=137438953472KB:gpu=1:cl_doom:scratch_type=ssd:scratch_volume=400mb:scratch_ssd=400mb
    public List<NodeSpec> getScheduledNodeSpecs() {
        if (nodeSpecs == null) {
            String sched_nodespec = attrs.get(ATTRIBUTE_SCHEDULED_NODES_SPECS);
            if (sched_nodespec == null) return null;
            nodeSpecs = new ArrayList<>();
            for (String spec : sched_nodespec.split("\\+")) {
                nodeSpecs.add(new NodeSpec(spec));
            }
        }
        return nodeSpecs;
    }

    public static class NodeSpec {
        public NodeSpec(String spec) {
            try {
                for (String attr : spec.split(":")) {
                    String[] kv = attr.split("=", 2);
                    if(kv.length!=2) continue; // like :cl_doom
                    String key = kv[0];
                    String value = kv[1];
                    switch (key) {
                        case "host":
                            hostname = value;
                            break;
                        case "ppn":
                            ppn = Integer.parseInt(value);
                            break;
                        case "mem":
                            mem = PbsUtils.formatInPbsUnits(PbsUtils.parsePbsBytes(value));
                            break;
                        case "gpu":
                            gpu = Integer.parseInt(value);
                            break;
                        case "scratch_type":
                            scratchType = value;
                            break;
                        case "scratch_volume":
                            scratchVolume = PbsUtils.formatInPbsUnits(PbsUtils.parsePbsBytes(value));
                            break;
                    }
                }
            } catch (Exception e) {
                log.error("problem parsing "+spec,e);
            }
        }

        private String hostname;
        private int ppn;
        private String mem;
        private int gpu;
        private String scratchType;
        private String scratchVolume;

        public String getHostname() {
            return hostname;
        }

        public int getPpn() {
            return ppn;
        }


        public int getGpu() {
            return gpu;
        }

        public String getScratchType() {
            return scratchType;
        }

        public String getMem() {
            return mem;
        }

        public String getScratchVolume() {
            return scratchVolume;
        }
    }


    private int fairshareRank = -1;

    public int getFairshareRank() {
        return fairshareRank;
    }

    public void setFairshareRank(Integer fairshareRank) {
        if (fairshareRank != null)
            this.fairshareRank = fairshareRank;
    }

    /*
     Přidělené scratche jsou v atributu sched_nodespec, který je potřeba rozdělit podle znaků +
     na jednotlivé hosty, a pro každý pak vyparsovat  scratch_type=local:scratch_volume
     pokud tam je, může nebýt u typu first
     */
    static Pattern pst = Pattern.compile("scratch_type=(\\w+):");
    static Pattern scratchRegex = Pattern.compile("^host=([^:]+):.*:scratch_type=(\\w+):scratch_volume=([0-9]+[kmgp]b)");

    public String getScratchType() {
        String sched_nodespec = attrs.get(ATTRIBUTE_SCHEDULED_NODES_SPECS);
        if (sched_nodespec == null) return null;
        Matcher m = pst.matcher(sched_nodespec);
        return m.find() ? m.group(1) : null;
    }

    public String getScratchDir() {
        String scratchType = getScratchType();
        switch (scratchType) {
            case "local":
                return "/scratch/" + getUser() + "/job_" + getId();
            case "ssd":
                return "/scratch.ssd/" + getUser() + "/job_" + getId();
            case "shared":
                return "/scratch.shared/" + getUser() + "/job_" + getId();
        }
        return null;
    }

    private Map<String, ReservedScratch> nodeName2reservedScratchMap;

    public Map<String, ReservedScratch> getNodeName2reservedScratchMap() {
        if (nodeName2reservedScratchMap == null) {
            synchronized (this) {
                nodeName2reservedScratchMap = new HashMap<>();
                String sched_nodespec = attrs.get(ATTRIBUTE_SCHEDULED_NODES_SPECS);
                if (sched_nodespec == null) return null;
                for (String hostString : sched_nodespec.split("\\+")) {
                    Matcher m = scratchRegex.matcher(hostString);
                    if (m.find()) {
                        String hostname = m.group(1);
                        String type = m.group(2);
                        String volume = m.group(3);
                        nodeName2reservedScratchMap.put(hostname, new ReservedScratch(type, volume));
                    }
                }
            }
        }
        return nodeName2reservedScratchMap;
    }

    public boolean isPlanned() {
        return attrs.get(ATTRIBUTE_PLANNED_START) != null;
    }


    public static class ReservedScratch {
        private String type;
        private String volume;
        private long volumeB;

        public ReservedScratch(String type, String volume) {
            this.type = type;
            this.volumeB = PbsUtils.parsePbsBytes(volume);
            this.volume = PbsUtils.formatInPbsUnits(volumeB);
        }

        public String getType() {
            return type;
        }

        public String getVolume() {
            return volume;
        }

        public long getVolumeB() {
            return volumeB;
        }
    }
}
