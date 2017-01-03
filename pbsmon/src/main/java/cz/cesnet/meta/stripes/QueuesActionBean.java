package cz.cesnet.meta.stripes;

import cz.cesnet.meta.pbs.*;
import cz.cesnet.meta.pbs.Queue;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: QueuesActionBean.java,v 1.12 2014/10/01 06:16:08 makub Exp $
 */
@SuppressWarnings("UnusedDeclaration")
@UrlBinding("/queues/{$event}")
public class QueuesActionBean extends BaseActionBean {

    final static Logger log = LoggerFactory.getLogger(QueuesActionBean.class);

    private Map<String,List<TextWithCount>> duvody;
    private Map<String, List<Job>> queueNames2QueuedJobs;
    private Map<String,List<Job>> plannedJobs;

    @DefaultHandler
    public Resolution list() {
        return new ForwardResolution("/queues/list.jsp");
    }

    public Resolution servers() {
        return new ForwardResolution("/queues/servers.jsp");
    }

    /**
     * Displays jobs waiting in queues.
     * @return JSP page to display
     */
    public Resolution jobsQueued() {
        log.debug("jobsQueued() started");
        duvody = new HashMap<>();
        for (PBS pbs : pbsky.getListOfPBS()) {
            duvody.put(pbs.getServer().getHost(),pbsky.getReasonsForJobsQueued(pbs));
        }

        plannedJobs = new HashMap<>();
        queueNames2QueuedJobs = new HashMap<>();
        Set<String> userNames = pbsky.getUserNames();
        for (PBS pbs : pbsky.getListOfPBS()) {
            boolean planbased = pbs.getServerConfig().isPlanbased();
            if(planbased) {
                //u rozvrhového plánovače řadit podle planned_start
                List<Job> queuedJobs = new ArrayList<>(pbs.getJobsQueuedCount());
                for(Job job : pbs.getJobsById()) {
                    if("Q".equals(job.getState())) {
                        queuedJobs.add(job);
                    }
                }
                queuedJobs.sort(Job.PLANNED_START_JOB_COMPARATOR);
                plannedJobs.put(pbs.getServer().getHost(),queuedJobs);
            } else {
                //dostat pro kazdou frontu seznam cekajicich uloh serazenych podle fairshare uzivatelu
                for (FairshareConfig fairshare : pbs.getServerConfig().getFairshares()) {
                    Map<String, Integer> rankMap = pbsCache.getRankMapForFairshareIdAndExistingUsers(fairshare.getId(), userNames);
                    if (pbs.getServerConfig().isBy_queue()) {
                        //fronty oddelene, v ramci front podle fairshare
                        for (Queue q : pbs.getQueuesByPriority()) {
                            if (q.getJobsQueued() > 0) {
                                String queueName = q.getName();
                                List<Job> queuedJobs = new ArrayList<>(q.getJobsQueued());
                                for (Job job : q.getPbs().getQueueToJobsMap().get(queueName)) {
                                    if ("Q".equals(job.getState())) {
                                        job.setFairshareRank(rankMap.get(job.getUser()));
                                        queuedJobs.add(job);
                                    }
                                }
                                //seradit podle fairshareRank
                                queuedJobs.sort(Job.FAIRSHARE_JOB_COMPARATOR);
                                queueNames2QueuedJobs.put(queueName, queuedJobs);
                            }
                        }
                    } else {
                        //vsechny joby na jednu hromadu, pak seradit podle priority front a fairshare,
                        // tj. fronty se stejnou prioritou jsou smichany
                        List<Job> queuedJobs = new ArrayList<>(pbs.getJobsQueuedCount());
                        for (Queue q : pbs.getQueuesByPriority()) {
                            if (!q.getFairshareTree().equals(fairshare.getTree())) continue;
                            if (q.getJobsQueued() > 0) {
                                String queueName = q.getName();
                                List<Job> jobs = q.getPbs().getQueueToJobsMap().get(queueName);
                                for (Job job : jobs) {
                                    if ("Q".equals(job.getState())) {
                                        job.setFairshareRank(rankMap.get(job.getUser()));
                                        queuedJobs.add(job);
                                    }
                                }

                            }
                        }
                        //seradit podle fairshareRank
                        queuedJobs.sort(Job.PRIORITY_FAIRSHARE_JOB_COMPARATOR);
                        queueNames2QueuedJobs.put(fairshare.getId(), queuedJobs);
                    }
                }
            }
        }
        return new ForwardResolution("/jobs/jobs_queued.jsp");
    }

    public List<PBS> getPbs() {
        return pbsky.getListOfPBS();
    }

    public Map<String, List<TextWithCount>> getDuvody() {
        return duvody;
    }

    public Map<String, List<Job>> getQ2qJobs() {
        return queueNames2QueuedJobs;
    }

    public Map<String, List<Job>> getPlannedJobs() {
        return plannedJobs;
    }
}
