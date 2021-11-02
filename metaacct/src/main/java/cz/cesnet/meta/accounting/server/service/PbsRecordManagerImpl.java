package cz.cesnet.meta.accounting.server.service;

import cz.cesnet.meta.accounting.server.data.*;
import cz.cesnet.meta.accounting.server.util.Page;
import cz.cesnet.meta.accounting.util.AcctCal;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("Convert2streamapi")
public class PbsRecordManagerImpl extends JdbcDaoSupport implements PbsRecordManager {

    final static Logger log = LoggerFactory.getLogger(PbsRecordManagerImpl.class);

    @Autowired
    PbsServerManager pbsServerManager;
    @Autowired
    HostManager hostManager;
    @Autowired
    UserManager userManager;
    @Autowired
    DbUtilsManager dbUtilsManager;

    private Map<String, Long> getAllUserIds(List<PBSRecord> records) {
        Set<String> usernames = new HashSet<>();
        for (PBSRecord r : records) {
            if (r.getMessageText().getUser() != null) {
                usernames.add(r.getMessageText().getUser());
            }
        }
        log.debug("read users #: " + usernames.size());
        return userManager.saveUsernames(usernames);
    }

    @SuppressWarnings("Convert2streamapi")
    private Map<String, Long> getAllHostIds(List<PBSRecord> records) {
        Set<String> hostnames = new HashSet<>();
        for (PBSRecord r : records) {

            for (PBSHost h : r.getMessageText().getExecHosts()) {
                hostnames.add(h.getHostName());
            }
            List<NodeSpec> nodesSpecs = r.getMessageText().getNodesSpecs();
            if(nodesSpecs!=null) {
                for (NodeSpec ns : nodesSpecs) {
                    hostnames.add(ns.getHostname());
                }
            }
        }
        log.debug("hostnames #: " + hostnames.size());
        return hostManager.saveHostnames(hostnames);
    }

    private static final SingleColumnRowMapper<Long> LONG_WRAPPER = SingleColumnRowMapper.newInstance(Long.class);

