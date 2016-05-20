package cz.cesnet.meta.accounting.web.util;

import net.sourceforge.stripes.action.ActionBeanContext;
import cz.cesnet.meta.accounting.displaytag.PaginationFilter;
import cz.cesnet.meta.accounting.web.filter.Filter;

public class AccountingActionBeanContext extends ActionBeanContext {

  public void setLoggedUser(String user) {
    getRequest().getSession().setAttribute("LOGGED_USER", user);
  }

  public String getLoggedUser() {
    return (String) getRequest().getSession().getAttribute("LOGGED_USER");
  }
  
  public void invalidateSession() {
    getRequest().getSession().invalidate();
  } 
  
  public String getSortColumn() {
	  return (String)getRequest().getAttribute(PaginationFilter.SORTING_COLUMN);
  }
  
  public Boolean isAscending() {
	  return (Boolean)getRequest().getAttribute(PaginationFilter.ASCENDING);
  }
  
  public Integer getPageNumber() {
	  return (Integer)getRequest().getAttribute(PaginationFilter.PAGE_NUMBER);
  }
  
  public Integer getPageSize() {
    return (Integer)getRequest().getAttribute(PaginationFilter.PAGE_SIZE);
  }
  
  public void setFilter(Filter filter) {
    getRequest().getSession().setAttribute("FILTER", filter);
  }
  
  public Filter getFilter() {
    return (Filter)getRequest().getSession().getAttribute("FILTER");
  }
  
}
