package cz.cesnet.meta.accounting.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;

public class Index extends AccountingWebBase {
    private String username;
    
    @DefaultHandler
    public Resolution index() throws UnsupportedEncodingException {
        Long userId = getLoggedUserId();

        if (userId == null) {          
            //nic nevime, poslat na Osobni, at nam povi, kdo to je
            HttpServletRequest request = getContext().getRequest();
            String backurl = request.getScheme() + "://" + request.getServerName()
                    + ":" + request.getServerPort() + request.getContextPath() + "/Index.action";
            String url = "https://metavo.metacentrum.cz/osobniv3/personal/personalize?backurl="
                    + URLEncoder.encode(backurl, "utf-8");
            return new RedirectResolution(url,false);
        } else if (userId == -1L) {
            username = getContext().getLoggedUser();
            return new ForwardResolution("/viewEmptyUser.jsp");
        } else {
            return new RedirectResolution("/PbsRecords.action?viewUser&userId=" + userId + "&number=10");
        }
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }
}
