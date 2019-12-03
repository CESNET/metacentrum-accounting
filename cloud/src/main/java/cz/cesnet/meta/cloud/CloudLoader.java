package cz.cesnet.meta.cloud;

import java.util.List;

public interface CloudLoader {

    List<CloudPhysicalHost> getPhysicalHosts();

    List<CloudVM> getVirtualHosts();

    void load();

    String getName();
}
