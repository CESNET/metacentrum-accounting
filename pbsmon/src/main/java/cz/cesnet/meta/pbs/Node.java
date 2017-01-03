package cz.cesnet.meta.pbs;

import cz.cesnet.meta.acct.OutageRecord;
import cz.cesnet.meta.pbscache.Scratch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class representing computing node as reported by a PBS server.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("unused")
public class Node extends PbsInfoObject {

    final static Logger log = LoggerFactory.getLogger(Node.class);


    public static final String ATTRIBUTE_PREFIX_RESOURCES_AVAILABLE_TORQUE = "resources_total.";
    public static final String ATTRIBUTE_PREFIX_RESOURCES_AVAILABLE_PBSPRO = "resources_available.";
    public static final String ATTRIBUTE_PREFIX_RESOURCES_ASSIGNED_TORQUE = "resources_used.";
    public static final String ATTRIBUTE_PREFIX_RESOURCES_ASSIGNED_PBSPRO = "resources_assigned.";

    //used attribute names
    public static final String ATTRIBUTE_STATE = "state";
    public static final String ATTRIBUTE_EXCLUSIVELY_ASSIGNED = "exclusively_assigned";
    public static final String ATTRIBUTE_NODE_TYPE = "ntype";
    public static final String ATTRIBUTE_RESOURCES_AVAILABLE_ARCH = "resources_available.arch";
    public static final String ATTRIBUTE_COMMENT = "comment";
    public static final String ATTRIBUTE_NOTE = "note";

    public static final String ATTRIBUTE_NUMBER_OF_PROCESSORS_TORQUE = "np";
    public static final String ATTRIBUTE_NUMBER_OF_PROCESSORS_PBSPRO = "resources_available.ncpus";

    public static final String ATTRIBUTE_RESOURCES_TOTAL_GPU_TORQUE = "resources_total.gpu";
    public static final String ATTRIBUTE_RESOURCES_TOTAL_GPU_PBSPRO = "resources_available.ngpus";

    public static final String ATTRIBUTE_RESOURCES_USED_GPU_TORQUE = "resources_used.gpu";
    public static final String ATTRIBUTE_RESOURCES_USED_GPU_PBSPRO = "resources_assigned.ngpus";

    public static final String ATTRIBUTE_NUMBER_OF_HT_CORES = "pcpus";
    public static final String ATTRIBUTE_NUMBER_OF_FREE_CPUS = "npfree";
    public static final String ATTRIBUTE_NUMBER_OF_USED_CPUS = "resources_assigned.ncpus";
    public static final String ATTRIBUTE_TOTAL_MEMORY_TORQUE = "resources_total.mem";
    public static final String ATTRIBUTE_TOTAL_MEMORY_PBSPRO = "resources_available.mem";
    public static final String ATTRIBUTE_USED_MEMORY_TORQUE = "resources_used.mem";
    public static final String ATTRIBUTE_USED_MEMORY_PBSPRO = "resources_assigned.mem";
    public static final String ATTRIBUTE_QUEUE = "queue";
    public static final String ATTRIBUTE_JOBS = "jobs";
    public static final String ATTRIBUTE_AVAILABLE_BEFORE = "available_before";
    public static final String ATTRIBUTE_AVAILABLE_AFTER = "available_after";
    public static final String ATTRIBUTE_PROPERTIES = "properties";
    public static final String ATTRIBUTE_STATUS = "status";

    //node states
    public static final String STATE_FREE = "free";
    public static final String STATE_PARTIALY_FREE = "partialy-free";
    public static final String STATE_OCCUPIED_WOULD_PREEMPT = "occupied-would-preempt";
    public static final String STATE_OFFLINE = "offline";
    public static final String STATE_DOWN = "down";
    public static final String STATE_JOB_BUSY = "job-busy";
    public static final String STATE_JOB_SHARING = "job-sharing";
    public static final String STATE_JOB_EXCLUSIVE = "job-exclusive";
    public static final String STATE_UNKNOWN = "state-unknown";
    public static final String STATE_MAINTENANCE = "maintenance";
    public static final String STATE_MAINTENANCE_BUSY = "maintenance-busy";
    public static final String STATE_TEST = "test";
    public static final String STATE_RESERVED = "reserved";
    public static final String STATE_CLOUD = "cloud";
    public static final String STATE_RUNNING_CLUSTER = "running-cluster";
    public static final String STATE_JOB_FULL = "job-full"; //pseudo state for all used CPUs or all used memory

