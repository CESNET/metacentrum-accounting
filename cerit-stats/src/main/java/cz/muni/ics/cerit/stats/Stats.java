package cz.muni.ics.cerit.stats;

import cz.cesnet.meta.perun.api.Perun;
import cz.cesnet.meta.perun.api.VypocetniZdroj;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Locale;

import static cz.muni.ics.cerit.stats.ScheduledDowntimeSync.syncScheduledDowntimes;

/**
 * Computes statistics.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class Stats {
    public final static DateTime AVAILABILITY_STATS_START = absoluteDate(2013, 10, 15);

    private final static Logger log = LoggerFactory.getLogger(Stats.class);
    private final static DateTimeFormatter DATE_FMT = ISODateTimeFormat.date();

    public static void main(String[] args) throws IOException, ParseException {
        ApplicationContext springCtx = new ClassPathXmlApplicationContext("spring-context.xml");
        syncScheduledDowntimes(springCtx);
        Perun perun = springCtx.getBean("perun", Perun.class);
        Accounting accounting = springCtx.getBean("acct", Accounting.class);
        lastDayStats(perun,accounting);
        //specificDaysStats(perun,accounting,absoluteDate(2014, 5, 18), absoluteDate(2014, 5, 20));
    }

    static void lastDayStats(Perun perun,Accounting accounting) {
        //denni statistiky
        for (VypocetniZdroj cluster : CheckCeritSC.getCeritSC(perun).getZdroje()) {
            String clusterId = cluster.getId();
            DateTime startDate = accounting.getClusterStartDate(clusterId);
            generateDayStats(accounting, clusterId, relativeDate(-1), relativeDate(-1));
        }
    }

    static void specificDaysStats(Perun perun,Accounting accounting,DateTime startDay,DateTime endDay) {
        //denni statistiky
        for (VypocetniZdroj cluster : CheckCeritSC.getCeritSC(perun).getZdroje()) {
            String clusterId = cluster.getId();
            DateTime startDate = accounting.getClusterStartDate(clusterId);
            generateDayStats(accounting, clusterId, startDay,endDay);
        }
    }

    static void allTimeStats(Perun perun,Accounting accounting) {
        //denni statistiky
        for (VypocetniZdroj cluster : CheckCeritSC.getCeritSC(perun).getZdroje()) {
            String clusterId = cluster.getId();
            DateTime startDate = accounting.getClusterStartDate(clusterId);
            generateDayStats(accounting, clusterId, max(startDate, AVAILABILITY_STATS_START), relativeDate(-1));
        }

        System.out.println("--------totals----------");
        //statistika za celou dobu
        for (VypocetniZdroj cluster : CheckCeritSC.getCeritSC(perun).getZdroje()) {
            String clusterId = cluster.getId();
            DateTime startDate = accounting.getClusterStartDate(clusterId);
            generateIntervalStats(accounting, cluster.getId(), max(startDate, AVAILABILITY_STATS_START), relativeDate(0), false);
        }
    }

    static void generateIntervalStats(Accounting accounting, String clusterId, DateTime startTime, DateTime endTime, boolean details) {
        ResourceAvailability a = accounting.getAvailability(clusterId, startTime, endTime, details);
        System.out.printf(Locale.US, "cluster " + a.getClusterId() + " was available %8.4f%% between " + DATE_FMT.print(a.getFrom()) + " and "
                + DATE_FMT.print(a.getTo()) + " %n", a.getPercentAvailable());
        if (a.getRecords() != null) {
            for (AvailabilityRecord ar : a.getRecords()) {
                System.out.println("" + ar);
            }
        }
    }

    static void generateDayStats(Accounting accounting, String clusterId, DateTime startDay, DateTime endDay) {
        log.debug("generateDayStats({},{},{})", clusterId, DATE_FMT.print(startDay), DATE_FMT.print(endDay));
        for (DateTime day = startDay; !day.isAfter(endDay); day = day.plusDays(1)) {
//            log.debug("computing {} on {} ",clusterId,DATE_FMT.print(day));
            ResourceAvailability a = accounting.getAvailability(clusterId, day);
            accounting.storeStatsForDay(clusterId, day, a);
            System.out.printf(Locale.US, "cluster " + a.getClusterId() + " was available %8.4f%% on "
                    + DATE_FMT.print(a.getFrom()) + " %n", a.getPercentAvailable());
        }
    }

    static DateTime absoluteDate(int year, int month, int day) {
        return absoluteTime(year, month, day, 0, 0, 0);
    }

    static DateTime absoluteTime(int year, int month, int day, int hour, int min, int sec) {
        return new DateTime(year, month, day, hour, min, sec);
    }

    static DateTime relativeDate(int daysFromToday) {
        return new DateTime().withTimeAtStartOfDay().plusDays(daysFromToday);
    }

    static DateTime max(DateTime t1, DateTime t2) {
        return t1.isBefore(t2) ? t2 : t1;
    }
}
