package cz.cesnet.meta.accounting.server.data;

import java.math.BigDecimal;

public class UserStats {

  private String username;

  private BigDecimal userTimeSum;
  private BigDecimal elapsedTimeSum;
  private BigDecimal systemTimeSum;
  private Long jobsSum;  

  public UserStats(String username, Long jobsSum) {
    this.username = username;
    this.jobsSum = jobsSum;
  }

  public UserStats(String username, BigDecimal elapsedTimeSum, BigDecimal userTimeSum, BigDecimal systemTimeSum) {
    this.username = username;
    this.userTimeSum = userTimeSum;
    this.elapsedTimeSum = elapsedTimeSum;
    this.systemTimeSum = systemTimeSum;
  }

  public UserStats(String username, BigDecimal userWalltimeSum) {
    this.username = username;
    this.userTimeSum = userWalltimeSum;
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

  public BigDecimal getSystemTimeSum() {
    return systemTimeSum;
  }

  public void setSystemTimeSum(BigDecimal systemTimeSum) {
    this.systemTimeSum = systemTimeSum;
  }

  public Long getJobsSum() {
    return jobsSum;
  }

  public void setJobsSum(Long jobsSum) {
    this.jobsSum = jobsSum;
  }

}
