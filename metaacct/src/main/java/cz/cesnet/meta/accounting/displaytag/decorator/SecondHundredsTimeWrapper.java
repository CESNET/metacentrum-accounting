package cz.cesnet.meta.accounting.displaytag.decorator;

import javax.servlet.jsp.PageContext;

import org.displaytag.decorator.DisplaytagColumnDecorator;
import org.displaytag.exception.DecoratorException;
import org.displaytag.properties.MediaTypeEnum;

/**
 * decorator only for long values representing time in hundreds of seconds
 * @author xsedlac4
 *
 */
public class SecondHundredsTimeWrapper implements DisplaytagColumnDecorator {
  
  @Override
  public Object decorate(Object columnValue, PageContext pageContext, MediaTypeEnum media) throws DecoratorException {
    if (columnValue == null) {
      return null;
    }
    if (columnValue instanceof Number) {
      long hundredths = ((Number) columnValue).longValue();
      long seconds = hundredths / 100;
      long minutes = seconds / 60;
      long hours = minutes / 60;
      return hours + ":" 
            + (((minutes % 60) < 10 ) ? ("0" + (minutes % 60)) : (minutes % 60)) + ":" 
            + (((seconds % 60) < 10 ) ? ("0" + (seconds % 60)) : (seconds % 60));
            //+ "," + (((hundredths % 100) < 10 ) ? ("0" + (hundredths % 100)) : (hundredths % 100));
    } else {
      return columnValue;
    }    
  }
}
