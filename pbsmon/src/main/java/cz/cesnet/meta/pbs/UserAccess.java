package cz.cesnet.meta.pbs;

import java.util.List;

/**
 * Reads info froum group file.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public interface UserAccess {

    List<Queue> getUserQueues(String userName);


    UserAccessImpl.Group getGroup(String pbsServerName, String groupName);
}
