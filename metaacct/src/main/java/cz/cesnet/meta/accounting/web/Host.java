package cz.cesnet.meta.accounting.web;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.integration.spring.SpringBean;
import cz.cesnet.meta.accounting.displaytag.PaginationFilter;
import cz.cesnet.meta.accounting.server.service.HostManager;
import cz.cesnet.meta.accounting.server.util.Page;
import cz.cesnet.meta.accounting.web.filter.HostsFilter;

public class Host extends AccountingWebBase {
  @SpringBean
  HostManager hostManager;
  
  Page hosts;
  
  @SpringBean
  HostsFilter hostsFilter;
  
  
  public Resolution clear() {
    hostsFilter.clear();
    return new RedirectResolution(getClass());
  }
  
  @DefaultHandler
	public Resolution view() {
		hosts = hostManager.getHostsStats(getFilter().getSearchCriteria(), getPageNumber(), PaginationFilter.DEFAULT_PAGE_SIZE_LONG, getPageSize(), getSortColumn(), isAscending());
		return new ForwardResolution("/viewHosts.jsp");
	}	

	public Page getHosts() {
		return hosts;
	}

	public void setHosts(Page hosts) {
		this.hosts = hosts;
	}

  public HostsFilter getFilter() {
    return hostsFilter;
  }

  public void setFilter(HostsFilter filter) {
    this.hostsFilter = filter;
  }	
	
}
