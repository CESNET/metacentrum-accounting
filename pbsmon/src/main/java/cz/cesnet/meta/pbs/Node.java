package cz.cesnet.meta.pbs;

import cz.cesnet.meta.acct.OutageRecord;
import cz.cesnet.meta.pbscache.Scratch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: Node.java,v 1.50 2015/08/26 11:24:27 makub Exp $
 */
@SuppressWarnings("unused")
public class Node extends PbsInfoObject {
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
    final static Logger log = LoggerFactory.getLogger(Node.class);
    //helpers
    private static final Pattern whole = Pattern.compile("^([A-Za-z]+)(\\d*)-*([0-9a-z]*)");
    public static final String RESOURCES_TOTAL = "resources_total.";
    private String shortName;
    private String state;
    private String pbsState;
    private String maghrateaState;
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
        maghrateaState = null;
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
            this.pbsState = PbsUtils.substringBefore(attrs.get("state"), ',');

            if (this.pbsState.equals(STATE_FREE)) {
                if (getNoOfUsedCPUInt() >= getNoOfCPUInt()) {
                    this.pbsState = STATE_JOB_FULL;
                } else if (this.getFreeMemoryInt() <= 0) {
                    this.pbsState = STATE_JOB_FULL;
                } else if (getNoOfUsedCPUInt() > 0) {
                    this.pbsState = STATE_PARTIALY_FREE;
                }
            }
            if ("True".equals(attrs.get("exclusively_assigned"))) {
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

        if (this.isCloud()) {
            this.state = STATE_CLOUD;
            return this.state;
        }
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
        if (state.equals(STATE_FREE) && maghrateaState != null && !STATE_FREE.equals(maghrateaState) && !"running".equals(maghrateaState)) {
            state = maghrateaState;
        }
        return this.state;
    }

    public void setMagratheaStatus(String state) {
        this.maghrateaState = state;
    }

    public String getMaghrateaState() {
        return maghrateaState;
    }

    public String getNtype() {
        //cluster, cloud, virtual
        return attrs.get("ntype");
    }

    /**
     * Returns whether node type is cloud, i.e. dom0 on virtualized nodes.
     *
     * @return true of false
     */
    public boolean isCloud() {
        return "cloud".equals(getNtype());
    }

    public boolean isComputingNode() {
        return !isCloud();
    }

    public String getArch() {
        if (pbs.isTorque()) {
            return getStatus("uname");
        } else {
            return attrs.get("resources_available.arch");
        }
    }

    public String getComment() {
        String comment = attrs.get("comment");
        if (comment == null) {
            comment = attrs.get("note");
        }
        return comment;
    }

    public String getNoOfCPU() {
        return attrs.get("np");
    }

    public int getNoOfCPUInt() {
        String n = getNoOfCPU();
        return n == null ? 0 : Integer.parseInt(n);
    }

    public String getNoOfGPU() {
        return attrs.get("resources_total.gpu");
    }

    public int getNoOfGPUInt() {
        String n = getNoOfGPU();
        return n == null ? 0 : Integer.parseInt(n);
    }

    public boolean getHasGPU() {
        return getNoOfGPUInt() > 0;
    }

    public String getNoOfUsedGPU() {
        return attrs.get("resources_used.gpu");
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
            return attrs.get("pcpus");
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
        return attrs.get("npfree");
    }

    public String getNoOfUsedCPU() {
        if (pbs.isTorque()) {
            return Integer.toString(getNoOfUsedCPUInt());
        } else {
            return attrs.get("resources_assigned.ncpus");
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
            mem = attrs.get("resources_total.mem");
        } else {
            mem = attrs.get("resources_available.mem");
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
            return PbsUtils.parsePbsBytes(attrs.get("resources_used.mem"));
        } else {
            return PbsUtils.parsePbsBytes(attrs.get("resources_assigned.mem"));
        }
    }

    public String getUsedMemory() {
        return PbsUtils.formatInPbsUnits(getUsedMemoryInt());
    }

    public boolean isExclusivelyAssigned() {
        return Boolean.parseBoolean(attrs.get("exclusively_assigned"));
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
        String rq = attrs.get("queue");
        if (rq != null) rq = rq + getPbs().getSuffix();
        return rq;
    }

    private String getRequiredQueueShort() {
        return attrs.get("queue");
    }

