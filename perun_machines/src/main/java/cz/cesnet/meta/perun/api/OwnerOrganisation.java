package cz.cesnet.meta.perun.api;

import java.io.Serializable;
import java.util.List;

public class OwnerOrganisation implements Serializable {
    public static final String CLUST_SPEC_KEY = "-clust-spec";
    private String id = null;
    private String nameKey = null;
    private String urlKey = null;
    private List<PerunComputingResource> perunComputingResources = null;

    public OwnerOrganisation(String id, String nameKey, String urlKey) {
        this.id = id;
        this.nameKey = nameKey;
        this.urlKey = urlKey;
    }

    public String getId() {
        return this.id;
    }

    public String getNameKey() {
        return this.nameKey;
    }

    public String getSpecKey() {
        return this.id + CLUST_SPEC_KEY;
    }

    public String getUrlKey() {
        return this.urlKey;
    }

    public List<PerunComputingResource> getPerunComputingResources() {
        return this.perunComputingResources;
    }

    public void setPerunComputingResources(List<PerunComputingResource> perunComputingResources) {
        this.perunComputingResources = perunComputingResources;
    }

    @Override
    public String toString() {
        return "OwnerOrganisation{" +
                "id='" + id + '\'' +
                ", nazev='" + nameKey + '\'' +
                ", url='" + urlKey + '\'' +
                '}';
    }
}