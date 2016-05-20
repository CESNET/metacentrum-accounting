package cz.cesnet.meta.accounting.web;

import java.util.Date;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.integration.spring.SpringBean;
import cz.cesnet.meta.accounting.displaytag.PaginationFilter;
import cz.cesnet.meta.accounting.server.service.KernelRecordManager;
import cz.cesnet.meta.accounting.server.util.Page;

public class UsersForBinary extends AccountingWebBase {

  @SpringBean
  KernelRecordManager kernelRecordManager;
  
  Page usersForBinaryStats;

  private String command;
  private Long fromDate;
  private Long toDate;

  @DefaultHandler
  public Resolution view() {    
    usersForBinaryStats = kernelRecordManager.getUsersForBinaryStats(command, new Date(fromDate), new Date(toDate), getPageNumber(), PaginationFilter.DEFAULT_PAGE_SIZE_SHORT, getPageSize(), getSortColumn(), isAscending());     
    return new ForwardResolution("/viewUsersForBinaryStats.jsp");
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  } 

  public Page getUsersForBinaryStats() {
    return usersForBinaryStats;
  }

  public void setUsersForBinaryStats(Page usersForBinaryStats) {
    this.usersForBinaryStats = usersForBinaryStats;
  }

  public Long getToDate() {
    return toDate;
  }

  public void setToDate(Long toDate) {
    this.toDate = toDate;
  }

  public Long getFromDate() {
    return fromDate;
  }

  public void setFromDate(Long fromDate) {
    this.fromDate = fromDate;
  }
  
}
