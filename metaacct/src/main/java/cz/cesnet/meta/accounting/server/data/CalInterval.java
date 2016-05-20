package cz.cesnet.meta.accounting.server.data;

import org.joda.time.LocalDate;

import cz.cesnet.meta.accounting.util.AcctCal;

public class CalInterval { 
  
  LocalDate from;
  LocalDate to;
  String key;
  String text;
  
  public CalInterval(String key, String text, LocalDate from, LocalDate to) {
    this.key = key;
    this.text = text;
    this.from = from;
    this.to = to;
  }  

  public LocalDate getFrom() {
    return from;
  }

  public void setFrom(LocalDate from) {
    this.from = from;
  }

  public LocalDate getTo() {
    return to;
  }

  public void setTo(LocalDate to) {
    this.to = to;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }
  
  public static CalInterval getOneYearFromNowInterval() {
    LocalDate now = AcctCal.createThisMonthYear();
    return new CalInterval("viewYear", null, now.minusYears(1), now);
  }
  public static CalInterval getTwoYearFromNowInterval() {
    LocalDate now = AcctCal.createThisMonthYear();
    return new CalInterval("viewTwoYears", null, now.minusYears(2), now);
  }
  
  public static CalInterval getFromStartToNowIterval(LocalDate start) {
    LocalDate now = AcctCal.createThisMonthYear();
    int startOfFirstYearQuarter = ((start.getMonthOfYear() - 1)/3 * 3) + 1;
    return new CalInterval("viewFromStart", null, start.withMonthOfYear(startOfFirstYearQuarter), now);
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

}
