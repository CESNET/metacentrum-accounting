package cz.cesnet.meta.pbs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: Queue.java,v 1.13 2014/03/05 14:50:15 makub Exp $
 */
public class Queue extends PbsInfoObject {

    final static Logger log = LoggerFactory.getLogger(Queue.class);

    public Queue() {
        super();
    }

    public Queue(String name) {
        super(name);
    }

    //primary data are inherited from PBSInfoObject

    private int running = 0;
    private int completed = 0;
    private int queued = 0;
    private int total = 0;
    private int priority = -1;
    private String[] aclUsers = null;
    private String[] aclGroups = null;
    private String[] aclHosts = null;

    @Override
    public void clear() {
        super.clear();
        aclUsers = null;
        aclGroups = null;
        aclHosts = null;
    }

    @Override
    public String getName() {
        return pbs == null ? name : name + pbs.getSuffix();
    }

    public String getShortName() {
        return name;
    }

    public boolean isMaintenance() {
        return name.equals(PbsUtils.MAINTENANCE);
    }

    public boolean isReserved() {
        return name.equals(PbsUtils.RESERVED);
    }

    public boolean isRouting() {
        return "Route".equals(attrs.get("queue_type"));
    }

    public List<String> getDestQueueNames() {
        String route_destinations = attrs.get("route_destinations");
        if (route_destinations == null) return Collections.emptyList();
        String[] shortNames = route_destinations.split(",");
        List<String> dsts = new ArrayList<String>(shortNames.length);
        for (String shortName : shortNames) {
            dsts.add(pbs == null ? shortName : shortName + pbs.getSuffix());
        }
        return dsts;
    }

    public String getWalltimeMin() {
        return attrs.get("resources_min.walltime");
    }

    public String getWalltimeMax() {
        return attrs.get("resources_max.walltime");
    }

    public long getWalltimeMinSeconds() {
        String t = getWalltimeMin();
        if (t == null) return 0;
        String[] sa = t.split(":");
        if (sa.length != 3) {
            log.error("cannot parsec queue {} mintime {}", getName(), t);
            return 0;
        }
        return Long.parseLong(sa[0]) * 3600l + Long.parseLong(sa[1]) * 60l + Long.parseLong(sa[2]);
    }

    public long getWalltimeMaxSeconds() {
        String t = getWalltimeMax();
        if (t == null) return Long.MAX_VALUE;
        String[] sa = t.split(":");
        if (sa.length != 3) {
            log.error("cannot parsec queue {} maxtime {}", getName(), t);
            return Long.MAX_VALUE;
        }
        return Long.parseLong(sa[0]) * 3600l + Long.parseLong(sa[1]) * 60l + Long.parseLong(sa[2]);
    }


    public int getPriority() {
        if (priority == -1) {
            String p = attrs.get("Priority");
            if (p == null) {
                this.priority = 0;
            } else {
                this.priority = Integer.parseInt(p);
            }
        }
        return priority;
    }

    /**
     * Returns a property that nodes must have to be used by this queue.
     *
     * @return value of required_property attribute
     */
    public String getRequiredProperty() {
        return attrs.get("required_property");
    }

    /**
     * Array of user names in ACL list.
     *
     * @return value of acl_users attribute split at commas
     */
    public String[] getAclUsersArray() {
        if (this.aclUsers == null) {
            String propatr = getAclUsers();
            if (propatr != null) {
                this.aclUsers = propatr.split(",");
            } else {
                this.aclUsers = new String[0];
            }
        }
        return this.aclUsers;
    }

    public String[] getAclGroupsArray() {
        if (this.aclGroups == null) {
            String propatr = getAclGroups();
            if (propatr != null) {
                this.aclGroups = propatr.split(",");
            } else {
                this.aclGroups = new String[0];
            }
        }
        return this.aclGroups;
    }

    public String[] getAclHostsArray() {
        if (this.aclHosts == null) {
            String propatr = getAclHosts();
            if (propatr != null) {
                this.aclHosts = propatr.split(",");
            } else {
                this.aclHosts = new String[0];
            }
        }
        return this.aclHosts;
    }

    public String getAclUsers() {
        return attrs.get("acl_users");
    }

    public String getAclGroups() {
        return attrs.get("acl_groups");
    }

    public String getAclHosts() {
        return attrs.get("acl_hosts");
    }

    public boolean isAclUsersEnabled() {
        return "True".equals(attrs.get("acl_user_enable"));
    }

    public boolean isAclGroupsEnabled() {
        return "True".equals(attrs.get("acl_group_enable"));
    }

    public boolean isAclHostsEnabled() {
        return "True".equals(attrs.get("acl_host_enable"));
    }

    public String getLockedForKey() {
        if (isAclUsersEnabled()) return "users";
        if (isAclGroupsEnabled()) return "groups";
        if (isAclHostsEnabled()) return "hosts";
        return null;
    }

    public String getLockedFor() {
        if (isAclUsersEnabled()) return getAclUsers().replace(',', ' ');
        if (isAclGroupsEnabled()) return getAclGroups().replace(',', ' ');
        if (isAclHostsEnabled()) return getAclHosts().replace(',', ' ');
        return "";
    }


    public boolean isLocked() {
        return this.isAclUsersEnabled() || this.isAclGroupsEnabled() || this.isAclHostsEnabled();
    }

    public String getMaxRunningJobs() {
        return attrs.get("max_running");
    }

    public String getMaxUserRun() {
        return attrs.get("max_user_run");
    }

    public String getMaxUserCPU() {
        return attrs.get("max_user_proc");
    }

    public String getFairshareTree() {
        String ft = attrs.get("fairshare_tree");
        return ft != null ? ft : "default";
    }

    public int getJobsRunning() {
        return running;
    }

    public int getJobsCompleted() {
        return completed;
    }

    public int getJobsQueued() {
        return queued;
    }

    public int getJobsTotal() {
        return total;
    }

    public void setJobNums(int running, int completed, int queued, int total) {
        this.running = running;
        this.completed = completed;
        this.queued = queued;
        this.total = total;
    }

    public String toString() {
        return this.getClass().getSimpleName() + "[" + name + ",R=" + running + ",C=" + completed + ",Q=" + queued + ",T=" + total + "]";
    }

    public String getOrigToString() {
        return super.toString();
    }

    public List<Node> getNodes() {
        return getPbs().getQueueToNodesMap().get(this.getName());
    }

    public List<Job> getJobs() {
        return this.getPbs().getQueueToJobsMap().get(this.getName());
    }

    public boolean isExecutionQueue() {
        return "Execution".equals(attrs.get("queue_type"));
    }


    private synchronized void prepareDescriptions() {
        descriptionsMap = new HashMap<Locale, String>();
        String description_cs = attrs.get("description_cs");
        if (description_cs != null) {
            descriptionsMap.put(new Locale("cs"), description_cs);
        }
        String description_en = attrs.get("description_en");
        if (description_en != null) {
            descriptionsMap.put(new Locale("en"), description_en);
        }
        descriptionAvailable = (description_cs != null && description_en != null);
        descriptionsPrepared = true;
    }

    private Map<Locale, String> descriptionsMap;
    private boolean descriptionAvailable;
    private boolean descriptionsPrepared = false;

    public Map<Locale, String> getDescriptionMap() {
        if (!descriptionsPrepared) prepareDescriptions();
        return descriptionsMap;
    }

    public boolean isDescriptionAvailable() {
        if (!descriptionsPrepared) prepareDescriptions();
        return descriptionAvailable;
    }
}
