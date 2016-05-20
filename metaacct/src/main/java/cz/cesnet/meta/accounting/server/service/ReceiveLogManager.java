package cz.cesnet.meta.accounting.server.service;

import cz.cesnet.meta.accounting.server.util.Page;

import java.util.Map;

public interface ReceiveLogManager {

    Page getPageReceiveLogs(Map<String, Object> searchCriteria, Integer pageNumber, Integer defaultPageSize, Integer pageSize, String sortColumn, Boolean ascending);

}
