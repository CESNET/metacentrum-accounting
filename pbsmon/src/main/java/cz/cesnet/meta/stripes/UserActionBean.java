package cz.cesnet.meta.stripes;

import cz.cesnet.meta.acct.Accounting;
import cz.cesnet.meta.acct.UserInfo;
import cz.cesnet.meta.cloud.CloudVM;
import cz.cesnet.meta.pbs.*;
import cz.cesnet.meta.perun.api.Perun;
import cz.cesnet.meta.perun.api.PerunUser;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: UserActionBean.java,v 1.9 2016/03/31 11:33:59 makub Exp $
 */
@UrlBinding("/user/{userName}/{sort}")
public class UserActionBean extends BaseActionBean {


    public static final String METACENTRUM = "MetaCentrum";
    public static final String CERIT_SC = "CERIT-SC";
    final static Logger log = LoggerFactory.getLogger(UserActionBean.class);

    @SpringBean("perun")
    protected Perun perun;

    @SpringBean("accounting")
    protected Accounting accounting;

    @SpringBean("userAccess")
    UserAccess userAccess;

    //parametr
    private String userName;
    //data
    private User user;
    private List<Job> jobs;
    private PerunUser perunUser;
    private UserInfo userInfo;
    private Map<String, String> nodesShortNamesMap;
    private JobsSortOrder sort = JobsSortOrder.Id;
    private List<String> usedQueueNames;
    private Map<String,JobsInfo> jobInfosByQueue;
    private List<CloudVM> userVMs;
    private List<UserAccessImpl.Group> groups;

    @DefaultHandler
    public Resolution show() {
        if (userName == null || userName.length() == 0) {
            return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND, "User name is needed in the URL.");
        }
        try {
            perunUser = perun.getUserByName(userName);
        } catch (Exception ex) {
            log.warn("Nemohu nacist PerunUser {} kvuli {} ", userName, ex.getMessage());
            return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND, "User not found.");
        }
        try {
            userInfo = accounting.getUserInfoByName(userName);
        } catch (Exception ex) {
            log.error("Nemohu nacist UserInfo " + userName, ex);
        }
        user = pbsky.getUserByName(userName);
        jobs = pbsky.getUserJobs(userName,sort);
        //node names
        nodesShortNamesMap = new HashMap<>(jobs.size());
        for (Job job : jobs) {
            if ("R".equals(job.getState())) {
                String execHostFirstName = job.getExecHostFirstName();
                Node node = job.getPbs().getNodes().get(execHostFirstName);
                if(node!=null) {
                    nodesShortNamesMap.put(execHostFirstName, node.getShortName());
                } else {
                    log.error("no node found for execHostFirstName={} for job {}",execHostFirstName,job.getId());
                }
            }
        }
        //find user jobs per queue
        Map<String, List<Job>> jobsByQueue = new HashMap<>();
        for(Job job : jobs) {
            jobsByQueue.computeIfAbsent(job.getQueueName(), k -> new ArrayList<>()).add(job);
        }
        //get sorted list of names of queues with user's jobs
        usedQueueNames = new ArrayList<>(jobsByQueue.keySet());
        Collections.sort(usedQueueNames);
        //compute numbers of jobs is some states
        jobInfosByQueue = new HashMap<>();
        for(String queueName : usedQueueNames) {
            jobInfosByQueue.put(queueName,new JobsInfo(jobsByQueue.get(queueName)));
        }

        //VM z cloudu
        userVMs = cloud.getVirtualHosts().stream()
                .filter(vm -> userName.equals(vm.getOwner()))
                .collect(Collectors.toList());

        //groups
        groups = userAccess.getUserGroups(userName);

        return new ForwardResolution("/users/user.jsp");
    }

    //-------

    public Map<String, String> getNodesShortNamesMap() {
        return nodesShortNamesMap;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public User getUser() {
        return user;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public PerunUser getPerunUser() {
        return perunUser;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public JobsSortOrder getSort() {
        return sort;
    }

    public void setSort(JobsSortOrder sort) {
        this.sort = sort;
    }

    public List<String> getUsedQueueNames() {
        return usedQueueNames;
    }

    public Map<String, JobsInfo> getJobInfosByQueue() {
        return jobInfosByQueue;
    }

    public List<CloudVM> getUserVMs() {
        return userVMs;
    }

    public List<UserAccessImpl.Group> getGroups() {
        return groups;
    }
}
