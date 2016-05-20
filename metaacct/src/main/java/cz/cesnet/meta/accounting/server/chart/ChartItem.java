package cz.cesnet.meta.accounting.server.chart;


public class ChartItem {

  private String label;
  private Number size;  

  public ChartItem(String label, Number size) {
    this.label = label;
    this.size = size;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public Number getSize() {
    return size;
  }

  public void setSize(Number size) {
    this.size = size;
  }
 
}
