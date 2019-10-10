package cz.cesnet.meta.pbscache;

import cz.cesnet.meta.pbs.PbsUtils;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class Scratch {

    private String nodeName;
    private long localAvailable;
    private long ssdAvailable;
    private long sharedAvailable;
    private long ssdAssigned;
    private long localAssigned;
    private long sharedAssigned;

    public Scratch(String nodeName) {
        this.nodeName = nodeName;
    }

    public void setLocalAvailable(long localAvailable) {
        this.localAvailable = localAvailable;
    }

    public void setSsdAvailable(long ssdAvailable) {
        this.ssdAvailable = ssdAvailable;
    }

    public void setSharedAvailable(long networkFreeKiB) {
        this.sharedAvailable = networkFreeKiB;
    }

    public void setSsdAssigned(long ssdAssigned) {
        this.ssdAssigned = ssdAssigned;
    }

    public void setLocalAssigned(long localAssigned) {
        this.localAssigned = localAssigned;
    }

    public void setSharedAssigned(long sharedAssigned) {
        this.sharedAssigned = sharedAssigned;
    }

    public String getAnyAvailableInHumanUnits() {
        long max = 0L;
        if (localAvailable > max) max = localAvailable;
        if (ssdAvailable > max) max = ssdAvailable;
        if (sharedAvailable > max) max = sharedAvailable;
        return PbsUtils.formatInHumanUnits(max);
    }

    public String getSsdAvailableInPbsUnits() {
        if (ssdAvailable == 0L) return "-";
        return PbsUtils.formatInPbsUnits(ssdAvailable);
    }

    public String getLocalAvailableInPbsUnits() {
        if (localAvailable == 0L) return "-";
        return PbsUtils.formatInPbsUnits(localAvailable);
    }

    public String getSharedAvailableInPbsUnits() {
        if (sharedAvailable == 0L) return "-";
        return PbsUtils.formatInPbsUnits(sharedAvailable);
    }

    public String getSsdAssignedInPbsUnits() {
        return PbsUtils.formatInPbsUnits(ssdAssigned);
    }

    public String getLocalAssignedInPbsUnits() {
        return PbsUtils.formatInPbsUnits(localAssigned);
    }

    public String getSharedAssignedInPbsUnits() {
        return PbsUtils.formatInPbsUnits(sharedAssigned);
    }

    public int getSsdReservedPercent() {
        return (ssdAvailable == 0L) ? 0 : (int) (ssdAssigned / ssdAvailable);
    }

    public int getLocalReservedPercent() {
        return (localAvailable == 0L) ? 0 : (int) (localAssigned * 100L / localAvailable);
    }

    public int getSharedReservedPercent() {
        return (sharedAvailable == 0L) ? 0 : (int) (sharedAssigned * 100L / sharedAvailable);
    }

    public boolean getHasSsd() {
        return ssdAssigned > 0L || ssdAvailable > 0L;
    }

    public boolean getHasFreeSsd() {
        return ssdAssigned < ssdAvailable;
    }

    public boolean getHasLocal() {
        return localAssigned > 0L || localAvailable > 0L;
    }

    public boolean getHasFreeLocal() {
        return localAssigned < localAvailable;
    }

    public boolean getHasShared() {
        return sharedAssigned > 0L || sharedAvailable > 0L;
    }

    public boolean getHasFreeShared() {
        return sharedAssigned < sharedAvailable;
    }

    public String getLocalFreeHuman() {
        return PbsUtils.formatInHumanUnits(localAvailable - localAssigned);
    }

    public String getSsdFreeHuman() {
        return PbsUtils.formatInHumanUnits(ssdAvailable - ssdAssigned);
    }

    public String getSharedFreeHuman() {
        return PbsUtils.formatInHumanUnits(sharedAvailable - sharedAssigned);
    }

    public boolean hasLocalAvailableBytes(long scratchBytes) {
        return this.localAvailable >= scratchBytes;
    }

    public boolean hasSsdAvailableBytes(long scratchBytes) {
        return this.ssdAvailable >= scratchBytes;
    }

    public boolean hasSharedAvailableBytes(long scratchBytes) {
        return this.sharedAvailable >= scratchBytes;
    }

    @Override
    public String toString() {
        return "Scratch{" +
                "node='" + nodeName + '\'' +
                ", localAssigned=" + localAssigned +
                ", localAvailable=" + localAvailable +
                ", ssdAssigned=" + ssdAssigned +
                ", ssdAvailable=" + ssdAvailable +
                ", sharedAssigned=" + sharedAssigned +
                ", sharedAvailable=" + sharedAvailable +
                '}';
    }
}