    private static final Pattern whole = Pattern.compile("^([A-Za-z]+)(\\d*)-*([0-9a-z]*)");

    private String shortName;
    private String state;
    private String pbsState;
    private String[] jobsIds;
    private List<Job> jobs;
    private String[] properties;
    private String clusterName;
    private int numInCluster;
    private int virtNum;
    private List<Queue> queues = new ArrayList<>();
    private List<OutageRecord> outages;
    private Scratch scratch;
    private Map<String, String> status;
    private Map<String, String> gpuJobMap;

    public Node() {
    }

    public Node(String name) {
        super(name);
        this.setName(name);
    }

    public boolean isWorking() {
        String pbsState = getPbsState();
        return pbsState.equals(STATE_JOB_BUSY) || pbsState.equals(STATE_JOB_EXCLUSIVE) ||
                pbsState.equals(STATE_JOB_SHARING) || pbsState.equals(STATE_MAINTENANCE_BUSY) ||
                pbsState.equals(STATE_PARTIALY_FREE) || pbsState.equals(STATE_JOB_FULL);

    }

    public boolean isDown() {
        return getPbsState().equals(STATE_DOWN);
    }

    @Override
    public void clear() {
        log.debug("Node {} clear()", name);
        super.clear();
        shortName = null;
        state = null;
        pbsState = null;
        jobsIds = null;
        if (jobs != null) jobs.clear();
        jobs = null;
        properties = null;
        clusterName = null;
        if (queues != null) queues.clear();
        queues = null;
        if (outages != null) outages.clear();
        outages = null;
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        int dot = name.indexOf('.');
        if (dot < 0) {
            this.shortName = name;
        } else {
            this.shortName = name.substring(0, dot);
        }
        Matcher m = whole.matcher(shortName);
        if (m.find()) {
            this.clusterName = m.group(1);
            String numInC = m.group(2);
            if (numInC != null && numInC.length() > 0) {
                this.numInCluster = Integer.parseInt(numInC);
            }
            String virtN = m.group(3);
            if (virtN != null && virtN.length() > 0) {
                try {
                    this.virtNum = Integer.parseInt(virtN);
                } catch (NumberFormatException ex) {
                    this.virtNum = Integer.parseInt(virtN, 36) - 9;//a=1 atd
                }
            }
        } else {
            //neco divnyho
            throw new RuntimeException("name " + shortName + " is non-parseable");
        }
    }

    public String getFQDN() {
        if (pbs != null && !pbs.isTorque()) {
            return attrs.get("Mom");
        } else {
            return name;
        }
    }

    public List<Queue> getQueues() {
        return queues;
    }

    /**
     * Gets number of virtual machine.
     *
     * @return integer
     */
    public int getVirtNum() {
        return virtNum;
    }

    /**
     * Gets short name of the node, up to the first dot in full DNS name.
     *
     * @return integer
     */
    public String getShortName() {
        return this.shortName;
    }

    /**
     * Gets name prefix from short name, like "skirit" from "skirit12".
     *
     * @return cluster name
     */
    public String getClusterName() {
        return this.clusterName;
    }

    /**
     * Gets number from short name, like "12" from "skirit12".
     *
     * @return integer
     */
    public int getNumInCluster() {
        return this.numInCluster;
    }

    /**
     * Gets state of the node. Returns the  "state" attribute with some corrections
     * for partially or exclusively assigned nodes.
     *
     * @return pbs state
     */
    public String getPbsState() {
        if (this.pbsState == null) {
            this.pbsState = PbsUtils.substringBefore(attrs.get(ATTRIBUTE_STATE), ',');

            if (this.pbsState.equals(STATE_FREE)) {
                if (getNoOfUsedCPUInt() >= getNoOfCPUInt()) {
                    this.pbsState = STATE_JOB_FULL;
                } else if (this.getFreeMemoryInt() <= 0) {
                    this.pbsState = STATE_JOB_FULL;
                } else if (getNoOfUsedCPUInt() > 0) {
                    this.pbsState = STATE_PARTIALY_FREE;
                }
            }
            if ("True".equals(attrs.get(ATTRIBUTE_EXCLUSIVELY_ASSIGNED))) {
                this.pbsState = STATE_JOB_EXCLUSIVE;
            }
        }
        return this.pbsState;
    }

