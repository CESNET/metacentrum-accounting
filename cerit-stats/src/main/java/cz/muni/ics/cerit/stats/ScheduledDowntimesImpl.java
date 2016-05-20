package cz.muni.ics.cerit.stats;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Synchronizes records about scheduled downtimes from a CSV file and database tables.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class ScheduledDowntimesImpl implements ScheduledDowntimes {

    private final static Logger log = LoggerFactory.getLogger(ScheduledDowntimesImpl.class);

    private JdbcTemplate jdbc;

    public void setDataSource(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Transactional
    @Override
    public List<ResourceAtDay> syncDowntimes(InputStream sd) throws IOException, ParseException {
        List<ResourceAtDay> deletedStats = new ArrayList<>();
        Set<Line> newLines = new HashSet<>(parseCsvFile(sd));
        Set<Line> oldLines = new HashSet<>(jdbc.query("select id,unparsed,start_time,end_time from scheduled_downtime_lines",
                (rs, rowNum) -> new Line(rs.getLong("id"), rs.getString("unparsed"), rs.getTimestamp("start_time"), rs.getTimestamp("end_time"))));
        //compute which lines to remove from DB
        Set<Line> remove = new HashSet<>(oldLines);
        remove.removeAll(newLines);
        //compute which lines to add to DB
        Set<Line> add = new HashSet<>(newLines);
        add.removeAll(oldLines);
        //log
        if (log.isDebugEnabled()) {
            for (Line l : remove) log.debug("remove {}", l);
            for (Line l : add) log.debug("add {}", l);
        }
        //remove lines from DB with deleting affected stats
        for (Line l : remove) {
            deletedStats.addAll(deleteAffectedStats(l));
            jdbc.update("delete from scheduled_downtime_lines where id=?", l.getId());
        }
        //add lines
        for (Line l : add) {
            Long lineId = jdbc.queryForObject("insert into scheduled_downtime_lines (unparsed,start_time,end_time,note) values (?,?,?,?) returning id", Long.class,
                    l.getUnparsed(), l.getFrom(), l.getTo(), l.getNote());
            l.setId(lineId);
            //for every node find whether it is physical resorce (cluster) or physical host
            for (String node : l.getNodes()) {
                Long resourceId = getResourceId(node);
                if (resourceId != null) {
                    jdbc.update("insert into scheduled_downtime_resources (line_id,pr_id) values (?,?)", lineId, resourceId);
                } else {
                    Long hostId = getHostId(node);
                    if (hostId != null) {
                        jdbc.update("insert into scheduled_downtime_hosts (line_id,ph_id) values (?,?)", lineId, hostId);
                    } else {
                        List<Long> ids = jdbc.queryForList("select acct_host_id from acct_host where hostname=?", Long.class, node);
                        if (ids.size() != 1) {
                            log.warn("node {} is neither physical cluster nor physical host nor PBS node", node);
                        }
                    }
                }
            }
            //smazat ovlivnene availability_reliability_stats
            deletedStats.addAll(deleteAffectedStats(l));
        }
        return deletedStats;
    }

    private RowMapper<ResourceAtDay> resourceAtDayRowMapper = (rs, rowNum) -> new ResourceAtDay(rs.getString("name"), rs.getDate("day"));

    private List<ResourceAtDay> deleteAffectedStats(Line line) {
        log.debug("deleting stats for resource line {} ", line);
        List<ResourceAtDay> dayList;
        if (line.getTo() != null) {
            dayList = jdbc.query("delete from availability_reliability_stats a using scheduled_downtime_lines dl, scheduled_downtime_resources d, physical_resources pr " +
                            "where dl.id=? and d.line_id=dl.id " +
                            " and d.pr_id=a.pr_id and d.pr_id=pr.id  " +
                            " and (a.day,a.day+ interval '1 day') overlaps (dl.start_time,dl.end_time) returning pr.name,a.day",
                    resourceAtDayRowMapper, line.getId()
            );
            dayList.addAll(jdbc.query("delete from availability_reliability_stats a using scheduled_downtime_lines dl, scheduled_downtime_hosts d, physical_resources pr," +
                            " physical_hosts_resources_rel phrr, physical_hosts ph " +
                            "where dl.id=? and d.line_id=dl.id " +
                            " and d.ph_id=ph.id and ph.id=phrr.ph_hosts_id and phrr.ph_resources_id=pr.id and pr.id=a.pr_id " +
                            " and (a.day,a.day+ interval '1 day') overlaps (dl.start_time,dl.end_time)" +
                            " and (phrr.start_time,phrr.end_time) overlaps (dl.start_time,dl.end_time) returning pr.name,a.day",
                    resourceAtDayRowMapper, line.getId()
            ));
        } else {
            dayList = jdbc.query("delete from availability_reliability_stats a using scheduled_downtime_lines dl, scheduled_downtime_resources d, physical_resources pr " +
                            "where dl.id=? and d.line_id=dl.id " +
                            " and d.pr_id=a.pr_id and d.pr_id=pr.id  and (a.day+ interval '1 day' > dl.start_time) returning pr.name,a.day",
                    resourceAtDayRowMapper, line.getId()
            );
            dayList.addAll(jdbc.query("delete from availability_reliability_stats a using scheduled_downtime_lines dl, scheduled_downtime_hosts d, physical_resources pr," +
                            " physical_hosts_resources_rel phrr, physical_hosts ph " +
                            "where dl.id=? and d.line_id=dl.id " +
                            " and d.ph_id=ph.id and ph.id=phrr.ph_hosts_id and phrr.ph_resources_id=pr.id and pr.id=a.pr_id " +
                            " and (a.day+ interval '1 day' > dl.start_time)" +
                            " and (dl.start_time between phrr.start_time and phrr.end_time)  returning pr.name,a.day",
                    resourceAtDayRowMapper, line.getId()
            ));
        }
        log.debug("deleted stats for resource {} ", dayList);
        return dayList;
    }

    private Long getHostId(String fqdn) {
        List<Long> ids = jdbc.queryForList("select id from physical_hosts where name=?", Long.class, fqdn);
        return ids.size() == 1 ? ids.get(0) : null;
    }

    private Long getResourceId(String fqdn) {
        List<Long> ids = jdbc.queryForList("select id from physical_resources where name=?", Long.class, fqdn);
        return ids.size() == 1 ? ids.get(0) : null;
    }


    static List<Line> parseCsvFile(InputStream inputStream) throws IOException, ParseException {
        CsvMapper mapper = new CsvMapper();
        ObjectReader csvReader = mapper.readerFor(String[].class);

        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        int lineNum = 0;
        List<Line> linesList = new ArrayList<>();
        while ((line = in.readLine()) != null) {
            lineNum++;
            if (lineNum == 1) continue;//skip first line
            String[] row = csvReader.readValue(line);
            if (row.length != 5) {
                log.error("line " + lineNum + " does not have 5 columns: {}", line);
                continue;
            }
            String type = row[2];
            if (!type.contains("scheduled")) {
                log.debug("skipping line {}, not scheduled", lineNum);
                continue;
            }
            Date from = parseDate(row[0], false, lineNum,false);
            if (from == null) continue;

            Date to = parseDate(row[1], true, lineNum, true);
            if (to == null) {
                //postgres ma maximum  294276-12-31 23:59:29
                to = Date.from(ZonedDateTime.of(100000, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant());
            }
            linesList.add(new Line(line, from, to, row[3], Arrays.asList(row[4].split(" "))));
        }
        return linesList;
    }

    private static final DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateFormat dateTimeInMinutesFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final Pattern datePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final Pattern dateTimePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{1,2}:\\d{2}:\\d{2}");
    private static final Pattern dateTimeInMinutesPattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{1,2}:\\d{2}");

    private static Date parseDate(String fromS, boolean roundToNextDate, int lineNum, boolean canBeEmpty) throws ParseException {
        fromS = fromS.trim();
        if(fromS.isEmpty()) {
            if(canBeEmpty) {
                return null;
            } else {
                log.error("empty time on line {}",lineNum);
                return null;
            }
        }
        if (dateTimePattern.matcher(fromS).matches()) {
            return dateTimeFormat.parse(fromS);
        } else if (dateTimeInMinutesPattern.matcher(fromS).matches()) {
            return dateTimeInMinutesFormat.parse(fromS);
        } else if (datePattern.matcher(fromS).matches()) {
            Date date = dateTimeFormat.parse(fromS + " 00:00:00");
            return roundToNextDate ? new DateTime(date.getTime()).withTimeAtStartOfDay().plusDays(1).toDate() : date;
        } else {
            log.error("Cannot parse time '{}' on line {}", fromS, lineNum);
            return null;
        }
    }

    /**
     * Represents one line from the CSV file.
     */
    static class Line {
        long id;
        String unparsed;
        Date from;
        Date to;
        String note;
        List<String> nodes;

        Line(String unparsed, Date from, Date to, String note, List<String> nodes) {
            this.unparsed = unparsed;
            this.from = from;
            this.to = to;
            this.note = note;
            this.nodes = nodes;
        }

        Line(long id, String unparsed, Timestamp from, Timestamp to) {
            this.id = id;
            this.unparsed = unparsed;
            this.from = from;
            this.to = to;
        }

        String getUnparsed() {
            return unparsed;
        }

        Date getFrom() {
            return from;
        }

        Date getTo() {
            return to;
        }

        String getNote() {
            return note;
        }

        List<String> getNodes() {
            return nodes;
        }

        long getId() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Line that = (Line) o;
            return unparsed.equals(that.unparsed);
        }

        @Override
        public int hashCode() {
            return unparsed.hashCode();
        }

        @Override
        public String toString() {
            return "Line{" +
                    "id=" + id +
                    ", from=" + (from == null ? "null" : dateTimeFormat.format(from)) +
                    ", to=" + (to == null ? "null" : dateTimeFormat.format(to)) +
                    ", note='" + note + '\'' +
                    ", nodes=" + nodes +
                    '}';
        }

        void setId(long id) {
            this.id = id;
        }
    }
}
