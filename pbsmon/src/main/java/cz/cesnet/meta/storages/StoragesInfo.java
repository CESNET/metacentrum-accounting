package cz.cesnet.meta.storages;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class StoragesInfo {

    private List<Storage> storages;
    private String totalSize;
    private String totalFree;
    private String totalUsed;

    public StoragesInfo(List<Storage> storages) {
        this.storages = storages;
        long totSizeGB = 0;
        long totFreeGB = 0;
        long totUsedGB = 0;
        for(Storage storage : storages) {
            totSizeGB += storage.getTotalGB();
            totFreeGB += storage.getFreeGB();
            totUsedGB += storage.getUsedGB();
        }
        this.totalSize = (totSizeGB/1024) + "\u00A0TiB";
        this.totalFree = (totFreeGB/1024) + "\u00A0TiB";
        this.totalUsed = (totUsedGB/1024) + "\u00A0TiB";
    }

    public List<Storage> getStorages() {
        return storages;
    }

    public String getTotalSize() {
        return totalSize;
    }

    public String getTotalFree() {
        return totalFree;
    }

    public String getTotalUsed() {
        return totalUsed;
    }

    @Override
    public String toString() {
        return "StoragesInfo{" +
                "storages=" + storages +
                ", totalSize='" + totalSize + '\'' +
                ", totalFree='" + totalFree + '\'' +
                ", totalUsed='" + totalUsed + '\'' +
                '}';
    }
}
