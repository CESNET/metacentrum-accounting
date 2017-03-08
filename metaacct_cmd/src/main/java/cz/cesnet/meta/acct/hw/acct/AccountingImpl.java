package cz.cesnet.meta.acct.hw.acct;

import cz.cesnet.meta.acct.hw.Accounting;
import cz.cesnet.meta.acct.hw.perun.ComputingResource;
import cz.cesnet.meta.acct.hw.perun.Machine;
import cz.cesnet.meta.perun.api.PerunUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: AccountingImpl.java,v 1.5 2009/11/26 17:18:35 makub Exp $
 */

public class AccountingImpl implements Accounting {

    final static Logger log = LoggerFactory.getLogger(AccountingImpl.class);

    private JdbcTemplate jdbc;

    private static final SingleColumnRowMapper<Integer> INT_MAPPER = new SingleColumnRowMapper<>(Integer.class);

    public void setDataSource(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Transactional
    @Override
    public void updateUsers(List<PerunUser> perunUsers) {
        log.debug("updateUsers()");
        List<AcctUser> acctUsers = jdbc.query("SELECT acct_user_id,user_name,organisation,skupina,status FROM acct_user",
                (rs, i) -> new AcctUser(rs.getInt("acct_user_id"), rs.getString("user_name"), rs.getString("organisation"), rs.getString("skupina"), rs.getString("status")));
        Map<String, AcctUser> acctUserMap = new HashMap<>(acctUsers.size() * 2);
        for (AcctUser acctUser : acctUsers) {
            acctUserMap.put(acctUser.getLogname(), acctUser);
        }
        acctUsers.clear();
        //find modifications
        for (PerunUser perunUser : perunUsers) {
            AcctUser acctUser = acctUserMap.get(perunUser.getLogname());
            if (acctUser == null) continue;
            boolean changed = false;
            if (!perunUser.getOrganization().equals(acctUser.getOrganization())) {
                log.info("changing user " + perunUser.getLogname() + " org from \"" + acctUser.getOrganization() + "\" to \"" + perunUser.getOrganization() + "\"");
                acctUser.setOrganization(perunUser.getOrganization());
                changed = true;
            }
            if (!perunUser.getResearchGroup().equals(acctUser.getResearchGroup())) {
                log.info("changing user " + perunUser.getLogname() + " researchGroup from \"" + acctUser.getResearchGroup() + "\" to \"" + perunUser.getResearchGroup() + "\"");
                acctUser.setResearchGroup(perunUser.getResearchGroup());
                changed = true;
            }
            if (!perunUser.getStatus().equals(acctUser.getStatus())) {
                log.info("changing user " + perunUser.getLogname() + " status from \"" + acctUser.getStatus() + "\" to \"" + perunUser.getStatus() + "\"");
                acctUser.setStatus(perunUser.getStatus());
                changed = true;
            }
            if (changed) {
                acctUsers.add(acctUser);
            }
        }
        //modify
        for (AcctUser acctUser : acctUsers) {
            jdbc.update("UPDATE acct_user SET organisation=?,skupina=?,status=? WHERE acct_user_id=?", acctUser.getOrganization(), acctUser.getResearchGroup(), acctUser.getStatus(), acctUser.getId());
        }
    }


    @Override
    @Transactional
    public void saveComputingResources(List<ComputingResource> computingResources, Set<String> frontends, Set<String> reserved) {
        log.debug("saveComputingResources()");
        Date now = new Date();
        int cpusTotal = 0;
        int machinesTotal = 0;
        boolean changes = false;

        frontends = convertMachineNamestoPhysicalHostNames(frontends);
        reserved = convertMachineNamestoPhysicalHostNames(reserved);
        for (ComputingResource cr : computingResources) {
            PhysicalResource pr = findPhysicalResource(cr);
            if (cr.isCluster()) {
                for (Machine m : cr.getMachines()) {
                    changes |= markMachine(now, m, pr, frontends, reserved);
                    cpusTotal += m.getCpuNum();
                    machinesTotal++;
                }
            } else {
                Machine m = cr.getMachine();
                changes |= markMachine(now, m, pr, frontends, reserved);
                cpusTotal += m.getCpuNum();
                machinesTotal++;
            }
        }
        saveReceiveLog(now, cpusTotal, machinesTotal, changes);
    }

    private Set<String> convertMachineNamestoPhysicalHostNames(Set<String> names) {
        HashSet<String> phNames = new HashSet<>(names.size() * 2);
        for (String name : names) {
            try {
                //nejdriv zkusime jestli existuje fyzicky
                PhysicalHost ph = findPhysicalHost(name);
                phNames.add(ph.getName());
                log.debug("physical host " + name + " -> " + ph.getName());
            } catch (EmptyResultDataAccessException ex) {
                //kdyz ne, zkusime mapovani z virtualnich na fyzicke
                int acct_host_id = findAcctHostId(name);
                try {
                    String phName = jdbc.queryForObject("SELECT ph.name FROM physical_virtual_rel pvr ,physical_hosts ph WHERE pvr.acct_host_id=? AND pvr.ph_id=ph.id",
                            String.class, acct_host_id);
                    phNames.add(phName);
                    log.info("changing virtual " + name + " -> " + phName);
                } catch (EmptyResultDataAccessException ex1) {
                    log.warn("machine name " + name + " not mapped to physical host");
                }
            }
        }
        return phNames;
    }

    private int findAcctHostId(String name) {
        while (true) {
            try {
                return jdbc.queryForObject("SELECT acct_host_id FROM acct_host WHERE hostname=?", Integer.class, name);
            } catch (EmptyResultDataAccessException ex) {
                log.info("creating acct_host " + name);
                jdbc.update("INSERT INTO acct_host(hostname) VALUES (?)", name);
            }
        }
    }


    @Override
    public void insertMapping(String virt, String fyz) {
        int fyzId = jdbc.queryForObject("SELECT id FROM physical_hosts WHERE name=?", Integer.class, fyz);
        int virtId = jdbc.queryForObject("SELECT acct_host_id FROM acct_host WHERE hostname=?", Integer.class, virt);
        List<Integer> ids = jdbc.query("SELECT id FROM physical_virtual_rel WHERE ph_id=? AND acct_host_id=?", INT_MAPPER, fyzId, virtId);
        if (!ids.isEmpty()) {
            log.error(" " + virt + " " + fyz + " already mapped !");
            return;
        }
        jdbc.update("INSERT INTO physical_virtual_rel(ph_id,acct_host_id) VALUES (?,?)", fyzId, virtId);
    }

    @Override
    public void checkMachinesTablesConsistency() {
        //table acct_host contains PBS nodes as referenced in logs in jobs' attributes exec_host and exec_vhost
        //table physical_hosts contains  physical machines as reported by Perun
        //table physical_virtual_rel assigns them together
        
        //find all nodes that are not assigned to any physical machine
        List<String> strings = jdbc.query("SELECT ah.hostname FROM acct_host ah WHERE ah.acct_host_id NOT IN (SELECT fvr.acct_host_id FROM physical_virtual_rel fvr)",
                new SingleColumnRowMapper<>(String.class));
        if (strings.isEmpty()) return;
        log.warn("database inconsistent, the following hosts are not assigned to physical machines: " + strings);
        log.info("correcting ...");
        //  nejdriv virtualni na fyzicke odstranenim pomlcky
        jdbc.update("INSERT INTO physical_virtual_rel(acct_host_id,ph_id) " +
                "SELECT ah.acct_host_id,ph.id FROM acct_host ah,physical_hosts ph " +
                "WHERE ah.acct_host_id NOT IN (SELECT fvr.acct_host_id FROM physical_virtual_rel fvr) " +
                "AND ah.hostname ~ '-[0-9]+' AND substr(ph.name,0,strpos(ph.name,'.'))=substr(ah.hostname,0,strpos(ah.hostname,'-'))");
        //pak virtualni na existujici fyzicke stejneho jmena
        String virt2phys = "INSERT INTO physical_virtual_rel(acct_host_id,ph_id)" +
                " SELECT ah.acct_host_id,ph.id FROM acct_host ah,physical_hosts ph" +
                " WHERE ah.acct_host_id NOT IN (SELECT fvr.acct_host_id FROM physical_virtual_rel fvr)" +
                " AND ph.name=ah.hostname";
        jdbc.update(virt2phys);
        //pak stroje s pridanym .priv. ve jmene
        jdbc.update("INSERT INTO physical_virtual_rel(acct_host_id,ph_id) " +
                "SELECT ah.acct_host_id,ph.id FROM acct_host ah,physical_hosts ph " +
                "WHERE ah.acct_host_id NOT IN (SELECT fvr.acct_host_id FROM physical_virtual_rel fvr) " +
                "AND substr(ah.hostname,0,strpos(ah.hostname,'.'))||'.priv'||substr(ah.hostname,strpos(ah.hostname,'.'))=ph.name "
                );
        //pak uzly s kratkym jmenem na stroje se stejnym jmenem pred prvni teckou
        jdbc.update("INSERT INTO physical_virtual_rel(acct_host_id,ph_id) " +
                "SELECT ah.acct_host_id,ph.id FROM acct_host ah,physical_hosts ph " +
                "WHERE ah.acct_host_id NOT IN (SELECT fvr.acct_host_id FROM physical_virtual_rel fvr) " +
                "AND ah.hostname ~ '^[a-z]+[0-9]*$' AND substr(ph.name,0,strpos(ph.name,'.'))=ah.hostname"
        );
        //pak vyrobit neexistujici fyzicke - radeji uz ne, aby nevznikaly kratke nazvy
//        jdbc.update("INSERT INTO physical_hosts (name) " +
//                "SELECT ah.hostname FROM acct_host ah " +
//                "WHERE ah.acct_host_id NOT IN (SELECT fvr.acct_host_id FROM physical_virtual_rel fvr)");
        // a znovu virtualni na existujici fyzicke stejneho jmena
//        jdbc.update(virt2phys);
        strings = jdbc.query("SELECT ah.hostname FROM acct_host ah WHERE ah.acct_host_id NOT IN (SELECT fvr.acct_host_id FROM physical_virtual_rel fvr)",
                new SingleColumnRowMapper<>(String.class));
        if(!strings.isEmpty()) {
            log.warn("the following PBS nodes are still not assigned to physical machines: " + strings);
        } else {
            log.info("corrected");
        }
    }


    private void saveReceiveLog(Date now, int cpusTotal, int machinesTotal, boolean changes) {
        jdbc.update("INSERT INTO physical_receive_log (receive_time,cpus_total,machines_total,changes) VALUES (?,?,?,?)",
                now, cpusTotal, machinesTotal, changes);
    }

    private boolean markMachine(Date now, Machine machine, PhysicalResource pr, Set<String> frontends, Set<String> reserveds) {
        PhysicalHost ph = findOrCreatePhysicalHost(machine.getName());
        boolean changes = markCpusForPhysicalHost(now, ph, machine.getCpuNum());
        changes |= markResourceForPhysicalHost(now, pr, ph);
        changes |= markPhysicalHostReserved(now, ph, frontends.contains(ph.getName()), reserveds.contains(ph.getName()));
        return changes;
    }

    private boolean markPhysicalHostReserved(Date now, PhysicalHost ph, boolean frontend, boolean reserved) {

        boolean changes = false;
        List<ReservedRecord> records
                = jdbc.query("SELECT id,frontend,reserved FROM physical_hosts_reserved " +
                        "WHERE ph_id=? ORDER BY end_time DESC, start_time DESC LIMIT 1",
                (rs, rowNum) -> new ReservedRecord(rs.getInt("id"), rs.getBoolean("frontend"), rs.getBoolean("reserved")), ph.getId());
        if (records.size() == 0) {
            //zaznamename, jen pokud je vyhrazeny
            if (frontend || reserved) {
                //zadny zaznam
                log.info("inserting first reserved record for " + ph.getName());
                changes = true;
                jdbc.update("INSERT INTO physical_hosts_reserved (ph_id,start_time,end_time,frontend,reserved) VALUES (?,?,?,?,?)",
                        ph.getId(), now, now, frontend, reserved);
            }
        } else {
            ReservedRecord record = records.get(0);
            //prodlouzime posledni zaznam do soucasneho okamziku
            jdbc.update("UPDATE physical_hosts_reserved SET end_time=? WHERE id=? ", now, record.getId());
            if (record.isFrontend() != frontend | record.isReserved() != reserved) {
                //zmena, zaznamename ji
                log.info("changing [frontend,reserved] from [" + record.isFrontend() + "," + record.isReserved() + "] to [" +
                        frontend + "," + reserved + "] for " + ph.getName());
                changes = true;
                jdbc.update("INSERT INTO physical_hosts_reserved (ph_id,start_time,end_time,frontend,reserved) VALUES (?,?,?,?,?)",
                        ph.getId(), now, now, frontend, reserved);
            }
        }
        return changes;
    }

    private boolean markResourceForPhysicalHost(Date now, PhysicalResource pr, PhysicalHost ph) {
        boolean changes = false;
        //najdeme posledni zaznam
        List<HostResourceRelation> records
                = jdbc.query("SELECT id,ph_resources_id,ph_hosts_id,start_time,end_time FROM physical_hosts_resources_rel"
                        + " WHERE ph_hosts_id=? ORDER BY end_time DESC, start_time DESC LIMIT 1",
                (rs, rowNum) -> new HostResourceRelation(rs.getInt("id"), rs.getInt("ph_hosts_id"), rs.getInt("ph_resources_id"),
                        rs.getTimestamp("start_time"), rs.getTimestamp("end_time")),
                ph.getId());
        if (records.size() == 0) {
            //zadny zaznam, vytvorime novy
            log.info("inserting first resource relation for " + ph.getName());
            changes = true;
            jdbc.update("INSERT INTO physical_hosts_resources_rel (ph_resources_id,ph_hosts_id,start_time,end_time) VALUES (?,?,?,?)",
                    pr.getId(), ph.getId(), now, now);
        } else {
            HostResourceRelation relation = records.get(0);
            //upravime konec posledniho zaznamu
            jdbc.update("UPDATE physical_hosts_resources_rel SET end_time=? WHERE id=?", now, relation.getId());
            if (relation.getResourceId() != pr.getId()) {
                //zmena clusteru
                changes = true;
                log.info("changing resource relation from " + relation.getResourceId() + " to " + pr.getId() + " for " + ph.getName());
                jdbc.update("INSERT INTO physical_hosts_resources_rel (ph_resources_id,ph_hosts_id,start_time,end_time) VALUES (?,?,?,?)",
                        pr.getId(), ph.getId(), now, now);
            }
        }
        return changes;
    }


    private boolean markCpusForPhysicalHost(Date now, PhysicalHost ph, int cpuNum) {
        boolean changes = false;
        //najdeme posledni zaznam
        List<CpuRecord> records = jdbc.query("SELECT id,ph_id,cpu,start_time,end_time FROM physical_hosts_cpu WHERE ph_id=? ORDER BY end_time DESC, start_time DESC LIMIT 1",
                (rs, rowNum) -> new CpuRecord(rs.getInt("id"), rs.getInt("ph_id"), rs.getInt("cpu"), rs.getTimestamp("start_time"), rs.getTimestamp("end_time")), ph.getId());
        if (records.size() == 0) {
            //zatim zadny zaznam, vytvorime uplne novy
            log.info("inserting first cpu count for " + ph.getName());
            changes = true;
            jdbc.update("INSERT INTO physical_hosts_cpu (ph_id,cpu,start_time,end_time) VALUES (?,?,?,?)", ph.getId(), cpuNum, now, now);
        } else {
            CpuRecord cpuRecord = records.get(0);
            //upravime konec posledniho zaznamu
            jdbc.update("UPDATE physical_hosts_cpu SET end_time=? WHERE id=?", now, cpuRecord.getId());
            if (cpuRecord.getCpuNum() != cpuNum) {
                //zmena poctu procesoru, vlozime novejsi zaznam
                log.info("changing cpu count from " + cpuRecord.getCpuNum() + " to " + cpuNum + " for " + ph.getName());
                changes = true;
                jdbc.update("INSERT INTO physical_hosts_cpu (ph_id,cpu,start_time,end_time) VALUES (?,?,?,?)", ph.getId(), cpuNum, now, now);
            }
        }
        return changes;
    }

    private PhysicalHost findPhysicalHost(String name) {
        return jdbc.queryForObject("SELECT * FROM physical_hosts WHERE name=?", (rs, rowNum) -> new PhysicalHost(rs.getInt("id"), rs.getString("name")), name);
    }

    private PhysicalHost findOrCreatePhysicalHost(String name) {
        while (true) {
            try {
                return findPhysicalHost(name);
            } catch (EmptyResultDataAccessException ex) {
                log.info("creating physical host " + name);
                jdbc.update("INSERT INTO physical_hosts (name) VALUES (?)", name);
            }
        }
    }


    private PhysicalResource findPhysicalResource(ComputingResource cr) {
        while (true) {
            try {
                return jdbc.queryForObject("SELECT * FROM physical_resources WHERE name=? AND cluster=?",
                        (rs, rowNum) -> new PhysicalResource(rs.getInt("id"), rs.getString("name"), rs.getBoolean("cluster")),
                        cr.getName(), cr.isCluster());
            } catch (EmptyResultDataAccessException ex) {
                log.info("creating physical resource " + cr.getName());
                jdbc.update("INSERT INTO physical_resources (name,cluster) VALUES (?,?)", cr.getName(), cr.isCluster());
            }
        }
    }


}
