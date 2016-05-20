package cz.cesnet.meta.accounting.web;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.DateTypeConverter;
import net.sourceforge.stripes.validation.Validate;
import cz.cesnet.meta.accounting.displaytag.PaginationFilter;
import cz.cesnet.meta.accounting.server.service.PbsRecordManager;
import cz.cesnet.meta.accounting.server.service.UserManager;
import cz.cesnet.meta.accounting.server.util.Page;
import cz.cesnet.meta.accounting.web.filter.Filter;
import cz.cesnet.meta.accounting.web.filter.PbsRecordsFilter;

public class AllPbs extends AccountingWebBase {

  @SpringBean
  PbsRecordManager pbsRecordManager;
  @SpringBean
  UserManager userManager;
  
  @SpringBean
  PbsRecordsFilter pbsRecordsFilter;
  
  private Page pbsRecords;
  
  private int periodInDays = 0;
  @Validate(converter=DateTypeConverter.class)
  private Date fromDate;
  @Validate(converter=DateTypeConverter.class)
  private Date toDate;
  private int number;

  public Resolution clear() {
    pbsRecordsFilter.clear();
    return new RedirectResolution("/AllPbs.action");
  }

  @DefaultHandler
  public Resolution view() {
    if (number != 0) {
      pbsRecordsFilter.clear();
      pbsRecords = pbsRecordManager.getAllPbsRecords(number, getPageNumber(), PaginationFilter.DEFAULT_PAGE_SIZE_SHORT, getPageSize(), getSortColumn(), isAscending());
    } else {
      if (periodInDays != 0 ) {          
          //pbsRecordsFilter.getDateTimeTo() == null) {
        pbsRecordsFilter.setDateTimeTo(new LocalDate().toDateTimeAtStartOfDay().toDate());  
        pbsRecordsFilter.setDateTimeFrom(new DateTime(pbsRecordsFilter.getDateTimeTo().getTime()).minusDays(periodInDays).toDate());
        periodInDays = 0; 
      }
      pbsRecords = pbsRecordManager.getAllPbsRecords(getFilter().getSearchCriteria(), getPageNumber(), PaginationFilter.DEFAULT_PAGE_SIZE_SHORT, getPageSize(), getSortColumn(), isAscending());
    }
    
    return new ForwardResolution("/viewAllPbsRecords.jsp");
  }  

  public int getPeriodInDays() {
    return periodInDays;
  }

  public void setPeriodInDays(int periodInDays) {
    this.periodInDays = periodInDays;
  }

  public Date getToDate() {
    return toDate;
  }

  public void setToDate(Date toDate) {
    this.toDate = toDate;
  }

  public int getNumber() {
    return number;
  }

  public void setNumber(int number) {
    this.number = number;
  }

  public Page getPbsRecords() {
    return pbsRecords;
  }

  public void setPbsRecords(Page pbsRecords) {
    this.pbsRecords = pbsRecords;
  }

  public Date getFromDate() {
    return fromDate;
  }

  public void setFromDate(Date fromDate) {
    this.fromDate = fromDate;
  }
  
  @Override
  public Filter getFilter() {
    return pbsRecordsFilter;
  } 
  
  public void setFilter(PbsRecordsFilter filter) {    
    this.pbsRecordsFilter = filter;
  }
  
}