    /**
     * Special states are assigned to nodes in special queues.
     *
     * @return state
     */
    public String getState() {
        if (this.state != null) return this.state;
        //report just text up to first comma
        this.state = getPbsState();
        if (this.isMaintenance()) {
            if (this.getHasJobs())
                this.state = STATE_MAINTENANCE_BUSY;
            else
                this.state = STATE_MAINTENANCE;
        } else if (this.isReserved()) {
            this.state = STATE_RESERVED;
        } else if (this.isTest()) {
            this.state = STATE_TEST;
        }
        return this.state;
    }

    public String getNtype() {
        //cluster, cloud, virtual, PBS
        return attrs.get(ATTRIBUTE_NODE_TYPE);
    }

    public String getArch() {
        if (pbs.isTorque()) {
            return getStatus("uname");
        } else {
            return attrs.get(ATTRIBUTE_RESOURCES_AVAILABLE_ARCH);
        }
    }

    public String getComment() {
        String comment = attrs.get(ATTRIBUTE_COMMENT);
        if (comment == null) {
            comment = attrs.get(ATTRIBUTE_NOTE);
        }
        return comment;
    }

    public String getNoOfCPU() {
        return attrs.get(pbs.isTorque() ? ATTRIBUTE_NUMBER_OF_PROCESSORS_TORQUE : ATTRIBUTE_NUMBER_OF_PROCESSORS_PBSPRO);
    }

    public int getNoOfCPUInt() {
        String n = getNoOfCPU();
        return n == null ? 0 : Integer.parseInt(n);
    }

    public String getNoOfGPU() {
        return attrs.get(pbs.isTorque() ? ATTRIBUTE_RESOURCES_TOTAL_GPU_TORQUE : ATTRIBUTE_RESOURCES_TOTAL_GPU_PBSPRO);
    }

    public int getNoOfGPUInt() {
        String n = getNoOfGPU();
        return n == null ? 0 : Integer.parseInt(n);
    }

    public boolean getHasGPU() {
        return getNoOfGPUInt() > 0;
    }

    public String getNoOfUsedGPU() {
        return attrs.get(pbs.isTorque() ? ATTRIBUTE_RESOURCES_USED_GPU_TORQUE : ATTRIBUTE_RESOURCES_USED_GPU_PBSPRO);
    }

    public int getNoOfUsedGPUInt() {
        String n = getNoOfUsedGPU();
        return n == null ? 0 : Integer.parseInt(n);
    }

    public int getNoOfFreeGPUInt() {
        return getNoOfGPUInt() - getNoOfUsedGPUInt();
    }

    public String getNoOfHTCores() {
        if (pbs.isTorque()) {
            return getStatus("ncpus");
        } else {
            return attrs.get(ATTRIBUTE_NUMBER_OF_HT_CORES);
        }
    }

    public int getNoOfHTCoresInt() {
        String n = getNoOfHTCores();
        if (n == null) return 0;
        return Integer.parseInt(n);
    }

    public boolean isEnabledHT() {
        int noOfCPUInt = getNoOfCPUInt();
        return noOfCPUInt > 0 && (getNoOfHTCoresInt() == 2 * noOfCPUInt);
    }

    public int getNoOfFreeCPUInt() {
        return Integer.parseInt(getNoOfFreeCPU());
    }

    public String getNoOfFreeCPU() {
        if (pbs.isTorque()) {
            return attrs.get(ATTRIBUTE_NUMBER_OF_FREE_CPUS);
        } else {
            return Integer.toString(getNoOfCPUInt() - getNoOfUsedCPUInt());
        }
    }

    public String getNoOfUsedCPU() {
        if (pbs.isTorque()) {
            return Integer.toString(getNoOfUsedCPUInt());
        } else {
            return attrs.get(ATTRIBUTE_NUMBER_OF_USED_CPUS);
        }
    }

    public int getNoOfUsedCPUInt() {
        if (pbs.isTorque()) {
            return getNoOfCPUInt() - getNoOfFreeCPUInt();
        } else {
            String n = getNoOfUsedCPU();
            if (n == null) return 0;
            return Integer.parseInt(n);
        }
    }

