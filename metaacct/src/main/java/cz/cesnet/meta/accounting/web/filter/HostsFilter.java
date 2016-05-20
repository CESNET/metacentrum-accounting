package cz.cesnet.meta.accounting.web.filter;

import java.util.Date;


public interface HostsFilter extends Filter {

  String getHostname();

  void setHostname(String hostname);

  Integer getKernelLogsFrom();

  void setKernelLogsFrom(Integer kernelLogsFrom);

  Integer getKernelLogsTo();

  void setKernelLogsTo(Integer kernelLogsTo);

  Date getLastLogDateFrom();

  void setLastLogDateFrom(Date lastLogDateFrom);

  Date getLastLogDateTo();

  void setLastLogDateTo(Date lastLogDateTo);
}
