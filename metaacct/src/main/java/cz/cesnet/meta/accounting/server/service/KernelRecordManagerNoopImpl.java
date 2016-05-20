package cz.cesnet.meta.accounting.server.service;

import cz.cesnet.meta.accounting.server.data.KernelRecord;
import cz.cesnet.meta.accounting.server.data.PBSHost;
import cz.cesnet.meta.accounting.server.util.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * No-operation implementation.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class KernelRecordManagerNoopImpl implements KernelRecordManager {


    final static Logger log = LoggerFactory.getLogger(KernelRecordManagerNoopImpl.class);

    @Autowired
    UserManager userManager;

    @Override
    public void saveRecords(List<KernelRecord> records, String hostname) {
        log.info("ignored saveRecords() hostname=" + hostname + " records.size=" + records.size());
    }

    @Override
    public List<KernelRecord> getRecordsForUserForHostsFromTo(long userId, List<PBSHost> execHosts, long startTimeSeconds, long endTimeSeconds) {
        log.info("ignored getRecordsForUserForHostsFromTo(userId=" + userId + ", user=" + userManager.getUserName(userId) + ")");
        return Collections.emptyList();
    }

    @Override
    public Page getBinariesStats(Date fromDate, Date toDate, Integer pageNumber, Integer defaultPageSize, Integer pageSize, String sortColumn, Boolean ascending) {
        log.info("ignored getBinariesStats()");
        if (pageSize == null) {
            pageSize = defaultPageSize;
        }
        if (sortColumn == null) {
            ascending = false;
            sortColumn = "userTimeSum";
        }
        Page page = new Page();
        page.setPageSize(pageSize);
        page.setPageNumber(pageNumber);
        page.setSortColumn(sortColumn);
        page.setFullListSize(0);
        page.setAscending(ascending);
        page.setList(Collections.emptyList());
        return page;
    }

    @Override
    public Page getUsersForBinaryStats(String command, Date fromDate, Date toDate, Integer pageNumber, Integer defaultPageSize, Integer pageSize, String sortColumn, Boolean ascending) {
        log.info("ignored getUsersForBinaryStats()");
        if (pageSize == null) {
            pageSize = defaultPageSize;
        }
        if (sortColumn == null) {
            ascending = false;
            sortColumn = "userTimeSum";
        }
        Page page = new Page();
        page.setPageSize(pageSize);
        page.setPageNumber(pageNumber);
        page.setSortColumn(sortColumn);
        page.setFullListSize(0);
        page.setAscending(ascending);
        page.setList(Collections.emptyList());
        return page;
    }

    @Override
    public Page getKernelRecordsForPbsIdString(String pbsIdString, Integer pageNumber, Integer defaultPageSize, Integer pageSize, String sortColumn, Boolean ascending) {
        log.info("ignored getKernelRecordsForPbsIdString(pbsIdString=" + pbsIdString + ")");
        if (pageSize == null) {
            pageSize = defaultPageSize;
        }
        if (sortColumn == null) {
            ascending = false;
            sortColumn = "userTimeSum";
        }
        Page page = new Page();
        page.setPageSize(pageSize);
        page.setPageNumber(pageNumber);
        page.setSortColumn(sortColumn);
        page.setFullListSize(0);
        page.setAscending(ascending);
        page.setList(Collections.emptyList());
        return page;
    }
}
