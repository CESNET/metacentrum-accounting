package cz.cesnet.meta.accounting.server.service;

import cz.cesnet.meta.accounting.server.data.*;
import cz.cesnet.meta.accounting.server.util.Page;
import cz.cesnet.meta.accounting.server.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

@SuppressWarnings("Convert2Diamond")
public class KernelRecordManagerImpl extends JdbcDaoSupport implements KernelRecordManager {
    final static Logger log = LoggerFactory.getLogger(KernelRecordManagerImpl.class);

    @Autowired
    UserManager userManager;
    @Autowired
    HostManager hostManager;
    @Autowired
    AppManager appManager;
    @Autowired
    DbUtilsManager dbUtilsManager;
    @Autowired
    TransactionTemplate transactionTemplate;

    public synchronized void saveRecords(final List<KernelRecord> records, final String hostname) {
        transactionTemplate.execute(status -> {
            Set<String> usernames = new HashSet<>();
            for (KernelRecord r : records) {
                usernames.add(r.getUsername());
            }
            Map<String, Long> users = userManager.saveUsernames(usernames);

            Set<String> hostnames = new HashSet<String>();
            hostnames.add(hostname);
            Map<String, Long> hosts = hostManager.saveHostnames(hostnames);
            long hostId = hosts.get(hostname);

            Long minimumTime = Long.MAX_VALUE;
            Long maximumTime = Long.MIN_VALUE;

            SimpleJdbcInsert insertKernelRecord = new SimpleJdbcInsert(getDataSource())
                    .withTableName("acct_kernel_record")
                    .usingColumns("acct_record_id", "command", "acct_host_id", "acct_user_id",
                            "user_time", "system_time", "create_time", "elapsed_time", "exitcode", "mem", "rw", "swaps", "pid", "ppid", "app");

            List<Application> apps = appManager.getAllApps();
            for (KernelRecord r : records) {
                if (r.getUserTime() + r.getSystemTime() < 100l)
                    continue; //skip processes taking less than 1 seconds
                long createTime = r.getCreateTime();
                Map<String, Object> params = new HashMap<String, Object>(20);
                params.put("acct_record_id", dbUtilsManager.getNextVal("acct_kernel_record", "acct_record_id"));
                params.put("command", r.getCommand());
                params.put("acct_host_id", hostId);
                params.put("acct_user_id", users.get(r.getUsername()));
                params.put("user_time", r.getUserTime()); // in 0.01 seconds unit
                params.put("system_time", r.getSystemTime()); //in in 0.01 seconds unit
                params.put("create_time", r.getCreateTime());
                params.put("elapsed_time", r.getElapsedTime()); // in 0.01 seconds unit
                params.put("exitcode", r.getExitcode());
                params.put("mem", r.getMem());
                params.put("rw", r.getRw());
                params.put("swaps", r.getSwaps());
                params.put("pid", r.getPid());
                params.put("ppid", r.getPpid());
                params.put("app", getAppNameByRegexp(apps, r.getCommand()));
                insertKernelRecord.execute(params);
                if (createTime < minimumTime) minimumTime = createTime;
                if (createTime > maximumTime) maximumTime = createTime;
            }

            //save note to receive_log
            if (minimumTime == Long.MAX_VALUE) minimumTime = null;
            if (maximumTime == Long.MIN_VALUE) maximumTime = null;
            SimpleJdbcInsert insertKernelReceiveLog = new SimpleJdbcInsert(getDataSource())
                    .withTableName("acct_receive_log")
                    .usingColumns("acct_receive_log_id", "acct_host_id", "receive_time", "minimal_time", "maximal_time");
            Map<String, Object> params = new HashMap<String, Object>(1);
            params.put("acct_receive_log_id", dbUtilsManager.getNextVal("acct_receive_log", "acct_receive_log_id"));
            params.put("acct_host_id", hostId);
            params.put("receive_time", new Timestamp(new Date().getTime()));
            params.put("minimal_time", minimumTime);
            params.put("maximal_time", maximumTime);
            insertKernelReceiveLog.execute(params);

            if (logger.isInfoEnabled()) logger.info(hostname + ": " + records.size() + " saved");
            return null;
        });
    }

