package cz.cesnet.meta.accounting.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PbsServerManagerImpl extends JdbcDaoSupport implements PbsServerManager {
    private static Logger logger = LoggerFactory.getLogger(PbsServerManagerImpl.class);

    @Autowired
    DbUtilsManager dbUtilsManager;

    /**
     * metoda pro ukladani nazvu pbs serveru, servery ulozi a vrati jejich mapu
     * hostname-id v db
     *
     * @param hostname hostname pbs serveru
     * @return mapa vsech serveru a jejich id
     */
    public Map<String, Long> saveHostname(String hostname) {
        Map<String, Long> servers = loadServers();
        SimpleJdbcInsert insertHost = new SimpleJdbcInsert(getDataSource())
                .withTableName("ci_acct_pbs_server")
                .usingColumns("ci_acct_pbs_server_id", "hostname");

        if (!servers.containsKey(hostname)) {
            Map<String, Object> params = new HashMap<>(2);
            Long nextval = dbUtilsManager.getNextVal("ci_acct_pbs_server", "ci_acct_pbs_server_id");
            params.put("ci_acct_pbs_server_id", nextval);
            params.put("hostname", hostname);
            insertHost.execute(params);

            servers.put(hostname, nextval);
        }
        if (logger.isDebugEnabled()) logger.debug("loaded " + servers.size() + " pbs server hostnames");
        return servers;
    }

    /**
     * nacte pbs servery aktualne ulozene v db
     *
     * @return mapa pbs serveru a jejich id
     */
    private Map<String, Long> loadServers() {
        Map<String, Long> servers = new HashMap<>();
        List<Map<String, Object>> res = getJdbcTemplate().queryForList(
                "SELECT ci_acct_pbs_server_id, hostname FROM ci_acct_pbs_server");

        for (Map<String, Object> item : res) {
            servers.put((String) item.get("hostname"), (Long) item.get("ci_acct_pbs_server_id"));
        }

        return servers;
    }
}