    @Transactional
    public List<String> saveRecords(final List<PBSRecord> records, final String hostname) {
        long start = System.currentTimeMillis();
        final List<String> unsaved = new ArrayList<>();

        Map<String, Long> allUsers = getAllUserIds(records);
        Map<String, Long> allHosts = getAllHostIds(records);
        Map<String, Long> allServers = pbsServerManager.saveHostname(hostname);
        log.debug("all users #: " + allUsers.size());

        Long minimumTime = null;
        Long maximumTime = null;
        DataSource dataSource = getDataSource();
        SimpleJdbcInsert insertEndPbsRecord = new SimpleJdbcInsert(dataSource)
                .withTableName("acct_pbs_record")
                .usingColumns("acct_id_string", "date_time", "jobname", "queue",
                        "create_time", "start_time", "end_time", "exit_status", "acct_user_id", "ci_acct_pbs_server_id",
                        "req_ncpus", "req_nodes", "req_nodect", "req_mem", "req_walltime", "soft_walltime",
                        "used_ncpus", "used_mem", "used_vmem", "used_walltime", "used_cputime", "used_cpupercent", "req_gpus");

        SimpleJdbcInsert insertStartPbsRecord = new SimpleJdbcInsert(dataSource)
                .withTableName("acct_pbs_record_started")
                .usingColumns("acct_id_string", "date_time", "jobname", "queue",
                        "create_time", "start_time", "acct_user_id", "ci_acct_pbs_server_id",
                        "req_ncpus", "req_nodes", "req_nodect", "req_mem", "req_walltime", "soft_walltime", "req_gpus");

        SimpleJdbcInsert insertPbsRecordHostRelation = new SimpleJdbcInsert(dataSource)
                .withTableName("acct_hosts_logs")
                .usingColumns("acct_id_string", "acct_host_id", "cpu_number");

        SimpleJdbcInsert insertStartPbsRecordHostRelation = new SimpleJdbcInsert(dataSource)
                .withTableName("acct_hosts_logs_started")
                .usingColumns("acct_id_string", "acct_host_id", "cpu_number");

        SimpleJdbcInsert insertHostsUsed = new SimpleJdbcInsert(dataSource)
                .withTableName("acct_hosts_used")
                .usingColumns("acct_id_string", "acct_host_id", "ppn", "gpu", "mem", "scratchtype", "scratch");
        SimpleJdbcInsert insertHostsUsedStarted = new SimpleJdbcInsert(dataSource)
                .withTableName("acct_hosts_used_started")
                .usingColumns("acct_id_string", "acct_host_id", "ppn", "gpu", "mem", "scratchtype", "scratch");


        JdbcTemplate jdbc = getJdbcTemplate();

        /*
         * U udalosti starsich 30 dnu se hlasi jenom udalosti E a G, ostatni je zbytecne zpracovavat.
         * U mladsich mohou ulohy jeste bezet, takze se zaznamenavaji S,E,G,R,D,A.
         * Obvykla sekvence je S,E, pripadne S,A, S,D a S,D,G ale vyskytuji se i komplikovane pripady jako S,R,S,E
         * nebo S,G,G,E a S,E,D.  Je nutne pamatovat si jen nejnovejsi, aby slo vkladat zaznamy zpetne.
         */
        for (PBSRecord r : records) {
            //zjistit jestli uz je v E
            List<Long> E_times = jdbc.query("SELECT date_time FROM acct_pbs_record WHERE acct_id_string = ? ORDER BY date_time DESC", LONG_WRAPPER, r.getIdString());
            if(log.isDebugEnabled()) log.debug("processing {}",r);
            switch (r.getRecordType()) {
                case ENDED:
                case G:
                    //konec jobu, smazat z S a D
                    jdbc.update("DELETE FROM acct_pbs_record_started WHERE acct_id_string = ? ", r.getIdString());
                    jdbc.update("DELETE FROM acct_pbs_record_deleted WHERE acct_id_string = ? ", r.getIdString());
                    if (E_times.isEmpty()) {
                        //prvni zaznam
                        if (log.isTraceEnabled()) log.trace("inserting " + r.getRecordType() + " " + r);
                        insertEndPbsRecord.execute(prepareJobParams(allUsers, allServers, r, hostname));
                        insertJob2HostCPURelation(allHosts, insertPbsRecordHostRelation, insertHostsUsed, r);
                    } else if (E_times.get(0) < r.getDateTime()) {
                        //je to novejsi zaznam, nahradit
                        if (log.isDebugEnabled()) log.debug("replacing E/G with newer " + r);
                        jdbc.update("DELETE FROM acct_hosts_logs WHERE acct_id_string = ? ", r.getIdString());
                        jdbc.update("DELETE FROM acct_pbs_record WHERE acct_id_string = ? ", r.getIdString());
                        insertEndPbsRecord.execute(prepareJobParams(allUsers, allServers, r, hostname));
                        insertJob2HostCPURelation(allHosts, insertPbsRecordHostRelation, insertHostsUsed, r);
                    } else {
                        //uz mame novejsi udaje, nepouzit
                        if (log.isDebugEnabled()) log.debug("older E/G for " + r);
                        unsaved.add("older E/G for " + r.getRecordType().type() + " " + r.getIdString());
                    }
                    break;
                case STARTED:
                case ABORTED:
                case DELETED:
                case RERUN:

                    if (!E_times.isEmpty()) {
                        //uz je znam konec, nepouzit
                        if (log.isDebugEnabled()) log.debug("already newer E for  " + r);
                        unsaved.add(r.getRecordType().type() + " after E " + r.getIdString());
                    } else {
                        List<Long> S_times = jdbc.query("SELECT date_time FROM acct_pbs_record_started WHERE acct_id_string = ? ORDER BY date_time DESC", LONG_WRAPPER, r.getIdString());
                        List<Long> D_times = jdbc.query("SELECT date_time FROM acct_pbs_record_deleted WHERE acct_id_string = ? ORDER BY date_time DESC", LONG_WRAPPER, r.getIdString());
                        Long last = null;
                        if (!S_times.isEmpty()) {
                            last = S_times.get(0);
                            if(log.isTraceEnabled()) log.trace("S exists since {}",new Date(last));
                        }
                        if (!D_times.isEmpty()) {
                            last = D_times.get(0);
                            if(log.isTraceEnabled()) log.trace("D exists since {}",new Date(last));
                        }
                        if (last != null) {
                            //uz existuje zaznam v started nebo deleted
                            if (last >= r.getDateTime()) {
                                //uz mame novejsi udaje, nepouzit
                                if (log.isDebugEnabled()) log.debug("older S/D exists for " + r.getRecordType().type() + " " + r);
                                unsaved.add("older S/D exists for " + r.getRecordType().type() + " " + r.getIdString());
                            } else {
                                //je novejsi, nahradit
                                if (!S_times.isEmpty()) {
                                    jdbc.update("DELETE FROM acct_hosts_logs_started WHERE acct_id_string = ? ", r.getIdString());
                                    jdbc.update("DELETE FROM acct_pbs_record_started WHERE acct_id_string = ? ", r.getIdString());
                                }
                                if (!D_times.isEmpty()) {
                                    jdbc.update("DELETE FROM acct_pbs_record_deleted WHERE acct_id_string = ? ", r.getIdString());
                                }
                                if (r.getRecordType() == PBSRecordType.STARTED) {
                                    if (log.isTraceEnabled()) log.trace("inserting S " + r);
                                    insertStartPbsRecord.execute(prepareJobParams(allUsers, allServers, r, hostname));
                                    insertJob2HostCPURelation(allHosts, insertStartPbsRecordHostRelation, insertHostsUsedStarted,r);
                                } else {
                                    if (log.isTraceEnabled()) log.trace("inserting D: " + r);
                                    jdbc.update("INSERT INTO acct_pbs_record_deleted (acct_id_string,date_time) VALUES (?,?)", r.getIdString(), r.getDateTime());
                                }
                            }
                        } else {
                            //prvni zaznam
                            if (r.getRecordType() == PBSRecordType.STARTED) {
                                if (log.isTraceEnabled()) log.trace("inserting S " + r);
                                insertStartPbsRecord.execute(prepareJobParams(allUsers, allServers, r, hostname));
                                insertJob2HostCPURelation(allHosts, insertStartPbsRecordHostRelation, insertHostsUsedStarted, r);
                            } else {
                                if (log.isTraceEnabled()) log.trace("inserting D: " + r);
                                jdbc.update("INSERT INTO acct_pbs_record_deleted (acct_id_string,date_time) VALUES (?,?)", r.getIdString(), r.getDateTime());
                            }
                        }
                    }
                    break;
                default:
                    log.error("unknown job type: " + r);
            }
            //ponamenat cas
            long dateTime = r.getDateTime();
            if (minimumTime == null || dateTime < minimumTime) minimumTime = dateTime;
            if (maximumTime == null || dateTime > maximumTime) maximumTime = dateTime;
        }

        //uklid starych zaznamu
        long limit = System.currentTimeMillis() - 720L*3600_000L;

        jdbc.update("DELETE FROM acct_pbs_record_started WHERE date_time < ?", limit);
        jdbc.update("DELETE FROM acct_pbs_record_deleted WHERE date_time < ?", limit);

        insertReceiveLogRecord(hostname, allServers, minimumTime, maximumTime);
        if (log.isInfoEnabled()) {
            long end = System.currentTimeMillis();

            log.info("saved " + (records.size() - unsaved.size()) + " pbs records, unsaved " + unsaved.size() + " records from " + hostname + " in " + ((end - start) / 1000) + "s");
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            log.info("times: " + (minimumTime != null ? sf.format(new Date(minimumTime)) : "nic")
                    + " --- " + (maximumTime != null ? sf.format(new Date(maximumTime)) : "nic"));

        }
        return unsaved;
    }

