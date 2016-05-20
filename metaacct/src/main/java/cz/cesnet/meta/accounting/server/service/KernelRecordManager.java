package cz.cesnet.meta.accounting.server.service;

import cz.cesnet.meta.accounting.server.data.KernelRecord;
import cz.cesnet.meta.accounting.server.data.PBSHost;
import cz.cesnet.meta.accounting.server.util.Page;

import java.util.Date;
import java.util.List;

public interface KernelRecordManager {

    void saveRecords(List<KernelRecord> records, String hostname);

    List<KernelRecord> getRecordsForUserForHostsFromTo(long userId, List<PBSHost> execHosts, long startTimeSeconds, long endTimeSeconds);

    Page getBinariesStats(Date fromDate, Date toDate, Integer pageNumber, Integer defaultPageSize,
                          Integer pageSize, String sortColumn, Boolean ascending);

    Page getUsersForBinaryStats(String command, Date fromDate, Date toDate, Integer pageNumber, Integer defaultPageSize,
                                Integer pageSize, String sortColumn, Boolean ascending);

    Page getKernelRecordsForPbsIdString(String pbsIdString, Integer pageNumber, Integer defaultPageSize,
                                        Integer pageSize, String sortColumn, Boolean ascending);


}
