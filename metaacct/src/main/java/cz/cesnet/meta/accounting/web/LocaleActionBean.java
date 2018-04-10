package cz.cesnet.meta.accounting.web;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;


import cz.cesnet.meta.accounting.web.util.AcctLocalePicker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocaleActionBean extends AccountingWebBase {
  static Logger log = LoggerFactory.getLogger(LocaleActionBean.class);

  private String kam;
  private String jazyk;


  public String getKam() {
      return kam;
  }

  public void setKam(String kam) {
      this.kam = kam;
  }

  public String getJazyk() {
      return jazyk;
  }

  public void setJazyk(String jazyk) {
      this.jazyk = jazyk;
  }

  @DefaultHandler
  @SuppressWarnings("unchecked")
  public Resolution nastav() {
      if(log.isDebugEnabled()) log.debug("nastav("+jazyk+")");
      AcctLocalePicker.setLocale(new Locale(jazyk),getContext().getRequest());
      Map<String, Object> params = new HashMap<String, Object>();
      
      Map parameterMap = getContext().getRequest().getParameterMap();
      for (Object o : parameterMap.keySet()){
        String s  = (String) o;
        if (!s.equals("kam") && !s.equals("jazyk")) {
          params.put(s, parameterMap.get(o));
        }
      }
      return new RedirectResolution(kam!=null?kam:"/").addParameters(params);
  }
}
