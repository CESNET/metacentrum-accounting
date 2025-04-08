package cz.cesnet.meta.perun.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PerunComputingResource implements Serializable {

    private String id = null;
    private String name = null;
    private boolean cluster = false;
    private String descriptionKey = null;

    private String photo;
    private String thumbnail;
    private String cpuDesc;
    private String gpuDesc;
    private String memory;

    private PerunMachine perunMachine;
    private List<PerunMachine> perunMachines;
    private List<String> voNames = new ArrayList<>(1);

    public PerunComputingResource(String id, String name, boolean cluster, String descriptionKey) {
        this.id = id;
        this.name = name;
        this.cluster = cluster;
        this.descriptionKey = descriptionKey;
        if (descriptionKey == null)
            this.descriptionKey = "";

    }

    public PerunComputingResource(String id, String name, boolean cluster) {
        this(id, name,cluster,"resource-" +id+ "-desc");
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public boolean isCluster() {
        return this.cluster;
    }

    public String getSpecKey() {
        return cluster?""+id+"-clust-spec":""+id+"-host-spec";
    }

    public String getDescriptionKey() {
        return this.descriptionKey;
    }

    public String getDiskKey() {
        return "resource-"+id+"-disk";
    }

    public String getNetworkKey() {
        return "resource-"+id+"-network";
    }

    public String getCommentKey() {
        return "resource-"+id+"-comment";
    }

    public String getOwnerKey() {
        return "resource-"+id+"-owner";
    }


    public PerunMachine getPerunMachine() {
        return perunMachine;
    }

    public void setPerunMachine(PerunMachine perunMachine) {
        this.perunMachine = perunMachine;
    }

    public List<PerunMachine> getPerunMachines() {
        return perunMachines;
    }

    public void setPerunMachines(List<PerunMachine> perunMachines) {
        this.perunMachines = perunMachines;
    }

    @Override
    public String toString() {
        return "PerunComputingResource{" +
                "id='" + id + '\'' +
                ", nazev='" + name + '\'' +
                ", cluster=" + cluster +
                ", photo='" + photo + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", cpuDesc='" + cpuDesc + '\'' +
                ", gpuDesc='" + gpuDesc + '\'' +
                ", memory='" + memory + '\'' +
                ", voNames=" + voNames +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PerunComputingResource that = (PerunComputingResource) o;
        return !(id != null ? !id.equals(that.id) : that.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getCpuDesc() {
        return cpuDesc;
    }

    public void setCpuDesc(String cpuDesc) {
        this.cpuDesc = cpuDesc;
    }

    public String getGpuDesc() {
        return gpuDesc;
    }

    public void setGpuDesc(String gpuDesc) {
        this.gpuDesc = gpuDesc;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public void setVoNames(List<String> voNames) {
        this.voNames = voNames;
    }

    public List<String> getVoNames() {
        return voNames;
    }
}