package cz.cesnet.meta.accounting.server.data;

public class User {

  private Long id;
  private String username;
  private Long numberOfPbsRecords;

  public User(Long id, String username, Long numberOfPbsRecords) {
    this.id = id;
    this.username = username;
    this.numberOfPbsRecords = numberOfPbsRecords;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getNumberOfPbsRecords() {
    return numberOfPbsRecords;
  }

  public void setNumberOfPbsRecords(Long numberOfPbsRecords) {
    this.numberOfPbsRecords = numberOfPbsRecords;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

}
