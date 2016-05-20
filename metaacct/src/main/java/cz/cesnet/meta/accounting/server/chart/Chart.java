package cz.cesnet.meta.accounting.server.chart;

import java.util.ArrayList;
import java.util.List;

public class Chart {

  public static final String CHART_TYPE_BAR_HORIZONTAL_STACKED = "bhs";
  public static final String CHART_TYPE_BAR_HORIZONTAL_GROUPED = "bhg";
  public static final String CHART_TYPE_BAR_VERTICAL_STACKED = "bvs";
  public static final String CHART_TYPE_BAR_VERTICAL_GROUPED = "bvg";
  
  private List<ChartGroup> groups = new ArrayList<ChartGroup>();
  private String title;
  private String xAxisText;
  private String yAxisText;
  private String chartType;
  private int xSize;
  private int ySize;
  private String proportions;//tri hodnoty oddelene carkami
  
  public Chart(int xSize, int ySize) {
    this.xSize = xSize;
    this.ySize = ySize;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getXAxisText() {
    return xAxisText;
  }

  public void setXAxisText(String axisText) {
    xAxisText = axisText;
  }

  public String getYAxisText() {
    return yAxisText;
  }

  public void setYAxisText(String axisText) {
    yAxisText = axisText;
  }

  public String getChartType() {
    return chartType;
  }

  public void setChartType(String chartType) {
    this.chartType = chartType;
  }

  public int getXSize() {
    return xSize;
  }

  public void setXSize(int size) {
    xSize = size;
  }

  public int getYSize() {
    return ySize;
  }

  public void setYSize(int size) {
    ySize = size;
  }

  public List<ChartGroup> getGroups() {
    return groups;
  }

  public void setGroups(List<ChartGroup> groups) {
    this.groups = groups;
  }

  public void addGroup(ChartGroup group) {
    groups.add(group);    
  }

  public String getProportions() {
    return proportions;
  }

  public void setProportions(String proportions) {
    this.proportions = proportions;
  }
}
