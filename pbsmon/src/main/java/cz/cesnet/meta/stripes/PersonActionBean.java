package cz.cesnet.meta.stripes;

import cz.cesnet.meta.pbs.*;
import cz.cesnet.meta.pbs.Queue;
import cz.cesnet.meta.pbscache.PbsAccess;
import cz.cesnet.meta.pbscache.PbsCache;
import cz.cesnet.meta.pbscache.Scratch;
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

/**
 * Akce pro personalizaci pohledu na PBS.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: PersonActionBean.java,v 1.17 2014/10/17 12:33:03 makub Exp $
 */
@UrlBinding("/person")
public class PersonActionBean extends BaseActionBean implements ValidationErrorHandler {

    final static Logger log = LoggerFactory.getLogger(PersonActionBean.class);

    public static final String PERSONALIZE_URL = "https://metavo.metacentrum.cz/osobniv3/personal/personalize?backurl=";

    @SpringBean("pbsCache")
    protected PbsCache pbsCache;

    @SpringBean("pbsky")
    protected Pbsky pbsky;

    static final String ACCESS = "access";
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


    PbsAccess pbsAccess;

    @DefaultHandler
    public Resolution show() throws UnsupportedEncodingException {
        log.debug("show()");
        Resolution r = data();
        if (r != null) return r;
        return new ForwardResolution("/nodes/personal.jsp");
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
                pbsAccess = pbsCache.getUserAccess(user);
                session.setAttribute(PERSON, user);
                session.setAttribute(ACCESS, pbsAccess);
            } else {
                pbsAccess = (PbsAccess) session.getAttribute(ACCESS);
                user = (String) session.getAttribute(PERSON);
                if (pbsAccess == null) {
                    //nic nevime, poslat na Osobni, at nam povi, kdo to je
                    String backurl = request.getScheme() + "://" + request.getServerName()
                            + ":" + request.getServerPort() + request.getContextPath() + "/person";
                    return new RedirectResolution(PERSONALIZE_URL + URLEncoder.encode(backurl, "utf-8"), false);
                }
            }
            log.debug("pripravujeme data pro {}", user);
            //vsechny pristupne fronty
            queues = new ArrayList<>(40);
            //priprav mapovani nazvu fronty na pristupne uzly
            q2n = new HashMap<>();
            for (String qname : pbsAccess.getQueues()) {
                queues.add(pbsky.getQueueByName(qname));
                List<Node> nodes = new ArrayList<Node>();
                for (String nodeName : pbsAccess.getQueueToHostsMap().get(qname)) {
                    nodes.add(pbsky.getNodeByName(nodeName));
                }
                q2n.put(qname, nodes);
            }
            //sort
            Collections.sort(queues, (q1, q2) -> {
                if (q1 == null) {
                    log.error("q1 is null");
                    return +1;
                }
                String q1name = q1.getName();
                if (q1name == null) {
                    log.error("queue {} has no name", q1.getOrigToString());
                    return +1;
                }
                if (q2 == null) {
                    log.error("q2 is null");
                    return -1;
                }
                String q2name = q2.getName();
                if (q2name == null) {
                    log.error("queue {} has no name", q2.getOrigToString());
                    return -1;
                }
                return q1name.compareTo(q2name);
            });
            //censored queues
            offerQueues = new ArrayList<>(queues.size());
            for (Queue q : queues) {
                if (q.getName().startsWith("q_")) continue;
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
    int ppn = 1;
    @Validate(on = {"sestavovac"}, required = true, minvalue = 0)
    long scratch = 400;
    String scratchu = "mb";
    String scratchtype = "-";
    String prop1 = "x86_64";
    String prop2 = "linux";
    String prop3 = "";
    int ww = 0;
    int wd = 1;
    int wm = 0;
    int wh = 0;
    int ws = 0;
    Queue queue;
    Queue finalQueue;

    private long walltimeSecs() {
        return 7l * 24l * 3600l * ww + 24l * 3600l * wd + 3600l * wh + 60l * wm + ws;
    }

    public Resolution sestavovac() throws UnsupportedEncodingException {
        log.debug("sestavovac()");
        Resolution r = data();
        if (r != null) return r;
        //Sestavovac
        log.info("sestavovac() user={} -l walltime={}w{}d{}h{}m{}s,q={},nodes={}:ppn={}:{}:{}:{},scratch={}{}:{},mem={}{}",
                user, ww, wd, wh, wm, ws, fronta, nodes, ppn, prop1, prop2, prop3, scratch, scratchu, scratchtype, mem, memu);

        long memBytes = PbsUtils.parsePbsBytes(this.mem + this.memu);
        long scratchKB = PbsUtils.parsePbsBytes(this.scratch + this.scratchu) / 1024;
        long walltimeSecs = walltimeSecs();


        queue = pbsky.getQueueByName(fronta);
        List<Node> nodesList;
        if (queue.isRouting()) {
            log.debug("queue {} is routing", queue.getName());
            finalQueue = null;
            for (String dstName : queue.getDestQueueNames()) {
                Queue dstQueue = pbsky.getQueueByName(dstName);
                if (dstQueue.getWalltimeMinSeconds() <= walltimeSecs && walltimeSecs <= dstQueue.getWalltimeMaxSeconds()) {
                    finalQueue = dstQueue;
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
        nodesList = q2n.get(finalQueue.getName());
        potencialni = new ArrayList<>(nodesList.size());
        tedVolne = new ArrayList<>(nodesList.size());

        List<String> props = new ArrayList<>(3);
        if (prop1 != null && prop1.trim().length() > 0) props.add(prop1);
        if (prop2 != null && prop2.trim().length() > 0) props.add(prop2);
        if (prop3 != null && prop3.trim().length() > 0) props.add(prop3);

        NODES: for (Node node : nodesList) {
            //musi mit dost procesoru
            if (node.getNoOfCPUInt() < ppn) continue;
            //musi mit dost pameti
            long totalMem = node.getTotalMemoryInt();
            if (totalMem < memBytes) continue;
            //musi mit vhodne vlastnosti
            for(String prop : props) {
                if(!node.hasProperty(prop)) continue NODES;
            }
            //musi mit vhodny typ scratche
            Scratch nodeScratch = node.getScratch();
            if (scratchKB > 0) {
                if (scratchtype.equals("ssd") && !nodeScratch.hasSsdSizeKiB(scratchKB)) continue;
                if (scratchtype.equals("local") && !nodeScratch.hasLocalSizeKiB(scratchKB)) continue;
                if (scratchtype.equals("shared") && !nodeScratch.getHasNetwork()) continue;
                if (scratchtype.equals("-") && !nodeScratch.hasAnySizeKiB(scratchKB)) continue;
            }
            //kdy se to dostalo az sem, je potencialne vhodny
            potencialni.add(node);
            //ted zkontrolujeme, zda ma ted dost volnych prostredku
            //musi byt volny aspon castecne
            if (!(node.getState().equals(Node.STATE_FREE) || node.getState().equals(Node.STATE_PARTIALY_FREE) || node.getState().equals(Node.STATE_OCCUPIED_WOULD_PREEMPT)))
                continue;
            //musi mit dost volnych procesoru
            if (node.getNoOfFreeCPUInt() < ppn) continue;
            //musi mit dost volne pameti
            if (node.getFreeMemoryInt() < memBytes) continue;
            //musi mit dost volneho scratche
            if(scratchKB>0) {
                if (scratchtype.equals("ssd") && !nodeScratch.hasSsdFreeKiB(scratchKB)) continue;
                if (scratchtype.equals("local") && !nodeScratch.hasLocalFreeKiB(scratchKB)) continue;
                if (scratchtype.equals("shared") && !nodeScratch.hasNetworkFreeKiB(scratchKB)) continue;
                if (scratchtype.equals("-")&& nodeScratch.getAnyFreeKiB() < scratchKB) continue;
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
        return new ForwardResolution("/nodes/personal.jsp");
    }

    private static String getNodesShortNames(List<Node> nodes) {
        StringBuilder sb = new StringBuilder("[");
        for (Node node : nodes) {
            sb.append(node.getShortName()).append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public Queue getQueue() {
        return queue;
    }

    public Queue getFinalQueue() {
        return finalQueue;
    }

    public Map<String, List<Node>> getQ2n() {
        return q2n;
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

    public int getPpn() {
        return ppn;
    }

    public void setPpn(int ppn) {
        this.ppn = ppn;
    }

    public String getProp1() {
        return prop1;
    }

    public void setProp1(String prop1) {
        this.prop1 = prop1;
    }

    public String getProp2() {
        return prop2;
    }

    public void setProp2(String prop2) {
        this.prop2 = prop2;
    }

    public String getProp3() {
        return prop3;
    }

    public void setProp3(String prop3) {
        this.prop3 = prop3;
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

    public int getWw() {
        return ww;
    }

    public void setWw(int ww) {
        this.ww = ww;
    }

    public int getWd() {
        return wd;
    }

    public void setWd(int wd) {
        this.wd = wd;
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

    static private final HashSet<String> vynechatProps = new HashSet<String>() {{
        add("globus");
        add("pa177");
        add("jcu");
        add("zsc");
        add("iti");
        add("forprivileged");
        add("quark");
        add("xentest");
        add("maintenance");
    }};

    public List<String> getProps() {
        if (props == null) {
            HashSet<String> propSet = new HashSet<String>(50);
            for (Node node : pbsky.getAllNodes()) {
                propSet.addAll(Arrays.asList(node.getProperties()));
            }
            Iterator<String> iterator = propSet.iterator();
            while (iterator.hasNext()) {
                String prop = iterator.next();
                if (prop.startsWith("q_") || vynechatProps.contains(prop)) {
                    iterator.remove();
                }
            }
            props = new ArrayList<String>(propSet);
            Collections.sort(props);
        }
        return props;
    }
}
