package cz.cesnet.meta.accounting.server.service;

import cz.cesnet.meta.accounting.server.chart.Chart;
import cz.cesnet.meta.accounting.server.chart.ChartGroup;
import cz.cesnet.meta.accounting.server.chart.ChartItem;
import cz.cesnet.meta.accounting.server.data.CalInterval;
import cz.cesnet.meta.accounting.server.util.LocalizationHelper;
import cz.cesnet.meta.accounting.util.AcctCal;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChartStatsManagerImpl extends JdbcDaoSupport implements ChartStatsManager {

    @Autowired
    LocalizationHelper messages;

    @Override
    public Chart getUsertimeStatsForUserYearMonth(Long userId, CalInterval interval) {
        ChartGroup elapsedGroup = new ChartGroup("Elapsed time", Color.RED);
        ChartGroup userGroup = new ChartGroup("User time", Color.BLUE);
        ChartGroup systemGroup = new ChartGroup("System time", Color.GREEN);

        LocalDate dateFrom = interval.getFrom();
        LocalDate dateTo = interval.getTo();
        LocalDate d = new LocalDate(dateFrom);
        Period p = new Period(dateFrom, dateTo);
        if (p.getYears() > 2) {
            while (d.compareTo(dateTo) < 0) {

                String query = "select sum(k.user_time) as user_time_sum, sum(k.system_time) as system_time_sum, sum(k.elapsed_time) as elapsed_time_sum" +
                        "  from acct_kernel_record k " +
                        "  where acct_user_id = " + userId +
                        "    and k.create_time >= " + d.toDateTimeAtStartOfDay().getMillis() / 1000 +
                        "    and (k.create_time*100 +k.elapsed_time)<= " + d.plusMonths(3).plusDays(1).toDateTimeAtStartOfDay().getMillis() / 10 + " "; // /10 protoze elapsed time je v setinach sekundy

                Map<String, Object> item = getJdbcTemplate().queryForMap(query);

                BigDecimal elapsed = ((BigDecimal) item.get("elapsed_time_sum"));
                BigDecimal userTime = ((BigDecimal) item.get("user_time_sum"));
                BigDecimal systemTime = ((BigDecimal) item.get("system_time_sum"));

                elapsedGroup.addItem(new ChartItem(AcctCal.toThreeMonthsYearString(d), elapsed != null ? elapsed.doubleValue() / 360000 : 0.0));
                userGroup.addItem(new ChartItem(AcctCal.toThreeMonthsYearString(d), userTime != null ? userTime.doubleValue() / 360000 : 0.0));
                systemGroup.addItem(new ChartItem(AcctCal.toThreeMonthsYearString(d), systemTime != null ? systemTime.doubleValue() / 360000 : 0.0));
                d = d.plusMonths(3);
            }


        } else {
            while (!d.equals(dateTo.plusMonths(1))) {

                String query = "select sum(k.user_time) as user_time_sum, sum(k.system_time) as system_time_sum, sum(k.elapsed_time) as elapsed_time_sum" +
                        "  from acct_kernel_record k " +
                        "  where acct_user_id = " + userId +
                        "    and k.create_time >= " + d.toDateTimeAtStartOfDay().getMillis() / 1000 +
                        "    and (k.create_time*100 +k.elapsed_time)<= " + d.plusMonths(1).plusDays(1).toDateTimeAtStartOfDay().getMillis() / 10 + " "; // /10 protoze elapsed time je v setinach sekundy

                Map<String, Object> item = getJdbcTemplate().queryForMap(query);

                BigDecimal elapsed = ((BigDecimal) item.get("elapsed_time_sum"));
                BigDecimal userTime = ((BigDecimal) item.get("user_time_sum"));
                BigDecimal systemTime = ((BigDecimal) item.get("system_time_sum"));

                elapsedGroup.addItem(new ChartItem(AcctCal.toMonthYearString(d), elapsed != null ? elapsed.doubleValue() / 360000 : 0.0));
                userGroup.addItem(new ChartItem(AcctCal.toMonthYearString(d), userTime != null ? userTime.doubleValue() / 360000 : 0.0));
                systemGroup.addItem(new ChartItem(AcctCal.toMonthYearString(d), systemTime != null ? systemTime.doubleValue() / 360000 : 0.0));
                d = d.plusMonths(1);
            }
        }


        Chart chart = new Chart(1000, 300);
        chart.setProportions("4,2,15");
        chart.setChartType(Chart.CHART_TYPE_BAR_VERTICAL_GROUPED);
        chart.setTitle(messages.getMessage("stats.chart.binariesHours", AcctCal.toMonthYearString(dateFrom), AcctCal.toMonthYearString(dateTo)));

        chart.addGroup(elapsedGroup);
        chart.addGroup(userGroup);
        chart.addGroup(systemGroup);
        return chart;
    }

    @Override
    public Chart getUserjobsStatsForUserMonths(Long userId, CalInterval interval) {
        List<Map<String, Object>> r = getJdbcTemplate().queryForList(" SELECT hostname FROM ci_acct_pbs_server ORDER BY hostname");
        List<ChartGroup> groups = new ArrayList<>();
        Color c = Color.CYAN;
        for (Map<String, Object> i : r) {
            groups.add(new ChartGroup((String) i.get("hostname"), c));
            c = c.darker();
        }

        LocalDate dateFrom = interval.getFrom();
        LocalDate dateTo = interval.getTo();
        LocalDate d = new LocalDate(dateFrom);

        Period p = new Period(dateFrom, dateTo);
        if (p.getYears() > 2) {
            while (d.compareTo(dateTo) < 0) {

                for (ChartGroup g : groups) {
                    String query = " select count(p.acct_id_string) " +
                            " from acct_pbs_record p natural left join ci_acct_pbs_server s " +
                            " where s.hostname = '" + g.getName() + "' " +
                            "  and p.date_time between " + d.toDateTimeAtStartOfDay().getMillis() + " and " + d.plusMonths(3).toDateTimeAtStartOfDay().getMillis() + " " +
                            "  and p.acct_user_id =  " + userId + " ";

                    Long count = getJdbcTemplate().queryForObject(query, Long.class);
                    g.addItem(new ChartItem(AcctCal.toThreeMonthsYearString(d), count));
                }

                d = d.plusMonths(3);
            }
        } else {
            while (!d.equals(dateTo.plusMonths(1))) {
                for (ChartGroup g : groups) {
                    String query = " select count(p.acct_id_string) " +
                            " from acct_pbs_record p natural left join ci_acct_pbs_server s " +
                            " where s.hostname = '" + g.getName() + "' " +
                            "  and p.date_time between " + d.toDateTimeAtStartOfDay().getMillis() + " and " + d.plusMonths(1).plusDays(1).toDateTimeAtStartOfDay().getMillis() + " " +
                            "  and p.acct_user_id =  " + userId + " ";

                    Long count = getJdbcTemplate().queryForObject(query, Long.class);
                    g.addItem(new ChartItem(AcctCal.toMonthYearString(d), count));
                }

                d = d.plusMonths(1);
            }
        }


        Chart chart = new Chart(1000, 300);
        chart.setProportions("6,2,15");
        chart.setChartType(Chart.CHART_TYPE_BAR_VERTICAL_GROUPED);
        chart.setTitle(messages.getMessage("stats.chart.jobs", AcctCal.toMonthYearString(dateFrom), AcctCal.toMonthYearString(dateTo)));
        chart.setGroups(groups);
        return chart;
    }

}
