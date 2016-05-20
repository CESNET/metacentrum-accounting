package cz.cesnet.meta.acct.hw.acct;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: CpuRecord.java,v 1.1 2009/09/23 15:42:09 makub Exp $
 */
public class CpuRecord {

    int id;
    int physicalHostId;
    int cpuNum;
    Date start;
    Date end;

    public CpuRecord(int id, int physicalHostId, int cpuNum, Date start, Date end) {
        this.id = id;
        this.physicalHostId = physicalHostId;
        this.cpuNum = cpuNum;
        this.start = start;
        this.end = end;
    }

    public int getId() {
        return id;
    }

    public int getPhysicalHostId() {
        return physicalHostId;
    }

    public int getCpuNum() {
        return cpuNum;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }
}
