package cz.cesnet.meta.accounting.server.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PBSMessage implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private String user;
  private String group;
  private String jobname;
  private String queue;
  private long createTime;  
  private long startTime;
  private long endTime;
  private int exitStatus;
  
  private long reqNcpus;
  private int reqNodect;
  private String reqNodes; 
  private long reqWalltime;
  private long reqMem;
  private Integer reqGpus;
  
  private long usedMem;
  private long usedVmem;  
  private long usedWalltime;
  private long usedCputime;
  private int usedNcpus;
  private int usedCpupercent;
  private long softWalltime;

  private List<PBSHost> execHosts;
  private List<NodeSpec> nodesSpecs;

  /** Creates a new instance of PBSMessage */
  public PBSMessage() {
    this.execHosts = new ArrayList<PBSHost>();
  }

  public long getCreateTime() {
    return createTime;
  }

  public void setCreateTime(long createTime) {
    this.createTime = createTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
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

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
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

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public long getReqNcpus() {
    return reqNcpus;
  }

  public void setReqNcpus(long reqNcpus) {
    this.reqNcpus = reqNcpus;
  }

  public int getReqNodect() {
    return reqNodect;
  }

  public void setReqNodect(int reqNodect) {
    this.reqNodect = reqNodect;
  }

  public String getReqNodes() {
    return reqNodes;
  }

  public void setReqNodes(String reqNodes) {
    this.reqNodes = reqNodes;
  }

  public long getReqWalltime() {
    return reqWalltime;
  }

  public void setReqWalltime(long reqWalltime) {
    this.reqWalltime = reqWalltime;
  }

  public long getReqMem() {
    return reqMem;
  }

  public void setReqMem(long reqMem) {
    this.reqMem = reqMem;
  }

  public long getUsedMem() {
    return usedMem;
  }

  public void setUsedMem(long usedMem) {
    this.usedMem = usedMem;
  }

  public long getUsedVmem() {
    return usedVmem;
  }

  public void setUsedVmem(long usedVmem) {
    this.usedVmem = usedVmem;
  }

  public long getUsedWalltime() {
    return usedWalltime;
  }

  public void setUsedWalltime(long usedWalltime) {
    this.usedWalltime = usedWalltime;
  }

  public long getUsedCputime() {
    return usedCputime;
  }

  public void setUsedCputime(long usedCputime) {
    this.usedCputime = usedCputime;
  }

  public int getUsedNcpus() {
    return usedNcpus;
  }

  public void setUsedNcpus(int usedNcpus) {
    this.usedNcpus = usedNcpus;
  }

  public int getUsedCpupercent() {
    return usedCpupercent;
  }

  public void setUsedCpupercent(int usedCpupercent) {
    this.usedCpupercent = usedCpupercent;
  }

  public Integer getReqGpus() {
    return reqGpus;
  }

  public void setReqGpus(Integer reqGpus) {
    this.reqGpus = reqGpus;
  }

  public long getSoftWalltime() {
    return softWalltime;
  }

  public void setSoftWalltime(long softWalltime) {
    this.softWalltime = softWalltime;
  }

  @Override
    public String toString() {
        return "PBSMessage{" +
                "user='" + user + '\'' +
                ", group='" + group + '\'' +
                ", jobname='" + jobname + '\'' +
                ", queue='" + queue + '\'' +
                ", createTime=" + createTime +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", exitStatus=" + exitStatus +
                ", reqNcpus=" + reqNcpus +
                ", reqNodect=" + reqNodect +
                ", reqNodes='" + reqNodes + '\'' +
                ", reqWalltime=" + reqWalltime +
                ", softWalltime=" + softWalltime +
                ", reqMem=" + reqMem +
                ", usedMem=" + usedMem +
                ", usedVmem=" + usedVmem +
                ", usedWalltime=" + usedWalltime +
                ", usedCputime=" + usedCputime +
                ", usedNcpus=" + usedNcpus +
                ", usedCpupercent=" + usedCpupercent +
                ", execHosts=" + execHosts +
                '}';
    }

  public void setNodesSpecs(List<NodeSpec> nodesSpecs) {
    this.nodesSpecs = nodesSpecs;
  }

  public List<NodeSpec> getNodesSpecs() {
    return nodesSpecs;
  }
}
