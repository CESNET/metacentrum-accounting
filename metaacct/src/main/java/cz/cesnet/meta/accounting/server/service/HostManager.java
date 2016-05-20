package cz.cesnet.meta.accounting.server.service;

import cz.cesnet.meta.accounting.server.data.PBSHost;
import cz.cesnet.meta.accounting.server.util.Page;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface HostManager {

    Map<String, Long> saveHostnames(Collection<String> hostnames);

    List<PBSHost> getHostsForPbsId(String idString);

    long getHostId(String hostName);

    Page getHostsStats(Map<String, Object> searchCriteria, Integer pageNumber, Integer defaultPageSize, Integer pageSize,
                       String sortColumn, Boolean ascending);

}
