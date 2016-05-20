package cz.cesnet.meta.accounting.server.chart;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ChartGroup {

  private String name;
  private Color color;
  private List<ChartItem> items = new ArrayList<ChartItem>();

  public ChartGroup(String name, Color color) {
    this.name = name;
    this.color = color;
  }
  
  public void addItem(ChartItem item) {
    items.add(item);
  }

  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  public List<ChartItem> getItems() {
    return items;
  }

  public void setItems(List<ChartItem> items) {
    this.items = items;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
