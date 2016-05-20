package cz.cesnet.meta.acct.hw.stats;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: PhysicalMachineRecord.java,v 1.2 2009/12/01 14:07:12 makub Exp $
 */
public class PhysicalMachineRecord {

    int virtCount;
    int physicalHostId;
    String name;
    int cpuCount;

    public PhysicalMachineRecord(int virtCount, int physicalHostId, String name, int cpuCount) {
        this.virtCount = virtCount;
        this.physicalHostId = physicalHostId;
        this.name = name;
        this.cpuCount = cpuCount;
    }

    public int getVirtCount() {
        return virtCount;
    }

    public int getPhysicalHostId() {
        return physicalHostId;
    }

    public String getName() {
        return name;
    }

    public int getCpuCount() {
        return cpuCount;
    }

    @Override
    public String toString() {
        return "PhysicalMachineRecord{" +
                "virtCount=" + virtCount +
                ", id=" + physicalHostId +
                ", name='" + name + '\'' +
                ", cpuCount=" + cpuCount +
                '}';
    }
}
