package cz.cesnet.meta.accounting.server.chart;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;



public class GoogleChartUrlHelper {

  public static final String GOOGLE_CHART_BASE_URL = "http://chart.apis.google.com/chart?";
  public static final String PARAM_DIVIDER = "&";
  
  public static String getImageUrl(Chart chart) {
    StringBuilder urlBuilder = new StringBuilder(GOOGLE_CHART_BASE_URL);
    urlBuilder.append("chs=").append(chart.getXSize()).append("x").append(chart.getYSize()).append(PARAM_DIVIDER);
    urlBuilder.append("cht=").append(chart.getChartType()).append(PARAM_DIVIDER);
    urlBuilder.append("chbh=" + chart.getProportions()).append(PARAM_DIVIDER);
    
    
    
    StringBuilder valuesBuilder = new StringBuilder();
    StringBuilder colorsBuilder = new StringBuilder();
    StringBuilder scalingBuilder = new StringBuilder();
    StringBuilder labelsBuilder = new StringBuilder();
    int j=1;
    long celkoveMax = 1;
    for (ChartGroup group : chart.getGroups()) {
      int i = 1;
      long max = 1;
      for (ChartItem item : group.getItems()) {
        max = max < item.getSize().longValue()? item.getSize().longValue() : max;
        celkoveMax = celkoveMax < item.getSize().longValue()? item.getSize().longValue() : celkoveMax;
        valuesBuilder.append(item.getSize().longValue());
        if (i < group.getItems().size()) {
          valuesBuilder.append(",");
        }
        i++;
      }
      scalingBuilder.append("0," + max);
      Color c = group.getColor();
      colorsBuilder.append(String.format( "%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue() ));
      labelsBuilder.append(group.getName());
      if (j < chart.getGroups().size()) {
        valuesBuilder.append("|");
        scalingBuilder.append(",");
        colorsBuilder.append(",");
        labelsBuilder.append("|");
      }
      j++;
    }
    
    urlBuilder.append("chd=t:").append(valuesBuilder.toString());
    urlBuilder.append(PARAM_DIVIDER);
    urlBuilder.append("chds=0," + celkoveMax).append(PARAM_DIVIDER);
    urlBuilder.append("chdl=").append(labelsBuilder.toString()).append(PARAM_DIVIDER);
    urlBuilder.append("chco=").append(colorsBuilder.toString()).append(PARAM_DIVIDER);
    urlBuilder.append("chxt=x,y").append(PARAM_DIVIDER);    
    urlBuilder.append("chxr=1,0," + celkoveMax).append(PARAM_DIVIDER);
    urlBuilder.append("chxs=0,0000ff,8").append(PARAM_DIVIDER);
    urlBuilder.append("chxl=0:|");
    try {
      for (ChartItem item : chart.getGroups().get(0).getItems()) {      
          urlBuilder.append(URLEncoder.encode(item.getLabel(), "utf-8")).append("|");
      }
      urlBuilder.append(PARAM_DIVIDER);      
      
      urlBuilder.append("chtt=").append(URLEncoder.encode(chart.getTitle(), "utf-8"));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return urlBuilder.toString();
  }
  
  
}
