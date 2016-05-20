package cz.cesnet.meta.accounting.server.service;

import cz.cesnet.meta.accounting.server.util.Page;

public interface ReceivePbsManager {

    Page getPageReceivedPbsLogs(Integer pageNumber, Integer defaultPageSize, Integer pageSize, String sortColumn, Boolean ascending);


}
