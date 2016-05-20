package cz.cesnet.meta.accounting.web.filter;

import java.util.Date;

public interface ReceiveLogFilter extends Filter {
    
  String getHostname();
  void setHostname(String hostname);
  
  Date getReceiveTimeFrom();
  void setReceiveTimeFrom(Date time);
  
  Date getReceiveTimeTo();
  void setReceiveTimeTo(Date time);  
}
