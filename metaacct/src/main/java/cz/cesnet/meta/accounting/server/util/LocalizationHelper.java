package cz.cesnet.meta.accounting.server.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import cz.cesnet.meta.accounting.web.util.AcctLocalePicker;

public class LocalizationHelper {
  private static Logger logger = Logger.getLogger(LocalizationHelper.class);
  
  String basename;
  
  public String getMessage(String key, Object ... args) {
    ResourceBundle bundle = null;    
    Locale locale = AcctLocalePicker.locale.get();
    bundle = ResourceBundle.getBundle(basename, locale);
    
    String message = bundle.getString(key);
    
    if (args.length > 0) {
      MessageFormat messageFormat = createMessageFormat(message, locale);
      return messageFormat.format(args);
    }
    
    return message;
  }
  
  protected MessageFormat createMessageFormat(String msg, Locale locale) {
    if (logger.isDebugEnabled()) {
      logger.debug("Creating MessageFormat for pattern [" + msg + "] and locale '" + locale + "'");
    }
    MessageFormat messageFormat = new MessageFormat("");
    messageFormat.setLocale(locale);
    if (msg != null) {
      messageFormat.applyPattern(msg);
    }
    return messageFormat;
  }

  public String getBasename() {
    return basename;
  }

  public void setBasename(String basename) {
    this.basename = basename;
  }

  
}
