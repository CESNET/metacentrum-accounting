package cz.cesnet.meta.pbs;

import cz.cesnet.meta.TimeStamped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * JavaBean odpovídající formátu dat zpracovávanému JSON Tools.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: PBS.java,v 1.23 2014/04/11 08:38:08 makub Exp $
 */
public class PBS implements TimeStamped {

    private final PbsServerConfig serverConfig;

    public PBS(PbsServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        this.torque = serverConfig.isTorque();
        this.mainServer = serverConfig.isMain();
    }

    final static Logger log = LoggerFactory.getLogger(PBS.class);

    private Date timeLoaded = new Date();
    private Date clearCalledTime = null;

    private PbsServer server;
    private boolean torque;
    private final boolean mainServer;
    private Map<String, Queue> queues;
    private Map<String, Job> jobs;
    private Map<String, Node> nodes;

    private List<Queue> queuesByPriority;
    private List<Node> nodesByName;
    private List<Job> jobsById;
    private Map<String, List<Node>> queueToNodesMap;
    private Map<String, List<Job>> queueToJobsMap;
    private int jobsQueuedCount;
    private Map<String, User> usersMap;
    private String suffix;
    private Set<String> fairshareTrees;

    public PbsServerConfig getServerConfig() {
        return serverConfig;
    }

    /**
     * Pomoc Garbage Collectoru.
     */
    public void clear() {
        try {
            log.debug("clearing old data "+this.toString());
            clearCalledTime = new Date();

            log.trace("clearing primary maps");
            server.clear();
            server = null;
            queues.clear();
            queues = null;
            jobs.clear();
            jobs = null;
            nodes.clear();
            nodes = null;

            log.trace("clearing secondary maps and queues");
            for (List<Node> lnodes : queueToNodesMap.values()) {
                lnodes.clear();
            }
            queueToNodesMap.clear();
            queueToNodesMap = null;

            for (List<Job> ljobs : queueToJobsMap.values()) {
                ljobs.clear();
            }
            queueToJobsMap.clear();
            queueToJobsMap = null;
            usersMap.clear();
            usersMap = null;

            for (Queue queue : queuesByPriority) {
                queue.clear();
            }
            queuesByPriority.clear();
            queuesByPriority = null;

            log.trace("clearing nodesByName");
            for (Node node : nodesByName) {
                node.clear();
            }
            nodesByName.clear();
            nodesByName = null;

            log.trace("clearing jobsById");
            for (Job job : jobsById) {
                job.clear();
            }
            jobsById.clear();
            jobsById = null;

            log.trace("clearing suffix");
            suffix = null;

            fairshareTrees.clear();
        } catch (Throwable ex) {
            log.error("PBS.clear() problem", ex);
        }
    }

    @Override
    public Date getTimeLoaded() {
        return timeLoaded;
    }

    public Date getClearCalledTime() {
        return clearCalledTime;
    }

    public PbsServer getServer() {
        return server;
    }

    public String getPbsServerHost() {
        return serverConfig.getHost();
    }

    public void setServer(PbsServer server) {
        this.server = server;
        if (this.torque != server.isTorque()) {
            log.error("server " + server.getHost() + " declared as Torque but is " + server.getVersion());
        }
    }

    /**
     * Should be named getShortQueueNamesToQueusMap() but its name is fixed
     * because it is mapped by JSON tools.
     *
     * @return Map from short queue names to queue objects
     */

    public Map<String, Queue> getQueues() {
        return queues;
    }

    public void setQueues(Map<String, Queue> queues) {
        this.queues = queues;
    }

    public Map<String, Job> getJobs() {
        return jobs;
    }

    public void setJobs(Map<String, Job> jobs) {
        this.jobs = jobs;
    }

    public Map<String, Node> getNodes() {
        return nodes;
    }

    public void setNodes(Map<String, Node> nodes) {
        this.nodes = nodes;
    }

    public List<Queue> getQueuesByPriority() {
        return queuesByPriority;
    }

    public List<Node> getNodesByName() {
        return nodesByName;
    }

    public List<Job> getJobsById() {
        return jobsById;
    }

    public Map<String, List<Node>> getQueueToNodesMap() {
        return queueToNodesMap;
    }

    public Map<String, List<Job>> getQueueToJobsMap() {
        return queueToJobsMap;
    }

    public int getJobsQueuedCount() {
        return jobsQueuedCount;
    }

    public Map<String, User> getUsersMap() {
        return usersMap;
    }

    public String getSuffix() {
        return suffix;
    }

    public Set<String> getFairshareTrees() {
        return fairshareTrees;
    }

    /**
     * Provede úpravy po prostém načtení dat.
     */
    public void uprav() {
        //ukazatele nahoru
        for (Queue queue : queues.values()) {
            queue.setPbs(this);
        }
        for (Job job : jobs.values()) {
            job.setPbs(this);
        }
        for (Node node : nodes.values()) {
            node.setPbs(this);
        }
        //suffix
        suffix = mainServer ? "" : "@" + server.getHost();

        //serazena pole
        queuesByPriority = new ArrayList<Queue>(queues.values());
        Collections.sort(queuesByPriority, PbskyImpl.queuesPriorityComparator);

        nodesByName = new ArrayList<Node>(nodes.values());
        Collections.sort(nodesByName, PbskyImpl.nodesNameComparator);

        jobsById = new ArrayList<Job>(jobs.values());
        Collections.sort(jobsById, PbskyImpl.jobsIdComparator);

        //mapy
        queueToNodesMap = makeQueuesToNodeMap(queuesByPriority, nodesByName);
        queueToJobsMap = makeQueueToJobsMap(queuesByPriority, jobsById);

        //spocitej joby ve frontach
        makeJobCountsForQueues();
        //spocitej uzivatele
        makeUsers();
        //zjisti fairshare_tree u front
        fairshareTrees = new HashSet<String>();
        for (Queue q : queuesByPriority) {
            fairshareTrees.add(q.getFairshareTree());
        }
    }