    /**
     * @return seznam kernelovych zaznamu pro daneho uzivatele, hosty, od do
     */
    public List<KernelRecord> getRecordsForUserForHostsFromTo(long userId, List<PBSHost> execHosts, long startTimeSeconds, long endTimeSeconds) {
        List<KernelRecord> kernelRecords = new ArrayList<KernelRecord>();
        if (execHosts.isEmpty()) {
            return kernelRecords;
        }
        String query = "select k.acct_record_id, k.command, k.user_time, k.system_time, " +
                "       k.exitcode, k.create_time, k.elapsed_time, k.mem, k.rw, k.swaps, k.app, " +
                "       k.acct_host_id, h.hostname " +
                "  from acct_kernel_record k, acct_host h " +
                "  where acct_user_id = " + userId +
                "    and k.acct_host_id = h.acct_host_id " +
                "    and k.acct_host_id in (" + StringUtils.hostIdsToString(execHosts) + ") " +
                "    and k.create_time >= " + startTimeSeconds +
                "    and (k.create_time*100 +k.elapsed_time)<= " + endTimeSeconds * 100 + //* 100 protoze elapsed time je v setinach sekundy
                "  order by k.elapsed_time desc limit 1000;";
        List<Map<String, Object>> res = getJdbcTemplate().queryForList(query);

        for (Map<String, Object> item : res) {
            long recordId = (Long) item.get("acct_record_id");
            String command = (String) item.get("command");
            long userTime = (Long) item.get("user_time");
            long systemTime = (Long) item.get("system_time");
            long exitCode = (Long) item.get("exitcode");
            long createTime = (Long) item.get("create_time");
            long elapsedTime = (Long) item.get("elapsed_time");
            long mem = (Long) item.get("mem");
            long rw = (Long) item.get("rw");
            long swaps = (Long) item.get("swaps");
            String app = (String) item.get("app");
            PBSHost host = new PBSHost((Long) item.get("acct_host_id"), (String) item.get("hostname"));
            KernelRecord kernelRecord = new KernelRecord(recordId, command, userTime, systemTime,
                    elapsedTime, createTime, null, exitCode, mem, rw, swaps, app
            );
            //kernelRecord.setApp(getAppNameByRegexp(apps, kernelRecord.getCommand()));
            kernelRecord.setHost(host);
            kernelRecords.add(kernelRecord);
        }

        return kernelRecords;
    }

