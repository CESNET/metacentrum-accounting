package cz.cesnet.meta.accounting.server.service;

import cz.cesnet.meta.accounting.server.data.PBSRecord;
import cz.cesnet.meta.accounting.server.data.PbsRecordData;
import cz.cesnet.meta.accounting.server.util.Page;
import org.joda.time.LocalDate;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface PbsRecordManager {
    List<String> saveRecords(List<PBSRecord> records, String hostname);

    Page getPbsRecordsForUserId(long userId, Map<String, Object> criteria, Integer pageNumber, Integer defaultPageSize, Integer pageSize, String sortColumn, Boolean ascending);

    PbsRecordData getPbsRecordForIdString(String id);

    Page getPbsRecordsForUserId(long userId, int number, Integer pageNumber, Integer defaultPageSize, Integer pageSize, String sortColumn, Boolean ascending);

    Page getUserjobStats(Date fromDate, Date toDate, Integer pageNumber, Integer defaultPageSize,
                         Integer pageSize, String sortColumn, Boolean ascending);

    LocalDate getLocalDateOfFirstPbsRecord();

    Page getAllPbsRecords(int number, Integer pageNumber, Integer defaultPageSize, Integer pageSize, String sortColumn, Boolean ascending);

    Page getAllPbsRecords(Map<String, Object> searchCriteria, Integer pageNumber, Integer defaultPageSize, Integer pageSize, String sortColumn, Boolean ascending);

    Page getUserWalltimeStats(Date fromDate, Date toDate, Integer pageNumber, Integer defaultPageSizeShort,
                              Integer pageSize, String sortColumn, Boolean ascending);

}
