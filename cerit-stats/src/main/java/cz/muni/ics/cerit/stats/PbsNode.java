package cz.muni.ics.cerit.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PBS node.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("UnusedDeclaration")
public class PbsNode {

    final static Logger log = LoggerFactory.getLogger(PbsNode.class);

    String name;
    String ntype;
    String queue;
    //possible states:  ["down,offline", "offline,job-busy", "job-exclusive", "free", "down", "state-unknown", "job-busy"]
    String state;
    String jobs;
    String note;

    public PbsNode(String name, String ntype, String queue, String state, String jobs,String note) {
        this.name = name;
        this.ntype = ntype;
        this.queue = queue;
        this.state = state;
        this.jobs = jobs;
        this.note = note;
    }

    private String reason;

    public boolean isAvailable() {
        if ("maintenance".equals(queue)) {
            boolean hasJobs = jobs != null && jobs.trim().length() > 0;
            reason = hasJobs ? "maintenance with jobs" : "maintenance note='"+note+"'";
            return hasJobs;
        } else if(state.startsWith("down")){
            reason = "down";
            return false;
        } else if(state.startsWith("offline")){
            reason = "offline";
            return false;
        } else if("job-exclusive".equals(state)||"free".equals(state)||"job-busy".equals(state)){ //free means even partially free
            reason = "free or working";
            return true;
        } else {
            log.error("unknown state '" + state + "' for node " + name);
            reason = state;
            return false;
        }
    }

    public String getReason() {
        return reason;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNtype() {
        return ntype;
    }

    public void setNtype(String ntype) {
        this.ntype = ntype;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "PbsNode{" +
                "name='" + name + '\'' +
                ", ntype='" + ntype + '\'' +
                ", queue='" + queue + '\'' +
                ", state='" + state + '\'' +
                ", jobs='" + jobs + '\'' +
                ", note='" + note + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }
}
