package cz.cesnet.meta.accounting.displaytag.decorator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.jsp.PageContext;

import org.displaytag.decorator.DisplaytagColumnDecorator;
import org.displaytag.exception.DecoratorException;
import org.displaytag.properties.MediaTypeEnum;

/**
 * decorator only for date values
 * @author xsedlac4
 *
 */
public class LongDateWrapper implements DisplaytagColumnDecorator {

  private static DateFormat df = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss"); 
  
  @Override
  public Object decorate(Object columnValue, PageContext pageContext, MediaTypeEnum media) throws DecoratorException {
    if (columnValue == null) {
      return null;
    }
    if (columnValue instanceof Date) {
      return df.format(columnValue);      
    } else {
      return columnValue;
    }    
  }
}
