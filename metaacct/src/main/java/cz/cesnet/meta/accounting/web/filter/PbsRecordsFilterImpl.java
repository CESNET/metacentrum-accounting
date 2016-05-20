package cz.cesnet.meta.accounting.web.filter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("session")
@Component("pbsRecordsFilter")
public class PbsRecordsFilterImpl implements PbsRecordsFilter, Serializable {
  
  private String idString;
  private String jobname;
  private String username;
  private String queue;
  private String pbsServer;
  private Integer cpusFrom;
  private Integer cpusTo;
  private String walltimeFrom;
  private String walltimeTo;
  private Date dateTimeFrom;
  private Date dateTimeTo;
  private Date createTimeFrom;
  private Date createTimeTo;
  private Date startTimeFrom;
  private Date startTimeTo;
  private Date endTimeFrom;
  private Date endTimeTo;
 

  @Override
  public void clear() {
    idString = null;
    jobname = null;
    username = null;
    queue = null;
    cpusFrom = null;
    cpusTo = null;
    walltimeFrom = null;
    walltimeTo = null;
    dateTimeFrom = null;
    dateTimeTo = null;
    createTimeFrom = null;
    createTimeTo = null;
    startTimeFrom = null;
    startTimeTo = null;
    endTimeFrom = null;
    endTimeTo = null;
  }

  @Override
  public Map<String, Object> getSearchCriteria() {
    Map<String, Object> filter = new HashMap<String, Object>();
    if (idString != null) {
      filter.put("idString", "%" + idString + "%");
    }
    if (jobname != null) {
      filter.put("jobname", "%" + jobname + "%");
    }
    if (username != null) {
      filter.put("username", "%" + username + "%");
    }
    if (queue != null) {
      filter.put("queue", "%" + queue + "%");
    }
    if (pbsServer != null) {
      filter.put("pbsServer", "%" + pbsServer + "%");
    }
    if (cpusFrom != null) {
      filter.put("cpusFrom", cpusFrom);
    }
    if (cpusTo != null) {
      filter.put("cpusTo", cpusTo);
    }
    if(walltimeFrom != null) {
      filter.put("walltimeFrom",walltimeFrom);
    }
    if(walltimeTo != null) {
          filter.put("walltimeTo",walltimeTo);
    }
    if (dateTimeFrom != null) {
      filter.put("dateTimeFrom", dateTimeFrom);
    }
    if (dateTimeTo != null) {
      filter.put("dateTimeTo", dateTimeTo);
    }
    if (createTimeFrom != null) {
      filter.put("createTimeFrom", createTimeFrom);
    }
    if (createTimeTo != null) {
      filter.put("createTimeTo", createTimeTo);
    }
    if (startTimeFrom != null) {
      filter.put("startTimeFrom", startTimeFrom);
    }
    if (startTimeTo != null) {
      filter.put("startTimeTo", startTimeTo);
    }
    if (endTimeFrom != null) {
      filter.put("endTimeFrom", endTimeFrom);
    }
    if (endTimeTo != null) {
      filter.put("endTimeTo", endTimeTo);
    }
    return filter;
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

  public Date getCreateTimeFrom() {
    return createTimeFrom;
  }

  public void setCreateTimeFrom(Date createTimeFrom) {
    this.createTimeFrom = createTimeFrom;
  }

  public Date getCreateTimeTo() {
    return createTimeTo;
  }

  public void setCreateTimeTo(Date createTimeTo) {
    this.createTimeTo = createTimeTo;
  }

  public Date getStartTimeFrom() {
    return startTimeFrom;
  }

  public void setStartTimeFrom(Date startTimeFrom) {
    this.startTimeFrom = startTimeFrom;
  }

  public Date getStartTimeTo() {
    return startTimeTo;
  }

  public void setStartTimeTo(Date startTimeTo) {
    this.startTimeTo = startTimeTo;
  }

  public Date getEndTimeFrom() {
    return endTimeFrom;
  }

  public void setEndTimeFrom(Date endTimeFrom) {
    this.endTimeFrom = endTimeFrom;
  }

  public Date getEndTimeTo() {
    return endTimeTo;
  }

  public void setEndTimeTo(Date endTimeTo) {
    this.endTimeTo = endTimeTo;
  }

  public String getPbsServer() {
    return pbsServer;
  }

  public void setPbsServer(String pbsServer) {
    this.pbsServer = pbsServer;
  }

  public Date getDateTimeFrom() {
    return dateTimeFrom;
  }

  public void setDateTimeFrom(Date dateTimeFrom) {
    this.dateTimeFrom = dateTimeFrom;
  }

  public Date getDateTimeTo() {
    return dateTimeTo;
  }

  public void setDateTimeTo(Date dateTimeTo) {
    this.dateTimeTo = dateTimeTo;
  }

  public Integer getCpusFrom() {
    return cpusFrom;
  }

  public void setCpusFrom(Integer cpusFrom) {
    this.cpusFrom = cpusFrom;
  }

  public Integer getCpusTo() {
    return cpusTo;
  }

  public void setCpusTo(Integer cpusTo) {
    this.cpusTo = cpusTo;
  }

    @Override
    public String getWalltimeFrom() {
        return walltimeFrom;
    }

    @Override
    public void setWalltimeFrom(String walltimeFrom) {
        this.walltimeFrom = walltimeFrom;
    }

    @Override
    public String getWalltimeTo() {
        return walltimeTo;
    }

    @Override
    public void setWalltimeTo(String walltimeTo) {
        this.walltimeTo = walltimeTo;
    }

    public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

}
