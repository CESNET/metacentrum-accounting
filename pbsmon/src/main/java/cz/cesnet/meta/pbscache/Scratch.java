package cz.cesnet.meta.pbscache;

import cz.cesnet.meta.pbs.PbsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class Scratch {

    final static Logger log = LoggerFactory.getLogger(Scratch.class);

    private String nodeName;
    Long localFreeKiB;
    Long ssdFreeKiB;
    Long sharedFreeKiB;
    Long localSize;
    Long ssdSize;
    Long sharedSize;
    private long ssdReservedByJobs;
    private long localReservedByJobs;
    private long sharedReservedByJobs;

    public Scratch(String nodeName) {
        this.nodeName = nodeName;
    }

    public long getAnyFreeKiB() {
        long max = 0L;
        if (ssdFreeKiB != null && ssdFreeKiB > max) max = ssdFreeKiB;
        if (localFreeKiB != null && localFreeKiB > max) max = localFreeKiB;
        if (sharedFreeKiB != null && sharedFreeKiB > max) max = sharedFreeKiB;
        return max;
    }

    public String getAnySizeInHumanUnits() {
        long max = 0L;
        if (localSize != null && localSize > max) max = localSize;
        if (ssdSize != null && ssdSize > max) max = ssdSize;
        if (sharedFreeKiB != null && sharedFreeKiB > max) max = sharedFreeKiB * 1024;
        return PbsUtils.formatInHumanUnits(max);
    }

    public String getSsdSizeInPbsUnits() {
        if (ssdSize == null) return "-";
        return PbsUtils.formatInPbsUnits(ssdSize);
    }

    public int getSsdUsedPercent() {
        if (ssdFreeKiB == null) return 0;
        if (ssdSize == null) return 0;
        return (int) (((ssdSize - ssdFreeKiB * 1024L) * 100L) / ssdSize);
    }

    public String getSsdUsedInPbsUnits() {
        if (ssdFreeKiB == null) return "unknown free size";
        if (ssdSize == null) return "unknown total size";
        return PbsUtils.formatInPbsUnits(ssdSize - ssdFreeKiB * 1024);
    }

    public String getLocalSizeInPbsUnits() {
        if (localSize == null) return "-";
        return PbsUtils.formatInPbsUnits(localSize);
    }

    public String getSharedSizeInPbsUnits() {
        if (sharedSize == null) return "-";
        return PbsUtils.formatInPbsUnits(sharedSize);
    }

    public int getLocalUsedPercent() {
        if (localFreeKiB == null) return 0;
        if (localSize == null) return 0;
        return (int) (((localSize - localFreeKiB * 1024L) * 100L) / localSize);
    }

    public int getSharedUsedPercent() {
        if (sharedFreeKiB == null) return 0;
        if (sharedSize == null) return 0;
        return (int) (((sharedSize - sharedFreeKiB * 1024L) * 100L) / sharedSize);
    }

    public String getLocalUsedInPbsUnits() {
        if (localFreeKiB == null) return "unknown free size";
        if (localSize == null) return "unknown total size";
        return PbsUtils.formatInPbsUnits(localSize - localFreeKiB * 1024);
    }

    public String getSharedUsedInPbsUnits() {
        if (sharedFreeKiB == null) return "unknown free size";
        if (sharedSize == null) return "unknown total size";
        return PbsUtils.formatInPbsUnits(sharedSize - sharedFreeKiB * 1024);
    }

    public String getSsdReservedInPbsUnits() {
        return PbsUtils.formatInPbsUnits(ssdReservedByJobs);
    }

    public int getSsdReservedPercent() {
        if (ssdSize == null) return 0;
        return (int) (ssdReservedByJobs / ssdSize);
    }

    public String getLocalReservedInPbsUnits() {
        return PbsUtils.formatInPbsUnits(localReservedByJobs);
    }

    public String getSharedReservedInPbsUnits() {
        return PbsUtils.formatInPbsUnits(sharedReservedByJobs);
    }

    public int getLocalReservedPercent() {
        if (localSize == null) return 0;
        return (int) (localReservedByJobs*100L / localSize);
    }

    public int getSharedReservedPercent() {
        if (sharedSize == null) return 0;
        return (int) (sharedReservedByJobs*100L / sharedSize);
    }

    public boolean getHasSsd() {
        return hasSsdSizeKiB(1L);
    }

    public boolean getHasFreeSsd() {
        return hasSsdFreeKiB(1L);
    }

    public boolean getHasLocal() {
        return hasLocalSizeKiB(1L);
    }

    public boolean getHasFreeLocal() {
        return hasLocalFreeKiB(1L);
    }

    public boolean getHasShared() {
        return hasSharedSizeKiB(1L);
    }

    public boolean getHasFreeShared() {
        return hasSharedFreeKiB(1L);
    }

    public String getLocalFreeHuman() {
        return PbsUtils.formatInHumanUnits(localFreeKiB * 1024);
    }

    public String getSsdFreeHuman() {
        return PbsUtils.formatInHumanUnits(ssdFreeKiB * 1024);
    }

    public String getSharedFreeHuman() {
        return PbsUtils.formatInHumanUnits(sharedFreeKiB * 1024);
    }

    public void setSsdSize(String ssdSizeInPbsUnits) {
        if (ssdSizeInPbsUnits == null) return;
        this.ssdSize = PbsUtils.parsePbsBytes(ssdSizeInPbsUnits);
    }

    public void setLocalSize(String localSizeInPbsUnits) {
        if (localSizeInPbsUnits == null) return;
        this.localSize = PbsUtils.parsePbsBytes(localSizeInPbsUnits);
    }

    public void setSharedSize(String sharedSizeInPbsUnits) {
        if (sharedSizeInPbsUnits == null) return;
        this.sharedSize = PbsUtils.parsePbsBytes(sharedSizeInPbsUnits);
    }

    public boolean hasAnySizeKiB(long scratchKB) {
        return hasLocalSizeKiB(scratchKB) || hasSsdSizeKiB(scratchKB) || getHasShared();
    }

    public boolean hasLocalFreeKiB(long scratchKB) {
        return this.localFreeKiB != null && this.localFreeKiB >= scratchKB;
    }

    public boolean hasLocalSizeKiB(long scratchKB) {
        return this.localSize != null && this.localSize >= scratchKB * 1024;
    }


    public void setLocalFreeKiB(long localFreeKiB) {
        if (localFreeKiB == 0L) return;
        this.localFreeKiB = localFreeKiB;
    }

    public boolean hasSsdFreeKiB(long scratchKB) {
        return this.ssdFreeKiB != null && this.ssdFreeKiB >= scratchKB;
    }

    public boolean hasSsdSizeKiB(long scratchKB) {
        return this.ssdSize != null && this.ssdSize >= scratchKB * 1024;
    }

    public void setSsdFreeKiB(long ssdFreeKiB) {
        if (ssdFreeKiB == 0L) return;
        this.ssdFreeKiB = ssdFreeKiB;
    }

    public boolean hasSharedFreeKiB(long scratchKB) {
        return this.sharedFreeKiB != null && this.sharedFreeKiB >= scratchKB;
    }

    public boolean hasSharedSizeKiB(long scratchKB) {
        return this.sharedSize != null && this.sharedSize >= scratchKB * 1024;
    }

    public void setSharedFreeKiB(long networkFreeKiB) {
        this.sharedFreeKiB = networkFreeKiB;
    }

    public void setSsdReservedByJobs(long ssdReservedByJobs) {
        this.ssdReservedByJobs = ssdReservedByJobs;
    }

    public void setLocalReservedByJobs(long localReservedByJobs) {
        this.localReservedByJobs = localReservedByJobs;
    }

    public void setSharedReservedByJobs(long sharedReservedByJobs) {
        this.sharedReservedByJobs = sharedReservedByJobs;
    }

    public long getSsdReservedByJobs() {
        return ssdReservedByJobs;
    }

    public long getLocalReservedByJobs() {
        return localReservedByJobs;
    }

    public long getSharedReservedByJobs() {
        return sharedReservedByJobs;
    }

    @Override
    public String toString() {
        return "Scratch{" +
                "localFreeKiB=" + localFreeKiB +
                ", ssdFreeKiB=" + ssdFreeKiB +
                ", networkFreeKiB=" + sharedFreeKiB +
                ", localSize=" + localSize +
                ", ssdSize=" + ssdSize +
                ", sharedSize=" + sharedSize +
                ", ssdReservedByJobs=" + ssdReservedByJobs +
                ", localReservedByJobs=" + localReservedByJobs +
                ", sharedReservedByJobs=" + sharedReservedByJobs +
                '}';
    }
}
