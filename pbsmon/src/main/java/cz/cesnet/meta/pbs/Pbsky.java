package cz.cesnet.meta.pbs;

import cz.cesnet.meta.TimeStamped;

import java.util.List;
import java.util.Date;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: Pbsky.java,v 1.10 2014/03/05 14:50:15 makub Exp $
 */
public interface Pbsky extends TimeStamped {

    List<PBS> getPbsky();

    Queue getQueueByName(String queueName);

    Job getJobByName(String jobName);

    List<Job> getSortedJobs(JobsSortOrder jobsSortOrder);

    List<Node> getAllNodes();

    List<User> getSortedUsers(UsersSortOrder usersSortOrder);

    List<TextWithCount> getReasonsForJobsQueued();

    User getUserByName(String userName);

    List<Job> getUserJobs(String userName, JobsSortOrder sort);

    Node getNodeByName(String nodeName);

    int getJobsQueuedCount();

    JobsInfo getJobsInfo();

    Set<String> getUserNames();

}
