package cz.muni.ics.cerit.stats;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class AccountingImpl implements Accounting {

    final static Logger log = LoggerFactory.getLogger(AccountingImpl.class);
    private JdbcTemplate jdbc;

    public void setDataSource(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Transactional
    @Override
    public void storeHostsAvailability(List<ExpectedHost> expectedHosts) {
        Date now = new Date();
        for (ExpectedHost host : expectedHosts) {
            String hostname = host.getHostname();
            log.debug("storing host {}", hostname);
            Long ph_id = getHostId(hostname);
            List<AvailabilityRecord> records = jdbc.query("select id,ph_id,available,reason,start_time,end_time from physical_hosts_avail where ph_id=? " +
                    "order by end_time desc, start_time desc limit 1", new RowMapper<AvailabilityRecord>() {
                @Override
                public AvailabilityRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return new AvailabilityRecord(rs.getLong("id"), rs.getLong("ph_id"), rs.getBoolean("available"),
                            rs.getString("reason"), rs.getTimestamp("start_time"), rs.getTimestamp("end_time"));
                }
            }, ph_id);

            if (records.size() == 0) {
                //vytvorime prvni
                log.info("inserting first availability record for host {}", hostname);
                jdbc.update("insert into physical_hosts_avail (ph_id,available,reason,start_time,end_time) values (?,?,?,?,?)",
                        ph_id, host.isAvailable(), host.getReason(), now, now);
            } else {
                AvailabilityRecord record = records.get(0);
                if (record.available == host.isAvailable() && record.reason.equals(host.getReason())) {
                    //stejne, prodlouzit cas
                    jdbc.update("update physical_hosts_avail set end_time=? where id=?", now, record.id);
                } else {
                    //vytvorit novy
                    log.info("{} changing from {}/'{}' to {}/'{}'", hostname, record.available, record.reason, host.isAvailable(), host.getReason());
                    jdbc.update("insert into physical_hosts_avail (ph_id,available,reason,start_time,end_time) values (?,?,?,?,?)",
                            ph_id, host.isAvailable(), host.getReason(), now, now);
                }
            }
        }
    }

    @Transactional
    @Override
    public ResourceAvailability getAvailability(String clusterId, DateTime from, DateTime to, boolean detailedRecords) {
        //vytvorim docasnou tabulku pro intervaly existence stroju v clusteru omezene zadanym intervalem statistiky
        jdbc.update("create temporary table hh (ph_id int,name varchar,start_t timestamp with time zone,end_t timestamp with time zone) on commit drop");
        //naplnim ji (z nejakeho duvodu nemuzu dosadit casove konstanty do overlaps pres ?)
        DateTimeFormatter sdf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        String fromString = "'" + sdf.print(from) + "'";
        String toString = "'" + sdf.print(to) + "'";
        jdbc.update("insert into hh (ph_id,name,start_t,end_t) " +
                "select ph.id,ph.name,greatest(phrr.start_time,timestamp " + fromString + ") as start_t, " +
                " least(phrr.end_time,timestamp " + toString + ") as end_t " +
                "from physical_resources pr,physical_hosts_resources_rel phrr,physical_hosts ph " +
                "where pr.name=? and pr.id=phrr.ph_resources_id and phrr.ph_hosts_id=ph.id and " +
                "((phrr.start_time,phrr.end_time) overlaps (timestamp " + fromString + ",timestamp " + toString + "))",
                clusterId);
        //spocitam celkovy pocet strojo-sekund, po ktere stroje existovaly
        Long totalSeconds = jdbc.queryForObject("select extract(epoch from sum(end_t-start_t)) as total_seconds from hh", Long.class);
        log.debug("totalSeconds={}", totalSeconds);
        //planovane vypadky na danem clusteru v dane dobe orezane na existenci stroju v clusteru
        jdbc.update("create temporary table sd (ph_id int,unparsed varchar,name varchar,from_t timestamp with time zone not null, to_t timestamp with time zone not null) on commit drop");
        // nejdriv stroje
        jdbc.update("insert into sd(ph_id,unparsed,name,from_t,to_t) " +
                "select hh.ph_id,dl.unparsed,hh.name,greatest(hh.start_t,dl.start_time) as from_t,least(hh.end_t,dl.end_time) as to_t " +
                "from scheduled_downtime_lines dl, scheduled_downtime_hosts dh, hh " +
                "where dh.line_id=dl.id and dh.ph_id = hh.ph_id and greatest(hh.start_t,dl.start_time)<least(hh.end_t,dl.end_time)");
        // pak clustery
        jdbc.update("insert into sd(ph_id,unparsed,name,from_t,to_t) " +
                "select hh.ph_id,dl.unparsed,hh.name,greatest(hh.start_t,dl.start_time) as from_t,least(hh.end_t,dl.end_time) as to_t " +
                "from scheduled_downtime_lines dl,scheduled_downtime_resources dr,physical_resources pr,hh " +
                "where dr.line_id=dl.id and dr.pr_id=pr.id and pr.name=? and greatest(hh.start_t,dl.start_time)<least(hh.end_t,dl.end_time)",
                clusterId);
        // sectu dobu planovanych vypadku
        Long scheduled_down = jdbc.queryForObject("select extract(epoch from sum(to_t-from_t)) from sd", Long.class);
        if (scheduled_down == null) scheduled_down = 0l;
        log.debug("scheduled_down={}", scheduled_down);
        //dva zpusoby
        //long up_pos = sumOKminusScheduledDown();
        long upSeconds = sumCRITplusScheduledDownminusoverlaps(totalSeconds, scheduled_down);

        //spocitam procento
        double availabilityPercent = ((double) upSeconds / (double) totalSeconds * 100d);
        double reliabilityPercent;
        if (totalSeconds - scheduled_down == 0) {
            log.info("Cannot compute reliability for {}, all time was scheduled down.",clusterId);
            reliabilityPercent = 0d;
        } else {
            reliabilityPercent = ((double) upSeconds / ((double) totalSeconds - scheduled_down) * 100d);
        }
        log.debug("availability: {}%", availabilityPercent);
        log.debug("reliability:  {}%", reliabilityPercent);
        //pokud je zajem, zjistim konkretni vypadky
        List<AvailabilityRecord> records = null;
        if (detailedRecords) {
            records =
                    jdbc.query("select p.id,hh.name,p.reason,p.start_time,p.end_time " +
                            "from physical_hosts_avail p,hh " +
                            "where not p.available and p.ph_id=hh.ph_id and " +
                            "((p.start_time,p.end_time) overlaps(hh.start_t,hh.end_t)) order by start_time",
                            new RowMapper<AvailabilityRecord>() {
                                @Override
                                public AvailabilityRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
                                    return new AvailabilityRecord(rs.getLong("id"),
                                            rs.getString("name"),
                                            false,
                                            rs.getString("reason"),
                                            rs.getTimestamp("start_time"),
                                            rs.getTimestamp("end_time"));
                                }
                            });
        }
        return new ResourceAvailability(clusterId, from, to, records, totalSeconds, availabilityPercent, reliabilityPercent);
    }

    private long sumCRITplusScheduledDownminusoverlaps(Long totalSeconds, Long scheduled_down) {
        //sectu intervaly s available=false
        Long unavail_seconds = jdbc.queryForObject(
                "select extract(epoch from sum(least(p.end_time,hh.end_t)-greatest(p.start_time,hh.start_t))) as unavail_seconds " +
                        "from physical_hosts_avail p,hh where not p.available and p.ph_id=hh.ph_id " +
                        "and ((p.start_time,p.end_time) overlaps(hh.start_t,hh.end_t))", Long.class);
        if (unavail_seconds == null) unavail_seconds = 0l;
        //sectu prekryvy  available=false s planovanycmi vypadky
        Long unavail_in_down_seconds = jdbc.queryForObject(
                "select extract(epoch from sum(least(p.end_time,sd.to_t)-greatest(p.start_time,sd.from_t))) as unav_in_down " +
                        "from sd,physical_hosts_avail p where sd.ph_id=p.ph_id and not p.available " +
                        "and (p.start_time,p.end_time) overlaps (sd.from_t,sd.to_t)", Long.class);
        if (unavail_in_down_seconds == null) unavail_in_down_seconds = 0l;
        return totalSeconds - unavail_seconds - scheduled_down + unavail_in_down_seconds;
    }

    @SuppressWarnings("UnusedDeclaration")
    private long sumOKminusScheduledDown() {
        //sectu intervaly available=true s prictenim 5 minut ke kazdemu
        Long avail_seconds = jdbc.queryForObject(
                "select extract(epoch from sum(least(p.end_time+interval '5 minutes',hh.end_t)-greatest(p.start_time,hh.start_t))) as avail_seconds" +
                        " from physical_hosts_avail p,hh where p.available and p.ph_id=hh.ph_id " +
                        "and ((p.start_time,p.end_time+interval '5 minutes') overlaps(hh.start_t,hh.end_t))", Long.class);
        // a odectu jejich prekryvy s planovanymi vypadky
        Long ok_in_down_seconds = jdbc.queryForObject(
                "select extract(epoch from sum(least(p.end_time+interval '5 minutes',sd.to_t)-greatest(p.start_time,sd.from_t))) as ok_in_down_seconds " +
                        "from sd,physical_hosts_avail p where sd.ph_id=p.ph_id and p.available " +
                        "and (p.start_time,p.end_time+interval '5 minutes') overlaps (sd.from_t,sd.to_t)", Long.class);
        if (ok_in_down_seconds == null) ok_in_down_seconds = 0l;
        return avail_seconds - ok_in_down_seconds;
    }

    @Transactional
    @Override
    public void storeStatsForDay(String clusterId, DateTime day, ResourceAvailability availability) {
        Long resourceId = getResourceId(clusterId);
        java.sql.Date date = new java.sql.Date(day.getMillis());
        List<Long> ids = jdbc.queryForList("select id from availability_reliability_stats where pr_id=? and day=?", Long.class, resourceId, date);
        if (ids.size() == 1) {
            jdbc.update("update availability_reliability_stats set availability_percent=?,reliability_percent=? where id=?",
                    availability.getPercentAvailable(), availability.getPercentReliable(), ids.get(0));
        } else {
            jdbc.update("insert into availability_reliability_stats (pr_id,day,availability_percent,reliability_percent) values (?,?,?,?)",
                    resourceId, date, availability.getPercentAvailable(), availability.getPercentReliable());
        }
    }

    @Override
    public DateTime getClusterStartDate(String clusterName) {
        Timestamp enabledTime = jdbc.queryForObject("select enabled_time from physical_resources where name = ?", Timestamp.class, clusterName);
        return new DateTime(enabledTime).withTimeAtStartOfDay().plusDays(1);
    }

    @Transactional
    @Override
    public ResourceAvailability getAvailability(String clusterId, DateTime day) {
        return getAvailability(clusterId, day, day.plusDays(1), false);
    }

    private Long getHostId(String hostname) {
        List<Long> ids = jdbc.queryForList("select id from physical_hosts where name = ?", Long.class, hostname);
        if (ids.size() == 1) {
            return ids.get(0);
        } else if (ids.size() == 0) {
            log.info("creating physical host {}", hostname);
            return jdbc.queryForObject("insert into physical_hosts(name) values (?) returning id", Long.class, hostname);
        } else {
            log.error("host " + hostname + " has " + ids.size() + " records");
            return ids.get(0);
        }
    }

    private Long getResourceId(String fqdn) {
        List<Long> ids = jdbc.queryForList("select id from physical_resources where name=?", Long.class, fqdn);
        return ids.size() == 1 ? ids.get(0) : null;
    }

}
