package cz.cesnet.meta.accounting.web;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import cz.cesnet.meta.accounting.web.util.AccountingActionBeanContext;

public class Logout implements ActionBean {
  
  
  @DefaultHandler
  public Resolution logout() {
    context.invalidateSession();
    return new ForwardResolution("/");    
  }
  
  private AccountingActionBeanContext context;
  public AccountingActionBeanContext getContext() {
    return context;
  }

  public void setContext(ActionBeanContext context) {
    this.context = (AccountingActionBeanContext)context;    
  }

}
