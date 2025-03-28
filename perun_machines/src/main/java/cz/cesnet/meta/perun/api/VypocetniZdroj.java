package cz.cesnet.meta.perun.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VypocetniZdroj implements Serializable {

    private String id = null;
    private String nazev = null;
    private boolean cluster = false;
    private String popisKey = null;

    private String photo;
    private String thumbnail;
    private String cpuDesc;
    private String gpuDesc;
    private String memory;

    private Stroj stroj;
    private List<Stroj> stroje;
    private List<String> voNames = new ArrayList<>(1);

    public VypocetniZdroj(String id, String nazev, boolean cluster, String popisKey, String urlKey) {
        this.id = id;
        this.nazev = nazev;
        this.cluster = cluster;
        this.popisKey = popisKey;
        if (popisKey == null)
            this.popisKey = "";

    }

    public VypocetniZdroj(String id, String nazev, boolean cluster) {
        this(id,nazev,cluster,"resource-" +id+ "-desc","resource-" + id + "-url");
    }

    public String getId() {
        return this.id;
    }

    public String getNazev() {
        return this.nazev;
    }

    public boolean isCluster() {
        return this.cluster;
    }

    public String getSpecKey() {
        return cluster?""+id+"-clust-spec":""+id+"-host-spec";
    }

    public String getPopisKey() {
        return this.popisKey;
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


    public Stroj getStroj() {
        return stroj;
    }

    public void setStroj(Stroj stroj) {
        this.stroj = stroj;
    }

    public List<Stroj> getStroje() {
        return stroje;
    }

    public void setStroje(List<Stroj> stroje) {
        this.stroje = stroje;
    }

    @Override
    public String toString() {
        return "VypocetniZdroj{" +
                "id='" + id + '\'' +
                ", nazev='" + nazev + '\'' +
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
        VypocetniZdroj that = (VypocetniZdroj) o;
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