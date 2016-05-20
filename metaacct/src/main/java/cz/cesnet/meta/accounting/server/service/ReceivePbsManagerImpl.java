package cz.cesnet.meta.accounting.server.service;

import cz.cesnet.meta.accounting.server.data.PbsReceiveLog;
import cz.cesnet.meta.accounting.server.util.Page;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ReceivePbsManagerImpl extends JdbcDaoSupport implements ReceivePbsManager {

    private static Logger logger = Logger.getLogger(ReceivePbsManagerImpl.class);

    /**
     * @return pocet kernel receive logu
     */
    private long getSize() {
        long size = getJdbcTemplate().queryForObject("SELECT count(*) FROM acct_receive_pbs", Long.class);
        logger.debug("size of received pbs logs : " + size);
        return size;
    }

    @Override
    public Page getPageReceivedPbsLogs(Integer pageNumber, Integer defaultPageSize, Integer pageSize, String sortColumn, Boolean ascending) {
        if (pageSize == null) {
            pageSize = defaultPageSize;
        }
        if (sortColumn == null) {
            ascending = false;
            sortColumn = "receiveTime";
        }
        Integer offset = (pageNumber - 1) * pageSize;
        Page page = new Page();
        page.setPageSize(pageSize);
        page.setPageNumber(pageNumber);
        page.setSortColumn(sortColumn);
        page.setFullListSize((int) getSize());
        page.setAscending(ascending);

        logger.debug("Getting received pbs logs from offset " + offset);
        List<PbsReceiveLog> logs = new ArrayList<>();
        List<Map<String, Object>> res = getJdbcTemplate().queryForList(
                "select l.acct_receive_pbs_id, l.receive_time as receiveTime, l.minimal_time as minimalTime, l.maximal_time as maximalTime, s.hostname as serverHostname" +
                        " from acct_receive_pbs l natural left join ci_acct_pbs_server s " +
                        " order by " + sortColumn + " " + (ascending ? "ASC" : "DESC") + ", receiveTime desc " +
                        " limit " + pageSize + " offset " + offset + " "
        );

        for (Map<String, Object> item : res) {
            logs.add(new PbsReceiveLog((Long) item.get("acct_receive_pbs_id"),
                    (Date) item.get("receiveTime"),
                    item.get("minimalTime") != null ? new Date((Long) item.get("minimalTime")) : null,
                    item.get("maximalTime") != null ? new Date((Long) item.get("maximalTime")) : null,
                    (String) item.get("serverHostname")
            ));
        }
        logger.debug("loaded " + logs.size() + " receive logs");
        page.setList(logs);
        return page;
    }
}
