package cz.cesnet.meta.accounting.server.data;

public class PBSRecord {
  private long dateTime;
  private PBSRecordType recordType;
  private String idString;
  private PBSMessage messageText;
  
  /** Creates a new instance of PBSEntry */
  public PBSRecord() {
   
  }

  public long getDateTime() {
    return dateTime;
  }

    /**
     * Java cas.
     * @param dateTime java cas z parsovani prvniho pole zaznamu v PBS
     */
  public void setDateTime(long dateTime) {
    this.dateTime = dateTime;
  }

  public String getIdString() {
    return idString;
  }

  public void setIdString(String idString) {
    this.idString = idString;
  }

  public PBSMessage getMessageText() {
    return messageText;
  }

  public void setMessageText(PBSMessage messageText) {
    this.messageText = messageText;
  }

  public PBSRecordType getRecordType() {
    return recordType;
  }

  public void setRecordType(PBSRecordType recordType) {
    this.recordType = recordType;
  }

    @Override
    public String toString() {
        return "PBSRecord{" +
                "dateTime=" + dateTime +
                ", recordType=" + recordType +
                ", idString='" + idString + '\'' +
                ", messageText=" + messageText +
                '}';
    }
}
