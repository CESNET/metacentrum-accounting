package cz.cesnet.meta.accounting.util;

import org.joda.time.LocalDate;

public class AcctCal {
  
  public static LocalDate createMonthYear(int month, int year) {
    return new LocalDate(year, month, 1);
  }
  
  public static LocalDate createThisMonthYear() {
    return new LocalDate().withDayOfMonth(1);
  }
  
  public static String toMonthYearString(LocalDate date) {
    return date.getMonthOfYear() + "/" + (date.getYearOfCentury()< 10 ? "0" + date.getYearOfCentury() : date.getYearOfCentury());
  }
  
  public static String toThreeMonthsYearString(LocalDate date) {
    return date.getMonthOfYear() + "-"  + (date.getMonthOfYear()+2) + "/" + (date.getYearOfCentury()< 10 ? "0" + date.getYearOfCentury() : date.getYearOfCentury());
  }

}
