package cz.cesnet.meta.pbs;

import cz.cesnet.meta.pbsmon.PbsmonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
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
    public static final String ATTRIBUTE_TIME_STARTED_TORQUE = "start_time";
    public static final String ATTRIBUTE_TIME_STARTED_PBSPRO = "stime";
    public static final String ATTRIBUTE_TIME_COMPLETED = "comp_time";
    public static final String ATTRIBUTE_PLANNED_START_TORQUE = "planned_start";
    public static final String ATTRIBUTE_PLANNED_START_PBSPRO = "estimated.start_time";
    public static final String ATTRIBUTE_PLANNED_NODES_TORQUE = "planned_nodes";
    public static final String ATTRIBUTE_PLANNED_NODES_PBSPRO = "estimated.exec_vnode";
    public static final String ATTRIBUTE_USED_GPUS_TORQUE = "resc_req_total.gpu";
    public static final String ATTRIBUTE_USED_GPUS_PBSPRO = "Resource_List.ngpus";
    public static final String ATTRIBUTE_USED_CPUS_TORQUE = "resc_req_total.procs";
    public static final String ATTRIBUTE_USED_CPUS_PBSPRO = "Resource_List.ncpus";
    public static final String ATTRIBUTE_USED_MEMORY = "resources_used.mem";
    public static final String ATTRIBUTE_USED_CPUTIME = "resources_used.cput";
    public static final String ATTRIBUTE_USED_WALLTIME = "resources_used.walltime";
    public static final String ATTRIBUTE_RESERVED_MEMORY_TORQUE = "resc_req_total.mem";
    public static final String ATTRIBUTE_RESERVED_MEMORY_PBSPRO = "Resource_List.mem";
    public static final String ATTRIBUTE_NODE_COUNT = "Resource_List.nodect";
    public static final String ATTRIBUTE_NODES_TORQUE = "Resource_List.nodes";
    public static final String ATTRIBUTE_NODES_PBSPRO = "Resource_List.select";
    public static final String ATTRIBUTE_VARIABLE_LIST = "Variable_List";
    public static final String ATTRIBUTE_JOB_OWNER = "Job_Owner";
    public static final String ATTRIBUTE_JOB_NAME = "Job_Name";
    public static final String ATTRIBUTE_WALLTIME_REMAINING_TORQUE = "Walltime.Remaining";
    public static final String ATTRIBUTE_WALLTIME_RESERVED = "Resource_List.walltime";   //both Troque and PBSPro
    public static final String ATTRIBUTE_JOB_STATE = "job_state";
    public static final String ATTRIBUTE_QUEUE = "queue";
    public static final String ATTRIBUTE_COMMENT = "comment";
    public static final String ATTRIBUTE_EXEC_HOST = "exec_host";
    public static final String ATTRIBUTE_SCHEDULED_NODES_SPECS = "sched_nodespec";
    public static final String ATTRIBUTE_EXEC_VNODE = "exec_vnode";
    public static final String ATTRIBUTE_EXECUTION_TIME = "Execution_Time";
    public static final String ATTRIBUTE_EXIT_STATUS_TORQUE = "exit_status";
    public static final String ATTRIBUTE_EXIT_STATUS_PBSPRO = "Exit_status";

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
    //private int noOfUsedGPU = -1;
    private String user = null;
    private String submitDir = null;
    private String workDir = null;
    private long cpuTimeUsedSec = -1L;
    private Duration wallTimeUsed = null;
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
            "submit_args", "Submit_arguments", ATTRIBUTE_COMMENT, ATTRIBUTE_JOB_NAME, ATTRIBUTE_JOB_OWNER, ATTRIBUTE_JOB_STATE, ATTRIBUTE_QUEUE,
            ATTRIBUTE_EXEC_HOST, "exec_vnode", "Output_Path", "Error_Path", "Join_Path",
            ATTRIBUTE_TIME_CREATED, "qtime", ATTRIBUTE_TIME_ELIGIBLE, ATTRIBUTE_PLANNED_START_TORQUE, ATTRIBUTE_TIME_MODIFIED, ATTRIBUTE_TIME_STARTED_PBSPRO, ATTRIBUTE_TIME_STARTED_TORQUE, ATTRIBUTE_TIME_COMPLETED, ATTRIBUTE_WALLTIME_REMAINING_TORQUE,
    };

    public String getId() {
        return name;
    }


    private static final Pattern TORQUE_ARRAY_JOB = Pattern.compile("(\\d+)-(\\d+)");
    private static final Pattern PBSPRO_ARRAY_JOB = Pattern.compile("(\\d+)\\[(\\d+)\\]");

    public int getIdNum() {
        if (idNum == -1) {
            String jobid = PbsUtils.substringBefore(name,'.');
            if(jobid.matches("\\d+")){
                idNum = Integer.parseInt(jobid);
            } else {
                Matcher m = PBSPRO_ARRAY_JOB.matcher(jobid);
                if (m.matches()) {
                    idNum = Integer.parseInt(m.group(1));
                    idSubNum = Integer.parseInt(m.group(2));
                } else {
                    Matcher m2 = TORQUE_ARRAY_JOB.matcher(jobid);
                    if (m2.matches()) {
                        idNum = Integer.parseInt(m.group(1));
                        idSubNum = Integer.parseInt(m.group(2));
                    } else {
                        log.warn("job {} has unparseable number",name);
                    }
                }
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
            start_time = PbsUtils.getJavaTime(
                    attrs.get(pbs.isTorque() ? ATTRIBUTE_TIME_STARTED_TORQUE : ATTRIBUTE_TIME_STARTED_PBSPRO));
        }
        return start_time;
    }

    public Date getTimeCompleted() {
        if (comp_time == null) {
            if (pbs.isTorque()) {
                comp_time = PbsUtils.getJavaTime(attrs.get(ATTRIBUTE_TIME_COMPLETED));
            } else {
                if (!"F".equals(this.getState())) return null;
                Date timeStarted = getTimeStarted();
                if (timeStarted == null) return null;
                Duration wallTimeUsed = getWalltimeUsed();
                if (wallTimeUsed == null) return null;
                comp_time = Date.from(timeStarted.toInstant().plus(wallTimeUsed));
            }
        }
        return comp_time;
    }


    public Date getPlannedStart() {
        if (planned_start == null) {
            planned_start = PbsUtils.getJavaTime(attrs.get(pbs.isTorque() ? ATTRIBUTE_PLANNED_START_TORQUE : ATTRIBUTE_PLANNED_START_PBSPRO));
        }
        return planned_start;
    }

    public String getPlannedNodes() {
        return attrs.get(pbs.isTorque() ? ATTRIBUTE_PLANNED_NODES_TORQUE : ATTRIBUTE_PLANNED_NODES_PBSPRO);
    }

    private static final Pattern HOSTNAME_FROM_EXEC_VNODE = Pattern.compile("\\(([^:]+):[^)]*\\)\\+?");

    private static String[] parseNodeNames(String exec_vhost) {
        Matcher m = HOSTNAME_FROM_EXEC_VNODE.matcher(exec_vhost);
        List<String> nodeNames = new ArrayList<>();
        while (m.find()) {
            nodeNames.add(m.group(1));
        }
        return nodeNames.toArray(new String[nodeNames.size()]);
    }

    private String[] nodeNames;

    public String[] getPlannedNodesNames() {
        if (nodeNames == null) {
            String plannedNodes = getPlannedNodes();
            if(pbs.isTorque()) {
                nodeNames = plannedNodes != null ? plannedNodes.split(", *") : new String[0];
            } else {
                nodeNames = plannedNodes != null ? parseNodeNames(plannedNodes) : new String[0];
            }
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
        return attrs.get(pbs.isTorque() ? ATTRIBUTE_USED_GPUS_TORQUE : ATTRIBUTE_USED_GPUS_PBSPRO);
    }

    /*
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
    */

    public int getNoOfUsedCPU() {
        if (noOfUsedCPU == -1) {
            String ncpu = attrs.get(pbs.isTorque() ? ATTRIBUTE_USED_CPUS_TORQUE : ATTRIBUTE_USED_CPUS_PBSPRO);
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
            reservedMemoryTotal = PbsUtils.parsePbsBytes(attrs.get(pbs.isTorque() ? ATTRIBUTE_RESERVED_MEMORY_TORQUE : ATTRIBUTE_RESERVED_MEMORY_PBSPRO));
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
        String s = attrs.get(pbs.isTorque() ? ATTRIBUTE_EXIT_STATUS_TORQUE : ATTRIBUTE_EXIT_STATUS_PBSPRO);
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
        return attrs.get(pbs.isTorque() ? ATTRIBUTE_NODES_TORQUE : ATTRIBUTE_NODES_PBSPRO);
    }

    /**
     * Gets directory PBS_O_WORKDIR, which is the directory where qsub was submitted and where stdin and stdout will be put.
     *
     * @return $PBS_O_WORKDIR
     */
    public String getSubmitDir() {
        if (submitDir == null) {
            submitDir = getVariables().get("PBS_O_WORKDIR");
        }
        return submitDir;
    }

    public String getWorkDir() {
        if (workDir == null) {
            String pbsOInitdir = getVariables().get("PBS_O_INITDIR");
            workDir = pbsOInitdir == null ? "$HOME" : pbsOInitdir;
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
        if (!("R".equals(state) || "C".equals(state)|| "F".equals(state))) return false;
        Duration walltimeUsed = getWalltimeUsed();
        if(walltimeUsed==null) {
            return false;
        }
        if (walltimeUsed.compareTo(Duration.ofMinutes(5)) < 0) return false;
        return getCPUTimeUsedSec() < getNoOfUsedCPU() * walltimeUsed.getSeconds() / 4 * 3;
    }

    public boolean getExceedsCPUTime() {
        String state = getState();
        if (!("R".equals(state) || "C".equals(state)|| "F".equals(state))) return false;
        Duration walltimeUsed = getWalltimeUsed();
        if(walltimeUsed==null) {
            return false;
        }
        return getCPUTimeUsedSec() > getNoOfUsedCPU() * walltimeUsed.getSeconds();
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

    /**
     * Returns used wall clock time.
     *
     * @return used wall clock time
     */
    public Duration getWalltimeUsed() {
        if (wallTimeUsed == null) {
            String wallTimeUsed = getWallTimeUsed();
            if (wallTimeUsed == null || wallTimeUsed.isEmpty()) {
                this.wallTimeUsed = null;
                return null;
            }
            this.wallTimeUsed = PbsUtils.parseTime(wallTimeUsed);
        }
        return wallTimeUsed;
    }


    /**
     * Gets milliseconds remaining for job
     *
     * @return milliseconds remaining
     */
    public Duration getWalltimeRemaining() {
        if (pbs.isTorque()) {
            String s = attrs.get(ATTRIBUTE_WALLTIME_REMAINING_TORQUE);
            if (s == null) return null;
            try {
                return Duration.of(Long.parseLong(s), ChronoUnit.SECONDS);
            } catch (NumberFormatException ex) {
                log.warn("Job {} has non-parseable value for Walltime.Remaining={}", getId(), s);
                return null;
            }
        } else {
            Duration reservedWalltime = getWalltimeReserved();
            if (reservedWalltime == null) return null;
            Duration walltimeUsed = getWalltimeUsed();
            if(walltimeUsed == null) return null;
            return reservedWalltime.minus(walltimeUsed);
        }
    }

    public Duration getWalltimeReserved() {
        return PbsUtils.parseTime(attrs.get(ATTRIBUTE_WALLTIME_RESERVED));
    }

    private Date expectedEndTime;

    public Date getTimeExpectedEnd() {
        if (expectedEndTime == null) {
            if (!"R".equals(this.getState())) return null;
            Date timeStarted = getTimeStarted();
            if (timeStarted == null) return null;
            Duration walltimeReserved = getWalltimeReserved();
            if (walltimeReserved == null) return null;
            expectedEndTime = Date.from(timeStarted.toInstant().plus(getWalltimeReserved()));
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
            Duration walltimeRemaining = getWalltimeRemaining();
            if (walltimeRemaining == null) return null;
            plannedEnd = Date.from(plannedStart.toInstant().plus(walltimeRemaining));
        }
        return plannedEnd;
    }


    public boolean isOverRun() {
        Date time = getTimeExpectedEnd();
        return time != null && time.getTime() < System.currentTimeMillis();
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

    List<Chunk> chunks;

    //používá se na stránce detailu jobu pro zobrazení položky "přidělené prostředky"
    //může obsahovat ten stejný uzel vícekrát díky chunkům
    // sched_nodespec	host=gram9.zcu.cz:ppn=2:mem=716800KB:vmem=137438953472KB:gpu=2:cl_gram:scratch_type=ssd:scratch_volume=400mb:scratch_ssd=400mb+host=doom7.metacentrum.cz:ppn=1:mem=614400KB:vmem=137438953472KB:gpu=1:cl_doom:scratch_type=ssd:scratch_volume=400mb:scratch_ssd=400mb
    // exec_vnode (storm1:scratch_local=10240kb:ngpus=1:mem=409600kb:ncpus=1)+(storm1:scratch_local=102400kb:mem=409600kb:ncpus=1)
    // estimated.exec_vnode    (tarkil2:ncpus=16:mem=16777216kb:scratch_local=16777216kb)
    public List<Chunk> getChunks() {
        if (chunks == null) {
            String spec = attrs.get(pbs.isTorque() ? ATTRIBUTE_SCHEDULED_NODES_SPECS :
                    ("Q".equals(getState())? "estimated.exec_vnode" : ATTRIBUTE_EXEC_VNODE));
            if (spec == null) return Collections.emptyList();
            chunks = new ArrayList<>();
            for (String hostString : spec.split("\\+")) {
                //remove () around the string for PBSPro
                if (!pbs.isTorque()) hostString = hostString.substring(1, hostString.length() - 1);
                chunks.add(new Chunk(hostString));
            }
        }
        return chunks;
    }

    public class Chunk {
        public Chunk(String spec) {
            try {
                if (pbs.isTorque()) {
                    for (String attr : spec.split(":")) {
                        String[] kv = attr.split("=", 2);
                        if (kv.length != 2) continue; // like :cl_doom
                        String key = kv[0];
                        String value = kv[1];
                        switch (key) {
                            case "host":
                                nodeName = value;
                                break;
                            case "ppn":
                                ncpus = Integer.parseInt(value);
                                break;
                            case "mem":
                                mem = PbsUtils.formatInPbsUnits(PbsUtils.parsePbsBytes(value));
                                break;
                            case "gpu":
                                ngpus = Integer.parseInt(value);
                                break;
                            case "scratch_type":
                                scratchType = value;
                                break;
                            case "scratch_volume":
                                scratchVolume = PbsUtils.formatInPbsUnits(PbsUtils.parsePbsBytes(value));
                                break;
                        }
                    }
                } else {
                    String[] strings = spec.split(":");
                    for (int i = 0, n = strings.length; i < n; i++) {
                        if (i == 0) {
                            nodeName = strings[0];
                            continue;
                        }
                        String[] kv = strings[i].split("=", 2);
                        if (kv.length != 2) continue;
                        String key = kv[0];
                        String value = kv[1];
                        switch (key) {
                            case "ncpus":
                                ncpus = Integer.parseInt(value);
                                break;
                            case "mem":
                                mem = PbsUtils.formatInPbsUnits(PbsUtils.parsePbsBytes(value));
                                break;
                            case "ngpus":
                                ngpus = Integer.parseInt(value);
                                break;
                        }
                        if (key.startsWith("scratch_")) {
                            scratchType = key.substring("scratch_".length());
                            scratchVolume = PbsUtils.formatInPbsUnits(PbsUtils.parsePbsBytes(value));
                        }
                    }
                }
            } catch (Exception e) {
                log.error("problem parsing " + spec, e);
            }
        }

        private String nodeName;
        private int ncpus;
        private String mem;
        private int ngpus;
        private String scratchType;
        private String scratchVolume;

        public String getNodeName() {
            return nodeName;
        }

        public int getNcpus() {
            return ncpus;
        }


        public int getNgpus() {
            return ngpus;
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

    static final Pattern pst_torque = Pattern.compile("scratch_type=(\\w+):");
    static final Pattern pst_pbspro = Pattern.compile("scratch_(\\w+)=");

    /**
     * Gets the type of scratch for the first chunk.
     *
     * @return local|ssd|shared
     */
    public String getScratchType() {
        String spec = attrs.get(pbs.isTorque() ? ATTRIBUTE_SCHEDULED_NODES_SPECS : ATTRIBUTE_EXEC_VNODE);
        if (spec == null) return null;
        Matcher m = (pbs.isTorque() ? pst_torque : pst_pbspro).matcher(spec);
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

    private Map<String, ReservedResources> nodeName2reservedResources;

    // využito v node_detail.tag v seznamu úloh běžících na uzlu, zobrazuje kolik která úloha na uzlu využívá zdrojů
    public Map<String, ReservedResources> getNodeName2reservedResources() {
        if (nodeName2reservedResources == null) {
            synchronized (this) {
                nodeName2reservedResources = new HashMap<>();
                for (Chunk chunk : getChunks()) {
                    String chunkNodeName = chunk.getNodeName();
                    String chunkScratchType = chunk.getScratchType();
                    long chunkScratchVolume = PbsUtils.parsePbsBytes(chunk.getScratchVolume());
                    long chunkMemBytes = PbsUtils.parsePbsBytes(chunk.getMem());
                    int chunkCpus = chunk.getNcpus();

                    ReservedResources reservedResources = nodeName2reservedResources.get(chunkNodeName);
                    if (reservedResources == null) { //první chunk na stroji
                        reservedResources = new ReservedResources(this,chunkNodeName,chunkScratchType, chunkScratchVolume, chunkMemBytes, chunkCpus);
                    } else { //další chunk na stejném stroji, přičíst k předešlému
                        reservedResources = new ReservedResources(this,chunkNodeName,
                                chunkScratchType.equals(reservedResources.getScratchType()) ? chunkScratchType : "mixed",
                                chunkScratchVolume + reservedResources.getScratchVolumeBytes(),
                                chunkMemBytes + reservedResources.getMemBytes(),
                                chunkCpus + reservedResources.getCpus()
                        );
                    }
                    nodeName2reservedResources.put(chunkNodeName, reservedResources);
                }
            }
        }
        return nodeName2reservedResources;
    }

    public static class ReservedResources {
        private final Job job;
        private final String nodeName;
        private final String scratchType;
        private final long scratchVolumeBytes;
        private final long memBytes;
        private final int cpus;

        public ReservedResources(Job job, String nodeName, String scratchType, long scratchVolumeBytes, long memBytes, int cpus) {
            this.job = job;
            this.nodeName = nodeName;
            this.scratchType = scratchType;
            this.scratchVolumeBytes = scratchVolumeBytes;
            this.memBytes = memBytes;
            this.cpus = cpus;
        }

        public Job getJob() {
            return job;
        }

        public String getNodeName() {
            return nodeName;
        }

        public String getScratchType() {
            return scratchType;
        }

        public String getVolume() {
            return PbsUtils.formatInPbsUnits(scratchVolumeBytes);
        }

        public long getScratchVolumeBytes() {
            return scratchVolumeBytes;
        }

        public String getMem() {
            return PbsUtils.formatInPbsUnits(memBytes);
        }

        public long getMemBytes() {
            return memBytes;
        }

        public int getCpus() {
            return cpus;
        }
    }


    private Map<String, String> variables;

    public Map<String, String> getVariables() {
        if (variables == null) {
            String vl = attrs.get(ATTRIBUTE_VARIABLE_LIST);
            if (vl != null) {
                variables = new TreeMap<>();
                for (String var : vl.split(",")) {
                    String[] ss = var.split("=", 2);
                    if (ss.length == 2) {
                        variables.put(ss[0], ss[1]);
                    } else {
                        log.warn("Job {} has unparseable variable {}", this.getId(), var);
                    }
                }
            } else {
                variables = Collections.emptyMap();
            }
        }
        return variables;
    }
}
