package cz.cesnet.meta.accounting.web;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.integration.spring.SpringBean;
import cz.cesnet.meta.accounting.server.service.UserManager;
import cz.cesnet.meta.accounting.web.filter.Filter;
import cz.cesnet.meta.accounting.web.util.AccountingActionBeanContext;


public class AccountingWebBase implements ActionBean {  
  private AccountingActionBeanContext context;
  
  @SpringBean("userManager")
  protected UserManager userManager;
  
  public AccountingActionBeanContext getContext() {
    return context;
  }

  public void setContext(ActionBeanContext context) {
    this.context = (AccountingActionBeanContext)context;
  }
  
  public String getTitle() {
    return "MetaCentrum VO accounting";
  }
  
  protected Long getLoggedUserId() {
    String username = context.getLoggedUser();
    Long userId = userManager.getUserId(username);
    if (userId == null && username != null) {
      return -1L;
    }
    
    return userId;
  }

  public void setUserManager(UserManager userManager) {
    this.userManager = userManager;
  }
  
  public String getSortColumn() {
	  return context.getSortColumn();
  }
  
  public Boolean isAscending() {
	  return context.isAscending();
  }
  
  public Integer getPageNumber() {
	  return context.getPageNumber();
  }
  
  public Integer getPageSize() {
    return context.getPageSize();
  }
  
  public void setFilter(Filter filter) {
    context.setFilter(filter);
  }
  
  public Filter getFilter() {
    return context.getFilter();
  }

}
