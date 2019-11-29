package cz.cesnet.meta.cloud;

import java.util.List;
import java.util.Map;

public interface CloudLoader {
    List<CloudPhysicalHost> getPhysicalHosts();

    List<CloudVirtualHost> getVirtualHosts();

    Map<String, List<CloudVirtualHost>> getPhysicalHostToVMsMap();

    Map<String, CloudPhysicalHost> getPhysFqdnToPhysicalHostMap();

    Map<String, CloudPhysicalHost> getVmFqdnToPhysicalHostMap();

    void load();
}