    /**
     * Gets percentage of used part of node.
     *
     * @return 0-100
     */
    public int getUsedPercent() {
        if (getHasGPU()) {
            return Math.max(getUsedGPUPercent(), Math.max(getUsedCPUPercent(), getUsedMemoryPercent()));
        } else {
            return Math.max(getUsedCPUPercent(), getUsedMemoryPercent());
        }
    }

    public int getUsedCPUPercent() {
        int noOfCPUInt = getNoOfCPUInt();
        return noOfCPUInt == 0 ? 0 : getNoOfUsedCPUInt() * 100 / noOfCPUInt;
    }

    public int getUsedMemoryPercent() {
        long totalMemoryInt = getTotalMemoryInt();
        return totalMemoryInt == 0 ? 0 : (int) (getUsedMemoryInt() * 100 / totalMemoryInt);
    }

    public int getUsedGPUPercent() {
        return getHasGPU() ? getNoOfUsedGPUInt() * 100 / getNoOfGPUInt() : 0;
    }

    public long getTotalMemoryInt() {
        String mem;
        if (pbs.isTorque()) {
            mem = attrs.get(ATTRIBUTE_TOTAL_MEMORY_TORQUE);
        } else {
            mem = attrs.get(ATTRIBUTE_TOTAL_MEMORY_PBSPRO);
        }
        return PbsUtils.parsePbsBytes(mem);
    }

    public String getTotalMemory() {
        return PbsUtils.formatInPbsUnits(getTotalMemoryInt());
    }

    public String getTotalMemoryB() {
        return PbsUtils.formatInHumanUnits(getTotalMemoryInt());
    }

    public long getUsedMemoryInt() {
        if (pbs.isTorque()) {
            return PbsUtils.parsePbsBytes(attrs.get(ATTRIBUTE_USED_MEMORY_TORQUE));
        } else {
            return PbsUtils.parsePbsBytes(attrs.get(ATTRIBUTE_USED_MEMORY_PBSPRO));
        }
    }

    public String getUsedMemory() {
        return PbsUtils.formatInPbsUnits(getUsedMemoryInt());
    }

    public boolean isExclusivelyAssigned() {
        return Boolean.parseBoolean(attrs.get(ATTRIBUTE_EXCLUSIVELY_ASSIGNED));
    }

    public long getFreeMemoryInt() {
        return getTotalMemoryInt() - getUsedMemoryInt();
    }

    public String getFreeMemory() {
        return PbsUtils.formatInPbsUnits(getFreeMemoryInt());
    }

    public String getFreeMemoryB() {
        return PbsUtils.formatInHumanUnits(getFreeMemoryInt());
    }

    /**
     * METACentrum-specific queue.
     *
     * @return true for nodes in the maintenance queue
     */
    public boolean isMaintenance() {
        return PbsUtils.MAINTENANCE.equals(getRequiredQueueShort());
    }

    /**
     * METACentrum-specific queue.
     *
     * @return true for nodes in the xentest queue
     */
    public boolean isTest() {
        return "xentest".equals(getRequiredQueueShort());
    }

    public boolean isReserved() {
        return PbsUtils.RESERVED.equals(getRequiredQueueShort());
    }

    /**
     * Returns a queue which has the only access to this node.
     *
     * @return queue name
     */
    public String getRequiredQueue() {
        String rq = attrs.get(ATTRIBUTE_QUEUE);
        if (rq != null) rq = rq + getPbs().getSuffix();
        return rq;
    }

    private String getRequiredQueueShort() {
        return attrs.get(ATTRIBUTE_QUEUE);
    }

    /**
     * Gets value of attribute "jobs", separated into Strings with removed CPU numbers.
     *
     * @return array of strings
     */
    public String[] getJobIds() {
        if (this.jobsIds == null) {
            String jobsatr = attrs.get(ATTRIBUTE_JOBS);
            String[] jobsIds = jobsatr != null ? jobsatr.split(", *") : new String[0];
            HashMap<String, Integer> cpuCounts = new HashMap<>();
            for (int i = 0; i < jobsIds.length; i++) {
                jobsIds[i] = pbs.isTorque() ?
                        PbsUtils.substringAfter(jobsIds[i], '/') : PbsUtils.substringBefore(jobsIds[i], '/');
                PbsUtils.updateCount(cpuCounts, jobsIds[i], 1);
            }
            Set<String> keys = cpuCounts.keySet();
            jobsIds = keys.toArray(new String[keys.size()]);
            Arrays.sort(jobsIds);
            this.jobsIds = jobsIds;
        }
        return this.jobsIds;
    }


