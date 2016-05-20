package cz.cesnet.meta.accounting.server.data;

import cz.cesnet.meta.accounting.web.util.PbsUtils;

import java.util.*;


public class PbsRecordData {
    private String idString;
    private Date dateTime;
    private String jobname;
    private String queue;
    private String reqNodes;
    private long usedMem;
    private int usedNcpus;
    private long usedWalltime;
    private long usedCputime;
    private long reqWalltime;
    private Date createTime;
    private Date startTime;
    private Date endTime;
    private int gpus;
    private int exitStatus;
    private List<PBSHost> execHosts = new ArrayList<PBSHost>();
    private List<KernelRecord> kernelRecords = new ArrayList<KernelRecord>();
    private long totalUserTime;
    private long totalSystemTime;
    private long userId;
    private boolean conflict;
    private long walltime;
    private String serverHostname;
    private String username;


    public PbsRecordData(String idString, Date dateTime, String jobname, String queue,
                         String reqNodes, long usedMem, int usedNcpus, long usedWalltime, long usedCputime, long reqWalltime,
                         Date createTime, Date startTime, Date endTime, int exitStatus, long userId) {
        this.idString = idString;
        this.dateTime = dateTime;
        this.jobname = jobname;
        this.queue = queue;
        this.reqNodes = reqNodes;
        this.usedMem = usedMem;
        this.usedNcpus = usedNcpus;
        this.usedWalltime = usedWalltime;
        this.usedCputime = usedCputime;
        this.reqWalltime = reqWalltime;
        this.createTime = createTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.exitStatus = exitStatus;
        this.userId = userId;
    }

    public PbsRecordData(String idString, Date dateTime, String jobname, String queue, Date createTime, Date startTime,
                         Date endTime, int exitStatus) {
        this.idString = idString;
        this.dateTime = dateTime;
        this.jobname = jobname;
        this.queue = queue;
        this.createTime = createTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.exitStatus = exitStatus;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public List<PBSHost> getExecHosts() {
        return execHosts;
    }

    public void setExecHosts(List<PBSHost> execHosts) {
        this.execHosts = execHosts;
    }

    public int getExitStatus() {
        return exitStatus;
    }

    public void setExitStatus(int exitStatus) {
        this.exitStatus = exitStatus;
    }

    public String getIdString() {
        return idString;
    }

    public void setIdString(String idString) {
        this.idString = idString;
    }

    public String getJobname() {
        return jobname;
    }

    public void setJobname(String jobname) {
        this.jobname = jobname;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public long getTotalSystemTime() {
        return totalSystemTime;
    }

    public void setTotalSystemTime(long totalSystemTime) {
        this.totalSystemTime = totalSystemTime;
    }

    public long getTotalUserTime() {
        return totalUserTime;
    }

    public void setTotalUserTime(long totalUserTime) {
        this.totalUserTime = totalUserTime;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }

    public List<KernelRecord> getKernelRecords() {
        return kernelRecords;
    }

    public void setKernelRecords(List<KernelRecord> kernelRecords) {
        this.kernelRecords = kernelRecords;
    }

    public List<KernelRecord> getKernelRecordsOrderedByTotalUserTimeDesc() {
        List<KernelRecord> result = new ArrayList<KernelRecord>(kernelRecords);

        Collections.sort(result, new Comparator<KernelRecord>() {

            public int compare(KernelRecord o1, KernelRecord o2) {
                return Long.valueOf(o2.getUserTime() - o1.getUserTime()).intValue();
            }

        });
        return result;
    }

    public List<KernelRecord> getKernelRecordsOrderedByTotalSystemTimeDesc() {
        List<KernelRecord> result = new ArrayList<KernelRecord>(kernelRecords);

        Collections.sort(result, new Comparator<KernelRecord>() {

            public int compare(KernelRecord o1, KernelRecord o2) {
                return Long.valueOf(o2.getSystemTime() - o1.getSystemTime()).intValue();
            }

        });
        return result;
    }

    public boolean isConflict() {
        return conflict;
    }

    public void setConflict(boolean conflict) {
        this.conflict = conflict;
    }

    public long getWalltime() {
        return walltime;
    }

    public void setWalltime(long walltime) {
        this.walltime = walltime;
    }

    public String getServerHostname() {
        return serverHostname;
    }

    public void setServerHostname(String serverHostname) {
        this.serverHostname = serverHostname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getReqNodes() {
        return reqNodes;
    }

    public long getUsedMem() {
        return usedMem;
    }

    public void setUsedNcpus(int usedNcpus) {
        this.usedNcpus = usedNcpus;
    }

    public int getUsedNcpus() {
        return usedNcpus;
    }

    public long getUsedWalltime() {
        return usedWalltime;
    }

    public long getUsedCputime() {
        return usedCputime;
    }

    public long getReqWalltime() {
        return reqWalltime;
    }

    public String getUsedMemFormated() {
        return PbsUtils.formatInPbsUnits(getUsedMem());
    }

    public int getCpus() {
        return getUsedNcpus();
    }

    public int getGpus() {
        return gpus;
    }

    public void setGpus(int gpus) {
        this.gpus = gpus;
    }
}
