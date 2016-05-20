package cz.cesnet.meta.accounting.web;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.integration.spring.SpringBean;
import cz.cesnet.meta.accounting.displaytag.PaginationFilter;
import cz.cesnet.meta.accounting.server.service.ReceiveLogManager;
import cz.cesnet.meta.accounting.server.util.Page;
import cz.cesnet.meta.accounting.web.filter.ReceiveLogFilter;

public class ReceiveLog extends AccountingWebBase {
  
  @SpringBean
  ReceiveLogManager receiveLogManager;
  private Page logs;
  
  @SpringBean
  ReceiveLogFilter receiveLogFilter;
  
  public Resolution clear() {
    receiveLogFilter.clear();
    return new RedirectResolution(getClass());
  }
  
  @DefaultHandler
  public Resolution view() {    
    logs = receiveLogManager.getPageReceiveLogs(getFilter().getSearchCriteria(), getPageNumber(), PaginationFilter.DEFAULT_PAGE_SIZE_LONG, getPageSize(), getSortColumn(), isAscending());    
    return new ForwardResolution("/viewReceiveLogs.jsp");
  }

  @Override
  public String getTitle() {
    return super.getTitle() + " - received kernel logs";
  }

  public Page getLogs() {
    return logs;
  }

  public void setLogs(Page logs) {
    this.logs = logs;
  }

  public ReceiveLogFilter getFilter() {
    return receiveLogFilter;
  }

  public void setFilter(ReceiveLogFilter receiveLogFilter) {
    this.receiveLogFilter = receiveLogFilter;
  }
  
}
