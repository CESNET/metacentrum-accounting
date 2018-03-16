package cz.cesnet.meta.pbs;

import cz.cesnet.meta.TimeStamped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Data about objects as provided by the PBS server.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class PBS implements TimeStamped {

    private final PbsServerConfig serverConfig;


    public PBS(PbsServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        this.torque = serverConfig.isTorque();
        this.mainServer = serverConfig.isMain();
    }
    public static final String QUEUE_LIST = "queue_list";

    final static Logger log = LoggerFactory.getLogger(PBS.class);

    private Date timeLoaded = new Date();
    private Date clearCalledTime = null;

    private PbsServer server;
    private boolean torque;
    private final boolean mainServer;
    private Map<String, Queue> queues;
    private Map<String, Job> jobs;
    private Map<String, Node> nodes;
    private Map<String, Reservation> reservations;
    private Map<String, PbsResource> resources;
    private Map<String, Scheduler> schedulers;
    private Map<String, Hook> hooks;

    private List<Queue> queuesByPriority;
    private List<Node> nodesByName;
    private List<Job> jobsById;
    private Map<String, List<Node>> queueToNodesMap;
    private Map<String, List<Job>> queueToJobsMap;
    private Map<String,Node> fqdnToNodeMap;
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
            reservations.clear();
            reservations = null;
            resources.clear();
            resources = null;
            schedulers.clear();
            schedulers = null;
            hooks.clear();
            hooks = null;

            log.trace("clearing secondary maps and queues");
            for (List<Node> lnodes : queueToNodesMap.values()) {
                lnodes.clear();
            }
            queueToNodesMap.clear();
            queueToNodesMap = null;

            fqdnToNodeMap.clear();
            fqdnToNodeMap = null;

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

    public String getHost() {
        return serverConfig.getHost();
    }

    public void setServer(PbsServer server) {
        this.server = server;
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

    public Map<String, Node> getFqdnToNodeMap() {
        return fqdnToNodeMap;
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

    public void setReservations(Map<String, Reservation> reservations) {
        this.reservations = reservations;
    }

    public Map<String, Reservation> getReservations() {
        return reservations;
    }

    public void setResources(Map<String, PbsResource> resources) {
        this.resources = resources;
    }

    public Map<String, PbsResource> getResources() {
        return resources;
    }

    public void setSchedulers(Map<String, Scheduler> schedulers) {
        this.schedulers = schedulers;
    }

    public Map<String, Scheduler> getSchedulers() {
        return schedulers;
    }

    public void setHooks(Map<String, Hook> hooks) {
        this.hooks = hooks;
    }

    public Map<String, Hook> getHooks() {
        return hooks;
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

        fqdnToNodeMap = new HashMap<>(nodes.size());
        for (Node node : nodes.values()) {
            node.setPbs(this);
            fqdnToNodeMap.put(node.getFQDN(),node);
        }
        //suffix
        suffix = mainServer ? "" : "@" + server.getHost();

        //mark reservation queues
        for(Reservation reservation : reservations.values()) {
            Queue qresv = queues.get(reservation.getQueue());
            if(qresv!=null) {
                qresv.setReservation(reservation);
            }
        }

        //serazena pole
        queuesByPriority = new ArrayList<>(queues.values());
        queuesByPriority.sort(PbskyImpl.queuesPriorityComparator);

        nodesByName = new ArrayList<>(nodes.values());
        nodesByName.sort(PbskyImpl.nodesNameComparator);

        jobsById = new ArrayList<>(jobs.values());
        jobsById.sort(PbskyImpl.jobsIdComparator);

        //mapy
        queueToNodesMap = makeQueuesToNodeMap(queuesByPriority, nodesByName);
        queueToJobsMap = makeQueueToJobsMap(queuesByPriority, jobsById);

        //spocitej joby ve frontach
        makeJobCountsForQueues();
        //spocitej uzivatele
        makeUsers();
        //zjisti fairshare_tree u front
        fairshareTrees = new HashSet<>();
        for (Queue q : queuesByPriority) {
            fairshareTrees.add(q.getFairshareTree());
        }
    }


    private void makeUsers() {
        usersMap = new HashMap<>(100);
        for (Job job : jobsById) {
            User user = usersMap.get(job.getUser());
            if (user == null) {
                user = new User(job.getUser());
                usersMap.put(user.getName(), user);
            }
            switch (job.getState()) {
                case "Q":
                    user.incJobsStateQ();
                    user.addCpusStateQ(job.getNoOfUsedCPU());
                    break;
                case "R":
                    user.incJobsStateR();
                    user.addCpusStateR(job.getNoOfUsedCPU());
                    break;
                case "C":
                case "F":
                    user.incJobsStateC();
                    user.addCpusStateC(job.getNoOfUsedCPU());
                    break;
                default:
                    user.incJobsOther();
                    user.addCpusOther(job.getNoOfUsedCPU());
                    break;
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
                if ("C".equals(state)||"F".equals(state)) {
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
     * The rules for assigning nodes to queues differ for each planner.
     * For vanilla Torque:
     * <ul>
     *  <li>A node can be used when it has a required property for queues that require a property,
     * and is not reserved for another queue.
     *  <li>If a queue has at least one node that is assigned to it, it can use only nodes
     * that are assigned to it. Otherwise it can use any host that is not assigned to any queue.</li>
     * </ul>
     * For PBS-Pro:
     * <ul>
     *     <li>If a queue has at least one node that is assigned to it, it can use only nodes
     * that are assigned to it. Otherwise it can use any host that is not assigned to any queue. Nodes are assigned
     * to a queue by node's attribute "queue"</li>
     *     <li>A queue has attribute default_chunk.queue_list, its value may be used in node's attribute resources_available.queue_list
     *     then node accepts only from such queues</li>
     * </ul>
     *
     * @param queues queues
     * @param nodes  nodes
     * @return map
     */
    private static Map<String, List<Node>> makeQueuesToNodeMap(List<Queue> queues, List<Node> nodes) {
        Map<String, List<Node>> queuesToNodesMap = new HashMap<>((int) (queues.size() * 1.5));
        for (Queue q : queues) {
            List<Node> nodeList = new ArrayList<>();
            String queueName = q.getName();
            //non-execution queues have no nodes
            if (!q.isExecutionQueue()) {
                queuesToNodesMap.put(queueName, nodeList);
                continue;
            }
            if (q.getPbs().isTorque()) {
                //TORQUE
                //naprogramovano podle udaju od Simona v https://rt3.cesnet.cz/rt/Ticket/Display.html?id=28959
                for (Node node : nodes) {
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
                //is any host assigned to the queue?
                boolean assignedHostsExist = false;
                for (Node node : nodes) {
                    String nrq = node.getRequiredQueue();
                    if (nrq != null && queueName.equals(nrq)) {
                        assignedHostsExist = true;
                        break;
                    }
                }
                //what jobs in the queue require
                String rql = q.getDefaultChunkQueuesList();
                //for each node, decide whether it belongs to the queue
                for (Node node : nodes) {
                    String nodeRequiredQueue = node.getRequiredQueue();
                    boolean assign;
                    if(nodeRequiredQueue!=null) {
                        //node assigned to a specific queue, so use it if it is this queue
                        assign = nodeRequiredQueue.equals(queueName);
                    } else {
                        //node not assigned to any queue, so use it only if no other node is assigned
                        // and the node has a required value of resource queue_list
                        assign = !assignedHostsExist && (rql == null || node.getResourceQueueList().contains(rql));
                    }
                    if(assign) {
                        //node is accessible by the queue
                        nodeList.add(node);
                        node.getQueues().add(q);
                    }
                }
            }

            queuesToNodesMap.put(queueName, nodeList);
        }
        return queuesToNodesMap;
    }

    private static Map<String, List<Job>> makeQueueToJobsMap(List<Queue> queues, List<Job> jobs) {
        Map<String, List<Job>> map = new HashMap<>((int) (queues.size() * 1.5));
        for (Queue q : queues) {
            List<Job> jobList = new ArrayList<>();
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

    public boolean isPBSPro() {
        return ! isTorque();
    }

    @Override
    public String toString() {
        return "PBS{" +
                "server=" + serverConfig.getHost() +
                ",timeLoaded=" + timeLoaded +
                '}';
    }


}
