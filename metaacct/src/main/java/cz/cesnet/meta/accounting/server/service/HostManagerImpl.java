package cz.cesnet.meta.accounting.server.service;

import cz.cesnet.meta.accounting.server.data.HostData;
import cz.cesnet.meta.accounting.server.data.PBSHost;
import cz.cesnet.meta.accounting.server.util.Page;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

public class HostManagerImpl extends JdbcDaoSupport implements HostManager {
    private static Logger logger = Logger.getLogger(HostManagerImpl.class);

    @Autowired
    DbUtilsManager dbUtilsManager;


    /**
     * metoda pro ukladani nazvu hostu, hosty ulozi a vrati jejich mapu
     * hostname-id v db
     *
     * @param hostnames kolekce hostnames pro ulozeni
     * @return mapa vsech hostu a jejich id
     */
    public Map<String, Long> saveHostnames(Collection<String> hostnames) {
        Map<String, Long> hosts = loadHosts();
        SimpleJdbcInsert insertHost = new SimpleJdbcInsert(getDataSource())
                .withTableName("acct_host")
                .usingColumns("acct_host_id", "hostname");

        int inserted = 0;
        for (String s : hostnames) {
            if (!hosts.containsKey(s)) {
                Map<String, Object> params = new HashMap<String, Object>(2);
                Long nextval = dbUtilsManager.getNextVal("acct_host", "acct_host_id");
                params.put("acct_host_id", nextval);
                params.put("hostname", s);
                insertHost.execute(params);

                hosts.put(s, nextval);
                inserted++;
            }
        }
        logger.debug("hostnames received: " + hostnames.size() + " saved: " + inserted + " already in db: " + (hostnames.size() - inserted));
        return hosts;
    }

    /**
     * nacte uzivatele aktualne ulozene v db
     *
     * @return mapa hostu a jejich id
     */
    private Map<String, Long> loadHosts() {
        Map<String, Long> hosts = new HashMap<String, Long>();
        List<Map<String, Object>> res = getJdbcTemplate().queryForList(
                "SELECT acct_host_id, hostname FROM acct_host ");

        for (Map<String, Object> item : res) {
            hosts.put((String) item.get("hostname"), (Long) item.get("acct_host_id"));
        }

        return hosts;
    }

    /**
     * @param idString pbs zaznamu
     * @return seznam hostu, na kterych uloha s danym idString bezela
     */
    public List<PBSHost> getHostsForPbsId(String idString) {
        List<PBSHost> hosts = new ArrayList<PBSHost>();
        List<Map<String, Object>> res = getJdbcTemplate().queryForList(
                "SELECT h.acct_host_id, h.hostname, l.cpu_number " +
                        " FROM acct_host h, acct_hosts_logs l " +
                        " WHERE h.acct_host_id = l.acct_host_id " +
                        "   AND l.acct_id_string = ? ORDER BY h.hostname", idString
        );

        for (Map<String, Object> item : res) {
            hosts.add(new PBSHost((Long) item.get("acct_host_id"), (String) item.get("hostname"), (Integer) item
                    .get("cpu_number")));
        }
        return hosts;
    }

    @Override
    @Transactional
    public long getHostId(String hostName) {
        final JdbcTemplate jdbc = getJdbcTemplate();
        try {
            return jdbc.queryForObject("SELECT acct_host_id FROM acct_host WHERE hostname = ?", Long.class, hostName);
        } catch (EmptyResultDataAccessException ex) {
            jdbc.update("INSERT INTO acct_host (hostname) VALUES (?)", hostName);
            return jdbc.queryForObject("SELECT acct_host_id FROM acct_host WHERE hostname = ?", Long.class, hostName);
        }
    }

    @Override
    public Page getHostsStats(Map<String, Object> criteria, Integer pageNumber, Integer defaultPageSize, Integer pageSize, String sortColumn, Boolean ascending) {
        if (pageSize == null) {
            pageSize = defaultPageSize;
        }
        //vychozi razeni je desc, vychozi sloupec lastLogDate
        if (sortColumn == null) {
            ascending = false;
            sortColumn = "lastLogDate";
        }
        StringBuilder where = new StringBuilder();
        if (criteria != null && !criteria.isEmpty()) {
            where.append(" where 1=1 ");
            if (criteria.get("hostname") != null) {
                where.append(" and hostname like '").append(criteria.get("hostname")).append("' ");
            }
            /*if (criteria.get("kernelLogsFrom") != null) {
              where.append(" and kernelLogsCount >= " + (Integer)criteria.get("kernelLogsFrom") + " ");
            }
            if (criteria.get("kernelLogsTo") != null) {
              where.append(" and kernelLogsCount <= " + (Integer)criteria.get("kernelLogsTo") + " ");
            }
            if (criteria.get("lastLogDateFrom") != null) {
              where.append(" and lastLogDate >= " + (Date)criteria.get("lastLogDateFrom") + " ");
            }
            if (criteria.get("lastLogDateTo") != null) {
              where.append(" and lastLogDate <= " + (Date)criteria.get("lastLogDateTo") + " ");
            }*/
        }

        Integer offset = (pageNumber - 1) * pageSize;
        Page page = new Page();
        page.setPageSize(pageSize);
        page.setPageNumber(pageNumber);
        page.setSortColumn(sortColumn);
        page.setFullListSize(getJdbcTemplate().queryForObject("select count(h.acct_host_id) from acct_host h " + where.toString(),Integer.class));
        page.setAscending(ascending);

        List<HostData> allHosts = new ArrayList<HostData>();

        StringBuilder query = new StringBuilder("select h.acct_host_id, h.hostname as hostname, count(l.acct_receive_log_id) as kernelLogsCount, ");
        query.append(" max(l.receive_time) as lastLogDate");
        query.append(" from acct_host h left join acct_receive_log l using (acct_host_id) ");

        query.append(where);

        query.append(" group by h.acct_host_id, hostname");
        if ("lastLogDate".equals(sortColumn)) {
            query.append(" order by max(l.receive_time) is not null " + (ascending ? "ASC" : "DESC") + " , max(l.receive_time) "
                    + (ascending ? "ASC" : "DESC"));
        } else {
            query.append(" order by " + sortColumn + " " + (ascending ? "ASC" : "DESC"));
        }

        query.append(" limit " + pageSize + " offset " + offset + " ");
        List<Map<String, Object>> res = getJdbcTemplate().queryForList(query.toString());

        for (Map<String, Object> item : res) {
            HostData hostData = new HostData((Long) item.get("acct_host_id"), (String) item.get("hostname"), (Long) item
                    .get("kernelLogsCount"), (Date) item.get("lastLogDate"));
            allHosts.add(hostData);
        }
        page.setList(allHosts);

        return page;
    }
}