    private void insertReceiveLogRecord(String hostname, Map<String, Long> allServers, Long minimumTime, Long maximumTime) {
        SimpleJdbcInsert insertPbsReceiveLog = new SimpleJdbcInsert(getDataSource())
                .withTableName("acct_receive_pbs")
                .usingColumns("acct_receive_pbs_id", "receive_time", "minimal_time", "maximal_time", "ci_acct_pbs_server_id");
        Map<String, Object> params = new HashMap<>(1);
        params.put("acct_receive_pbs_id", dbUtilsManager.getNextVal("acct_receive_pbs", "acct_receive_pbs_id"));
        params.put("receive_time", new Timestamp(new Date().getTime()));
        params.put("minimal_time", minimumTime);
        params.put("maximal_time", maximumTime);
        params.put("ci_acct_pbs_server_id", allServers.get(hostname));
        insertPbsReceiveLog.execute(params);
    }

    private static void insertJob2HostCPURelation(Map<String, Long> allHosts, SimpleJdbcInsert insertPbsRecordHostRelation, SimpleJdbcInsert insertHostsUsed, PBSRecord r) {
        for (PBSHost h : r.getMessageText().getExecHosts()) {
            Map<String, Object> paramsRelation = new HashMap<>(1);
            paramsRelation.put("acct_id_string", r.getIdString());
            paramsRelation.put("acct_host_id", allHosts.get(h.getHostName()));
            paramsRelation.put("cpu_number", h.getProcessorNumber());
            insertPbsRecordHostRelation.execute(paramsRelation);
        }
        List<NodeSpec> nodesSpecs = r.getMessageText().getNodesSpecs();
        if(nodesSpecs!=null) {
            for (NodeSpec ns : nodesSpecs) {
                Map<String, Object> params = new HashMap<>(1);
                params.put("acct_id_string", r.getIdString());
                params.put("acct_host_id", allHosts.get(ns.getHostname()));
                params.put("ppn", ns.getPpn());
                params.put("gpu", ns.getGpu());
                params.put("mem", ns.getMem());
                params.put("scratchtype", ns.getScratchType());
                params.put("scratch", ns.getScratchVolume());
                insertHostsUsed.execute(params);
            }
        }
    }

