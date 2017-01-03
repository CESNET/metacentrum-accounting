package cz.cesnet.meta.stripes;

import cz.cesnet.meta.pbs.FairshareConfig;
import cz.cesnet.meta.pbs.PBS;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.ForwardResolution;
import cz.cesnet.meta.pbs.User;
import cz.cesnet.meta.pbs.UsersSortOrder;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: UsersActionBean.java,v 1.7 2014/03/05 17:03:53 makub Exp $
 */
@UrlBinding("/users/{trideni}/{srv}")
public class UsersActionBean extends BaseActionBean {


    //parametr
    private String trideni;
    private String srv;
    //data pro zobrazeni
    private List<User> users;

    private List<FairshareConfig> fairshares;

    @DefaultHandler
    public Resolution list() {
        if(trideni==null) trideni = UsersSortOrder.name.toString();
        //get users in specified order
        users = pbsky.getSortedUsers(UsersSortOrder.valueOf(trideni));
        //collect user names

        List <PBS> pbsServers = pbsky.getListOfPBS();
        Set<String> userNames = pbsky.getUserNames();

        //prepare fairshare
        fairshares = pbsCache.getFairshareConfigs();
        for (FairshareConfig fairshare : fairshares) {
            Map<String, Integer> rankMap = pbsCache.getRankMapForFairshareIdAndExistingUsers(fairshare.getId(), userNames);
            for (User user : users) {
                user.setFairshareRank(fairshare.getId(),rankMap.get(user.getName()));
            }
        }
        if("fairshare".equals(trideni)) {
            Collections.sort(users,new Comparator<User>() {
                @Override
                public int compare(User u1, User u2) {
                    Integer u1f = u1.getFairshareRank(srv);
                    if(u1f==null) u1f=-1;
                    Integer u2f = u2.getFairshareRank(srv);
                    if(u2f==null) u2f=-1;
                    return u2f-u1f;
                }
            });
        }
        return new ForwardResolution("/users/users.jsp");
    }

    public String getTrideni() {
        return trideni;
    }

    public void setTrideni(String trideni) {
        this.trideni = trideni;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<FairshareConfig> getFairshares() {
        return fairshares;
    }

    public String getSrv() {
        return srv;
    }

    public void setSrv(String srv) {
        this.srv = srv;
    }
}
