package cz.cesnet.meta.acct.hw.acct;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: ReservedRecord.java,v 1.1 2009/09/24 11:45:02 makub Exp $
 */
public class ReservedRecord {
    int id;
    boolean frontend;
    boolean reserved;

    public ReservedRecord(int id, boolean frontend, boolean reserved) {
        this.id = id;
        this.frontend = frontend;
        this.reserved = reserved;
    }

    public int getId() {
        return id;
    }

    public boolean isFrontend() {
        return frontend;
    }

    public boolean isReserved() {
        return reserved;
    }
}
