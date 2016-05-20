package cz.cesnet.meta.accounting.web.filter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("session")
@Component("receiveLogFilter")
public class ReceiveLogFilterImpl implements ReceiveLogFilter {
  
  private String hostname;
  private Date receiveTimeFrom;
  private Date receiveTimeTo;

  @Override
  public void clear() {
    hostname = null;
    receiveTimeFrom = null;
    receiveTimeTo = null;
  }

  @Override
  public Map<String, Object> getSearchCriteria() {
    Map<String, Object> filter = new HashMap<String, Object>();
    
    if (hostname != null) {
      filter.put("hostname", "%" + hostname + "%");
    }
    if (receiveTimeFrom != null) {
      filter.put("receiveTimeFrom", receiveTimeFrom);
    }
    if (receiveTimeTo != null) {
      filter.put("receiveTimeTo", receiveTimeTo);
    }
    return filter;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public Date getReceiveTimeFrom() {
    return receiveTimeFrom;
  }

  public void setReceiveTimeFrom(Date receiveTimeFrom) {
    this.receiveTimeFrom = receiveTimeFrom;
  }

  public Date getReceiveTimeTo() {
    return receiveTimeTo;
  }

  public void setReceiveTimeTo(Date receiveTimeTo) {
    this.receiveTimeTo = receiveTimeTo;
  }

 

}
