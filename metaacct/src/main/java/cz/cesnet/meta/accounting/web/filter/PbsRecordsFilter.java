package cz.cesnet.meta.accounting.web.filter;

import java.util.Date;

public interface PbsRecordsFilter extends Filter {
  
  String getIdString();
  void setIdString(String string);
  
  String getJobname();
  void setJobname(String jobname);
  
  String getUsername();
  void setUsername(String name);
  
  String getQueue();
  void setQueue(String queue);
  
  String getPbsServer();
  void setPbsServer(String server);
  
  Integer getCpusFrom();
  void setCpusFrom(Integer i);
  
  Integer getCpusTo();
  void setCpusTo(Integer i);
  
  Date getDateTimeFrom();
  void setDateTimeFrom(Date time);
  
  Date getDateTimeTo();
  void setDateTimeTo(Date time);
  
  Date getCreateTimeFrom();
  void setCreateTimeFrom(Date time);
  
  Date getCreateTimeTo();
  void setCreateTimeTo(Date time);
  
  Date getStartTimeFrom();
  void setStartTimeFrom(Date time);
  
  Date getStartTimeTo();
  void setStartTimeTo(Date time);
  
  Date getEndTimeFrom();
  void setEndTimeFrom(Date time);
  
  Date getEndTimeTo();
  void setEndTimeTo(Date time);

  String getWalltimeFrom();
  void setWalltimeFrom(String walltimeFrom);

  String getWalltimeTo();
  void setWalltimeTo(String walltimeTo);
}
