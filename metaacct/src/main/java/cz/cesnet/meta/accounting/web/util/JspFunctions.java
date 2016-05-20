package cz.cesnet.meta.accounting.web.util;

import java.util.Collection;
import java.util.Date;

public class JspFunctions {

  @SuppressWarnings( "unchecked" )
  public static Integer getSize(Collection c) {
    return c.size();
  }
  
  public static String formatMilisecondsToHhMmSs(Long miliseconds) {
    long seconds = miliseconds / 1000;
    long minutes = seconds / 60;
    long hours = minutes / 60;
    return hours + ":" 
          + (((minutes % 60) < 10 ) ? ("0" + (minutes % 60)) : (minutes % 60)) + ":" 
          + (((seconds % 60) < 10 ) ? ("0" + (seconds % 60)) : (seconds % 60)) + ","
          + (miliseconds % 1000);
 
  }
  
  public static String formatHundredthsToHhMmSs(Long hundredths) {
    long seconds = hundredths / 100;
    long minutes = seconds / 60;
    long hours = minutes / 60;
    return hours + ":" 
          + (((minutes % 60) < 10 ) ? ("0" + (minutes % 60)) : (minutes % 60)) + ":" 
          + (((seconds % 60) < 10 ) ? ("0" + (seconds % 60)) : (seconds % 60)) + ","
          + (((hundredths % 100) < 10 ) ? ("0" + (hundredths % 100)) : (hundredths % 100));
 
  }
  
  public static Date secondsToDate(Long seconds) {    
    return new Date(seconds * 1000); 
  }
}
