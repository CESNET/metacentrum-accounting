package cz.cesnet.meta.accounting.web;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.integration.spring.SpringBean;
import cz.cesnet.meta.accounting.displaytag.PaginationFilter;
import cz.cesnet.meta.accounting.server.service.UserManager;
import cz.cesnet.meta.accounting.server.util.Page;

public class Users extends AccountingWebBase {

  @SpringBean
  UserManager userManager;
  
  private Page users;  

  @DefaultHandler
  public Resolution view() {
    users = userManager.getAllUsersWithPbsRecordNumber(getPageNumber(), 1000, getPageSize(), getSortColumn(), isAscending());
    return new ForwardResolution("/viewUsers.jsp");
  }
  
  @Override
  public String getTitle() {    
    return super.getTitle() + " - users";
  }

  public Page getUsers() {
    return users;
  }

  public void setUsers(Page users) {
    this.users = users;
  }  
}
