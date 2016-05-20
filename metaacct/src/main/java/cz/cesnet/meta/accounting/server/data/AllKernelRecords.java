package cz.cesnet.meta.accounting.server.data;

import java.util.ArrayList;
import java.util.List;

public class AllKernelRecords {

  private String hostname;

  private List<KernelRecord> records = new ArrayList<KernelRecord>();
  
  public void addKernelRecord(KernelRecord k) {
    records.add(k);
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public List<KernelRecord> getRecords() {
    return records;
  }

  public void setRecords(List<KernelRecord> records) {
    this.records = records;
  }
}