    /**
     * Gets jobs listed in the "jobs" attribute of node.
     *
     * @return list of jobs
     */
    public List<Job> getJobs() {
        if (jobs == null) {
            String[] jobIds = getJobIds();
            List<Job> jobs = new ArrayList<>(jobIds.length);
            PBS pbs = this.getPbs();
            for (String id : jobIds) {
                Job job = pbs.getJobs().get(id);
                if (job == null) {
                    log.error("job id=" + id + " for node " + getName() + " not found !");
                } else {
                    jobs.add(job);
                }
            }
            jobs.sort(Job.TIME_STARTED_JOB_COMPARATOR);
            this.jobs = jobs;
        }
        return jobs;
    }

    private List<Job> plannedJobs;

    public List<Job> getPlannedJobs() {
        if (plannedJobs == null) {
            PBS pbs = this.getPbs();
            if (!pbs.getServerConfig().isPlanbased()) return Collections.emptyList();
            List<Job> plannedJobs = new ArrayList<>(pbs.getJobsQueuedCount());
            pbs.getJobsById().stream()
                    .filter(job -> "Q".equals(job.getState()))
                    .forEach(job -> {
                        for (String plannedNodeName : job.getPlannedNodesNames()) {
                            if (plannedNodeName.equals(this.getName())) {
                                plannedJobs.add(job);
                            }
                        }
                    });
            plannedJobs.sort(Job.PLANNED_START_JOB_COMPARATOR);
            this.plannedJobs = plannedJobs;
        }
        return plannedJobs;
    }

    public Date getLastJobEndTime() {
        long maxend = 0L;
        for (Job job : getJobs()) {
            Date timeExpectedEnd = job.getTimeExpectedEnd();
            if (timeExpectedEnd != null && timeExpectedEnd.getTime() > maxend) {
                maxend = timeExpectedEnd.getTime();
            }
        }
        return (maxend == 0L) ? null : new Date(maxend);
    }

    private Date availableBefore;

    public Date getAvailableBefore() {
        // available_before - začátek plánované odstávky
        if (availableBefore == null) {
            String available_before = attrs.get(ATTRIBUTE_AVAILABLE_BEFORE);
            if (available_before == null || "0".equals(available_before)) return null;
            availableBefore = PbsUtils.getJavaTime(available_before);
        }
        return availableBefore;
    }

    private Date availableAfter;

    public Date getAvailableAfter() {
        // available_after - konec plánované odstávky
        if (availableAfter == null) {
            String available_after = attrs.get(ATTRIBUTE_AVAILABLE_AFTER);
            if (available_after == null || "0".equals(available_after)) return null;
            availableAfter = PbsUtils.getJavaTime(available_after);
        }
        return availableAfter;
    }

    public boolean isPlannedOutage() {
        return getAvailableBefore() != null || getAvailableAfter() != null;
    }

    public boolean getHasJobs() {
        return attrs.get(ATTRIBUTE_JOBS) != null;
    }

    /**
     * Gets value of attribute "properties", separated into Strings.
     *
     * @return array of properties
     */
    public String[] getProperties() {
        if (this.properties == null) {
            String propatr = attrs.get(ATTRIBUTE_PROPERTIES);
            this.properties = propatr != null ? propatr.split(",") : new String[0];
        }
        return this.properties;
    }

    public boolean hasProperty(String propName) {
        return PbsUtils.isIn(propName, getProperties());
    }

    public List<OutageRecord> getOutages() {
        return outages;
    }

    public void setOutages(List<OutageRecord> outages) {
        this.outages = outages;
    }

    public String getScratchFree() {
        if (this.scratch == null) return "?";
        return PbsUtils.formatInHumanUnits(this.scratch.getAnyFreeKiB() * 1024);
    }

    public Scratch getScratch() {
        if (scratch == null) {
            //happens for nodes that are down
            log.debug("creating empty scratch for node {}", this.getName());
            scratch = new Scratch(this.getName());
        }
        return scratch;
    }

