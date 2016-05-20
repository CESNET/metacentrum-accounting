package cz.cesnet.meta.acct.hw.acct;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: HostResourceRelation.java,v 1.1 2009/09/23 15:42:09 makub Exp $
 */
public class HostResourceRelation {

    int id;
    int hostId;
    int resourceId;
    Date start;
    Date end;

    public HostResourceRelation(int id, int hostId, int resourceId, Date start, Date end) {
        this.id = id;
        this.hostId = hostId;
        this.resourceId = resourceId;
        this.start = start;
        this.end = end;
    }

    public int getId() {
        return id;
    }

    public int getHostId() {
        return hostId;
    }

    public int getResourceId() {
        return resourceId;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }
}
