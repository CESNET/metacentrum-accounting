package cz.cesnet.meta.accounting.server.service;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Zapisuje udalosti z provozniho logu PBS a vypocitava maintenance a reserved doby.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id:$
 */
public class OutageManagerImpl extends JdbcDaoSupport implements OutageManager {

    static private final Logger log = Logger.getLogger(OutageManagerImpl.class);

    //pripady nastaveni nebo smazani queue nebo comment
    private static final Pattern EVENT_TORQUE
            = Pattern.compile("^([^;]*);[^;]*;PBS_Server;(node;([^;]*);attributes set: (queue|note) (=|\\+|-) (.*)|Req;update_node_state;adjusting state for node ([^ ]*) - state=(\\d+), newstate=(\\d+).*)");

    // 06/09/2011 00:09:57;0004;PBS_Server;node;skirit80-2.ics.muni.cz;attributes set: queue = maintenance";
    // 06/16/2011 11:21:43;0004;PBS_Server;node;nympha1-1.zcu.cz;attributes set: note = upgrade OS, nastaveni BMC
    // 06/16/2011 11:00:15;0040;PBS_Server;Req;update_node_state;adjusting state for node tarkil10-1.cesnet.cz - state=514, newstate=2

    //format pouzivany PBS. Pri prechodu na zimni cas je prvni hodina po prechodu dvakrat :-(
    private static final SimpleDateFormat SDF;

    static {
        SDF = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
        SDF.setTimeZone(TimeZone.getTimeZone("CET"));
    }

    private static final String NODE_UP = "node up";
    private static final String NODE_DOWN = "node down";
    static private final Set<String> ALLOWED_TYPES = new HashSet<>(Arrays.asList("queue", "comment", NODE_UP, NODE_DOWN));

    @Autowired
    protected HostManager hostManager;

    @Autowired
    protected PbsServerManager pbsServerManager;

    @Override
    @Transactional
    public void saveLogEvents(BufferedReader in, String server) throws IOException {
        long startTime = System.currentTimeMillis();
        String line;
        long lastTime = 0;
        long citac = 0;
        long minTime = Long.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;
        Map<String, String> lastStates = new HashMap<>(500);
        while ((line = in.readLine()) != null) {
            Matcher m = EVENT_TORQUE.matcher(line);
            if (m.find()) {
                boolean process = false;
                String cas = m.group(1);
                String host = null;
                String nadtyp = m.group(2);
                String type = null;
                String operace = null;
                String value = null;
                if (nadtyp.startsWith("node")) {
                    host = m.group(3);
                    type = m.group(4);//queue|note
                    if ("note".equals(type)) type = "comment";
                    operace = m.group(5);
                    value = m.group(6);
                    process = true;
                } else if (nadtyp.startsWith("Req")) {
                    host = m.group(7);
                    int oldbit = Integer.parseInt(m.group(8)) & 2;
                    int newbit = Integer.parseInt(m.group(9)) & 2;
                    if (oldbit == 0 && newbit == 2) {
                        type = NODE_DOWN;
                        process = true;
                    } else if (newbit == 0) {
                        type = NODE_UP;
                        process = true;
                    }
                }
                if (process) {
                    try {
                        Date eventTime = SDF.parse(cas);
                        //kvuli usporadani sekvenci udalosti v ramci jedne sekundy
                        long time = eventTime.getTime();
                        if (time == lastTime) {
                            citac++;
                            eventTime = new Date(lastTime + citac);
                        } else {
                            lastTime = time;
                            citac = 0;
                        }
                        if (time < minTime) minTime = time;
                        if (time > maxTime) maxTime = time;
                        if (host != null && ALLOWED_TYPES.contains(type)) {
                            this.saveLogEvent(lastStates, host, eventTime, type, operace, value);
                        } else {
                            log.error("cannot save host=" + host + " type=" + type + " for line=" + line);
                        }
                    } catch (ParseException e) {
                        log.error("failed parsing " + cas, e);
                    }
                }
            } else {
                log.warn("line unrecognized: " + line);
            }
        }
        this.logReceivedOutages(server,
                minTime == Long.MAX_VALUE ? null : new Date(minTime),
                maxTime == Long.MIN_VALUE ? null : new Date(maxTime));
        long endTime = System.currentTimeMillis();
        log.info("pbs log events saved in " + (endTime - startTime) + "ms");
    }

