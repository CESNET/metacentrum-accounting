package cz.cesnet.meta.acct;

import java.sql.Timestamp;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: OutageRecord.java,v 1.2 2009/11/20 15:50:37 makub Exp $
 */
public class OutageRecord {

    private final String pbsHostName;
    private final String type;
    private final Timestamp start;
    private final Timestamp end;
    private final String comment;


    public OutageRecord(String pbsHostName, String type, Timestamp start, Timestamp end,String comment) {
        this.pbsHostName = pbsHostName;
        this.type = type;
        this.start = start;
        this.end = end;
        this.comment = comment;
    }

    public String getPbsHostName() {
        return pbsHostName;
    }

    public String getType() {
        return type;
    }

    public Timestamp getStart() {
        return start;
    }

    public Timestamp getEnd() {
        return end;
    }

    public String getComment() {
        return comment;
    }
}
