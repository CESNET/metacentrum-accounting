package cz.cesnet.meta.accounting.web.apps;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.integration.spring.SpringBean;
import cz.cesnet.meta.accounting.server.service.AppManager;
import cz.cesnet.meta.accounting.web.AccountingWebBase;

public class Delete extends AccountingWebBase {

  @SpringBean("appManager")
  AppManager appManager;
  
  private Long id;
  
  @DefaultHandler
  public Resolution view() {    
    appManager.deleteApp(id);
    return new RedirectResolution("/apps/View.action");
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
}
