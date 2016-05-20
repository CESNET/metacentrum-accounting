package cz.cesnet.meta.accounting.web;

import java.util.Map;
import java.util.TreeMap;

import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.integration.spring.SpringBean;

import org.joda.time.LocalDate;

import cz.cesnet.meta.accounting.server.chart.GoogleChartUrlHelper;
import cz.cesnet.meta.accounting.server.data.CalInterval;
import cz.cesnet.meta.accounting.server.service.ChartStatsManager;
import cz.cesnet.meta.accounting.server.service.PbsRecordManager;
import cz.cesnet.meta.accounting.server.service.UserManager;
import cz.cesnet.meta.accounting.util.AcctCal;

public class ChartStats extends AccountingWebBase {
  
  @SpringBean
  UserManager userManager;
  @SpringBean
  ChartStatsManager chartStatsManager;
  @SpringBean
  PbsRecordManager pbsRecordManager;
  
  Map<String, Long> users;
  Long userId;
  String statsUsertimeUrl;
  String statsUserjobsUrl;
  
  Long intervalId;
  
  Map<Long, CalInterval> intervals;
  
  
  @Before(stages = LifecycleStage.HandlerResolution)
  public void prepareData() {
    LocalDate monthYearFrom = pbsRecordManager.getLocalDateOfFirstPbsRecord();
    
    users = new TreeMap<String, Long>(userManager.loadUsers());
    users.remove("--");
    intervals = new TreeMap<Long, CalInterval>();
    
    
    intervals.put(-10L, CalInterval.getOneYearFromNowInterval());
    intervals.put(-2L, CalInterval.getTwoYearFromNowInterval());
    intervals.put(-1L, CalInterval.getFromStartToNowIterval(monthYearFrom));
        
    int yearTo = new LocalDate().getYear();
    for (int i = yearTo; i >= monthYearFrom.getYear(); i--) {
      intervals.put((long)yearTo - i, new CalInterval(null, i + "", AcctCal.createMonthYear(1, i), AcctCal.createMonthYear(12, i)));
    }
    
    
  }
  
  @DefaultHandler
  public Resolution view() {    
    return new ForwardResolution("/chart-stats.jsp");
  }
  
  @HandlesEvent("chart")
  public Resolution chart() {        
    CalInterval interval = intervals.get(intervalId);
    statsUsertimeUrl = GoogleChartUrlHelper.getImageUrl(chartStatsManager.getUsertimeStatsForUserYearMonth(userId, interval));
    statsUserjobsUrl = GoogleChartUrlHelper.getImageUrl(chartStatsManager.getUserjobsStatsForUserMonths(userId, interval));
    return new ForwardResolution("/chart-stats.jsp");
  }

  public Map<String, Long> getUsers() {
    return users;
  }

  public void setUsers(Map<String, Long> users) {
    this.users = users;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getStatsUsertimeUrl() {
    return statsUsertimeUrl;
  }

  public void setStatsUsertimeUrl(String statsUsertimeUrl) {
    this.statsUsertimeUrl = statsUsertimeUrl;
  }

  public String getStatsUserjobsUrl() {
    return statsUserjobsUrl;
  }

  public void setStatsUserjobsUrl(String statsUserjobsUrl) {
    this.statsUserjobsUrl = statsUserjobsUrl;
  }
  
  public Map<Long, CalInterval> getIntervals() {
    return intervals;
  }

  public void setIntervals(Map<Long, CalInterval> intervals) {
    this.intervals = intervals;
  }

  public Long getIntervalId() {
    return intervalId;
  }

  public void setIntervalId(Long intervalId) {
    this.intervalId = intervalId;
  }  
}
