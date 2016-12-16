package cz.cesnet.meta.pbs;

import cz.cesnet.meta.RefreshLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Implements UserAccess. Reads /etc/group for each PBS server to know in which groups a user is.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class UserAccessImpl extends RefreshLoader implements UserAccess {

    final static Logger log = LoggerFactory.getLogger(UserAccessImpl.class);

    private Pbsky pbsky;

    public void setPbsky(Pbsky pbsky) {
        this.pbsky = pbsky;
    }

    @Override
    public List<Queue> getUserQueues(String userName) {
        checkLoad();
        List<Queue> allowedQueues = new ArrayList<>();
        for (PBS pbs : pbsky.getListOfPBS()) {
            for(Queue q : pbs.getQueuesByPriority()) {
                if(!q.isLocked()) {
                    allowedQueues.add(q);
                } else if(q.isAclUsersEnabled()&& Arrays.asList(q.getAclUsersArray()).contains(userName)) {
                    allowedQueues.add(q);
                } else if(q.isAclGroupsEnabled()) {
                    boolean found = false;
                    Map<String, Group> groupMap = pbs2groupName2GroupMap.get(pbs.getHost());
                    for (String aclGroup : q.getAclGroupsArray()) {
                        Group group = groupMap.get(aclGroup);
                        if(group.getUsers().contains(userName)) {
                            found = true;
                            break;
                        }
                    }
                    if(found) {
                        allowedQueues.add(q);
                    }
                }
            }
        }
        return allowedQueues;
    }

    @Override
    public Group getGroup(String pbsServerName, String groupName) {
        checkLoad();
        log.debug("getGroup({},{})",pbsServerName,groupName);
        return pbs2groupName2GroupMap.get(pbsServerName).get(groupName);
    }

    @Override
    protected void load() {
        log.debug("load() started");
        LinkedHashMap<String, Map<String,Group>> map = new LinkedHashMap<>();
        for (PbsServerConfig pbsServerConfig : pbsky.getPbsServerConfigs()) {
            List<Group> groups = loadGroups(pbsServerConfig);
            Map<String,Group> groupMap = new HashMap<>();
            String host = pbsServerConfig.getHost();
            map.put(host,groupMap);
            if(groups!=null) {
                for(Group group : groups) {
                    log.debug("adding group {} for server {}",group.getName(),host);
                    groupMap.put(group.getName(),group);
                }
            }
        }
        pbs2groupName2GroupMap = map;
        log.debug("load() done");
    }

    LinkedHashMap<String, Map<String,Group>> pbs2groupName2GroupMap;

    private List<Group> loadGroups(PbsServerConfig serverConfig) {
        log.debug("loadGroups({})",serverConfig.getHost());
        try {
            List<Group> groups = new ArrayList<>();
            for(String line : Files.readAllLines(Paths.get(serverConfig.getGroupFile()))) {
                String[] split = line.split(":");
                String groupName = split[0];
                if(split.length>=4) {
                    List<String> userList = Arrays.asList(split[3].split(","));
                    Collections.sort(userList);
                    LinkedHashSet<String> userSet = new LinkedHashSet<>(userList);
                    groups.add(new Group(groupName, userSet));
                } else {
                    groups.add(new Group(groupName, new LinkedHashSet<>()));
                }
            }
            return groups;
        } catch (Exception e) {
            log.error("Cannot read file "+serverConfig.getGroupFile(),e);
        }
        return null;
    }



    static public class Group {
        private String name;
        private LinkedHashSet<String> users;

        public Group(String name, LinkedHashSet<String> users) {
            this.name = name;
            this.users = users;
        }

        public String getName() {
            return name;
        }

        public LinkedHashSet<String> getUsers() {
            return users;
        }
    }
}
