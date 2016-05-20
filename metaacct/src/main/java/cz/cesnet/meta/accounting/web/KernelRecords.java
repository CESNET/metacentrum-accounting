package cz.cesnet.meta.accounting.web;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.integration.spring.SpringBean;
import cz.cesnet.meta.accounting.displaytag.PaginationFilter;
import cz.cesnet.meta.accounting.server.service.KernelRecordManager;
import cz.cesnet.meta.accounting.server.util.Page;

public class KernelRecords extends AccountingWebBase {

  @SpringBean
  KernelRecordManager kernelRecordManager;
  
  private Page kernelRecords;
  private String pbsIdString;
  
  @DefaultHandler
  public Resolution view() {        
    kernelRecords = kernelRecordManager.getKernelRecordsForPbsIdString(pbsIdString, getPageNumber(), PaginationFilter.DEFAULT_PAGE_SIZE_SHORT, getPageSize(), getSortColumn(), isAscending());
    return new ForwardResolution("/viewKernelRecords.jsp");
  }
  
  @Override
  public String getTitle() {    
    return super.getTitle() + " - kernel records for pbs job " + pbsIdString;
  }

  public void setPbsIdString(String pbsIdString) {
    this.pbsIdString = pbsIdString;
  }

  public String getPbsIdString() {
    return pbsIdString;
  }

  public Page getKernelRecords() {
    return kernelRecords;
  }

  public void setKernelRecords(Page kernelRecords) {
    this.kernelRecords = kernelRecords;
  }
  
}
