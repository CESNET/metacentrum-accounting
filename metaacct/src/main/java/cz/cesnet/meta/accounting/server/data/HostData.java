package cz.cesnet.meta.accounting.server.data;

import java.util.Date;

public class HostData {
  private long id;
  private String hostName;  
  private long kernelLogsCount;
  private Date lastLogDate;

  public HostData(long id, String hostName, long logsCount, Date lastLogDate) {
    this.id = id;
    this.hostName = hostName;    
    this.kernelLogsCount = logsCount;
    this.lastLogDate = lastLogDate;
  }

  /** Creates a new instance of PBSHost */
  public HostData() {
  }
  
  public String getHostName() {
    return hostName;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getKernelLogsCount() {
    return kernelLogsCount;
  }

  public void setKernelLogsCount(long kernelLogsCount) {
    this.kernelLogsCount = kernelLogsCount;
  }

  public Date getLastLogDate() {
    return lastLogDate;
  }

  public void setLastLogDate(Date lastLogDate) {
    this.lastLogDate = lastLogDate;
  }

  
}
