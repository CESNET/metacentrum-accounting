package cz.cesnet.meta.perun.api;

import java.io.Serializable;
import java.util.List;

public class VypocetniCentrum implements Serializable {
    public static final String CLUST_SPEC_KEY = "-clust-spec";
    private String id = null;
    private String nazevKey = null;
    private String urlKey = null;
    private List<VypocetniZdroj> zdroje = null;

    public VypocetniCentrum(String id, String nazevKey, String urlKey) {
        this.id = id;
        this.nazevKey = nazevKey;
        this.urlKey = urlKey;
    }

    public String getId() {
        return this.id;
    }

    public String getNazevKey() {
        return this.nazevKey;
    }

    public String getSpecKey() {
        return this.id + CLUST_SPEC_KEY;
    }

    public String getUrlKey() {
        return this.urlKey;
    }

    public List<VypocetniZdroj> getZdroje() {
        return this.zdroje;
    }

    public void setZdroje(List<VypocetniZdroj> zdroje) {
        this.zdroje = zdroje;
    }

    @Override
    public String toString() {
        return "VypocetniCentrum{" +
                "id='" + id + '\'' +
                ", nazev='" + nazevKey + '\'' +
                ", url='" + urlKey + '\'' +
                '}';
    }
}