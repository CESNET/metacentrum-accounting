package cz.cesnet.meta.acct.hw;

import cz.cesnet.meta.acct.hw.stats.ClusterAtDay;
import cz.cesnet.meta.acct.hw.stats.Stats;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.xml.datatype.DatatypeConfigurationException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Hlavní program pro výpočet statistik. Bere data z accountingové datatbáze, a pro zadané datumy
 * spočítá statistiky pro všechny clustery. Výsledky ukládá do tabulky workload.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: Statistics.java,v 1.5 2011/06/27 08:53:19 makub Exp $
 */
public class Statistics {

    private static final Locale CS = new Locale("cs", "CZ");

    public static void main(String[] args) throws DatatypeConfigurationException {
        Stats stats = new ClassPathXmlApplicationContext("spring-context.xml").getBean("stats", Stats.class);
        long startTime = System.currentTimeMillis();

//        generateStats(stats, absoluteDate(2014,1,27), absoluteDate(2014,1,27));
        generateStats(stats, absoluteDate(2015,11,1), absoluteDate(2015,11,1));
//        generateStats(stats, relativeDate(-1), relativeDate(-1));
//        generateStats(stats, relativeDate(-31), relativeDate(-31));
        long endTime = System.currentTimeMillis();
        System.out.println();
        long duration = (endTime - startTime) / 60000;
        System.out.println("Computed in "+duration+" minutes");
    }

    private static void generateStats(Stats stats, GregorianCalendar start, GregorianCalendar end) {
        PrintStream out = System.out;
        out.println();
        out.println("computing MetaCentrum VO statistics for " + CZECH_DATE.format(start.getTime()) + " - " +CZECH_DATE.format(end.getTime()));
//        for (String cluster : stats.getClusters(start, end)) {
        for(String cluster : Arrays.asList("hda.cerit-sc.cz")) {
            out.println();
            out.println(cluster);
            out.println();
            out.println("den       ;CPU;cas celkem;maintenance;reserved;perun reserved;jobs time;maintenance%;reserved%;jobs%;vytizeni%;hrube vytizeni%");
            generateLines(stats, cluster, start, end, out);
        }
    }

    private static void generateLines(Stats stats, String cluster, Calendar start, Calendar end, PrintStream out) {
        Calendar day = (Calendar) start.clone();
        while (day.getTimeInMillis() <= end.getTimeInMillis()) {
            ClusterAtDay c = new ClusterAtDay(cluster, day);
            stats.computeStats(c);
            out.printf("%s;%3d;%10d;%11d;%8d;%14d;%9d;%12.1f;%9.1f;%5.1f;%9.1f;%15.1f%n", CZECH_DATE.format(c.getDate()),
                    c.getCpuCount(), c.getAllCpuTime(), c.getMaintenanceCpuTime(),
                    c.getReservedCpuTime(), c.getPerunReservedCpuTime(), c.getJobsTime(),
                    c.getMaintenanceRatio() * 100, c.getReservedRatio() * 100d, c.getJobsRatio() * 100d, c.getUtilizationRatio() * 100d, c.getRawUtilizationRatio() * 100);
            stats.saveStats(c);
            day.add(Calendar.DATE, 1);
        }
    }

    @SuppressWarnings("MagicConstant")
    static GregorianCalendar absoluteDate(int year, int month, int day) {
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("CET"), CS);
        calendar.clear();
        calendar.set(year, month - 1, day, 0, 0, 0);
        return calendar;
    }

    static GregorianCalendar relativeDate(int daysFromToday) {
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("CET"), CS);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        calendar.add(Calendar.DATE, daysFromToday);
        return calendar;
    }

    private static final DateFormat CZECH_DATE = new SimpleDateFormat("dd.MM.yyyy", new Locale("cs"));

}