    private static Map<String, Object> prepareJobParams(Map<String, Long> allUsers, Map<String, Long> allServers, PBSRecord r, String hostname) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("acct_id_string", r.getIdString());
        params.put("date_time", r.getDateTime());
        params.put("jobname", r.getMessageText().getJobname());
        params.put("queue", r.getMessageText().getQueue());
        params.put("create_time", r.getMessageText().getCreateTime());
        params.put("start_time", r.getMessageText().getStartTime());
        params.put("acct_user_id", allUsers.get(r.getMessageText().getUser()));
        params.put("ci_acct_pbs_server_id", allServers.get(hostname));
        params.put("req_ncpus", r.getMessageText().getReqNcpus());
        params.put("req_gpus", r.getMessageText().getReqGpus());
        params.put("req_nodes", r.getMessageText().getReqNodes());
        params.put("req_nodect", r.getMessageText().getReqNodect());
        params.put("req_mem", r.getMessageText().getReqMem());
        params.put("req_walltime", r.getMessageText().getReqWalltime());
        params.put("soft_walltime", r.getMessageText().getSoftWalltime());
        if (PBSRecordType.ENDED.equals(r.getRecordType())) {
            params.put("end_time", r.getMessageText().getEndTime());
            params.put("exit_status", r.getMessageText().getExitStatus());
            params.put("used_ncpus", r.getMessageText().getUsedNcpus());
            params.put("used_mem", r.getMessageText().getUsedMem());
            params.put("used_vmem", r.getMessageText().getUsedVmem());
            params.put("used_walltime", r.getMessageText().getUsedWalltime());
            params.put("used_cputime", r.getMessageText().getUsedCputime());
            params.put("used_cpupercent", r.getMessageText().getUsedCpupercent());
        }
        return params;
    }

    /**
     * nacte vsechny pbs zaznamy a vypocita pro ne celkovy user a system time
     *
     * @return list pbs zaznamu
     */
    @Override
    public Page getAllPbsRecords(Map<String, Object> criteria, Integer pageNumber, Integer defaultPageSize,
                                 Integer pageSize, String sortColumn, Boolean ascending) {
        log.debug("loading all pbsrecords with criteria ");
        if (pageSize == null) {
            pageSize = defaultPageSize;
        }
        if (sortColumn == null) {
            ascending = false;
            sortColumn = "endTime";
        }

        StringBuilder where = new StringBuilder();
        if (criteria != null && !criteria.isEmpty()) {

            if (criteria.get("idString") != null) {
                where.append(" and p.acct_id_string like '").append(criteria.get("idString")).append("' ");
            }
            if (criteria.get("jobname") != null) {
                where.append(" and p.jobname like '").append(criteria.get("jobname")).append("' ");
            }
            if (criteria.get("username") != null) {
                where.append(" and u.user_name like '").append(criteria.get("username")).append("' ");
            }
            if (criteria.get("dateTimeFrom") != null) {
                where.append(" and p.date_time >= ").append(((Date) criteria.get("dateTimeFrom")).getTime()).append(" ");
            }
            if (criteria.get("dateTimeTo") != null) {
                where.append(" and p.date_time <= ").append(((Date) criteria.get("dateTimeTo")).getTime()).append(" ");
            }
            if (criteria.get("startTimeFrom") != null) {
                where.append(" and p.start_time >= ").append(((Date) criteria.get("startTimeFrom")).getTime() / 1000).append(" ");
            }
            if (criteria.get("startTimeTo") != null) {
                where.append(" and p.start_time <= ").append(((Date) criteria.get("startTimeTo")).getTime() / 1000).append(" ");
            }
            if (criteria.get("cpusFrom") != null) {
                where.append(" and p.used_ncpus >= ").append(criteria.get("cpusFrom")).append(" ");
            }
            if (criteria.get("cpusTo") != null) {
                where.append(" and p.used_ncpus <= ").append(criteria.get("cpusTo")).append(" ");
            }
            if (criteria.get("walltimeFrom") != null) {
                where.append(" and p.used_walltime >= ").append(time2sec((String) criteria.get("walltimeFrom"))).append(" ");
            }
            if (criteria.get("walltimeTo") != null) {
                where.append(" and p.used_walltime <= ").append(time2sec((String) criteria.get("walltimeTo"))).append(" ");
            }
            if (log.isDebugEnabled()) log.debug("where: " + where);
        }

        Integer offset = (pageNumber - 1) * pageSize;
        Page page = new Page();
        page.setPageSize(pageSize);
        page.setPageNumber(pageNumber);
        page.setSortColumn(sortColumn);
        page.setFullListSize(getJdbcTemplate().queryForObject(
                "select count(*) from acct_pbs_record p natural left join ci_acct_pbs_server s natural left join acct_user u " +
                        " where 1=1 " +
                        where.toString(), Integer.class
        ));
        page.setAscending(ascending);


        List<Map<String, Object>> res = getJdbcTemplate().queryForList(
                "select p.acct_id_string as idString, p.date_time as dateTime, p.jobname as jobname, p.queue as queue, p.acct_user_id as userId, u.user_name as username, " +
                        "       p.used_ncpus as cpus, " +
                        "       p.create_time as createTime, p.start_time as startTime, p.end_time as endTime, p.exit_status as exitStatus, p.used_walltime as walltime, s.hostname as serverHostname" +
                        " from acct_pbs_record p natural left join ci_acct_pbs_server s natural left join acct_user u " +
                        " where 1=1 " +
                        where.toString() +
                        " order by " + sortColumn + " " + (ascending ? "ASC" : "DESC") +
                        " limit " + pageSize + " offset " + offset + " "
        );
        List<PbsRecordData> pbsRecords = processRecords(res);

        log.debug("Loaded " + pbsRecords.size() + " pbs records.");

        page.setList(pbsRecords);
        return page;
    }


    /**
     * nacte pbs zaznamy pro daneho uzivatele a vypocita pro ne celkovy user a system time
     *
     * @return list pbs zaznamu
     */
    public Page getPbsRecordsForUserId(long userId, Map<String, Object> criteria, Integer pageNumber, Integer defaultPageSize, Integer pageSize, String sortColumn, Boolean ascending) {
        log.debug("loading pbsrecords for userId " + userId);
        if (log.isDebugEnabled()) {
            log.debug("criteria: " + criteria);
        }
        if (pageSize == null) {
            pageSize = defaultPageSize;
        }
        if (sortColumn == null) {
            ascending = false;
            sortColumn = "endTime";
        }

        StringBuilder where = new StringBuilder();
        if (criteria != null && !criteria.isEmpty()) {

            if (criteria.get("idString") != null) {
                where.append(" and p.acct_id_string like '").append((String) criteria.get("idString")).append("' ");
            }
            if (criteria.get("jobname") != null) {
                where.append(" and p.jobname like '").append((String) criteria.get("jobname")).append("' ");
            }
            if (criteria.get("username") != null) {
                where.append(" and u.user_name like '").append((String) criteria.get("username")).append("' ");
            }
            if (criteria.get("dateTimeFrom") != null) {
                where.append(" and p.date_time >= ").append(((Date) criteria.get("dateTimeFrom")).getTime()).append(" ");
            }
            if (criteria.get("dateTimeTo") != null) {
                where.append(" and p.date_time <= ").append(((Date) criteria.get("dateTimeTo")).getTime()).append(" ");
            }
            if (criteria.get("startTimeFrom") != null) {
                where.append(" and p.start_time >= ").append(((Date) criteria.get("startTimeFrom")).getTime() / 1000).append(" ");
            }
            if (criteria.get("startTimeTo") != null) {
                where.append(" and p.start_time <= ").append(((Date) criteria.get("startTimeTo")).getTime() / 1000).append(" ");
            }
            if (criteria.get("cpusFrom") != null) {
                where.append(" and p.used_ncpus >= ").append(criteria.get("cpusFrom")).append(" ");
            }
            if (criteria.get("cpusTo") != null) {
                where.append(" and p.used_ncpus <= ").append(criteria.get("cpusTo")).append(" ");
            }
            if (criteria.get("walltimeFrom") != null) {
                where.append(" and p.used_walltime >= ").append(time2sec((String) criteria.get("walltimeFrom"))).append(" ");
            }
            if (criteria.get("walltimeTo") != null) {
                where.append(" and p.used_walltime <= ").append(time2sec((String) criteria.get("walltimeTo"))).append(" ");
            }
            if (log.isDebugEnabled()) log.debug("where: " + where);
        }

        Integer offset = (pageNumber - 1) * pageSize;
        Page page = new Page();
        page.setPageSize(pageSize);
        page.setPageNumber(pageNumber);
        page.setSortColumn(sortColumn);
        page.setFullListSize(getJdbcTemplate().queryForObject(
                "select count(*) from acct_pbs_record p natural left join ci_acct_pbs_server s" +
                        " where 1=1 " + //date_time between " + from + " and " + to +" " +
                        "   and acct_user_id = " + userId + " " +
                        where.toString(), Integer.class
        ));
        page.setAscending(ascending);

        List<Map<String, Object>> res = getJdbcTemplate().queryForList(
                "select p.acct_id_string as idString, p.date_time as dateTime, p.jobname as jobname, p.queue as queue, p.acct_user_id as userId, " +
                        "       p.used_ncpus as cpus, " +
                        "       p.create_time as createTime, p.start_time as startTime, p.end_time as endTime, p.exit_status as exitStatus, p.used_walltime as walltime, s.hostname as serverHostname" +
                        " from acct_pbs_record p natural left join ci_acct_pbs_server s " +
                        " where 1=1 " + //p.date_time between " + from + " and " + to +
                        "   and p.acct_user_id = " + userId +
                        where.toString() +
                        " order by " + sortColumn + " " + (ascending ? "ASC" : "DESC") +
                        " limit " + pageSize + " offset " + offset + " "
        );

        List<PbsRecordData> pbsRecords = processRecords(res);

        log.debug("Loaded " + pbsRecords.size() + " pbs records.");

        page.setList(pbsRecords);
        return page;
    }

    private static long time2sec(String time) {
        Matcher m = Pattern.compile("(\\d+):(\\d\\d):(\\d\\d)").matcher(time);
        return m.matches() ? Long.parseLong(m.group(1)) * 3600l + Long.parseLong(m.group(2)) * 60l + Long.parseLong(m.group(3)) : 0l;
    }

    private List<PbsRecordData> processRecords(List<Map<String, Object>> res) {
        List<PbsRecordData> pbsRecords = new ArrayList<>();
        for (Map<String, Object> item : res) {
            long dateTimeMillis = (Long) item.get("dateTime");
            String jobname = (String) item.get("jobname");
            String queue = (String) item.get("queue");
            long createTimeSeconds = (Long) item.get("createTime");
            long startTimeSeconds = (Long) item.get("startTime");
            long endTimeSeconds = (Long) item.get("endTime");
            int cpus = (Integer) item.get("cpus");
            String username = (String) item.get("username");
            PbsRecordData pbsRecord = new PbsRecordData((String) item.get("idString"),
                    new Date(dateTimeMillis),
                    jobname,
                    queue,
                    new Date(createTimeSeconds * 1000),
                    new Date(startTimeSeconds * 1000),
                    new Date(endTimeSeconds * 1000),
                    (Integer) item.get("exitStatus"));
            pbsRecord.setServerHostname((String) item.get("serverHostname"));
            pbsRecord.setUsername(username);
            pbsRecord.setWalltime((Long) item.get("walltime") * 100);
            pbsRecord.setUsedNcpus(cpus);
            pbsRecord.setExecHosts(hostManager.getHostsForPbsId(pbsRecord.getIdString()));
//            List<KernelRecord> kernelRecords = kernelRecordManager.getRecordsForUserForHostsFromTo(userId,
//                    pbsRecord.getExecHosts(), startTimeSeconds, endTimeSeconds);

            long totalUserTimeHundreths = 0;
            long totalSystemTimeHundreths = 0;
//            for (KernelRecord k : kernelRecords) {
//                totalUserTimeHundreths += k.getUserTime();
//                totalSystemTimeHundreths += k.getSystemTime();
//            }
//            pbsRecord.setKernelRecords(kernelRecords);
            pbsRecord.setKernelRecords(Collections.emptyList());
            pbsRecord.setTotalUserTime(totalUserTimeHundreths);
            pbsRecord.setTotalSystemTime(totalSystemTimeHundreths);
            pbsRecord.setConflict(existConflict(pbsRecord.getIdString(), pbsRecord.getExecHosts(),
                    pbsRecord.getCreateTime(), pbsRecord.getDateTime()));
            pbsRecords.add(pbsRecord);
        }
        return pbsRecords;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private boolean existConflict(String idString, List<PBSHost> hosts, Date fromDate, Date toDate) {
        /*long to = toDate.getTime();
        long from = fromDate.getTime();
        long res = getSimpleJdbcTemplate().queryForLong(
            "select count(distinct p.acct_id_string) " +
            " from acct_pbs_record p, acct_hosts_logs l " +
            " where p.create_time <= " + to + " and p.date_time >= " + from +
            "   and l.acct_id_string = p.acct_id_string " +
            "   and l.acct_host_id in (" + StringUtils.hostIdsToString(hosts) + ") " +
            "   and p.acct_id_string not like '" + idString + "' ");
        if (res > 0) {
          return true;
        }*/
        // jestli existuje nejaky job, ktery koliduje s jobem danym idStringem od do
        return false;
    }

    @Override
    public Page getAllPbsRecords(int number, Integer pageNumber, Integer defaultPageSize, Integer pageSize,
                                 String sortColumn, Boolean ascending) {
        log.debug("loading all pbsrecords ");

        if (pageSize == null) {
            pageSize = defaultPageSize;
        }
        if (sortColumn == null) {
            ascending = false;
            sortColumn = "endTime";
        }
        Integer offset = (pageNumber - 1) * pageSize;
        Page page = new Page();
        page.setPageSize(pageSize);
        page.setPageNumber(pageNumber);
        page.setSortColumn(sortColumn);
        page.setFullListSize((int) getSizeForAllPbs(number));
        page.setAscending(ascending);

        int kolik = pageSize;
        if (offset <= page.getFullListSize()) {
            kolik = (offset + kolik) > page.getFullListSize() ? page.getFullListSize() - offset : kolik;
        }
        List<Map<String, Object>> res = getJdbcTemplate().queryForList(
                "select p.acct_id_string as idString, p.date_time as dateTime, p.jobname as jobname, p.queue as queue, p.acct_user_id as userId, u.user_name as username, " +
                        "       p.used_ncpus as cpus, " +
                        "       p.create_time as createTime, p.start_time as startTime, p.end_time as endTime, p.exit_status as exitStatus, p.used_walltime as walltime, s.hostname as serverHostname" +
                        " from acct_pbs_record p natural left join ci_acct_pbs_server s natural left join acct_user u " +
                        " order by " + sortColumn + " " + (ascending ? "ASC" : "DESC") +
                        " limit " + kolik + " offset " + offset + " "
        );

        List<PbsRecordData> pbsRecords = processRecords(res);

        log.debug("Loaded " + pbsRecords.size() + " pbs records.");

        page.setList(pbsRecords);
        return page;

    }

    public Page getPbsRecordsForUserId(long userId, int number, Integer pageNumber, Integer defaultPageSize, Integer pageSize, String sortColumn, Boolean ascending) {
        log.debug("loading pbsrecords for userId " + userId);
        if (pageSize == null) {
            pageSize = defaultPageSize;
        }
        //FIX for SQL injection attack
        if (sortColumn == null || !sortColumn.matches("(idString|jobname|queue|cpus|createTime|startTime|endTime|walltime|serverHostname)")) {
            ascending = false;
            sortColumn = "endTime";
        }
        Integer offset = (pageNumber - 1) * pageSize;
        Page page = new Page();
        page.setPageSize(pageSize);
        page.setPageNumber(pageNumber);
        page.setSortColumn(sortColumn);
        page.setFullListSize((int) getSizeForUserId(userId, number));
        page.setAscending(ascending);

        int kolik = pageSize;
        if (offset <= page.getFullListSize()) {
            kolik = (offset + kolik) > page.getFullListSize() ? page.getFullListSize() - offset : kolik;
        }
        List<Map<String, Object>> res = getJdbcTemplate().queryForList(
                "select p.acct_id_string as idString, p.date_time as dateTime, p.jobname as jobname, p.queue as queue, " +
                        "p.acct_user_id as userId, p.used_ncpus as cpus, p.create_time as createTime, " +
                        "p.start_time as startTime, p.end_time as endTime, p.exit_status as exitStatus, " +
                        "p.used_walltime as walltime, s.hostname as serverHostname" +
                        " from acct_pbs_record p natural left join ci_acct_pbs_server s " +
                        "   where p.acct_user_id = " + userId +
                        " order by " + sortColumn + " " + (ascending ? "ASC" : "DESC") +
                        " limit " + kolik + " offset " + offset + " "
        );

        List<PbsRecordData> pbsRecords = processRecords(res);

        if (log.isDebugEnabled()) log.debug("Loaded " + pbsRecords.size() + " pbs records.");

        page.setList(pbsRecords);
        return page;
    }


    private long getSizeForAllPbs(int number) {
        long size = getJdbcTemplate().queryForObject("SELECT count(*) FROM acct_pbs_record ", Long.class);
        size = size > number ? number : size;
        log.debug("size of all pbs records: " + size);
        return size;
    }

    private long getSizeForUserId(long userId, int number) {
        long size = getJdbcTemplate().queryForObject(
                "select count(*) from acct_pbs_record where acct_user_id = " + userId + " ", Long.class);
        size = size > number ? number : size;
        log.debug("size of pbs records for userId " + userId + ": " + size);
        return size;
    }

    /**
     * @param id pbs zaznamu
     * @return cely pbs zaznam i se spocitanym celkovym user a system timem a vsemi kernel logy pro dany zaznam
     */
    public PbsRecordData getPbsRecordForIdString(String id) {
        log.debug("loading pbsrecord for idString " + id);

        List<PbsRecordData> l = getJdbcTemplate().query(
                "SELECT p.acct_id_string, p.date_time, p.jobname, p.queue, " +
                        " p.req_nodes, p.used_mem, p.used_ncpus, p.used_walltime, p.used_cputime, p.req_walltime,  " +
                        " p.create_time, p.start_time, p.end_time, p.exit_status, p.acct_user_id " +
                        " FROM acct_pbs_record p " +
                        " WHERE p.acct_id_string = ?",
                (rs, rowNum) -> new PbsRecordData(
                        rs.getString("acct_id_string"),
                        new Date(rs.getLong("date_time")),
                        rs.getString("jobname"),
                        rs.getString("queue"),
                        rs.getString("req_nodes"),
                        rs.getLong("used_mem"),
                        rs.getInt("used_ncpus"),
                        rs.getLong("used_walltime"),
                        rs.getLong("used_cputime"),
                        rs.getLong("req_walltime"),
                        new Date(rs.getLong("create_time") * 1000),
                        new Date(rs.getLong("start_time") * 1000),
                        new Date(rs.getLong("end_time") * 1000),
                        rs.getInt("exit_status"),
                        rs.getLong("acct_user_id")
                ),
                id
        );
        if (l.isEmpty())
            return new PbsRecordData(id, null, "K uloze bohuzel nejsou informace", null, null, null, null, 0);
        PbsRecordData pbsRecord = l.get(0);

        pbsRecord.setExecHosts(hostManager.getHostsForPbsId(pbsRecord.getIdString()));
//        pbsRecord.setKernelRecords(kernelRecordManager.getRecordsForUserForHostsFromTo(pbsRecord.getUserId(),
//                pbsRecord.getExecHosts(), pbsRecord.getStartTime().getTime()/1000, pbsRecord.getEndTime().getTime()/1000));

        long totalUserTime = 0;
        long totalSystemTime = 0;
        for (KernelRecord k : pbsRecord.getKernelRecords()) {
            totalUserTime += k.getUserTime();
            totalSystemTime += k.getSystemTime();
        }
        pbsRecord.setTotalUserTime(totalUserTime);
        pbsRecord.setTotalSystemTime(totalSystemTime);
        pbsRecord.setConflict(existConflict(pbsRecord.getIdString(), pbsRecord.getExecHosts(),
                pbsRecord.getCreateTime(), pbsRecord.getDateTime()));

        return pbsRecord;
    }


    public Page getUserjobStats(Date fromDate, Date toDate, Integer pageNumber, Integer defaultPageSize,
                                Integer pageSize, String sortColumn, Boolean ascending) {
        long to = toDate.getTime();
        long from = fromDate.getTime();
        if (pageSize == null) {
            pageSize = defaultPageSize;
        }
        if (sortColumn == null) {
            ascending = false;
            sortColumn = "jobsSum";
        }
        Integer offset = (pageNumber - 1) * pageSize;
        Page page = new Page();
        page.setPageSize(pageSize);
        page.setPageNumber(pageNumber);
        page.setSortColumn(sortColumn);
        page.setFullListSize(getJdbcTemplate().queryForObject(
                "select count(distinct p.acct_user_id) " +
                        "from acct_pbs_record p " +
                        "where p.date_time between " + from + " and " + to + " ", Integer.class));
        page.setAscending(ascending);

        List<UserStats> stats = new ArrayList<>();

        List<Map<String, Object>> res = getJdbcTemplate().queryForList(
                " select u.user_name as username, count(p.acct_id_string) as jobsSum " +
                        " from acct_pbs_record p, acct_user u " +
                        " where p.date_time between " + from + " and " + to + " " +
                        "  and p.acct_user_id = u.acct_user_id " +
                        " group by u.user_name " +
                        " order by " + sortColumn + " " + (ascending ? "ASC" : "DESC") +
                        " limit " + pageSize + " offset " + offset + " "
        );

        for (Map<String, Object> item : res) {
            stats.add(new UserStats(
                    (String) item.get("username"),
                    (Long) item.get("jobsSum")));
        }

        page.setList(stats);
        return page;
    }

    @Override
    public Page getUserWalltimeStats(Date fromDate, Date toDate, Integer pageNumber, Integer defaultPageSize,
                                     Integer pageSize, String sortColumn, Boolean ascending) {
        long to = toDate.getTime() / 1000;
        long from = fromDate.getTime() / 1000;
        if (pageSize == null) {
            pageSize = defaultPageSize;
        }
        if (sortColumn == null) {
            ascending = false;
            sortColumn = "userTimeSum";
        }
        Integer offset = (pageNumber - 1) * pageSize;
        Page page = new Page();
        page.setPageSize(pageSize);
        page.setPageNumber(pageNumber);
        page.setSortColumn(sortColumn);
        page.setFullListSize(getJdbcTemplate().queryForObject(
                "select count(distinct p.acct_user_id) " +
                        "from acct_pbs_record p " +
                        "where p.start_time between " + from + " and " + to + " ", Integer.class
        ));
        page.setAscending(ascending);
        List<UserStats> stats = new ArrayList<>();

        List<Map<String, Object>> res = getJdbcTemplate().queryForList(
                "select u.user_name as username, " +
                        "       sum(least(p.end_time," + to + ") - greatest(p.start_time," + from + ")) as userTimeSum " +
                        "from acct_pbs_record p, acct_hosts_logs l, acct_user u " +
                        "where p.start_time <= " + to +
                        "  and p.end_time >= " + from + " " +
                        "  and p.acct_id_string = l.acct_id_string " +
                        "  and p.acct_user_id = u.acct_user_id " +
                        "group by u.user_name " +
                        " order by " + sortColumn + " " + (ascending ? "ASC" : "DESC") +
                        " limit " + pageSize + " offset " + offset + " "
        );

        for (Map<String, Object> item : res) {
            stats.add(new UserStats(
                    (String) item.get("username"),
                    ((BigDecimal) item.get("userTimeSum")).multiply(new BigDecimal(100))));
        }

        page.setList(stats);
        return page;
    }

    public LocalDate getLocalDateOfFirstPbsRecord() {
        LocalDate date = new LocalDate(getJdbcTemplate().queryForObject("SELECT min(p.date_time) FROM acct_pbs_record p ", Long.class));
        return AcctCal.createMonthYear(date.getMonthOfYear(), date.getYear());
    }
}

