package cz.cesnet.meta.accounting.server.service;

import java.util.Map;

public interface PbsServerManager {

    Map<String, Long> saveHostname(String serverHostname);

}