    /**
     * Gets value of attribute "jobs", separated into Strings with removed CPU numbers.
     *
     * @return array of strings
     */
    public String[] getJobIds() {
        if (this.jobsIds == null) {
            String jobsatr = attrs.get("jobs");
            String[] jobsIds = jobsatr != null ? jobsatr.split(", *") : new String[0];
            HashMap<String, Integer> cpuCounts = new HashMap<>();
            for (int i = 0; i < jobsIds.length; i++) {
                jobsIds[i] = PbsUtils.substringAfter(jobsIds[i], '/');
                PbsUtils.updateCount(cpuCounts, jobsIds[i], 1);
            }
            Set<String> keys = cpuCounts.keySet();
            jobsIds = keys.toArray(new String[keys.size()]);
            Arrays.sort(jobsIds);
            this.jobsIds = jobsIds;
        }
        return this.jobsIds;
    }


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
            Collections.sort(jobs, Job.TIME_STARTED_JOB_COMPARATOR);
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
            Collections.sort(plannedJobs, Job.PLANNED_START_JOB_COMPARATOR);
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
            String available_before = attrs.get("available_before");
            if (available_before == null || "0".equals(available_before)) return null;
            availableBefore = PbsUtils.getJavaTime(available_before);
        }
        return availableBefore;
    }

    private Date availableAfter;

    public Date getAvailableAfter() {
        // available_after - konec plánované odstávky
        if (availableAfter == null) {
            String available_after = attrs.get("available_after");
            if (available_after == null || "0".equals(available_after)) return null;
            availableAfter = PbsUtils.getJavaTime(available_after);
        }
        return availableAfter;
    }

    public boolean isPlannedOutage() {
        return getAvailableBefore() != null || getAvailableAfter() != null;
    }

    public boolean getHasJobs() {
        return attrs.get("jobs") != null;
    }

    /**
     * Gets value of attribute "properties", separated into Strings.
     *
     * @return array of properties
     */
    public String[] getProperties() {
        if (this.properties == null) {
            String propatr = attrs.get("properties");
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
            scratch = new Scratch();
        }
        return scratch;
    }

    public void setScratch(Scratch scratch) {
        if (scratch == null) scratch = new Scratch();
        scratch.setSsdSize(getStatus("scratch_ssd"));
        scratch.setLocalSize(getStatus("scratch_local"));
        long allocatedSsd = 0L;
        long allocatedLocal = 0L;
        for (Job.ReservedScratch reservedScratch : this.getReservedScratches()) {
            switch (reservedScratch.getType()) {
                case "ssd":
                    allocatedSsd += reservedScratch.getVolumeB();
                    break;
                case "local":
                    allocatedLocal += reservedScratch.getVolumeB();
                    break;
            }
        }
        scratch.setSsdReservedByJobs(allocatedSsd);
        scratch.setLocalReservedByJobs(allocatedLocal);
        this.scratch = scratch;
    }

    public List<Job.ReservedScratch> getReservedScratches() {
        List<Job.ReservedScratch> list = new ArrayList<>();
        for (Job job : this.getJobs()) {
            Job.ReservedScratch reservedScratch = job.getNodeName2reservedScratchMap().get(this.getName());
            if (reservedScratch != null) list.add(reservedScratch);
        }
        return list;
    }

    public String getStatus(String name) {
        if (status == null) {
            status = new HashMap<>();
            String all = attrs.get("status");
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
        if(max!=null) {
            long maxSeconds = PbsUtils.parseWalltime(max);
            if(walltimeSecs>maxSeconds) return false;
        }
        String min = findPrefixedProperty("min_");
        if(min!=null) {
            long minSeconds = PbsUtils.parseWalltime(min);
            if(walltimeSecs<minSeconds) return false;
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
     * @param prefix characters for beginning of the string
     * @return null or value of property without prefix
     */
    private String findPrefixedProperty(String prefix) {
        for (String prop : properties) {
            if (prop.startsWith(prefix)) {
                return prop.substring(prefix.length());
            }
        }
        return  null;
    }

    /**
     * Returns attributes that sterted with resources_total, but without the prefix.
     * @return map fo attributes with attribute names without the prefix
     */
    public Map<String,String> getResources() {
        Map<String,String> map = new HashMap<>();
        for(Map.Entry<String,String> e : getAttributes().entrySet()) {
            if(e.getKey().startsWith(RESOURCES_TOTAL)) {
                map.put(e.getKey().substring(RESOURCES_TOTAL.length()),e.getValue());
            }
        }
        return map;
    }

    public String getResource(String name) {
        return attrs.get(RESOURCES_TOTAL+name);
    }
}
