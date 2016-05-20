package cz.cesnet.meta.accounting.web;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.integration.spring.SpringBean;
import cz.cesnet.meta.accounting.displaytag.PaginationFilter;
import cz.cesnet.meta.accounting.server.service.ReceivePbsManager;
import cz.cesnet.meta.accounting.server.util.Page;

public class ReceivedPbs extends AccountingWebBase {
  
  @SpringBean
  ReceivePbsManager receivePbsManager;
  private Page logs;  

  @DefaultHandler
  public Resolution view() {    
    logs = receivePbsManager.getPageReceivedPbsLogs(getPageNumber(), PaginationFilter.DEFAULT_PAGE_SIZE_LONG, getPageSize(), getSortColumn(), isAscending());
    return new ForwardResolution("/viewReceivedPbs.jsp");
  }

  public Page getLogs() {
    return logs;
  }

  public void setLogs(Page logs) {
    this.logs = logs;
  }  
  
}
