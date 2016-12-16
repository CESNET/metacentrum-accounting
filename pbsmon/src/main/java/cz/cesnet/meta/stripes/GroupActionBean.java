package cz.cesnet.meta.stripes;

import cz.cesnet.meta.pbs.*;
import cz.cesnet.meta.perun.api.Perun;
import cz.cesnet.meta.perun.api.PerunUser;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Displayes users in a group at a given PBS server.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@UrlBinding("/group/{pbsServerName}/{groupName}")
public class GroupActionBean extends BaseActionBean {

    final static Logger log = LoggerFactory.getLogger(GroupActionBean.class);

    @SpringBean("userAccess")
    UserAccess userAccess;

    @SpringBean("perun")
    protected Perun perun;

    private String pbsServerName;
    private String groupName;
    private List<PerunUser> users;

    public void setPbsServerName(String pbsServerName) {
        this.pbsServerName = pbsServerName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @DefaultHandler
    public Resolution show() {
        PBS pbsServer = null;
        for(PBS pbs : pbsky.getListOfPBS()) {
            if(pbs.getHost().equals(pbsServerName)) {
                pbsServer = pbs;
            }
        }
        if(pbsServer==null) {
            log.warn("pbsServer {} not found",pbsServerName);
            return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND, "PBS server name is needed in the URL.");
        }
        UserAccessImpl.Group group = userAccess.getGroup(pbsServerName, groupName);
        if(group==null) {
            log.warn("group {} not found",groupName);
            return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND, "Group name is needed in the URL.");
        }
        HashSet<String> aclGroupsNames = new HashSet<>();
        for(Queue q : pbsServer.getQueuesByPriority()) {
            if(q.isAclGroupsEnabled()) {
                aclGroupsNames.addAll(Arrays.asList(q.getAclGroupsArray()));
            }
        }
        if(!aclGroupsNames.contains(groupName)) {
            log.warn("group {} is not acl group on server {}",groupName,pbsServerName);
            return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND, "Group is not ACL group.");
        }

        users =  new ArrayList<>();
        for(String userName : group.getUsers()) {
            users.add(perun.getUserByName(userName));
        }
        return new ForwardResolution("/users/group.jsp");
    }

    public List<PerunUser> getUsers() {
        return users;
    }

    public String getPbsServerName() {
        return pbsServerName;
    }

    public String getGroupName() {
        return groupName;
    }
}
