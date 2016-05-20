package cz.cesnet.meta.accounting.web.apps;

import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.integration.spring.SpringBean;
import cz.cesnet.meta.accounting.server.data.Application;
import cz.cesnet.meta.accounting.server.service.AppManager;
import cz.cesnet.meta.accounting.web.AccountingWebBase;

public class View extends AccountingWebBase {

  @SpringBean
  AppManager appManager;
  
  List<Application> apps;
  
  @DefaultHandler
  public Resolution view() {    
    apps = appManager.getAllApps();
    return new ForwardResolution("/apps/list.jsp");
  }

  public List<Application> getApps() {
    return apps;
  }

  public void setApps(List<Application> apps) {
    this.apps = apps;
  }
}
