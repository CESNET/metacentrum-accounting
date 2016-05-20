package cz.cesnet.meta.accounting.server.data;

import java.io.Serializable;
import java.util.Date;

public class KernelReceiveLog implements Serializable {
  private static final long serialVersionUID = 1L;

  long id;
  long hostId;
  String hostname;
  Date receiveTime;
  Date minimalTime;
  Date maximalTime;

  public KernelReceiveLog() {    
  }
  
  public KernelReceiveLog(long id, long hostId, String hostname, Date receiveTime, Date minimalTime, Date maximalTime) {
    this.id = id;
    this.hostId = hostId;
    this.hostname = hostname;
    this.receiveTime = receiveTime;
    this.minimalTime = minimalTime;
    this.maximalTime = maximalTime;
  }

  public long getHostId() {
    return hostId;
  }

  public void setHostId(long hostId) {
    this.hostId = hostId;
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

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

}
