package cz.cesnet.meta.accounting.server.data;

import java.util.Date;

public class PbsReceiveLog {
  
  long id;  
  Date receiveTime;
  Date minimalTime;
  Date maximalTime;
  String serverHostname;
  
  public PbsReceiveLog(long id, Date receiveTime, Date minimalTime, Date maximalTime, String serverHostname) {
    this.id = id;
    this.receiveTime = receiveTime;
    this.minimalTime = minimalTime;
    this.maximalTime = maximalTime;
    this.serverHostname = serverHostname;    
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Date getMaximalTime() {
    return maximalTime;
  }

  public void setMaximalTime(Date maximalTime) {
    this.maximalTime = maximalTime;
  }

  public Date getMinimalTime() {
    return minimalTime;
  }

  public void setMinimalTime(Date minimalTime) {
    this.minimalTime = minimalTime;
  }

  public Date getReceiveTime() {
    return receiveTime;
  }

  public void setReceiveTime(Date receiveTime) {
    this.receiveTime = receiveTime;
  }

  public String getServerHostname() {
    return serverHostname;
  }

  public void setServerHostname(String serverHostname) {
    this.serverHostname = serverHostname;
  }

}
