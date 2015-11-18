package cz.cesnet.meta.nebula;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@Component
public class PbsNebulaImpl implements PbsNebula {

    final static Logger log = LoggerFactory.getLogger(PbsNebulaImpl.class);

    private JdbcTemplate jdbc;

    @Autowired
    public void setSource(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    private static final String NODE_UP = "node up";
    private static final String NODE_DOWN = "node down";


    /**
     * For every record of a VM running on a physical host, marks "node up" and "node down" for the specified times.
     * Thus only time when a VM registered in PBS was running will be computed in accounting.
     * @param vmHosts data imported from OpenNebula
     */
    @Transactional
    @Override
    public void markNodes(List<Main.VMHost> vmHosts) {
        log.info("marking hosts");
        for (Main.VMHost vmhost : vmHosts) {
            String pbsNode = vmhost.getName();
            try {
                int node_id = jdbc.queryForObject("SELECT acct_host_id FROM acct_host WHERE hostname=?", Integer.class, pbsNode);
                for (Main.HistoryRecord historyRecord : vmhost.getHistoryRecords()) {
                    mark(pbsNode, node_id, historyRecord.getStartTime(), NODE_UP);
                    mark(pbsNode, node_id, historyRecord.getEndTime(), NODE_DOWN);
                }
            } catch (EmptyResultDataAccessException ex) {
                log.warn("node {} not found ",pbsNode);
            }
        }
    }

    private void mark(String pbsNode, int node_id, Timestamp time, String state) {
        if (time == null) return;
        if (jdbc.queryForList("SELECT id FROM acct_pbs_log_events WHERE acct_host_id=? AND event_time=? AND type=?", Long.class, node_id, time, state).isEmpty()) {
            jdbc.update("INSERT INTO acct_pbs_log_events (acct_host_id, event_time, type) VALUES (?,?,?)", node_id, time, state);
            log.debug("marking host {} {} at {}", pbsNode, state, time);
        } else {
            log.warn("host {} marked {} at {} already", pbsNode, state, time);
        }
    }

    /**
     * Fix assignment of PBS nodes to physical machines. One time only.
     */
    @Transactional
    @Override
    public void fixNodeAssignment() {
        for (int i = 1; i <= 48; i++) {
            assign("hda" + i + ".priv.cerit-sc.cz", "zegox" + i + ".cerit-sc.cz");
        }
        for (int i = 1; i <= 32; i++) {
            assign("hdb" + i + ".priv.cerit-sc.cz", "zigur" + i + ".cerit-sc.cz");
        }
        for (int i = 1; i <= 112; i++) {
            assign("hdc" + i + ".priv.cerit-sc.cz", "zapat" + i + ".cerit-sc.cz");
        }
    }

    private void assign(String physicalHost, String pbsNode) {
        log.debug("assign({},{})", physicalHost, pbsNode);
        //get physical host id
        int ph_id = jdbc.queryForObject("SELECT id FROM physical_hosts WHERE name=?", Integer.class, physicalHost);
        //get time it was created
        Timestamp hostCreated = jdbc.queryForList("SELECT start_time FROM physical_hosts_resources_rel phrr WHERE  phrr.ph_hosts_id=?", Timestamp.class, ph_id).get(0);
        //get pbs node id
        int node_id = jdbc.queryForObject("SELECT acct_host_id FROM acct_host WHERE hostname=?", Integer.class, pbsNode);
        //insert their assignment
        jdbc.update("INSERT INTO physical_virtual_rel (ph_id, acct_host_id, start_time) VALUES (?,?,?)", ph_id, node_id, hostCreated);
        //insert NODE DOWN event
        jdbc.update("INSERT INTO acct_pbs_log_events (acct_host_id, event_time, type) VALUES (?,?,?)", node_id, hostCreated, NODE_DOWN);
    }
}
