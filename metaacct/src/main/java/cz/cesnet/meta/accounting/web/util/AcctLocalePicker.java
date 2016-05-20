package cz.cesnet.meta.accounting.web.util;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.sourceforge.stripes.localization.DefaultLocalePicker;
import net.sourceforge.stripes.localization.LocalePicker;

import org.apache.log4j.Logger;

public class AcctLocalePicker extends DefaultLocalePicker implements LocalePicker {
  static Logger log = Logger.getLogger(AcctLocalePicker.class);

  public static final String SESSION_KEY = "AcctLocalePicker.locale";

  public static ThreadLocal<Locale> locale = new ThreadLocal<Locale>();
  
  /**
   * Pokusi se najit lokale v session, jinak deleguje na DefaultLocalePicker.
   *
   * @param request http request
   * @return selected locale
   */
  @Override
  public Locale pickLocale(HttpServletRequest request) {
      HttpSession session = request.getSession(false);
      if (session != null) {
          Locale loc = (Locale) session.getAttribute(SESSION_KEY);
          if (loc != null) {
              if (this.locales.contains(loc)) {
                  if (log.isDebugEnabled()) log.debug("pickLocale(): " + loc);
                  locale.set(loc);
                  return loc;
              }
          }
      }
      log.debug("super.pickLocale()");
      Locale l = super.pickLocale(request);
      locale.set(l);
      return l;
  }

  public static void setLocale(Locale loc, HttpServletRequest request) {
      if (log.isDebugEnabled()) log.debug("setLocale(" + loc + ")");
      HttpSession session = request.getSession();
      session.setAttribute(SESSION_KEY, loc);
  }
}