    private static final String ZACATEK = "select count(*) from acct_pbs_log_events where acct_host_id=? and event_time=? and type=? ";
    private static final String NN = ZACATEK + "and value is null and operation is null";
    private static final String NV = ZACATEK + "and value is null and operation=?";
    private static final String VN = ZACATEK + "and value=? and operation is null";
    private static final String VV = ZACATEK + "and value=? and operation=?";

    private HashMap<String, Long> hostIdCache = new HashMap<>(500);

    private Long getCachedHostId(String host) {
        Long hostid = hostIdCache.get(host);
        if (hostid == null) {
            hostid = hostManager.getHostId(host);
            hostIdCache.put(host, hostid);
        }
        return hostid;
    }

    /**
     * Uklada udalost z provozniho logu PBS. Zajimaji nas jen nastavovani front a komentaru.
     *
     * @param lastStates mapa z nazvu stroju na posledni zapsany stav stroje
     * @param host       PBS node na kterem doslo k udalosti
     * @param time       cas kdy k ni doslo
     * @param type       [ "queue" | "comment" | "node up" | "node down" ]
     * @param operace    [ "+" | "-" | "=" ]
     * @param value      podle typu bud jmeno fronty, nebo obsah komentare
     */
    private void saveLogEvent(Map<String, String> lastStates, String host, Date time, String type, String operace, String value) {
        //normalizujeme prazdny string na null
        if (value != null && value.isEmpty()) value = null;

        //zkontrolujeme, jestli nejde o nasobny host up nebo host down
        String lastState = lastStates.get(host);
        if(NODE_UP.equals(type)&&NODE_UP.equals(lastState)) return;
        if(NODE_DOWN.equals(type)&&NODE_DOWN.equals(lastState)) return;
        lastStates.put(host, type);

        Long hostId = getCachedHostId(host);
        JdbcTemplate jdbc = getJdbcTemplate();

        //podivame se, jestli uz to mame v databazi, bohuzel porovnavat na null se musi specialne
        int count;
        if (value == null) {
            if (operace == null) {
                count = jdbc.queryForObject(NN, Integer.class, hostId, time, type);
            } else {
                count = jdbc.queryForObject(NV, Integer.class, hostId, time, type, operace);
            }
        } else {
            if (operace == null) {
                count = jdbc.queryForObject(VN, Integer.class, hostId, time, type, value);
            } else {
                count = jdbc.queryForObject(VV, Integer.class, hostId, time, type, value, operace);
            }
        }
        if (count > 0) {
            if (type.startsWith("node")) {
                if (log.isTraceEnabled()) log.trace("event already in database: " + time + "," + host + "," + type);
            } else {
                if (log.isEnabledFor(Level.WARN))
                    log.warn("event already in database: " + time + "," + host + "," + type + "," + value);
            }
        } else {
            jdbc.update("insert into acct_pbs_log_events (acct_host_id,event_time,type,value,operation) values (?,?,?,?,?)",
                    hostId, time, type, value, operace);
            if(log.isTraceEnabled()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                log.trace("inserted event "+hostId+","+sdf.format(time)+","+type+(value!=null?","+value:"")+(operace!=null?","+operace:""));
            }
        }
    }

    @Transactional
    public void computeOutages() {
        JdbcTemplate jdbc = getJdbcTemplate();
        //vymazat vsechny
        long start = System.currentTimeMillis();
        jdbc.update("DELETE FROM acct_outages");
        long end = System.currentTimeMillis();
        log.debug("Deleted all outages in " + (end - start) + " milliseconds.");
        start = System.currentTimeMillis();
        //ziskam vsechny stroje
        List<Long> hostIds = jdbc.queryForList("SELECT acct_host_id FROM acct_host", Long.class);
        for (Long hostId : hostIds) {
            computeOutagesForHost(hostId);
        }
        end = System.currentTimeMillis();
        log.info("Computed all outages in " + (end - start) + " milliseconds.");

    }

