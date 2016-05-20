package cz.cesnet.meta.accounting.server.service;

import cz.cesnet.meta.accounting.server.chart.Chart;
import cz.cesnet.meta.accounting.server.data.CalInterval;

public interface ChartStatsManager {

    Chart getUsertimeStatsForUserYearMonth(Long userId, CalInterval interval);

    Chart getUserjobsStatsForUserMonths(Long userId, CalInterval interval);
}
