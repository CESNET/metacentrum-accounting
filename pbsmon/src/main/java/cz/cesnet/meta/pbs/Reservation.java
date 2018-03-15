package cz.cesnet.meta.pbs;

import java.util.Date;

/**
 * Represents a reservation in PBSPro.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class Reservation extends PbsInfoObject {

    public Reservation(String name) {
        super(name);
    }

    public String getQueue() {
        return attrs.get("queue");
    }

    private Date reserveStart=null;
    public Date getReserveStart() {
        if (reserveStart == null) {
            reserveStart = PbsUtils.getJavaTime(attrs.get("reserve_start"));
        }
        return reserveStart;
    }

    private Date reserveEnd;
    public Date getReserveEnd() {
        if (reserveEnd == null) {
            reserveEnd = PbsUtils.getJavaTime(attrs.get("reserve_end"));
        }
        return reserveEnd;
    }

    private Date ctime;
    public Date getCreatedTime() {
        if (ctime == null) {
            ctime = PbsUtils.getJavaTime(attrs.get("ctime"));
        }
        return ctime;
    }

    public String getOwner() {
        return attrs.get("Reserve_Owner");
    }
}