    private void logReceivedOutages(String server, Date min, Date max) {
        JdbcTemplate jdbc = getJdbcTemplate();
        Long serverId = pbsServerManager.saveHostname(server).get(server);
        jdbc.update("INSERT INTO acct_receive_outages(receive_time,minimal_time,maximal_time,ci_acct_pbs_server_id) VALUES (?,?,?,?)",
                new Date(), min, max, serverId);
    }


    private void computeOutagesForHost(Long hostId) {
        JdbcTemplate jdbc = getJdbcTemplate();
        //ziskam vsechny udalosti z logu pro dany stroj
        List<PbsLogEvent> events = jdbc.query("SELECT id,event_time,type,value,operation FROM acct_pbs_log_events WHERE acct_host_id=? ORDER BY event_time",
                (rs, rowNum) -> new PbsLogEvent(rs.getInt("id"), rs.getTimestamp("event_time"), rs.getString("type"), rs.getString("value"), rs.getString("operation")), hostId);
        //a po rade zpracuju
        PbsLogEvent actualQueue = null;
        PbsLogEvent actualComment = null;
        PbsLogEvent lastComment = null;
        PbsLogEvent actualDown = null;
        Date lastCommentEnd = null;
        ArrayList<Outage> outages = new ArrayList<>();
        for (PbsLogEvent event : events) {
            if (event.isCommentEvent()) {
                if (event.getOperation() == '+') {
                    //pridat k poslednimu
                    if (actualComment != null) actualComment.addToValue(event.getComment());
                } else {
                    //nastaveni ci smazani
                    if (actualComment != null && actualComment.getComment() != null) {
                        lastComment = actualComment;
                        lastCommentEnd = event.getEventTime();
                    }
                    actualComment = event;
                }
            } else if (event.isQueueEvent()) {
                String newQueue = event.getQueue();
                //byl ve fronte a ted je v jine nebo zadne
                if ((actualQueue != null) && !actualQueue.getQueue().equals(newQueue)) {
                    //zjisti posledni pouzitelny komentar
                    String comment = findComment(event, actualQueue, actualComment, lastComment, lastCommentEnd);
                    //ukoncit predchozi
                    outages.add(new Outage(hostId, actualQueue.getQueue(), actualQueue.getEventTime(), event.getEventTime(), comment));
                }
                if ("maintenance".equals(newQueue) || "reserved".equals(newQueue) || "xentest".equals(newQueue)) {
                    //pokud nas nova fronta zajima
                    if (actualQueue == null || !actualQueue.getQueue().equals(newQueue)) {
                        actualQueue = event;
                    }
                } else {
                    //jinak nic
                    actualQueue = null;
                }
            } else if (event.isNodeDownEvent()) {
                if (actualDown == null) actualDown = event;
            } else if (event.isNodeUpEvent()) {
                //ukoncit vypadek
                if (actualDown != null) {
                    //pokud byl kratsi nez nejaka minimalni doba, nema smysl zaznamenavat
                    Date zacatek = actualDown.getEventTime();
                    Date konec = event.getEventTime();
                    long delka = konec.getTime() - zacatek.getTime();
                    if (delka > 30 * 60 * 1000) {
                        delka = delka / 1000;
                        long dnu = delka / (24 * 3600);
                        delka = delka % (24 * 3600);
                        long hodin = delka / 3600;
                        delka = delka % (3600);
                        long minut = delka / 60;
                        long sekund = delka % 60;
                        String k = String.format("doba %dd %02d:%02d:%02d", dnu, hodin, minut, sekund);
                        outages.add(new Outage(hostId, NODE_DOWN, zacatek, konec, k));
                    }
                    actualDown = null;
                }
            } else {
                log.error("unknown event type: " + event.getType());
            }
        }
        if (actualQueue != null) {
            //neskoncilo to
            String comment = findComment(null, actualQueue, actualComment, lastComment, lastCommentEnd);
            outages.add(new Outage(hostId, actualQueue.getQueue(), actualQueue.getEventTime(), null, comment));
        }
        if (actualDown != null) {
            outages.add(new Outage(hostId, NODE_DOWN, actualDown.getEventTime(), null, null));
        }
        saveOutages(outages);
    }

