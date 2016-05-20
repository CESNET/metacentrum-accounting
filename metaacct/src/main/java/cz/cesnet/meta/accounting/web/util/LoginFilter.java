package cz.cesnet.meta.accounting.web.util;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class LoginFilter implements Filter {
  FilterConfig filterConfig = null;
  
  public void destroy() {

  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
      ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest)request;    
    
    HttpSession session = httpRequest.getSession();
    String user = httpRequest.getParameter("user");
    if (user != null) {      
      session.setAttribute("LOGGED_USER", user);
    }
    chain.doFilter(request, response);
  }

  public void init(FilterConfig filterConfig) throws ServletException {
    this.filterConfig = filterConfig;
  }

}
