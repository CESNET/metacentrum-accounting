package cz.cesnet.meta.accounting.server.data;

public enum PBSRecordType {
  
  ABORTED("A"), //  Job was aborted by the server.
  RERUN("R"),   // Job was rerun.
  DELETED("D"),  // Job was deleted by request.
  ENDED("E"),    // Job ended (terminated execution).
  STARTED("S"),  // Job execution started.
  G("G"); // G - informace o smazane uloze ktera bezela  
    // B -  Beginning of reservation period.
    // F -  Resources reservation period finished.
    // K -  Scheduler or server requested removal of the reservation.
    // k -  Resources reservation terminated by ordinary client.
    // Q -  Job entered a queue.
    // C - Job was checkpointed and held.
    // T - Job was restarted from a checkpoint file.
    // U - Created unconfirmed resources reservation on Server.
    // Y - Resources reservation confirmed by the Scheduler.



  private final String type;

  PBSRecordType(String type) {
    this.type = type;
  }

  public String type() {
    return type;
  }
}
