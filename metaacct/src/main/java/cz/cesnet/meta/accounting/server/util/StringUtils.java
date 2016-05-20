package cz.cesnet.meta.accounting.server.util;

import java.util.Iterator;
import java.util.List;

import cz.cesnet.meta.accounting.server.data.PBSHost;

public class StringUtils {
  
  /**
   * prevede objekty PBSHost na string - posloupnost host id oddelene carkami.
   * @param execHosts
   * @return
   */
  public static String hostIdsToString(List<PBSHost> execHosts) {
    StringBuilder builder = new StringBuilder();
    Iterator<PBSHost> iter = execHosts.iterator();
    
    while (iter.hasNext()) {
      builder.append(iter.next().getId());
      if (iter.hasNext()) {
        builder.append(", ");
      }
    }
    return builder.toString();
  }
  
  public static Integer parsePbsLogTimeFormat(String time) {    
    Integer timeInSeconds = null;
    try {
      timeInSeconds = Integer.parseInt(time);
    } catch (NumberFormatException e) {
      //neni v sekundach
    }
    if (timeInSeconds == null) {
      String[] timeParts = time.split(":");
      String seconds = null;
      String minutes = null;
      String hours = null;
      int lastIndex = timeParts.length - 1;              
      if (lastIndex >= 0){
        seconds = timeParts[lastIndex];
      }
      if (lastIndex-1 >= 0) {
        minutes = timeParts[lastIndex - 1];
      }
      if (lastIndex-2 >= 0) {
        hours = timeParts[lastIndex - 2];
      }
      timeInSeconds = Integer.parseInt(hours)*3600 + Integer.parseInt(minutes)*60 + new Double(seconds).intValue(); 
    }
    
    return timeInSeconds;
  }
  
  /**
   * returns size in kilobytes
   * @param size
   * @return
   */
  public static Long parsePbsLogSizeFormat(String size) {
      Long sizeInBytes = null;
      if (size.endsWith("tb")) {
          sizeInBytes = Long.parseLong(size.substring(0, size.length() - 2)) * 1024l * 1024l * 1024l * 1024l;
      } else if (size.endsWith("gb")) {
          sizeInBytes = Long.parseLong(size.substring(0, size.length() - 2)) * 1024l * 1024l * 1024l;
      } else if (size.endsWith("mb")) {
          sizeInBytes = Long.parseLong(size.substring(0, size.length() - 2)) * 1024l * 1024l;
      } else if (size.endsWith("kb")) {
          sizeInBytes = Long.parseLong(size.substring(0, size.length() - 2)) * 1024l;
      } else if (size.endsWith("b")) {
          sizeInBytes = Long.parseLong(size.substring(0, size.length() - 1));
      } else if (size.endsWith("tw")) {
          sizeInBytes = Long.parseLong(size.substring(0, size.length() - 2)) * 8l * 1024l * 1024l * 1024l * 1024l;
      } else if (size.endsWith("gw")) {
          sizeInBytes = Long.parseLong(size.substring(0, size.length() - 2)) * 8l * 1024l * 1024l * 1024l;
      } else if (size.endsWith("mw")) {
          sizeInBytes = Long.parseLong(size.substring(0, size.length() - 2)) * 8l * 1024l * 1024l;
      } else if (size.endsWith("kw")) {
          sizeInBytes = Long.parseLong(size.substring(0, size.length() - 2)) * 8l * 1024l;
      } else if (size.endsWith("w")) {
          sizeInBytes = Long.parseLong(size.substring(0, size.length() - 1)) * 8l;
      } else {
          throw new NumberFormatException("unknown unit in number "+size);
      }
      return sizeInBytes;
  }

}
