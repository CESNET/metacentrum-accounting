package cz.cesnet.meta.stripes;

import cz.cesnet.meta.pbs.*;
import cz.cesnet.meta.pbs.Node.NodeResource;
import cz.cesnet.meta.pbs.Node.NodeResource.Type;
import cz.cesnet.meta.pbs.Queue;
import cz.cesnet.meta.pbscache.PbsCache;
import cz.cesnet.meta.pbs.Scratch;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.LocalizableError;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidationErrorHandler;
import net.sourceforge.stripes.validation.ValidationErrors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Sestavovač qsub pro PBSPro.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("unused") //methods are accessed by Stripes through reflection
@UrlBinding(QsubPbsproActionBean.URI_BINDING)
public class QsubPbsproActionBean extends BaseActionBean implements ValidationErrorHandler {

    final static Logger log = LoggerFactory.getLogger(QsubPbsproActionBean.class);

    private static final String PERSONALIZE_URL = "https://metavo.metacentrum.cz/osobniv3/personal/personalize?backurl=";
    private static final String JSP_PAGE = "/nodes/qsub_pbspro.jsp";
    public static final String URI_BINDING = "/qsub_pbspro";
    private static final String CPU_FLAG = "cpu_flag";

    @SpringBean("pbsCache")
    protected PbsCache pbsCache;

    @SpringBean("pbsky")
    protected Pbsky pbsky;

    @SpringBean("userAccess")
    UserAccess userAccess;

    static final String PERSON = "person";

