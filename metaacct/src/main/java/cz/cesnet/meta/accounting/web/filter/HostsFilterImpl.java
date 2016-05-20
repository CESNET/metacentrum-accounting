package cz.cesnet.meta.accounting.web.filter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("session")
@Component("hostsFilter")
public class HostsFilterImpl implements HostsFilter {
  
  private String hostname;
  private Integer kernelLogsFrom;
  private Integer kernelLogsTo;
  private Date lastLogDateFrom;
  private Date lastLogDateTo;
  
  @Override
  public void clear() {
    hostname = null;
    kernelLogsFrom = null;
    kernelLogsTo = null;
    lastLogDateFrom = null;
    lastLogDateTo = null;
  }
  
  @Override
  public Map<String, Object> getSearchCriteria() {
    Map<String, Object> filter = new HashMap<String, Object>();
    if (hostname != null) {
      filter.put("hostname", "%" + hostname + "%");
    }
    if (kernelLogsFrom != null) {
      filter.put("kernelLogsFrom", kernelLogsFrom);
    }
    if (kernelLogsTo != null) {
      filter.put("kernelLogsTo", kernelLogsTo);
    }
    if (lastLogDateFrom != null) {
      filter.put("lastLogDateFrom", lastLogDateFrom);
    }
    if (lastLogDateTo != null) {
      filter.put("lastLogDateTo", lastLogDateTo);
    }
    return filter;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public Integer getKernelLogsFrom() {
    return kernelLogsFrom;
  }

  public void setKernelLogsFrom(Integer kernelLogsFrom) {
    this.kernelLogsFrom = kernelLogsFrom;
  }

  public Integer getKernelLogsTo() {
    return kernelLogsTo;
  }

  public void setKernelLogsTo(Integer kernelLogsTo) {
    this.kernelLogsTo = kernelLogsTo;
  }

  public Date getLastLogDateFrom() {
    return lastLogDateFrom;
  }

  public void setLastLogDateFrom(Date lastLogDateFrom) {
    this.lastLogDateFrom = lastLogDateFrom;
  }

  public Date getLastLogDateTo() {
    return lastLogDateTo;
  }

  public void setLastLogDateTo(Date lastLogDateTo) {
    this.lastLogDateTo = lastLogDateTo;
  }  
}
