package cz.cesnet.meta.accounting.server.service;

import cz.cesnet.meta.accounting.server.data.KernelReceiveLog;
import cz.cesnet.meta.accounting.server.util.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ReceiveLogManagerImpl extends JdbcDaoSupport implements ReceiveLogManager {
    private static Logger logger = LoggerFactory.getLogger(ReceiveLogManagerImpl.class);
    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public Page getPageReceiveLogs(Map<String, Object> criteria, Integer pageNumber, Integer defaultPageSize, Integer pageSize, String sortColumn, Boolean ascending) {
        if (pageSize == null) {
            pageSize = defaultPageSize;
        }
        if (sortColumn == null) {
            ascending = false;
            sortColumn = "receiveTime";
        }

        StringBuilder where = new StringBuilder();
        if (criteria != null && !criteria.isEmpty()) {

            if (criteria.get("hostname") != null) {
                where.append(" and h.hostname like '").append((String) criteria.get("hostname")).append("' ");
            }
            if (criteria.get("receiveTimeFrom") != null) {
                where.append(" and l.receive_time >= '").append(df.format((Date) criteria.get("receiveTimeFrom"))).append("' ");
            }
            if (criteria.get("receiveTimeTo") != null) {
                where.append(" and l.receive_time <= '").append(df.format((Date) criteria.get("receiveTimeTo"))).append("' ");
            }
        }

        Integer offset = (pageNumber - 1) * pageSize;
        Page page = new Page();
        page.setPageSize(pageSize);
        page.setPageNumber(pageNumber);
        page.setSortColumn(sortColumn);
        page.setFullListSize(getJdbcTemplate().queryForObject("select count(*) from acct_receive_log l, acct_host h where l.acct_host_id = h.acct_host_id " + where.toString(), Long.class).intValue());

        page.setAscending(ascending);
        logger.debug("Getting receive logs from offset " + offset);
        List<KernelReceiveLog> logs = new ArrayList<>();
        List<Map<String, Object>> res = getJdbcTemplate().queryForList("select l.acct_receive_log_id, l.acct_host_id, " +
                " h.hostname as hostname, l.receive_time as receiveTime, l.minimal_time as minimalTime, l.maximal_time as maximalTime " +
                " from acct_receive_log l, acct_host h where l.acct_host_id = h.acct_host_id " +
                where.toString() +
                " order by " + sortColumn + " " + (ascending ? "ASC" : "DESC") +
                " limit " + pageSize + " offset " + offset + " ");

        for (Map<String, Object> item : res) {
            logs.add(new KernelReceiveLog((Long) item.get("acct_receive_log_id"),
                    (Long) item.get("acct_host_id"),
                    (String) item.get("hostname"),
                    (Date) item.get("receiveTime"),
                    item.get("minimalTime") != null ? new Date((Long) item.get("minimalTime") * 1000) : null,
                    item.get("maximalTime") != null ? new Date((Long) item.get("maximalTime") * 1000) : null
            ));
        }
        logger.debug("loaded " + logs.size() + " receive logs");
        page.setList(logs);
        return page;
    }

}
