package cz.cesnet.meta.accounting.server.data;

public class PBSHost {
  private long id;
  private String hostName;

  private int processorNumber;

    @Override
    public String toString() {
        return "PBSHost{" + hostName + ':' + processorNumber +'}';
    }

    /** Creates a new instance of PBSHost */
  public PBSHost() {
  }
 
  public PBSHost(long id, String hostName, int processorNumber) {
    this.id = id;
    this.hostName = hostName;
    this.processorNumber = processorNumber;
  }

  public PBSHost(long id, String hostName) {
    this.id = id;
    this.hostName = hostName;
  }
  
  public PBSHost(String hostName) {
    this.hostName = hostName;
  }

  public String getHostName() {
    return hostName;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public int getProcessorNumber() {
    return processorNumber;
  }

  public void setProcessorNumber(int processorNumber) {
    this.processorNumber = processorNumber;
  }

  
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final PBSHost other = (PBSHost) obj;
    if (hostName == null) {
      if (other.hostName != null)
        return false;
    } else if (!hostName.equals(other.hostName))
      return false;
    if (processorNumber != other.processorNumber)
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = super.hashCode();
    result = PRIME * result + ((hostName == null) ? 0 : hostName.hashCode());
    result = PRIME * result + processorNumber;
    return result;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }
}
