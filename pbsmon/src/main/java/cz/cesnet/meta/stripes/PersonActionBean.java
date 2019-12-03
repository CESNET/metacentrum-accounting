package cz.cesnet.meta.stripes;

import cz.cesnet.meta.cloud.CloudVM;
import cz.cesnet.meta.pbs.Queue;
import cz.cesnet.meta.pbs.User;
import cz.cesnet.meta.pbs.UserAccess;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Akce pro personalizaci pohledu na PBS.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: PersonActionBean.java,v 1.17 2014/10/17 12:33:03 makub Exp $
 */
@UrlBinding("/person")
public class PersonActionBean extends BaseActionBean {

    final static Logger log = LoggerFactory.getLogger(PersonActionBean.class);

    public static final String PERSONALIZE_URL = "https://metavo.metacentrum.cz/osobniv3/personal/personalize?backurl=";

    @SpringBean("userAccess")
    UserAccess userAccess;


    static final String PERSON = "person";

    private String user;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }


    User jobUser;
    List<Queue> queues;
    List<CloudVM> userVMs;

    @DefaultHandler
    public Resolution show() throws UnsupportedEncodingException {
        log.debug("show()");
        HttpServletRequest request = ctx.getRequest();
        HttpSession session = request.getSession(true);

        if (user != null) {
            //prichazime z Osobniho, nastavit
            session.setAttribute(PERSON, user);
        } else {
            user = (String) session.getAttribute(PERSON);
            if (user == null) {
                //nic nevime, poslat na Osobni, at nam povi, kdo to je
                String backurl = request.getScheme() + "://" + request.getServerName()
                        + ":" + request.getServerPort() + request.getContextPath() + "/person";
                return new RedirectResolution(PERSONALIZE_URL + URLEncoder.encode(backurl, "utf-8"), false);
            }
        }
        //vsechny pristupne fronty
        queues = userAccess.getUserQueues(user);
        //user
        if (jobUser == null) {
            jobUser = pbsky.getUserByName(user);
            if (jobUser == null) jobUser = new User(user);
        }

        //VM z cloudu
        userVMs = cloud.getVirtualHosts().stream()
                .filter(vm -> user.equals(vm.getOwner()))
                .collect(Collectors.toList());

        return new ForwardResolution("/nodes/personal.jsp");
    }


    public User getJobUser() {

        return jobUser;
    }

    public List<Queue> getQueues() {
        return queues;
    }

    public List<CloudVM> getUserVMs() {
        return userVMs;
    }
}