    private void makeUsers() {
        usersMap = new HashMap<String, User>(100);
        for (Job job : jobsById) {
            User user = usersMap.get(job.getUser());
            if (user == null) {
                user = new User(job.getUser());
                usersMap.put(user.getName(), user);
            }
            String state = job.getState();
            if (state.equals("Q")) {
                user.incJobsStateQ();
                user.addCpusStateQ(job.getNoOfUsedCPU());
            } else if (state.equals("R")) {
                user.incJobsStateR();
                user.addCpusStateR(job.getNoOfUsedCPU());
            } else if (state.equals("C")) {
                user.incJobsStateC();
                user.addCpusStateC(job.getNoOfUsedCPU());
            } else {
                user.incJobsOther();
                user.addCpusOther(job.getNoOfUsedCPU());
            }
        }
    }

    private void makeJobCountsForQueues() {
        int totalQueued = 0;
        for (Queue queue : queuesByPriority) {
            //count jobs
            int running = 0;
            int completed = 0;
            int queued = 0;
            int total = 0;
            for (Job job : queueToJobsMap.get(queue.getName())) {
                total++;
                String state = job.getState();
                if ("C".equals(state)) {
                    completed++;
                } else if ("R".equals(state)) {
                    running++;
                } else if ("Q".equals(state)) {
                    queued++;
                    totalQueued++;
                }
            }
            queue.setJobNums(running, completed, queued, total);
        }
        jobsQueuedCount = totalQueued;
    }


    /**
     * For each queue creates a list of nodes that can be used.
     * A node can be used when it has a required property for queues that require a property,
     * and is not reserved for another queue.
     * If a queue has at least one node that is assigned to it, it can use only nodes
     * that are assigned to it. Otherwise it can use any host that is not assigned to any queue.
     *
     * @param queues queues
     * @param nodes  nodes
     * @return map
     */
    private static Map<String, List<Node>> makeQueuesToNodeMap(List<Queue> queues, List<Node> nodes) {
        Map<String, List<Node>> queuesToNodesMap = new HashMap<String, List<Node>>((int) (queues.size() * 1.5));
        for (Queue q : queues) {
            List<Node> nodeList = new ArrayList<Node>();
            String queueName = q.getName();
            if (!q.isExecutionQueue()) {
                queuesToNodesMap.put(queueName, nodeList);
                continue;
            }
            if (q.getPbs().isTorque()) {
                //TORQUE
                //naprogramovano podle udaju od Simona v https://rt3.cesnet.cz/rt/Ticket/Display.html?id=28959
                for (Node node : nodes) {
                    if (node.isCloud()) continue; //dom0 nejsou ve frontach
                    String nrq = node.getRequiredQueue();//jmeno fronty vcetne pripony @server
                    if (nrq != null) {
                        if (nrq.equals(queueName)) {
                            //node is assigned to the queue
                            nodeList.add(node);
                            node.getQueues().add(q);
                            continue;
                        } else {
                            //node is reserved for another queue
                            continue;
                        }
                    }
                    if (!queueName.startsWith("maintenance") && !queueName.startsWith("reserved")) { //u maintenance brat jen vyhrazene stroje
                        String qrp = q.getRequiredProperty();
                        if (qrp != null && !node.hasProperty(qrp)) {
                            //queue requires a node property that the node does not have
                            continue;
                        }
                        //node is accessible by the queue
                        nodeList.add(node);
                        node.getQueues().add(q);
                    }
                }
            } else {
                //PBSPRO
                //kod je nejspis v poradku, pokud uzel neni ve fronte,
                // asi ma fronta required property a uzel ji nema
                boolean assignedHostsExist = false;
                for (Node node : nodes) {
                    String nrq = node.getRequiredQueue();
                    if (nrq != null && queueName.equals(nrq)) {
                        assignedHostsExist = true;
                        break;
                    }
                }

                for (Node node : nodes) {
                    String nrq = node.getRequiredQueue();//s priponou
                    //if some nodes are assigned, a queue cannot use unassigned nodes
                    if (assignedHostsExist && nrq == null) continue;

                    if (nrq != null && !nrq.equals(queueName)) {
                        //node is reserved for another queue
                        continue;
                    }
                    String qrp = q.getRequiredProperty();
                    if (qrp != null && !node.hasProperty(qrp)) {
                        //queue requires a node property that the node does not have
                        continue;
                    }
                    //node is accessible by the queue
                    nodeList.add(node);
                    node.getQueues().add(q);
                }
            }
            queuesToNodesMap.put(queueName, nodeList);
        }
        return queuesToNodesMap;
    }

    private static Map<String, List<Job>> makeQueueToJobsMap(List<Queue> queues, List<Job> jobs) {
        Map<String, List<Job>> map = new HashMap<String, List<Job>>((int) (queues.size() * 1.5));
        for (Queue q : queues) {
            List<Job> jobList = new ArrayList<Job>();
            for (Job job : jobs) {
                if (job.getQueueName().equals(q.getName())) {
                    jobList.add(job);
                }
            }
            map.put(q.getName(), jobList);
        }
        return map;
    }

    public boolean isTorque() {
        return torque;
    }

    @Override
    public String toString() {
        return "PBS{" +
                "server=" + serverConfig.getHost() +
                ",timeLoaded=" + timeLoaded +
                '}';
    }
}
