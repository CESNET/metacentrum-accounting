package cz.cesnet.meta.acct.hw.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */

@SuppressWarnings("deprecation")
public class StatsImpl implements Stats {

    private final static Logger logHW = LoggerFactory.getLogger("cz.cesnet.meta.acct.hw.stats.StatsImpl.hw");
    private final static Logger logTime = LoggerFactory.getLogger("cz.cesnet.meta.acct.hw.stats.StatsImpl.time");
    private final static Logger logExe = LoggerFactory.getLogger("cz.cesnet.meta.acct.hw.stats.StatsImpl.exe");
    private final static Logger logSQL = LoggerFactory.getLogger("cz.cesnet.meta.acct.hw.stats.StatsImpl.sql");

    private static final SingleColumnRowMapper<String> STRING_MAPPER = new SingleColumnRowMapper<>(String.class);

    private static final RowMapper<JobRecord> JOB_RECORD_MAPPER = (rs, rowNum) -> new JobRecord(
            rs.getString(1),
            rs.getString(2),
            rs.getInt("cpu_number"),
            new Timestamp(rs.getLong("start_time") * 1000L),
            new Timestamp(rs.getLong("end_time") * 1000L)
    );
    private JdbcTemplate jdbc;

    private static long computeOverlappingJobsCpuTime(List<JobRecord> jobs, Timestamp start, Timestamp end) {
        long odecist = 0;

        for (JobRecord job : jobs) {
            //bezel job mezi danymi casovymi okamziky ?
            if ((job.getStart().getTime() <= end.getTime()) && (job.getEnd().getTime() >= start.getTime())) {
                //oriznu dobu behu jobu na obdobi
                long z = Math.max(job.getStart().getTime(), start.getTime());
                long k = Math.min(job.getEnd().getTime(), end.getTime());
                logExe.trace("odecitam job {} celkem {} cpums", job, k - z);
                odecist += (k - z);
            }
        }
        return odecist;
    }

    @SuppressWarnings("UnnecessaryContinue")
    private static Map<String, OutageRecord.Type> assesVirtualMachinesStates(Map<String, List<OutageRecord>> m1) {
        Map<String, OutageRecord.Type> vyhodnocene = new HashMap<>(m1.size() + 1);
        for (String virtName : m1.keySet()) {
            List<OutageRecord> recs = m1.get(virtName);
            boolean xentest = false;
            boolean maintenance = false;
            boolean reserved = false;
            for (OutageRecord or : recs) {
                switch (or.getType()) {
                    case xentest:
                        xentest = true;
                        break;
                    case node_down:
                    case maintenance:
                        maintenance = true;
                        break;
                    case reserved:
                        reserved = true;
                        break;
                    default:
                        throw new RuntimeException("moznost " + or.getType() + " neznama!");
                }
            }
            if (xentest) {
                continue;
            } else if (reserved) {
                vyhodnocene.put(virtName, OutageRecord.Type.reserved);
                continue;
            } else if (maintenance) {
                vyhodnocene.put(virtName, OutageRecord.Type.maintenance);
            } else {
                throw new RuntimeException(virtName + ": neni zadne z xentest|reserved|maintenance - " + recs);
            }

        }
        return vyhodnocene;
    }

    private static List<OutageRecord> extractActiveOutagesForPeriod(PhysicalMachineRecord fmr, List<OutageRecord> outs, Timestamp start, Timestamp end) {
        List<OutageRecord> aktivni = new ArrayList<>(fmr.getVirtCount());
        for (OutageRecord or : outs) {
            if (or.getStart().getTime() <= start.getTime() && (or.getEnd() == null || or.getEnd().getTime() >= end.getTime())) {
                aktivni.add(or);
            }
        }
        return aktivni;
    }

    private static Map<String, List<OutageRecord>> computeVirtualHostMap(List<OutageRecord> outs) {
        //rozdelime si vypadky podle virtualnich stroju
        Map<String, List<OutageRecord>> m1 = new HashMap<>();
        for (OutageRecord out : outs) {
            List<OutageRecord> list = m1.get(out.getPbsHostName());
            if (list == null) {
                list = new ArrayList<>(2);
                m1.put(out.getPbsHostName(), list);
            }
            list.add(out);
        }
        return m1;
    }

    private static List<Timestamp> divideDayToHomogeneousPeriods(ClusterAtDay c, List<OutageRecord> outs) {
        //rozdelime den na homogenni useky
        SortedSet<Timestamp> okamziky = new TreeSet<>();
        okamziky.add(c.getDate());
        okamziky.add(c.getNextDay());
        for (OutageRecord or : outs) {
            okamziky.add(or.getStart());
            okamziky.add(or.getEnd());
        }
        return new ArrayList<>(okamziky);
    }