    private String findComment(PbsLogEvent event, PbsLogEvent actualQueue, PbsLogEvent actualComment, PbsLogEvent lastComment, Date lastCommentEnd) {
        String comment;
        boolean commentOldEnough;
        //noinspection SimplifiableIfStatement
        if (event == null) {
            commentOldEnough = true;
        } else {
            commentOldEnough = (actualComment != null
                    && actualComment.getEventTime().before(new Date(event.getEventTime().getTime() - 600000l)));
        }
        if (actualComment != null && actualComment.getComment() != null
                && commentOldEnough) {
            //v okamziku ukonceni byl platny komentar starsi nez 10 minut
            comment = actualComment.getComment();
        } else if (lastComment != null && lastComment.getComment() != null
                && lastCommentEnd.after(actualQueue.getEventTime())) {
            //v okamziku ukonceni nebyl platny komentar, ale byl predtim
            comment = lastComment.getComment();
        } else {
            //nebyl zadny komentar
            comment = "-";
        }
        return comment;
    }

    private static class Outage {
        Long hostId;
        String queue;
        Date start;
        Date end;
        String comment;

        private Outage(Long hostId, String queue, Date start, Date end, String comment) {
            this.hostId = hostId;
            this.queue = queue;
            this.start = start;
            this.end = end;
            this.comment = comment;
        }

        public Long getHostId() {
            return hostId;
        }

        public String getQueue() {
            return queue;
        }

        public Date getStart() {
            return start;
        }

        public Date getEnd() {
            return end;
        }

        public String getComment() {
            return comment;
        }
    }

    private void saveOutages(final List<Outage> outages) {
        List<Object[]> batch = new ArrayList<>();
        for (Outage outage : outages) {
            Object[] values = new Object[]{
                    outage.getHostId(), outage.getQueue(), outage.getStart(), outage.getEnd(),
                    "xentest".equals(outage.getQueue()) ? "" : outage.getComment()};
            batch.add(values);

        }
        getJdbcTemplate().batchUpdate(
                "INSERT INTO acct_outages (acct_host_id,type,start_time,end_time,comment) VALUES (?,?,?,?,?)",
                batch);
    }

    public class PbsLogEvent {
        int id;
        Date eventTime;
        String type; //queue | comment
        String value;
        char operation; //u komentaru ma smysl pridavat, u queue je mozne ignorovat

        public PbsLogEvent(int id, Date eventTime, String type, String value, String operace) {
            this.id = id;
            this.eventTime = eventTime;
            this.type = type;
            this.value = value;
            this.operation = operace != null ? operace.charAt(0) : 0;
        }

        public int getId() {
            return id;
        }

        public Date getEventTime() {
            return eventTime;
        }

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

        public String getQueue() {
            return value;
        }

        public String getComment() {
            return value;
        }

        public void addToValue(String s) {
            value += s;
        }

        public char getOperation() {
            return operation;
        }

        public boolean isQueueEvent() {
            return "queue".equals(type);
        }

        @SuppressWarnings("UnusedDeclaration")
        public boolean isReservedEvent() {
            return isQueueEvent() && "reserved".equals(value);
        }

        @SuppressWarnings("UnusedDeclaration")
        public boolean isMaintenanceEvent() {
            return isQueueEvent() && "maintenance".equals(value);
        }

        public boolean isCommentEvent() {
            return "comment".equals(type);
        }

        public boolean isNodeDownEvent() {
            return NODE_DOWN.equals(type);
        }

        public boolean isNodeUpEvent() {
            return NODE_UP.equals(type);
        }

        @Override
        public String toString() {
            return "PbsLogEvent{" +
                    "id=" + id +
                    ", eventTime=" + eventTime +
                    ", type='" + type + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }
}