/*
            if (PBSRecordType.STARTED == r.getRecordType()) {
                //zjisti jestli je uloha nastartovana
                long pocetStarted = jdbc.queryForLong(
                        "select count(*) from acct_pbs_record_started where acct_id_string = ?", r.getIdString());
                //zjisti jestli je uloha ukoncena
                long pocetEnded = jdbc.queryForLong(
                        "select count(*) from acct_pbs_record where acct_id_string = ?", r.getIdString());

                List<Long> deleted = jdbc.query(
                        "select date_time from acct_pbs_record_deleted where acct_id_string = ?",
                        LONG_WRAPPER, r.getIdString());
                if (pocetStarted > 0) {
                    //smaze vsechny zaznamy o nastartovane uloze
                    jdbc.update("delete from acct_hosts_logs_started where acct_id_string = ? ", r.getIdString());
                    jdbc.update("delete from acct_pbs_record_started where acct_id_string = ? ", r.getIdString());
                }

                if (pocetEnded > 0) {
                    log.debug("E of S job is already in DB: " + r.getIdString());
                    unsaved.add(r.getIdString() + " S-E");
                } else if (deleted.size() > 0) {
                    if (deleted.get(0) > r.getDateTime()) {
                        log.debug("D of S job is already in DB: " + r.getIdString());
                        unsaved.add(r.getIdString() + " S-D");
                    } else {
                    }
                } else {
                    //pokud neni zaznam o tom, ze skoncila nebo byla smazana, vlozi zaznam o nastartovani
                    insertStartPbsRecord.execute(prepareJobParams(allUsers, allServers, r, hostname));
                    //vlozi vazbu na procesory
                    insertJob2HostCPURelation(allHosts, insertStartPbsRecordHostRelation, r);
                    log.debug("inserting S1 " + r);
                }

            } else if (PBSRecordType.ENDED == r.getRecordType() || PBSRecordType.G == r.getRecordType()) {
                // E zaznamename, pokud neni v databazi nebo pokud je jeho end pozdejsi nez ten zaznamenany
                long pocetVDb = jdbc.queryForLong(
                        "select count(*) from acct_pbs_record where acct_id_string = ?", r.getIdString());
                if (pocetVDb != 0) {
                    log.debug("E/G of job is already in DB: " + r.getIdString());
                    //vicenasobne E a G
                    long date_time = jdbc.queryForLong("select date_time from acct_pbs_record where acct_id_string = ?", r.getIdString());
                    if (date_time < r.getDateTime()) {
                        //nahradit
                        log.debug("replacing E/G with newer " + r);
                        jdbc.update("delete from acct_hosts_logs where acct_id_string = ? ", r.getIdString());
                        jdbc.update("delete from acct_pbs_record where acct_id_string = ? ", r.getIdString());
                        insertEndPbsRecord.execute(prepareJobParams(allUsers, allServers, r, hostname));
                        insertJob2HostCPURelation(allHosts, insertPbsRecordHostRelation, r);
                    } else {
                        unsaved.add(r.getIdString() + " E/G");
                    }
                    continue;
                }
                log.debug("inserting E/G " + r);
                insertEndPbsRecord.execute(prepareJobParams(allUsers, allServers, r, hostname));
                //vlozi vazbu na procesory
                insertJob2HostCPURelation(allHosts, insertPbsRecordHostRelation, r);
                //smaze ji z nastartovanych
                jdbc.update("delete from acct_hosts_logs_started where acct_id_string = ? ", r.getIdString());
                jdbc.update("delete from acct_pbs_record_started where acct_id_string = ? ", r.getIdString());
                jdbc.update("delete from acct_pbs_record_deleted where acct_id_string = ? ", r.getIdString());

            } else if (PBSRecordType.DELETED == r.getRecordType()
                    || PBSRecordType.ABORTED == r.getRecordType()
                    || PBSRecordType.RERUN == r.getRecordType()
                    ) {
                List<Long> date_times = jdbc.query("select date_time from acct_pbs_record_deleted where acct_id_string = ?", LONG_WRAPPER, r.getIdString());
                if (date_times.size() > 0) {

                }
                jdbc.update("delete from acct_pbs_record_started where acct_id_string = ? ", r.getIdString());
                int pocet = jdbc.queryForInt("select count(*) from acct_pbs_record_deleted where acct_id_string = ?", r.getIdString());
                if (pocet == 0) {
                    jdbc.update("insert into acct_pbs_record_deleted (acct_id_string,date_time) values (?,?)", r.getIdString(), r.getDateTime());
                    log.debug("deleted job: " + r.getIdString());
                } else {
                    unsaved.add(r.getIdString() + " D");
                }
            } else {
                log.error("unknown job type: " + r);
            }
            */
