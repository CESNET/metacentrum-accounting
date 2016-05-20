package cz.cesnet.meta.accounting.web;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

public class Statistics extends AccountingWebBase {
  
  @DefaultHandler
  public Resolution view() {
    return new ForwardResolution("/stats.jsp");
  }
}
