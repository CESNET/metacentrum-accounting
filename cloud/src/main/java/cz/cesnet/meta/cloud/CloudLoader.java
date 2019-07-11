package cz.cesnet.meta.cloud;

import java.util.List;
import java.util.Map;

public interface CloudLoader {
    List<CloudPhysicalHost> getPhysicalHosts();

    List<CloudVirtualHost> getVirtualHosts();

    Map<String, List<CloudVirtualHost>> getHostName2VirtualHostsMap();

    Map<String, CloudPhysicalHost> getHostname2HostMap();

    Map<String, CloudPhysicalHost> getVmFqdn2HostMap();

    void load();
}