    public void setScratch(Scratch scratch) {
        if (scratch == null) scratch = new Scratch(this.getName());
        scratch.setSsdSize(getStatus("scratch_ssd"));
        scratch.setLocalSize(getStatus("scratch_local"));
        long allocatedSsd = 0L;
        long allocatedLocal = 0L;
        for (Job.ReservedResources reservedResources : this.getResourcesReservedByJobs()) {
            String scratchType = reservedResources.getScratchType();
            if (scratchType != null) {
                switch (scratchType) {
                    case "ssd":
                        allocatedSsd += reservedResources.getScratchVolumeBytes();
                        break;
                    case "local":
                        allocatedLocal += reservedResources.getScratchVolumeBytes();
                        break;
                }
            }
        }
        scratch.setSsdReservedByJobs(allocatedSsd);
        scratch.setLocalReservedByJobs(allocatedLocal);
        this.scratch = scratch;
    }

    /*
        resources_available.scratch_* je kolik zbývá volného místa
        resources_assigned.scratch_* je kolik mají zarezervované úlohy
        tato dvě čísla spolu nesouvisí, protože úloha může část zarezervovaného místa zaplnit a tím sníží volné místo
     */
    public void setScratchPBSPro() {
        log.debug("setScratchPBSPro() for node {}", this.getName());
        scratch = new Scratch(this.getName());
        scratch.setSsdFreeKiB(PbsUtils.parsePbsBytes(attrs.get("resources_available.scratch_ssd")) / 1024L);
        scratch.setSsdReservedByJobs(PbsUtils.parsePbsBytes(attrs.get("resources_assigned.scratch_ssd")));
        scratch.setLocalFreeKiB(PbsUtils.parsePbsBytes(attrs.get("resources_available.scratch_local")) / 1024L);
        scratch.setLocalReservedByJobs(PbsUtils.parsePbsBytes(attrs.get("resources_assigned.scratch_local")));
        scratch.setSharedFreeKiB(PbsUtils.parsePbsBytes(attrs.get("resources_available.scratch_shared")) / 1024L);
        scratch.setSharedReservedByJobs(PbsUtils.parsePbsBytes(attrs.get("resources_assigned.scratch_shared")));
    }

    private List<Job.ReservedResources> getResourcesReservedByJobs() {
        List<Job.ReservedResources> list = new ArrayList<>();
        for (Job job : this.getJobs()) {
            Job.ReservedResources reservedResources = job.getNodeName2reservedResources().get(this.getName());
            if (reservedResources != null) list.add(reservedResources);
        }
        return list;
    }

    public String getStatus(String name) {
        if (status == null) {
            status = new HashMap<>();
            String all = attrs.get(ATTRIBUTE_STATUS);
            if (all != null) {
                for (String s : all.split(", *")) {
                    int pos = s.indexOf('=');
                    if (pos != -1) {
                        status.put(s.substring(0, pos), s.substring(pos + 1));
                    }
                }
            }
        }
        return status.get(name);
    }

//    public Map<String, String> getStatus() {
//        return status;
//    }

    public String getStatusMessage() {
        return getStatus("message");
    }

    public void setGpuJobMap(Map<String, String> gpuJobMap) {
        this.gpuJobMap = gpuJobMap;
    }

    public Map<String, String> getGpuJobMap() {
        return gpuJobMap;
    }

    Pattern p = Pattern.compile("max_(.)+");

    public boolean allowsWalltime(long walltimeSecs) {
        String[] properties = getProperties();
        String max = findPrefixedProperty("max_");
        if (max != null) {
            long maxSeconds = PbsUtils.parseWalltime(max);
            if (walltimeSecs > maxSeconds) return false;
        }
        String min = findPrefixedProperty("min_");
        if (min != null) {
            long minSeconds = PbsUtils.parseWalltime(min);
            if (walltimeSecs < minSeconds) return false;
        }
        return true;
    }

    public String getMaxWalltime() {
        return findPrefixedProperty("max_");
    }

    public String getMinWalltime() {
        return findPrefixedProperty("min_");
    }

    /**
     * Searches node properties and returns the first that start with given prefix, with the prefix truncated.
     *
     * @param prefix characters for beginning of the string
     * @return null or value of property without prefix
     */
    private String findPrefixedProperty(String prefix) {
        for (String prop : properties) {
            if (prop.startsWith(prefix)) {
                return prop.substring(prefix.length());
            }
        }
        return null;
    }