    public void setDataSource(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Override
    @Transactional(readOnly = true)
    public void computeStats(ClusterAtDay c) {
        logExe.debug("entering computeStats({})",c.getId());
        if (!wasClusterActive(c)) {
            logExe.debug("cluster was not active on that day");
            return;
        } else {
            logExe.debug("cluster was active on that day");
        }
        long end;
        long start = System.currentTimeMillis();

        //jmenovatel
        countCpus(c);
        end = System.currentTimeMillis();
        logExe.debug("counted CPUs = {}",c.getCpuCount());
        logTime.debug("countCpus: " + (end - start) + "ms");
        start = end;

        //citatel i jmenovatel - reserved i maintenance
        countOutagesTime(c);
        end = System.currentTimeMillis();
        logTime.debug("countOutagesTime: " + (end - start) + "ms");
        start = end;
        //citatel
        countJobsTime(c);
        end = System.currentTimeMillis();
        logTime.debug("countJobsTime: " + (end - start) + "ms");
        start = end;
        countPerunReservedTime(c);
        end = System.currentTimeMillis();
        logTime.debug("countPerunReservedTime: " + (end - start) + "ms");
        //start = end;
    }

    @Override
    public List<String> getClusters(GregorianCalendar start, GregorianCalendar end) {
        //vraci clustery, ktere byly zapnuty pred koncem zkoumaneho obdobi, a zaroven vypnuty po zacatku obdobi nebo nebyly vypnuty vubec
        //jinymi slovy, clustery jejichz obdobi zapnuti se prekryva se zkoumanym obdobim
        logSQL.debug("get clusters");
        return jdbc.query("SELECT name FROM physical_resources WHERE (enabled_time <= ?) " +
                "AND (disabled_time IS NULL OR disabled_time >= ?) ORDER BY name", STRING_MAPPER, end, start);
    }

    @Override
    public boolean wasClusterActive(ClusterAtDay c) {
        logSQL.debug("was cluster active?");
        List<Boolean> l = jdbc.queryForList("SELECT TRUE FROM physical_resources WHERE name=? " +
                        "AND ? >= enabled_time AND (disabled_time IS NULL OR ? <= disabled_time )",
                Boolean.class, c.getCluster(), c.getDate(), c.getDate());
        return l.size() > 0;
    }

    @Override
    @Transactional
    public void saveStats(ClusterAtDay c) {
        logSQL.debug("get cluster id");
        Integer clusterId = jdbc.queryForObject("SELECT id FROM physical_resources WHERE name=?", Integer.class, c.getCluster());
        logSQL.debug("check workload existence");
        List<Integer> ids = jdbc.query("SELECT id FROM workload WHERE ph_resources_id=? AND day=?",
                new SingleColumnRowMapper<>(Integer.class),
                clusterId, c.getDate());
        if (ids.isEmpty()) {
            logSQL.debug("inserting workload");
            jdbc.update("INSERT INTO workload (ph_resources_id,day," +
                            "cpu,cputime,maintenance,reserved,perun_reserved,jobs_time," +
                            "maintenance_ratio,reserved_ratio,utilization_ratio,raw_ratio,jobs_ratio) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    clusterId, c.getDate(),
                    c.getCpuCount(), c.getAllCpuTime(), c.getMaintenanceCpuTime(), c.getReservedCpuTime(), c.getPerunReservedCpuTime(), c.getJobsTime(),
                    c.getMaintenanceRatio() * 100d, c.getReservedRatio() * 100d, c.getUtilizationRatio() * 100d, c.getRawUtilizationRatio() * 100d, c.getJobsRatio() * 100d);
        } else {
            logSQL.debug("updating workload");
            jdbc.update("UPDATE workload SET cpu=?,cputime=?,maintenance=?,reserved=?,perun_reserved=?,jobs_time=?,maintenance_ratio=?,reserved_ratio=?,utilization_ratio=?,raw_ratio=?,jobs_ratio=? WHERE id=?",
                    c.getCpuCount(), c.getAllCpuTime(), c.getMaintenanceCpuTime(), c.getReservedCpuTime(), c.getPerunReservedCpuTime(), c.getJobsTime(),
                    c.getMaintenanceRatio() * 100d, c.getReservedRatio() * 100d, c.getUtilizationRatio() * 100d, c.getRawUtilizationRatio() * 100d, c.getJobsRatio() * 100d,
                    ids.get(0));
        }

    }

    @Override
    public void checkReceiveLogs(Calendar start, Calendar end) {
        Calendar c = (Calendar) start.clone();
        while (c.getTimeInMillis() <= end.getTimeInMillis()) {
            Timestamp day = new Timestamp(c.getTimeInMillis());
            c.add(Calendar.DATE, 1);
            Timestamp nextDay = new Timestamp(c.getTimeInMillis());

            int count = jdbc.queryForObject(
                    "SELECT count(*) FROM acct_receive_pbs r, ci_acct_pbs_server s WHERE r.ci_acct_pbs_server_id=s.ci_acct_pbs_server_id" +
                            "  AND receive_time>=? AND receive_time<=?", Integer.class, day, nextDay);
            if (count != 2) {
                System.out.println("received " + count + " logs on " + day);
                List<Map<String, Object>> res = jdbc.queryForList("SELECT receive_time,hostname," +
                        " minimal_time ," +
                        " maximal_time  " +
                        "FROM acct_receive_pbs r, ci_acct_pbs_server s WHERE r.ci_acct_pbs_server_id=s.ci_acct_pbs_server_id" +
                        "  AND receive_time>=? AND receive_time<=?", day, nextDay);
                for (Map<String, Object> m : res) {
                    long min = (Long) m.get("minimal_time");
                    long max = (Long) m.get("maximal_time");

                    System.out.println("     " + m.get("hostname") + " " + new Timestamp(min) + " - " + new Timestamp(max));
                }
            }
        }
    }

    @Override
    public List<WorkloadRecord> getWorkload(String cluster, Calendar start, Calendar end) {
        logSQL.debug("get workload");
        return jdbc.query("SELECT w.* FROM physical_resources pr,workload w " +
                        "WHERE  pr.name=? AND pr.id=w.ph_resources_id " +
                        "AND day>=? AND day<=? ORDER BY day",
                (rs, rowNum) -> new WorkloadRecord(
                        rs.getDate("day"),
                        rs.getInt("cpu"),
                        rs.getLong("cputime"),
                        rs.getLong("maintenance"),
                        rs.getLong("reserved"),
                        rs.getLong("perun_reserved"),
                        rs.getLong("jobs_time"),
                        rs.getDouble("maintenance_ratio"),
                        rs.getDouble("reserved_ratio"),
                        rs.getDouble("utilization_ratio"),
                        rs.getDouble("raw_ratio")
                ), cluster, start, end);
    }

    private void countJobsTime(ClusterAtDay c) {
        //cas uloh
        //z resource se najdou PBS stroje
        //pro kazdou ulohu je v acct_hosts_logs tolik radku, kolik pouzila procesoru
        //berou se jen procesory patrici do clusteru
        logSQL.debug("jobs non-exclusive time");
        long jobsNonexclusiveTime = queryForLong(jdbc.queryForObject(
                "SELECT sum(least(j.end_time,?)- greatest(j.start_time,?)) " +
                        "FROM acct_host h, acct_pbs_record j, acct_hosts_logs l,physical_resources pr," +
                        "physical_hosts_resources_rel phrr,physical_hosts ph,physical_virtual_rel fvr " +
                        "WHERE j.start_time <= ? " +
                        "  AND j.end_time >= ? " +
                        "  AND j.acct_id_string=l.acct_id_string " +
                        "  AND l.acct_host_id=h.acct_host_id " +
                        "  AND pr.name=? AND pr.id=phrr.ph_resources_id AND phrr.ph_hosts_id=ph.id  " +
                        "  AND phrr.start_time<=? AND ?<phrr.end_time  " +
                        "  AND ph.id=fvr.ph_id AND fvr.acct_host_id=h.acct_host_id  " +
                        "  AND NOT j.req_nodes ~ '#excl'", //jen neexkluzivni vyhrazeni
                Long.class,
                c.getNextDayAsLinuxTime(), c.getDateAsLinuxTime(),
                c.getNextDayAsLinuxTime(), c.getDateAsLinuxTime(),
                c.getCluster(),
                c.getDate(), c.getDate()
        ));
        //pri pouziti #excl se zaberou vsechny procesory, musime dohledat kolik jich bylo
        //ale zapocitat kazdy stroj jen jednou, proto je tam distinct
        logSQL.debug("jobs exclusive time");

        String sql =  "SELECT sum( (end_time-start_time)*cpu ) FROM (" +
                        "SELECT DISTINCT" +
                        "     j.acct_id_string,j.req_nodes," +
                        "     least(j.end_time,?) AS end_time," +
                        "     greatest(j.start_time,?) AS start_time," +
                        "     h.hostname,ph.name,phcpu.cpu " +
                        "FROM acct_host h, acct_pbs_record j, acct_hosts_logs l,physical_resources pr,physical_hosts_resources_rel phrr," +
                        "     physical_hosts ph,physical_virtual_rel fvr,physical_hosts_cpu phcpu " +
                        "WHERE j.start_time <= ? " +
                        "  AND j.end_time >= ? " +
                        "  AND j.acct_id_string=l.acct_id_string" +
                        "  AND l.acct_host_id=h.acct_host_id" +
                        "  AND pr.name=? AND pr.id=phrr.ph_resources_id AND phrr.ph_hosts_id=ph.id" +
                        "  AND phrr.start_time<? AND ?<phrr.end_time" +
                        "  AND ph.id=fvr.ph_id AND fvr.acct_host_id=h.acct_host_id" +
                        "  AND ph.id=phcpu.ph_id AND phcpu.start_time<? AND ?<phcpu.end_time" +
                        "  AND j.req_nodes ~ '#excl'" +
                        ") AS t1";
        long jobsExclusiveTime = queryForLong(jdbc.queryForObject(sql,
                Long.class,
                c.getNextDayAsLinuxTime(), c.getDateAsLinuxTime(),
                c.getNextDayAsLinuxTime(), c.getDateAsLinuxTime(),
                c.getCluster(),
                c.getDate(), c.getDate(),
                c.getDate(), c.getDate()
        ));
        if (logExe.isDebugEnabled()) {
            logExe.debug("jobsNonexclusiveTime={}", jobsNonexclusiveTime);
            logExe.debug("jobsExclusiveTime={}", jobsExclusiveTime);
        }
        logSQL.debug("started jobs non-exclusive time");
        long startedNonexclusiveJobsTime = queryForLong(jdbc.queryForObject(
                "SELECT sum(? - greatest(j.start_time,?)) " +
                        "FROM acct_host h, acct_pbs_record_started j, acct_hosts_logs_started l,physical_resources pr," +
                        "physical_hosts_resources_rel phrr,physical_hosts ph,physical_virtual_rel fvr " +
                        "WHERE j.start_time <= ? " +
                        "  AND j.acct_id_string=l.acct_id_string " +
                        "  AND l.acct_host_id=h.acct_host_id " +
                        "  AND pr.name=? AND pr.id=phrr.ph_resources_id AND phrr.ph_hosts_id=ph.id  " +
                        "  AND phrr.start_time<=? AND ?<phrr.end_time  " +
                        "  AND ph.id=fvr.ph_id AND fvr.acct_host_id=h.acct_host_id  " +
                        "  AND NOT j.req_nodes ~ '#excl'",
                Long.class,
                c.getNextDayAsLinuxTime(), c.getDateAsLinuxTime(),
                c.getNextDayAsLinuxTime(),
                c.getCluster(),
                c.getDate(), c.getDate()
        ));
        logSQL.debug("started jobs exclusive time");
        long startedExclusiveJobsTime = queryForLong(jdbc.queryForObject(
                "SELECT sum( (?-start_time)*cpu ) FROM (" +
                        "SELECT DISTINCT" +
                        "     j.acct_id_string,j.req_nodes," +
                        "     greatest(j.start_time,?) AS start_time," +
                        "     h.hostname,ph.name,phcpu.cpu " +
                        "FROM acct_host h, acct_pbs_record_started j, acct_hosts_logs_started l,physical_resources pr,physical_hosts_resources_rel phrr," +
                        "     physical_hosts ph,physical_virtual_rel fvr,physical_hosts_cpu phcpu " +
                        "WHERE j.start_time <= ? " +
                        "  AND j.acct_id_string=l.acct_id_string" +
                        "  AND l.acct_host_id=h.acct_host_id" +
                        "  AND pr.name=? AND pr.id=phrr.ph_resources_id AND phrr.ph_hosts_id=ph.id" +
                        "  AND phrr.start_time<? AND ?<phrr.end_time" +
                        "  AND ph.id=fvr.ph_id AND fvr.acct_host_id=h.acct_host_id" +
                        "  AND ph.id=phcpu.ph_id AND phcpu.start_time<? AND ?<phcpu.end_time" +
                        "  AND j.req_nodes ~ '#excl'" +
                        ") AS t1",
                Long.class,
                c.getNextDayAsLinuxTime(), c.getDateAsLinuxTime(),
                c.getNextDayAsLinuxTime(),
                c.getCluster(),
                c.getDate(), c.getDate(),
                c.getDate(), c.getDate()
        ));

        if (startedExclusiveJobsTime > 0 || startedNonexclusiveJobsTime > 0) {
            if (logExe.isDebugEnabled()) {
                logExe.debug("startedJobsNonexclusiveTime={}", startedNonexclusiveJobsTime);
                logExe.debug("starttedJobsExclusiveTime={}", startedExclusiveJobsTime);
            }
        }
        if (c.getAllCpuTime() < (jobsNonexclusiveTime + jobsExclusiveTime + startedExclusiveJobsTime + startedNonexclusiveJobsTime)) {
            System.out.println("Pozor pro den " + c.getDate() + " je vice propocitano nez dostupno:");
            System.out.println("jobsNonexclusiveTime / jobsExclusiveTime: " + jobsNonexclusiveTime + " / " + jobsExclusiveTime);
            System.out.println("startedNonexclusiveJobsTime / startedExclusiveJobsTime: " + startedNonexclusiveJobsTime + " / " + startedExclusiveJobsTime);
            if (logExe.isDebugEnabled()) {
                List<Map<String, Object>> mapListJobsNonex = jdbc.queryForList(
                        "SELECT j.acct_id_string AS jobid, h.hostname, l.cpu_number, to_timestamp(least(j.end_time,?)) AS time2, to_timestamp(greatest(j.start_time,?)) AS time1  " +
                                "FROM acct_host h, acct_pbs_record j, acct_hosts_logs l,physical_resources pr," +
                                "physical_hosts_resources_rel phrr,physical_hosts ph,physical_virtual_rel fvr " +
                                "WHERE j.start_time <= ? " +
                                "  AND j.end_time >= ? " +
                                "  AND j.acct_id_string=l.acct_id_string " +
                                "  AND l.acct_host_id=h.acct_host_id " +
                                "  AND pr.name=? AND pr.id=phrr.ph_resources_id AND phrr.ph_hosts_id=ph.id  " +
                                "  AND phrr.start_time<=? AND ?<phrr.end_time  " +
                                "  AND ph.id=fvr.ph_id AND fvr.acct_host_id=h.acct_host_id  " +
                                "  AND NOT j.req_nodes ~ '#excl' ORDER BY 2,3,4", //jen neexkluzivni vyhrazeni
                        c.getNextDayAsLinuxTime(), c.getDateAsLinuxTime(),
                        c.getNextDayAsLinuxTime(), c.getDateAsLinuxTime(),
                        c.getCluster(),
                        c.getDate(), c.getDate()
                );
                logExe.debug("jobs ended non-exclusive:");
                for (Map<String, Object> j : mapListJobsNonex) {
                    logExe.debug(" job {} {} {} {} {}", j.get("jobid"), j.get("hostname"), j.get("cpu_number"), j.get("time1"), j.get("time2"));
                }
                List<Map<String, Object>> mapListStartedJobsNonEx = jdbc.queryForList(
                        "SELECT j.acct_id_string AS jobid, h.hostname, l.cpu_number, to_timestamp(?) AS time2, to_timestamp(greatest(j.start_time,?)) AS time1 " +
                                "FROM acct_host h, acct_pbs_record_started j, acct_hosts_logs_started l,physical_resources pr," +
                                "physical_hosts_resources_rel phrr,physical_hosts ph,physical_virtual_rel fvr " +
                                "WHERE j.start_time <= ? " +
                                "  AND j.acct_id_string=l.acct_id_string " +
                                "  AND l.acct_host_id=h.acct_host_id " +
                                "  AND pr.name=? AND pr.id=phrr.ph_resources_id AND phrr.ph_hosts_id=ph.id  " +
                                "  AND phrr.start_time<=? AND ?<phrr.end_time  " +
                                "  AND ph.id=fvr.ph_id AND fvr.acct_host_id=h.acct_host_id  " +
                                "  AND NOT j.req_nodes ~ '#excl' ORDER BY 2,3,4",
                        c.getNextDayAsLinuxTime(), c.getDateAsLinuxTime(),
                        c.getNextDayAsLinuxTime(),
                        c.getCluster(),
                        c.getDate(), c.getDate()
                );
                logExe.debug("jobs started non-exclusive:");
                for (Map<String, Object> j : mapListStartedJobsNonEx) {
                    logExe.debug(" job {} {} {} {} {}", j.get("jobid"), j.get("hostname"), j.get("cpu_number"), j.get("time1"), j.get("time2"));
                }
            }
        }
        c.setJobsTime(jobsNonexclusiveTime + jobsExclusiveTime + startedExclusiveJobsTime + startedNonexclusiveJobsTime);
    }

    private void countPerunReservedTime(ClusterAtDay c) {
        // cas jako frontend a cas vyhrazeny v Perunovi
        logSQL.debug("perun reserved time");
        List<Long> longs = jdbc.query(
                //orezani na pulnoci
                "SELECT phcpu.cpu,greatest(rez.start_time,?),least(rez.end_time,?) "
                        + "FROM physical_resources pr,physical_hosts_resources_rel phrr,physical_hosts ph," +
                        " physical_hosts_reserved rez,physical_hosts_cpu phcpu " +
                        //byly v dane dobe v danem clusteru
                        "WHERE pr.name=? AND pr.id=phrr.ph_resources_id AND phrr.ph_hosts_id=ph.id " +
                        "   AND phrr.start_time<=? AND ?<phrr.end_time AND rez.ph_id=ph.id " +
                        //mely v te dobe CPU
                        "   AND ph.id=phcpu.ph_id AND phcpu.start_time<=? AND ?<phcpu.end_time " +
                        // a byly v te dobe rezervovany
                        " AND rez.start_time<? AND ?<rez.end_time" +
                        " AND (rez.frontend = TRUE OR rez.reserved = TRUE)",
                (rs, rowNum) -> rs.getInt("cpu") * (rs.getTimestamp(3).getTime() - rs.getTimestamp(2).getTime()),
                c.getDate(), c.getNextDay(),
                c.getCluster(),
                c.getDate(), c.getDate(),
                c.getDate(), c.getDate(),
                c.getDate(), c.getDate());
        long sum = 0L;
        for (long l : longs) {
            sum += l;
        }
        c.setPerunReservedCpuTime(sum);
    }

    private void countOutagesTime(ClusterAtDay c) {
        logExe.debug("countOutagesTime({})", c.getId());
        long maintenanceCpuMillis = 0;
        long reservedCpuMillis = 0;
        List<PhysicalMachineRecord> phys = findPhysicalMachinesWithOutages(c);
        if (logExe.isDebugEnabled()) {
            if (phys.size() > 0) {
                logExe.debug("findPhysicalMachinesWithOutages() produced:");
                for (PhysicalMachineRecord pmr : phys) {
                    logExe.debug("    {}", pmr);
                }
                logExe.debug("");
            } else {
                logExe.debug("List<PhysicalMachineRecord> is empty");
            }
        }
        //pro kazdy fyzicky stroj
        for (PhysicalMachineRecord pmr : phys) {
            logExe.debug("=== physical machine {}", pmr);
            // najde vypadky na vsech jeho virtualnich strojich
            List<OutageRecord> outs = findOutagesOnPhysicalMachine(c, pmr);
            if (logExe.isDebugEnabled() && outs.size() > 0) {
                logExe.debug("    findOutagesOnPhysicalMachine({}) produced: ", pmr);
                for (OutageRecord outageRecord : outs) {
                    logExe.debug("        {}", outageRecord);
                }
            }
            //rozdeli den na homogenni useky
            List<Timestamp> ok2 = divideDayToHomogeneousPeriods(c, outs);
            if (logExe.isDebugEnabled() && ok2.size() > 0) {
                logExe.debug("    divideDayToHomogeneousPeriods() produced: {}", ok2);
            }

            //najde joby v ten den bezici na danem fyz. stroji
            List<JobRecord> jobs = findJobsOnPhysicalMachine(c, pmr);
            if (logExe.isDebugEnabled() && jobs.size() > 1) {
                logExe.debug("    findJobsOnPhysicalMachine({}) produced: ", pmr);
                for (JobRecord job : jobs) {
                    logExe.debug("        {}", job);
                }
            }

            //pro kazdy homogenni casovy usek dne
            for (int i = 0; i < ok2.size() - 1; i++) {
                Timestamp start = ok2.get(i);
                Timestamp end = ok2.get(i + 1);
                //najdeme vypadky, ktere v te dobe byly aktivni
                List<OutageRecord> activeOutages = extractActiveOutagesForPeriod(pmr, outs, start, end);
                //rozdelime je podle virt. hostu
                Map<String, List<OutageRecord>> virtualHostMap = computeVirtualHostMap(activeOutages);
                logExe.trace("    homogenni usek {} --- {}", start, end);
                logExe.trace("    extractActiveOutagesForPeriod(): {} ", activeOutages);
                logExe.trace("    map<virtHost,outages> ={}", virtualHostMap);

                // doplneno Dalibor Klusacek
                // zjistime zda alespon jeden vypadek byl v dom0 -> pokud ano, pak cely fyz. stroj je down
                // zaroven osetrime pripad, ze vsechny domU byly ve vypadku/reserved a dom0 nebyl -> pokud ano, pak je opet cely fyz. stroj down
                boolean fyz_machine_maintenance_found = false;
                boolean dom0_found_in_outages = false;
                int domU_outages_count = 0;

                // zjistime konkretni typ "vypadku" -> tedy napr. maintenance nebo reserved
                Map<String, OutageRecord.Type> virtNames2StateMap = assesVirtualMachinesStates(virtualHostMap);
                // projdeme vsechny stroje s "vypadky"
                for (String virt_host_name : virtualHostMap.keySet()) {
                    OutageRecord.Type outage_type = virtNames2StateMap.get(virt_host_name);
                    // nasli jsme ve "vypadcich" i dom0
                    if (isHostNameDom0(virt_host_name, pmr)) {
                        dom0_found_in_outages = true;
                    } else {
                        domU_outages_count++;
                    }
                    // je to dom0 a existuje tam vypadek typu maintenance, pak koncime s hledanim a prohlasujeme cely stroj za down
                    if (dom0_found_in_outages && outage_type == OutageRecord.Type.maintenance) {
                        fyz_machine_maintenance_found = true;
                        break;
                    }
                }


                boolean outages_on_all_domU_found = false;
                // pokud jsou 2 vypadky na domU pak je treba vyhodnotit typy outages, coz vynutime pomoci: outages_on_all_domU_found = true;
                if (domU_outages_count >= pmr.getVirtCount() - 1) {
                    outages_on_all_domU_found = true;
                }
                // konec doplneni Dalibor

                //vyhodnotit (Dalibor zmenil podminku, puvodni: virtualHostMap.size() < fmr.getVirtCount())
                if (!outages_on_all_domU_found && !fyz_machine_maintenance_found) {
                    //existoval pouzitelny virtualni stroj, takze nic
                    logExe.trace("    virtualHostMap.size()={}  fmr.getVirtCount()={}", virtualHostMap.size(), pmr.getVirtCount());
                    logExe.debug("    na {} obdobi {} --- {} bylo bezny provoz", pmr.getName(), start, end);
                } else if (virtualHostMap.size() > pmr.getVirtCount()) {
                    throw new RuntimeException("neni mozne! na fyzickem stroji " + pmr.getName() + " s " + pmr.getVirtCount()
                            + " virtualnimi stroji bylo " + virtualHostMap.size() + " vypadku - " + activeOutages);

                } else if (outages_on_all_domU_found || fyz_machine_maintenance_found) {
                    // (Dalibor pridal zmenil podminku, puvodne bylo: virtualHostMap.size() == fmr.getVirtCount()
                    //v.stroje v xentestu ignorujeme
                    //pokud byl aspon nejaky reserved, stroj byl reserved
                    //pokud byly vsechny maintenance, byl maintenance
                    //kdyz byl down, jako kdyby byl v maintenance
                    //pak odecist joby
                    // Dalibor: pokud byl dom0 v maintenance je vse v maintenance

                    Map<String, OutageRecord.Type> virtName2StateMap = assesVirtualMachinesStates(virtualHostMap);
                    logExe.trace("    virtName2StateMap: {}", virtName2StateMap);

                    boolean atLeastOneReserved = false;
                    boolean allMaintenance = true;
                    for (String key : virtName2StateMap.keySet()) {
                        OutageRecord.Type type = virtName2StateMap.get(key);
                        if (type == OutageRecord.Type.reserved) {
                            atLeastOneReserved = true;
                        }
                        // musime pohlidat, ze kontrolujeme pouze -1 a -2 stroje, nikoliv dom0, ktera teoreticky nemusi byt pouze v maintenance
                        if (type != OutageRecord.Type.maintenance && !isHostNameDom0(key, pmr)) {
                            allMaintenance = false;
                        }
                    }

                    // Dalibor: pridana podminka fyz_machine_maintenance_found == true -> pak musime nastavit: allMaintenance = true;
                    // a ignorujeme rezervace, protoze cely stroj byl dole
                    if (fyz_machine_maintenance_found) {
                        allMaintenance = true;
                        atLeastOneReserved = false;
                    }

                    long periodMillis = end.getTime() - start.getTime();
                    if (atLeastOneReserved) {
                        logHW.debug("    na {} obdobi {} --- {} bylo reserved", pmr.getName(), start, end);
                        reservedCpuMillis += pmr.getCpuCount() * periodMillis;
                        reservedCpuMillis -= computeOverlappingJobsCpuTime(jobs, start, end);
                    } else if (allMaintenance) {
                        logHW.debug("    na {} obdobi {} --- {} bylo maintenance", pmr.getName(), start, end);

                        long avail = pmr.getCpuCount() * periodMillis;
                        long used = computeOverlappingJobsCpuTime(jobs, start, end);

                        maintenanceCpuMillis += pmr.getCpuCount() * periodMillis;
                        maintenanceCpuMillis -= used;

                        if (avail < used) {
                            System.out.println("---Problem---");
                            System.out.println("Homogeneous time period was = " + start + " -- " + end + ", CPUs = " + pmr.getCpuCount());
                            System.out.println();
                            System.out.println("Jobs on " + pmr.getName() + ": ");
                            for (JobRecord job : jobs) {
                                System.out.println(job.toString());
                            }
                            System.out.println(pmr.getName() + " error avail = " + avail + " used = " + used + " diff = " + (avail - used) + "");
                            //System.out.println(fmr.getName()+" Allmaintanance = "+allMaintenance+" fyzDown = "+fyz_machine_maintenance_found+ " reserved = "+atLeastOneReserved);
                            System.out.println(pmr.getName() + " Seznam vypadku virtualHostMap:\n " + virtualHostMap);
                            System.out.println(pmr.getName() + " --------------------------------------------------  ");
                        }
                    } else {
                        logHW.warn("obdobi {} --- {} je podivne, stavy=" + virtName2StateMap, start, end);
                    }
                } else {
                    throw new RuntimeException("chyba v predivu vesmiru, tahle moznost nemuze nastat ! fmr=" + pmr + " virtualHostMap=" + virtualHostMap);
                }
            }
        }
        // prozatim nutna uprava aby nedochazelo k nastaveni zaporneho *CpuMillis v dusledku preempci (zapocitani jako beh 2 uloh i kdyz ve skutecnosti pouze 1 bezi)
        maintenanceCpuMillis = Math.max(0, maintenanceCpuMillis);
        reservedCpuMillis = Math.max(0, reservedCpuMillis);

        c.setMaintenanceCpuTime(maintenanceCpuMillis);
        c.setReservedCpuTime(reservedCpuMillis);
        //TODO co viz nez 100% vytizeni na orkach ?
    }

    private List<JobRecord> findJobsOnPhysicalMachine(ClusterAtDay c, PhysicalMachineRecord fmr) {
        //pro skoncene i jeste bezici ulohy
        long t1 = System.currentTimeMillis();
        logSQL.debug("job records");
        List<JobRecord> jobRecords = jdbc.query("SELECT j.acct_id_string,h.hostname,l.cpu_number, " +
                        "     greatest(j.start_time,?) AS start_time, " +
                        "     least(j.end_time,?) AS end_time " +
                        "FROM acct_pbs_record j,acct_hosts_logs l,acct_host h,physical_virtual_rel fvr,physical_hosts ph " +
                        "WHERE ph.id=? " +
                        "  AND ph.id=fvr.ph_id " +
                        "  AND fvr.acct_host_id=h.acct_host_id " +
                        "  AND h.acct_host_id=l.acct_host_id " +
                        "  AND l.acct_id_string=j.acct_id_string " +
                        "  AND j.start_time <= ? " +
                        "  AND j.end_time >= ? ",
                JOB_RECORD_MAPPER,
                c.getDateAsLinuxTime(), c.getNextDayAsLinuxTime(),
                fmr.getPhysicalHostId(),
                c.getNextDayAsLinuxTime(), c.getDateAsLinuxTime()
        );
        logSQL.debug("started job records");
        List<JobRecord> startedJobRecords = jdbc.query("SELECT j.acct_id_string,h.hostname,l.cpu_number, " +
                        "     greatest(j.start_time,?) AS start_time, " +
                        "     ? AS end_time " +
                        "FROM acct_pbs_record_started j,acct_hosts_logs_started l,acct_host h,physical_virtual_rel fvr,physical_hosts ph " +
                        "WHERE ph.id=? " +
                        "  AND ph.id=fvr.ph_id " +
                        "  AND fvr.acct_host_id=h.acct_host_id " +
                        "  AND h.acct_host_id=l.acct_host_id " +
                        "  AND l.acct_id_string=j.acct_id_string " +
                        "  AND j.start_time <= ? "
                ,
                JOB_RECORD_MAPPER,
                c.getDateAsLinuxTime(), c.getNextDayAsLinuxTime(),
                fmr.getPhysicalHostId(),
                c.getNextDayAsLinuxTime()
        );
        long t2 = System.currentTimeMillis();
        if (logTime.isDebugEnabled()) {
            logTime.debug("findJobsOnPhysicalMachine({},{})= {}ms", c.getId(), fmr.getName(), t2 - t1);
        }
        jobRecords.addAll(startedJobRecords);
        return jobRecords;
    }

    private List<PhysicalMachineRecord> findPhysicalMachinesWithOutages(ClusterAtDay c) {
        //ziska fyzicke stroje, na kterych byl nejaky mimoradny stav
        //a k nim pocty virtualnich stroju a pocty cpu
        logSQL.debug("machines with outages");
        long t1 = System.currentTimeMillis();
        List<PhysicalMachineRecord> l = jdbc.query(
                "SELECT count(*),ph1.id,ph1.name,phcpu.cpu " +
                        "FROM acct_host ah1,physical_virtual_rel fvr1,physical_hosts ph1,physical_hosts_cpu phcpu " +
                        "WHERE ah1.acct_host_id=fvr1.acct_host_id AND fvr1.ph_id=ph1.id " +
                        "      AND ph1.id=phcpu.ph_id AND phcpu.start_time<=? AND ?<phcpu.end_time" +
                        "      AND ph1.id IN (" +
                        "       SELECT DISTINCT(ph1.id) FROM acct_outages ao,acct_host ah1,physical_virtual_rel fvr1,physical_hosts ph1" +
                        "       WHERE ((ao.end_time IS NULL AND ao.start_time<?) " +
                        "             OR (?<ao.start_time AND ao.start_time<?) " +
                        "             OR (?<ao.end_time AND ao.end_time<?) " +
                        "             OR (ao.start_time<? AND ?<ao.end_time)) " +
                        "            AND ao.acct_host_id IN (" +
                        "             SELECT ah.acct_host_id FROM physical_resources pr,physical_hosts_resources_rel phrr,physical_hosts ph," +
                        "                   physical_virtual_rel fvr,acct_host ah  " +
                        "             WHERE pr.name=? AND pr.id=phrr.ph_resources_id AND phrr.ph_hosts_id=ph.id " +
                        "                  AND phrr.start_time<=? AND ?<phrr.end_time " +
                        "                  AND ph.id=fvr.ph_id AND fvr.acct_host_id=ah.acct_host_id" +
                        "            )" +
                        "            AND ao.acct_host_id=ah1.acct_host_id" +
                        "            AND ah1.acct_host_id=fvr1.acct_host_id AND fvr1.ph_id=ph1.id" +
                        "      )" +
                        "GROUP BY ph1.id,ph1.name,phcpu.cpu " +
                        "ORDER BY ph1.id",
                (rs, rowNum) -> new PhysicalMachineRecord(rs.getInt(1), rs.getInt("id"), rs.getString("name"), rs.getInt("cpu")),
                c.getDate(), c.getDate(),
                c.getNextDay(),
                c.getDate(), c.getNextDay(),
                c.getDate(), c.getNextDay(),
                c.getDate(), c.getNextDay(),
                c.getCluster(),
                c.getDate(), c.getDate()
        );
        long t2 = System.currentTimeMillis();
        if (logTime.isDebugEnabled()) {
            logTime.debug("findPhysicalMachinesWithOutages({})= {}ms", c.getId(), t2 - t1);
        }
        return l;
    }

    private List<OutageRecord> findOutagesOnPhysicalMachine(ClusterAtDay c, PhysicalMachineRecord fmr) {
        //pro fyzicky stroj najde vypadky na vsech jeho virtualnich strojich, orezane na dobu vyhledavaneho dne
        long t1 = System.currentTimeMillis();
        logSQL.debug("outages on physical machine");
        List<OutageRecord> l = jdbc.query(
                "SELECT ah1.acct_host_id,ah1.hostname,ao.type,greatest(ao.start_time,?) as greatest, " +
                        "       least(ao.end_time,?) as least  " +
                        "FROM acct_outages ao,acct_host ah1  " +
                        "WHERE " +
                        " ((ao.end_time IS NULL AND ao.start_time<?)  " +
                        "  OR (?<ao.start_time AND ao.start_time<?)  " +
                        "  OR (?<ao.end_time AND ao.end_time<?)  " +
                        "  OR (ao.start_time<? AND ?<ao.end_time)) " +
                        " AND ao.acct_host_id=ah1.acct_host_id " +
                        " AND ao.acct_host_id IN ( " +
                        "     SELECT ah1.acct_host_id FROM acct_host ah1,physical_virtual_rel fvr1,physical_hosts ph1  " +
                        "     WHERE ah1.acct_host_id=fvr1.acct_host_id AND fvr1.ph_id=ph1.id AND ph1.id=?)  " +
                        "ORDER BY greatest,least",
                (rs, rowNum) -> {
                    OutageRecord outageRecord = new OutageRecord();
                    outageRecord.setPbsHostId(rs.getInt("acct_host_id"));
                    outageRecord.setPbsHostName(rs.getString("hostname"));
                    outageRecord.setType(rs.getString("type"));
                    outageRecord.setStart(rs.getTimestamp("greatest"));
                    outageRecord.setEnd(rs.getTimestamp("least"));
                    return outageRecord;
                },
                c.getDate(),
                c.getNextDay(),
                c.getNextDay(),
                c.getDate(), c.getNextDay(),
                c.getDate(), c.getNextDay(),
                c.getDate(), c.getNextDay(),
                fmr.getPhysicalHostId()
        );
        long t2 = System.currentTimeMillis();
        if (logTime.isDebugEnabled()) {
            logTime.debug("findOutagesOnPhysicalMachine({},{})= {}ms", c.getId(), fmr.getName(), t2 - t1);
        }
        return l;
    }

    private void countCpus(ClusterAtDay c) {
        logSQL.debug("count CPUs");
        c.setCpuCount(queryForInt(jdbc.queryForObject(
                "SELECT sum(phcpu.cpu) FROM physical_resources pr,physical_hosts_resources_rel phrr,physical_hosts ph,physical_hosts_cpu phcpu "
                        + "WHERE pr.name=? AND pr.id=phrr.ph_resources_id AND phrr.ph_hosts_id=ph.id AND "
                        + "phrr.start_time<=? AND ?<phrr.end_time AND ph.id=phcpu.ph_id AND "
                        + "phcpu.start_time<=? AND ?<phcpu.end_time",
                Integer.class,
                c.getCluster(), c.getDate(), c.getDate(), c.getDate(), c.getDate())));
    }

    private boolean isHostNameDom0(String name, PhysicalMachineRecord pmr) {
        return name.equals(pmr.getName());
        //return name.contains("nympha-xen.zcu.cz") || !name.contains("nympha.zcu.cz") && !name.contains("-");
    }

    private static int queryForInt(Integer i) {
        return i == null ? 0 : i;
    }

    private static long queryForLong(Long l) {
        return l == null ? 0 : l;
    }
}
