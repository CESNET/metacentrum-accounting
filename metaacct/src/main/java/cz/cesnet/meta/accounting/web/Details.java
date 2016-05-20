package cz.cesnet.meta.accounting.web;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.integration.spring.SpringBean;
import cz.cesnet.meta.accounting.server.data.PbsRecordData;
import cz.cesnet.meta.accounting.server.service.PbsRecordManager;

public class Details extends AccountingWebBase {

  @SpringBean
  PbsRecordManager pbsRecordManager;
  
  private PbsRecordData pbsRecord;
  private long offset;
  private long userId;

  private long size;
  private String pbsIdString;

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  @DefaultHandler
  public Resolution view() {
    pbsRecord = pbsRecordManager.getPbsRecordForIdString(pbsIdString);
    return new ForwardResolution("/viewDetail.jsp");
  }
  
  public Resolution next() {
    pbsRecord = pbsRecordManager.getPbsRecordForIdString(pbsIdString);
    return new ForwardResolution("/viewDetail.jsp");
  } 

  public PbsRecordData getPbsRecord() {
    return pbsRecord;
  }

  public void setPbsRecords(PbsRecordData pbsRecord) {
    this.pbsRecord = pbsRecord;
  }

  @Override
  public String getTitle() {    
    return super.getTitle() + " - details for pbs record " + pbsIdString;
  }

  public long getOffset() {
    return offset;
  }

  public void setOffset(long offset) {
    this.offset = offset;
  }

  public long getUserId() {
    return userId;
  }

  public void setUserId(long userId) {
    this.userId = userId;
  }

  public void setPbsIdString(String pbsIdString) {
    this.pbsIdString = pbsIdString;
  }

  public String getPbsIdString() {
    return pbsIdString;
  }
  
}