    /**
     * Returns attributes that started with resources_total, but without the prefix.
     *
     * @return map of attributes with attribute names without the prefix
     */
    public Map<String, String> getResources() {
        String RESOURCE_PREFIX = pbs.isTorque() ? ATTRIBUTE_PREFIX_RESOURCES_AVAILABLE_TORQUE : ATTRIBUTE_PREFIX_RESOURCES_AVAILABLE_PBSPRO;
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<String, String> e : getAttributes().entrySet()) {
            if (e.getKey().startsWith(RESOURCE_PREFIX)) {
                map.put(e.getKey().substring(RESOURCE_PREFIX.length()), e.getValue());
            }
        }
        return map;
    }

    public String getResourceString(String name) {
        String RESOURCE_PREFIX = pbs.isTorque() ? ATTRIBUTE_PREFIX_RESOURCES_AVAILABLE_TORQUE : ATTRIBUTE_PREFIX_RESOURCES_AVAILABLE_PBSPRO;
        return attrs.get(RESOURCE_PREFIX + name);
    }

    public List<String> getResourceOfTypeList(String resourceName) {
        String resourceValue = getResourceString(resourceName);
        return Arrays.asList(resourceValue == null ? new String[0] : resourceValue.split(","));
    }

    public List<String> getResourceQueueList() {
        return getResourceOfTypeList(PBS.QUEUE_LIST);
    }


    private List<NodeResource> nodeResources;
    /**
     * Parses and returns available and assigned resources
     * @return list of NodeResource objects
     */
    public List<NodeResource> getNodeResources() {
        if(nodeResources!=null) return nodeResources;
        if(pbs.isTorque()) return Collections.emptyList();

        Map<String,String> assigned = new HashMap<>();
        Map<String,String> available = new HashMap<>();

        for (Map.Entry<String, String> e : getAttributes().entrySet()) {
            String key = e.getKey();
            if (key.startsWith(ATTRIBUTE_PREFIX_RESOURCES_AVAILABLE_PBSPRO)) {
                String resourceName = key.substring(ATTRIBUTE_PREFIX_RESOURCES_AVAILABLE_PBSPRO.length());
                available.put(resourceName,e.getValue());
            } else if (key.startsWith(ATTRIBUTE_PREFIX_RESOURCES_ASSIGNED_PBSPRO)) {
                String resourceName = key.substring(ATTRIBUTE_PREFIX_RESOURCES_ASSIGNED_PBSPRO.length());
                assigned.put(resourceName,e.getValue());
            }
        }
        Set<String> allResourceNames = new HashSet<>();
        allResourceNames.addAll(available.keySet());
        allResourceNames.addAll(assigned.keySet());
        List<NodeResource> tmpNodeResources = new ArrayList<>(allResourceNames.size());
        for (String resourceName : allResourceNames) {
            tmpNodeResources.add(new NodeResource(resourceName,available.get(resourceName),assigned.get(resourceName)));
        }
        tmpNodeResources.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
        nodeResources = tmpNodeResources;
        return nodeResources;
    }

    public static class NodeResource {
        public enum Type {INT, LIST, SIZE, BOOLEAN, STRING}

        private Type type;
        private String name;
        private String available;
        private String assigned;

        public NodeResource(String name, String available, String assigned) {
            this.name = name;
            this.available = available;
            this.assigned = assigned;
            this.type = detectType(available!=null?available:assigned);
        }

        public static Type detectType(String value) {
            Type type;
            if ("True".equals(value)) {
                type = Type.BOOLEAN;
            } else if (value.matches("\\d+")) {
                type = Type.INT;
            } else if (value.matches("\\d+[kmg]b")) {
                type = Type.SIZE;
            } else if (value.contains(",")) {
                type = Type.LIST;
            } else {
                type = Type.STRING;
            }
            return type;
        }

        public Type getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getAvailable() {
            return available;
        }

        public String getAssigned() {
            return assigned;
        }

        @Override
        public String toString() {
            return "NodeResource{" +
                    "type=" + type +
                    ", name='" + name + '\'' +
                    ", available='" + available + '\'' +
                    ", assigned='" + assigned + '\'' +
                    '}';
        }
    }
}
