package cz.cesnet.meta.acct.hw.stats;

import java.sql.Timestamp;


public class OutageRecord {


    Enum e;

    public enum Type { maintenance, reserved, xentest, node_down }

    private String pbsHostName;
    private int pbsHostId;
    private Type type;
    private Timestamp start;
    private Timestamp end;

   

    public void setPbsHostId(int pbsHostId) {
        this.pbsHostId = pbsHostId;
    }

    public void setPbsHostName(String pbsHostName) {
        this.pbsHostName = pbsHostName;
    }


    public void setType(String type) {
        if("node down".equals(type)) {
            this.type = Type.node_down;
        } else {
           this.type = Type.valueOf(type);
        }
    }

    public void setStart(Timestamp start) {
        this.start = start;
    }

    public void setEnd(Timestamp end) {
        this.end = end;
    }

    public String getPbsHostName() {
        return pbsHostName;
    }

    public int getPbsHostId() {
        return pbsHostId;
    }


    public Type getType() {
        return type;
    }

    public Timestamp getStart() {
        return start;
    }

    public Timestamp getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "OutageRecord{" +
                " pbsHostId=" + pbsHostId +
                ", pbsHostName='" + pbsHostName + '\'' +
                ", type=" + type +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}
