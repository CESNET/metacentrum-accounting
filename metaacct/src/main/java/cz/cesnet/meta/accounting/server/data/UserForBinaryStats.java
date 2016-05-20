package cz.cesnet.meta.accounting.server.data;

import java.math.BigDecimal;

public class UserForBinaryStats {

  private String username;

  private BigDecimal elapsedTimeSum;
  
  private BigDecimal userTimeSum;

  private BigDecimal systemTimeSum;

  public UserForBinaryStats(String username, BigDecimal elapsedTimeSum, BigDecimal userTimeSum, BigDecimal systemTimeSum) {
    this.username = username;
    this.elapsedTimeSum = elapsedTimeSum;
    this.userTimeSum = userTimeSum;
    this.systemTimeSum = systemTimeSum;
  }

  public BigDecimal getSystemTimeSum() {
    return systemTimeSum;
  }

  public void setSystemTimeSum(BigDecimal systemTimeSum) {
    this.systemTimeSum = systemTimeSum;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public BigDecimal getUserTimeSum() {
    return userTimeSum;
  }

  public void setUserTimeSum(BigDecimal userTimeSum) {
    this.userTimeSum = userTimeSum;
  }

  public BigDecimal getElapsedTimeSum() {
    return elapsedTimeSum;
  }

  public void setElapsedTimeSum(BigDecimal elapsedTimeSum) {
    this.elapsedTimeSum = elapsedTimeSum;
  }

}