    private String user;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }


    Map<String, List<Node>> q2n;
    User jobUser;
    List<Node> potencialni;
    List<Node> tedVolne;
    boolean vyber;
    List<Queue> queues;
    List<Queue> offerQueues;
    List<String> props;
    Map<String, Set<String>> resourceValues;
    List<String> clusters;
    List<String> cities;

    @DefaultHandler
    public Resolution show() throws UnsupportedEncodingException {
        log.debug("show()");
        Resolution r = data();
        if (r != null) return r;
        return new ForwardResolution(JSP_PAGE);
    }

    @Override
    public Resolution handleValidationErrors(ValidationErrors errors) throws Exception {
        return data();
    }

    private boolean loaded = false;

    //tenhle cirkus je tady proto, ze kdyz jsou ve formulari spatne hodnoty, jde se na person.jsp
    //bez vyvolani actionBeanu. Proto je tu handleValidationError(), ktera se vyvola pri chybach
    //ale aby se data nenacitala dvakrat, je tu vyhybka loaded
    private Resolution data() throws UnsupportedEncodingException {
        log.debug("data(loaded={})", loaded);
        if (!loaded) {
            HttpServletRequest request = ctx.getRequest();
            HttpSession session = request.getSession(true);

            if (user != null) {
                //prichazime z Osobniho, nastavit
                session.setAttribute(PERSON, user);
            } else {
                user = (String) session.getAttribute(PERSON);
                if (user == null) {
                    //nic nevime, poslat na Osobni, at nam povi, kdo to je
                    String backurl = request.getScheme() + "://" + request.getServerName()
                            + ":" + request.getServerPort() + request.getContextPath() + URI_BINDING;
                    return new RedirectResolution(PERSONALIZE_URL + URLEncoder.encode(backurl, "utf-8"), false);
                }
            }
            log.debug("pripravujeme data pro {}", user);
            //vsechny pristupne fronty
            queues = new ArrayList<>(40);
            //priprav mapovani nazvu fronty na pristupne uzly
            //a seznam resources
            q2n = new HashMap<>();
            resourceValues = new TreeMap<>();
            for (Queue q : userAccess.getUserQueues(user)) {
                if (!q.getPbs().isPBSPro()) continue;
                queues.add(q);
                //budeme předpokládat, že uživatel může na všechny uzly fronty, do které může
                List<Node> nodes = q.getPbs().getQueueToNodesMap().get(q.getName());
                q2n.put(q.getName(), nodes);
                //resources
                for (Node node : nodes) {
                    for (NodeResource nr : node.getNodeResources()) {
                        if (nr.getType() == Type.LONG) continue;
                        if (nr.getType() == Type.SIZE) continue;
                        if (nr.getAvailable() == null) continue;
                        if(nr.getName().equals("queue_list")) continue;
                        Set<String> foundValues = resourceValues.computeIfAbsent(nr.getName(), k -> new TreeSet<>());
                        switch (nr.getType()) {
                            case STRING:
                                foundValues.add(nr.getAvailable());
                                if(nr.getAvailable().indexOf(',')!=-1) {
                                    log.warn("node {} has resource {} of type {} but value is {}", node.getName(), nr.getName(), nr.getType(), nr.getAvailable());
                                }
                                break;
                            case BOOLEAN:
                                foundValues.add("True");
                                foundValues.add("False");
                                break;
                            case STRING_ARRAY:
                                foundValues.addAll(Arrays.asList(nr.getAvailable().split(",")));
                                break;
                        }
                    }
                }
            }
            //filter out clusters
            clusters = resourceValues.keySet().stream().filter(k -> k.startsWith("cl_")).collect(Collectors.toList());
            clusters.forEach(resourceValues::remove);
            //filter out cities
            List<String> citynames = Arrays.asList("brno", "budejovice", "olomouc", "liberec", "plzen", "praha");
            cities = resourceValues.keySet().stream().filter(citynames::contains).collect(Collectors.toList());
            cities.forEach(resourceValues::remove);
            //censored queues for offering the -q parameter
            offerQueues = new ArrayList<>(queues.size());
            //put default queues first
            for (Queue q : queues) {
                if (q.getName().startsWith("default")) {
                    offerQueues.add(q);
                }
            }
            //then the rest of submitable queues
            for (Queue q : queues) {
                if (q.isFromRouteOnly()) continue;
                if (q.getName().startsWith("default")) continue;
                offerQueues.add(q);
            }
            //user
            if (jobUser == null) {
                jobUser = pbsky.getUserByName(user);
                if (jobUser == null) jobUser = new User(user);
            }
            loaded = true;
        }
        return null;
    }

    public List<Queue> getOfferQueues() {
        return offerQueues;
    }


    //formular
    String fronta = "default";
    @Validate(on = {"sestavovac"}, required = true, minvalue = 1)
    long mem = 400;
    String memu = "mb";
    int nodes = 1;
    int ncpus = 1;
    int ngpus = 0;
    @Validate(on = {"sestavovac"}, required = true, minvalue = 0)
    long scratch = 400;
    String scratchu = "mb";
    String scratchtype = "local";
    String cluster;
    String city;

    int wm = 0;
    int wh = 1;
    int ws = 0;

    Queue queue;
    Queue finalQueue;

    Map<String, String> resources = new HashMap<>();

    public Map<String, String> getResources() {
        return resources;
    }

    public void setResources(Map<String, String> resources) {
        this.resources = resources;
    }

    private long walltimeSecs() {
        return 3600L * wh + 60L * wm + ws;
    }

    public Resolution sestavovac() throws UnsupportedEncodingException {
        log.debug("sestavovac()");
        Resolution r = data();
        if (r != null) return r;
        //Sestavovac
        log.info("sestavovac() user={} -l walltime={}:{}:{}:,q={},nodes={}:ncpus={}:scratch_{}={}{}:mem={}{}: resources= {}:cluster={}:city={}",
                user, wh, wm, ws, fronta, nodes, ncpus, scratchtype, scratch, scratchu, mem, memu, resources,cluster,city);

        long memBytes = PbsUtils.parsePbsBytes(this.mem + this.memu);
        long scratchBytes = PbsUtils.parsePbsBytes(this.scratch + this.scratchu);
        long walltimeSecs = walltimeSecs();

        if(cluster!=null&&!cluster.isEmpty()) {
            resources.put(cluster,"True");
        }
        if(city!=null&&!city.isEmpty()) {
            resources.put(city,"True");
        }

        queue = pbsky.getQueueByName(fronta);
        List<Node> nodesList;
        if (queue.isRouting()) {
            log.debug("queue {} is routing", queue.getName());
            finalQueue = null;
            for (String dstName : queue.getDestQueueNames()) {
                Queue dstQueue = pbsky.getQueueByName(dstName);
                if (dstQueue.getWalltimeMinSeconds() <= walltimeSecs && walltimeSecs <= dstQueue.getWalltimeMaxSeconds()) {
                    finalQueue = dstQueue;
                    break;
                }
            }
            if (finalQueue == null) {
                getContext().getValidationErrors().add("queue", new LocalizableError("person.qsub.no.queue.for.walltime"));
                return getContext().getSourcePageResolution();
            }
            log.debug("routing queue {} for walltime {} routed to queue {}", queue.getName(), walltimeSecs, finalQueue.getName());
        } else {
            if (!(queue.getWalltimeMinSeconds() <= walltimeSecs && walltimeSecs <= queue.getWalltimeMaxSeconds())) {
                log.debug("walltime {} not good for queue {}", walltimeSecs, queue.getName());
                getContext().getValidationErrors().add("queue", new LocalizableError("person.qsub.queue.not.for.walltime"));
                return getContext().getSourcePageResolution();
            }
            finalQueue = queue;
        }
        log.debug("finalQueue={}", finalQueue.getName());
        nodesList = q2n.get(finalQueue.getName());
        potencialni = new ArrayList<>(nodesList.size());
        tedVolne = new ArrayList<>(nodesList.size());

        if (log.isDebugEnabled()) {
            log.debug("nodesList=" + nodesList.stream().map(Node::getShortName).collect(Collectors.joining(",")));
        }

        NODES:
        for (Node node : nodesList) {
            log.debug("deciding node {}", node.getShortName());
            //musi mit dost CPU
            if (node.getNoOfCPUInt() < ncpus) {
                log.debug("node {} has not enough CPUs: {}<{}", node.getName(), node.getNoOfCPUInt(), ncpus);
                continue;
            }
            //musit mit dost GPU
            if (node.getNoOfGPUInt() < ngpus) {
                log.debug("node {} has not enough GPUs: {}<{}", node.getName(), node.getNoOfGPUInt(), ngpus);
                continue;
            }
            //musi mit dost pameti
            if (node.getTotalMemoryInt() < memBytes) {
                log.debug("node {} has not enough RAM: {}<{}", node.getName(), node.getTotalMemoryInt(), memBytes);
                continue;
            }
            //musi mit vhodny typ scratche
            Scratch nodeScratch = node.getScratch();
            if (scratchBytes > 0) {
                if (scratchtype.equals("ssd") && !nodeScratch.getHasSsd()) {
                    log.debug("node {} has no scratch_ssd", node.getName());
                    continue;
                }
                if (scratchtype.equals("local") && !nodeScratch.getHasLocal()) {
                    log.debug("node {} has no scratch_local", node.getName());
                    continue;
                }
                if (scratchtype.equals("shared") && !nodeScratch.getHasShared()) {
                    log.debug("node {} has no scratch_shared", node.getName());
                    continue;
                }
            }
            //resources
            for (Map.Entry<String, String> re : resources.entrySet()) {
                String resourceName = re.getKey();
                String requiredValue = re.getValue();
                if (requiredValue == null || requiredValue.isEmpty()) continue;
                String nodeValue = node.getResources().get(resourceName);
                if(requiredValue.equals("False")) {
                    if("True".equals(nodeValue)) {
                        log.debug("node {} has resource {}={}, but False was required", node.getName(), resourceName, nodeValue, requiredValue);
                        continue NODES;
                    }
                } else {
                    if (nodeValue == null || !nodeValue.contains(requiredValue)) {
                        log.debug("node {} has no resource {}={}, only {}", node.getName(), resourceName, requiredValue, nodeValue);
                        continue NODES;
                    }
                }
            }
            //kdy se to dostalo az sem, uzel je potencialne vhodny
            log.debug("node {} is potential", node.getName());
            potencialni.add(node);


            //ted zkontrolujeme, zda ma ted dost volnych prostredku
            //musi byt volny aspon castecne
            if (!(node.getState().equals(Node.STATE_FREE) || node.getState().equals(Node.STATE_PARTIALY_FREE)))
                continue;
            //musi mit dost volnych CPU
            if (node.getNoOfFreeCPUInt() < ncpus) continue;
            //musimit dost volnych GPU
            if (node.getNoOfFreeGPUInt() < ngpus) continue;
            //musi mit dost volne pameti
            if (node.getFreeMemoryInt() < memBytes) continue;
            //musi mit dost volneho scratche
            if (scratchBytes > 0) {
                if (scratchtype.equals("ssd") && !nodeScratch.hasSsdAvailableBytes(scratchBytes)) continue;
                if (scratchtype.equals("local") && !nodeScratch.hasLocalAvailableBytes(scratchBytes)) continue;
                if (scratchtype.equals("shared") && !nodeScratch.hasSharedAvailableBytes(scratchBytes)) continue;
            }

            //kdyz se to dostalo az, je i ted volny
            tedVolne.add(node);
        }

        String vysledek;
        if (potencialni.size() < nodes) {
            vysledek = "NeniDostUzlu potencialni=" + potencialni.size();
        } else if (tedVolne.size() < nodes) {
            vysledek = "NejsouVolne tedVolne=" + tedVolne.size() + " potencialni=" + potencialni.size();
        } else {
            vysledek = "OK tedVolne=" + tedVolne.size();
        }
        log.info("SestavovacQsub vysledek: " + vysledek);
        vyber = true;
        return new ForwardResolution(JSP_PAGE);
    }

    public Queue getQueue() {
        return queue;
    }

    public Queue getFinalQueue() {
        return finalQueue;
    }

    public User getJobUser() {

        return jobUser;
    }

    public String getFronta() {
        return fronta;
    }

    public void setFronta(String fronta) {
        this.fronta = fronta;
    }

    public String getMemu() {
        return memu;
    }

    public void setMemu(String memu) {
        this.memu = memu;
    }

    public String getScratchu() {
        return scratchu;
    }

    public void setScratchu(String scratchu) {
        this.scratchu = scratchu;
    }

    public long getMem() {
        return mem;
    }

    public void setMem(long mem) {
        this.mem = mem;
    }

    public int getNodes() {
        return nodes;
    }

    public void setNodes(int nodes) {
        this.nodes = nodes;
    }

    public int getNcpus() {
        return ncpus;
    }

    public void setNcpus(int ncpus) {
        this.ncpus = ncpus;
    }

    public int getNgpus() {
        return ngpus;
    }

    public void setNgpus(int ngpus) {
        this.ngpus = ngpus;
    }

    public long getScratch() {
        return scratch;
    }

    public void setScratch(long scratch) {
        this.scratch = scratch;
    }

    public List<Node> getPotencialni() {
        return potencialni;
    }

    public List<Node> getTedVolne() {
        return tedVolne;
    }

    public boolean isVyber() {
        return vyber;
    }

    public List<Queue> getQueues() {
        return queues;
    }

    public String getScratchtype() {
        return scratchtype;
    }

    public void setScratchtype(String scratchtype) {
        this.scratchtype = scratchtype;
    }

    public int getWm() {
        return wm;
    }

    public void setWm(int wm) {
        this.wm = wm;
    }

    public int getWh() {
        return wh;
    }

    public void setWh(int wh) {
        this.wh = wh;
    }

    public int getWs() {
        return ws;
    }

    public void setWs(int ws) {
        this.ws = ws;
    }

    public Map<String, Set<String>> getResourceValues() {
        return resourceValues;
    }

    public List<String> getCities() {
        return cities;
    }

    public List<String> getClusters() {
        return clusters;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
