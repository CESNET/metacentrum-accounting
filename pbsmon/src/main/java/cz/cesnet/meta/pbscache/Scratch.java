package cz.cesnet.meta.pbscache;

import cz.cesnet.meta.pbs.PbsUtils;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class Scratch {

    //free je z pbs_cache
    Long localFreeKiB;
    Long ssdFreeKiB;
    Long networkFreeKiB;
    //size je z Node z PBS v bajtech
    Long localSize;
    Long ssdSize;
    private long ssdReservedByJobs;
    private long localReservedByJobs;

    public Scratch() {
    }

    public long getAnyFreeKiB() {
        long max = 0l;
        if (ssdFreeKiB != null && ssdFreeKiB > max) max = ssdFreeKiB;
        if (localFreeKiB != null && localFreeKiB > max) max = localFreeKiB;
        if (networkFreeKiB != null && networkFreeKiB > max) max = networkFreeKiB;
        return max;
    }

    public String getAnySizeInHumanUnits() {
        long max = 0l;
        if (localSize != null && localSize > max) max = localSize;
        if (ssdSize != null && ssdSize > max) max = ssdSize;
        if (networkFreeKiB != null && networkFreeKiB > max) max = networkFreeKiB * 1024;
        return PbsUtils.formatInHumanUnits(max);
    }

    public String getSsdSizeInPbsUnits() {
        if (ssdSize == null) return "-";
        return PbsUtils.formatInPbsUnits(ssdSize);
    }

    public int getSsdUsedPercent() {
        if (ssdFreeKiB == null) return 0;
        if (ssdSize == null) return 0;
        return (int) (((ssdSize - ssdFreeKiB * 1024l) * 100l) / ssdSize);
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

    public int getLocalUsedPercent() {
        if (localFreeKiB == null) return 0;
        if (localSize == null) return 0;
        return (int) (((localSize - localFreeKiB * 1024l) * 100l) / localSize);
    }

    public String getLocalUsedInPbsUnits() {
        if (localFreeKiB == null) return "unknown free size";
        if (localSize == null) return "unknown total size";
        return PbsUtils.formatInPbsUnits(localSize - localFreeKiB * 1024);
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

    public int getLocalReservedPercent() {
        if (localSize == null) return 0;
        return (int) (localReservedByJobs / localSize);
    }

    public boolean getHasSsd() {
        return ssdFreeKiB != null;
    }

    public boolean getHasLocal() {
        return localFreeKiB != null;
    }

    public boolean getHasNetwork() {
        return networkFreeKiB != null;
    }

    public String getLocalFreeHuman() {
        return PbsUtils.formatInHumanUnits(localFreeKiB * 1024);
    }

    public String getSsdFreeHuman() {
        return PbsUtils.formatInHumanUnits(ssdFreeKiB * 1024);
    }

    public String getNetworkFreeHuman() {
        return PbsUtils.formatInHumanUnits(networkFreeKiB * 1024);
    }

    public void setSsdSize(String ssdSizeInPbsUnits) {
        if (ssdSizeInPbsUnits == null) return;
        this.ssdSize = PbsUtils.parsePbsBytes(ssdSizeInPbsUnits);
    }

    public void setLocalSize(String localSizeInPbsUnits) {
        if (localSizeInPbsUnits == null) return;
        this.localSize = PbsUtils.parsePbsBytes(localSizeInPbsUnits);
    }

    @Override
    public String toString() {
        return "Scratch{" +
                "localFreeKiB=" + localFreeKiB +
                ", ssdFreeKiB=" + ssdFreeKiB +
                ", networkFreeKiB=" + networkFreeKiB +
                '}';
    }

    public boolean hasAnySizeKiB(long scratchKB) {
        return hasLocalSizeKiB(scratchKB)||hasSsdSizeKiB(scratchKB)||getHasNetwork();
    }

    public boolean hasLocalFreeKiB(long scratchKB) {
        return this.localFreeKiB != null && this.localFreeKiB >= scratchKB;
    }

    public boolean hasLocalSizeKiB(long scratchKB) {
        return this.localSize!=null && this.localSize >= scratchKB *1024;
    }

    public void setLocalFreeKiB(long localFreeKiB) {
        if (localFreeKiB == 0l) return;
        this.localFreeKiB = localFreeKiB;
    }

    public boolean hasSsdFreeKiB(long scratchKB) {
        return this.ssdFreeKiB != null && this.ssdFreeKiB >= scratchKB;
    }

    public boolean hasSsdSizeKiB(long scratchKB) {
        return this.ssdSize!=null && this.ssdSize >= scratchKB *1024;
    }

    public void setSsdFreeKiB(long ssdFreeKiB) {
        if (ssdFreeKiB == 0l) return;
        this.ssdFreeKiB = ssdFreeKiB;
    }

    public boolean hasNetworkFreeKiB(long scratchKB) {
        return this.networkFreeKiB != null && this.networkFreeKiB >= scratchKB;
    }

    public void setNetworkFreeKiB(long networkFreeKiB) {
        this.networkFreeKiB = networkFreeKiB;
    }

    public Long getLocalSize() {
        return localSize;
    }

    /**
     * Gets SSD disk size in bytes.
     * @return SSD size in bytes
     */
    public Long getSsdSize() {
        return ssdSize;
    }

    public void setSsdReservedByJobs(long ssdReservedByJobs) {
        this.ssdReservedByJobs = ssdReservedByJobs;
    }

    public long getSsdReservedByJobs() {
        return ssdReservedByJobs;
    }

    public void setLocalReservedByJobs(long localReservedByJobs) {
        this.localReservedByJobs = localReservedByJobs;
    }

    public long getLocalReservedByJobs() {
        return localReservedByJobs;
    }


    public Long getLocalFreeKiB() {
        return localFreeKiB;
    }

    public Long getSsdFreeKiB() {
        return ssdFreeKiB;
    }

    public Long getNetworkFreeKiB() {
        return networkFreeKiB;
    }
}