    public Page getBinariesStats(Date fromDate, Date toDate, Integer pageNumber, Integer defaultPageSize,
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
                "select count(distinct k.command) " +
                        "from acct_kernel_record k " +
                        "where k.create_time between " + from + " and " + to + " ", Integer.class
        ));
        page.setAscending(ascending);

        List<BinaryStats> stats = new ArrayList<BinaryStats>();

        String query = "select k.command, k.app, " +
                "       sum(k.elapsed_time) as elapsedTimeSum, " +
                "       sum(k.user_time) as userTimeSum, " +
                "       sum(k.system_time) as systemTimeSum " +
                "from acct_kernel_record k " +
                "where k.create_time between " + from + " and " + to + " " +
                "group by k.command, k.app  " +
                " order by " + sortColumn + " " + (ascending ? "ASC" : "DESC") +
                " limit " + pageSize + " offset " + offset + " ";
        List<Map<String, Object>> res = getJdbcTemplate().queryForList(
                query);

        for (Map<String, Object> item : res) {
            BinaryStats bs = new BinaryStats(
                    (String) item.get("command"),
                    (String) item.get("app"),
                    (BigDecimal) item.get("elapsedTimeSum"),
                    (BigDecimal) item.get("userTimeSum"),
                    (BigDecimal) item.get("systemTimeSum"));
            //bs.setApp(getAppNameByRegexp(apps, bs.getCommand()));
            stats.add(bs);
        }

        page.setList(stats);
        return page;
    }

    public Page getUsersForBinaryStats(String command, Date fromDate, Date toDate, Integer pageNumber, Integer defaultPageSize,
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
                "select count(distinct k.acct_user_id) " +
                        "from acct_kernel_record k " +
                        "where k.command = '" + command + "' " +
                        "  and k.create_time between " + from + " and " + to + " ", Integer.class
        ));
        page.setAscending(ascending);

        List<UserForBinaryStats> stats = new ArrayList<>();


        String query = "select u.user_name as username,  " +
                "       sum(k.elapsed_time) as elapsedTimeSum, " +
                "       sum(k.user_time) as userTimeSum, " +
                "       sum(k.system_time) as systemTimeSum " +
                "from acct_kernel_record k, acct_user u " +
                "where k.command = '" + command + "' " +
                "  and k.acct_user_id = u.acct_user_id " +
                "  and k.create_time between " + from + " and " + to + " " +
                "group by u.user_name " +
                " order by " + sortColumn + " " + (ascending ? "ASC" : "DESC") +
                " limit " + pageSize + " offset " + offset + " ";
        List<Map<String, Object>> res = getJdbcTemplate().queryForList(
                query);

        for (Map<String, Object> item : res) {
            stats.add(new UserForBinaryStats(
                    (String) item.get("username"),
                    (BigDecimal) item.get("elapsedTimeSum"),
                    (BigDecimal) item.get("userTimeSum"),
                    (BigDecimal) item.get("systemTimeSum")));
        }

        page.setList(stats);
        return page;
    }


    @Override
    public Page getKernelRecordsForPbsIdString(String pbsIdString, Integer pageNumber, Integer defaultPageSize,
                                               Integer pageSize, String sortColumn, Boolean ascending) {
        if (pageSize == null) {
            pageSize = defaultPageSize;
        }
        if (sortColumn == null) {
            ascending = false;
            sortColumn = "userTime";
        }
        Integer offset = (pageNumber - 1) * pageSize;
        Page page = new Page();
        page.setPageSize(pageSize);
        page.setPageNumber(pageNumber);
        page.setSortColumn(sortColumn);
        page.setFullListSize((int) getSizeKernelRecordsForPbsIdString(pbsIdString));
        page.setAscending(ascending);
        List<KernelRecord> kernelRecords = new ArrayList<>();

        List<Map<String, Object>> res = getJdbcTemplate().queryForList(
                "select distinct(k.acct_record_id), k.command as command, k.app as app, k.user_time as userTime, k.create_time as createTime, " +
                        "       k.elapsed_time as elapsedTime, k.system_time as systemTime, k.exitcode as exitcode, " +
                        "       h.acct_host_id , h.hostname as hostname " +
                        " from acct_kernel_record k, acct_host h, acct_pbs_record p, acct_hosts_logs hl " +
                        " where p.acct_id_string = '" + pbsIdString + "' " +
                        "   and k.acct_user_id = p.acct_user_id " +
                        "   and hl.acct_id_string = p.acct_id_string" +
                        "   and hl.acct_host_id = k.acct_host_id " +
                        "   and k.acct_host_id = h.acct_host_id " +
                        "   and k.create_time >= p.start_time " +
                        "   and k.create_time*100+k.elapsed_time <= p.end_time*100" + //*100 protoze elapsedTime je v setinach a startTime, endtime v sekundach
                        " order by " + sortColumn + " " + (ascending ? "ASC" : "DESC") +
                        " limit " + pageSize + " offset " + offset + " "
        );

        for (Map<String, Object> item : res) {
            KernelRecord kr = new KernelRecord((Long) item.get("acct_record_id"), (String) item.get("command"), (String) item.get("app"), (Long) item.get("userTime"),
                    (Long) item.get("systemTime"), (Long) item.get("elapsedTime"), (Long) item.get("createTime"), (Long) item.get("exitcode"),
                    (Long) item.get("acct_host_id"), (String) item.get("hostname"));

            //kr.setApp(getAppNameByRegexp(apps, kr.getCommand()));
            kernelRecords.add(kr);
        }

        page.setList(kernelRecords);
        return page;
    }

    private static String getAppNameByRegexp(List<Application> apps, String command) {
        String name = "";
        for (Application a : apps) {
            if (a.getPattern().matcher(command).matches()) {
                name = a.getName();
                break;
            }
        }
        return name;
    }

    private long getSizeKernelRecordsForPbsIdString(String pbsIdString) {
        long size = getJdbcTemplate().queryForObject(
                "select count(distinct(k.acct_record_id)) " +
                        "from acct_kernel_record k, acct_host h, acct_pbs_record p, acct_hosts_logs hl " +
                        " where p.acct_id_string = '" + pbsIdString + "' " +
                        "   and k.acct_user_id = p.acct_user_id " +
                        "   and hl.acct_id_string = p.acct_id_string" +
                        "   and hl.acct_host_id = k.acct_host_id " +
                        "   and k.acct_host_id = h.acct_host_id " +
                        "   and k.create_time >= p.start_time " +
                        "   and k.create_time*100+k.elapsed_time <= p.end_time*100", Long.class
        );//*100 protoze elapsedTime je v setinach a startTime, endtime v sekundach");
        logger.debug("size of kernel records for " + pbsIdString + " : " + size);
        return size;
    }

}
