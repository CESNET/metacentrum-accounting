package cz.cesnet.meta.accounting.server.data;

import java.math.BigDecimal;

public class BinaryStats {

  private String command;
  private BigDecimal elapsedTimeSum;
  private BigDecimal userTimeSum;
  private BigDecimal systemTimeSum;
  private String app;
  
  public BinaryStats(String command, String app, BigDecimal elapsedTimeSum, BigDecimal userTimeSum, BigDecimal systemTimeSum) {
    this.command = command;
    this.app = app;
    this.elapsedTimeSum = elapsedTimeSum;
    this.userTimeSum = userTimeSum;
    this.systemTimeSum = systemTimeSum;
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public BigDecimal getSystemTimeSum() {
    return systemTimeSum;
  }

  public void setSystemTimeSum(BigDecimal systemTimeSum) {
    this.systemTimeSum = systemTimeSum;
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

  public String getApp() {
    return app;
  }

  public void setApp(String app) {
    this.app = app;
  }
}
