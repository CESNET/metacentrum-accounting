package cz.cesnet.meta.cloud;

import cz.cesnet.meta.RefreshLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class CloudImpl extends RefreshLoader implements Cloud {

    final static Logger log = LoggerFactory.getLogger(CloudImpl.class);

    private List<CloudServer> servers = Arrays.asList(
            new CloudServer("MetaCloud", "http://carach1.ics.muni.cz:12147/exports/hosts.json", "http://carach1.ics.muni.cz:12147/exports/vms.json"),
            new CloudServer("fedCloud", "http://carach5.ics.muni.cz:12147/exports/hosts.json", "http://carach5.ics.muni.cz:12147/exports/vms.json")
    );

    private CloudLoader cloudLoader;

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    private boolean disabled;


    @Override
    public int getRefreshTime() {
        return 0;
    }

    List<CloudPhysicalHost> physicalHosts;
    List<CloudVirtualHost> virtualHosts;
    Map<String, CloudPhysicalHost> hostname2HostMap;
    Map<String, List<CloudVirtualHost>> hostName2VirtualHostsMap;
    Map<String, CloudPhysicalHost> vmFqdn2HostMap;

    @Override
    protected void load() {
        List<CloudPhysicalHost> physicalHosts = null;
        List<CloudVirtualHost> virtualHosts = null;
        Map<String, CloudPhysicalHost> hostname2HostMap = null;
        Map<String, List<CloudVirtualHost>> hostName2VirtualHostsMap = null;
        Map<String, CloudPhysicalHost> vmFqdn2HostMap = null;
        if (disabled) {
            log.debug("cloud is disabled, using empty data");
            physicalHosts = Collections.emptyList();
            virtualHosts = Collections.emptyList();
            hostname2HostMap = Collections.emptyMap();
            hostName2VirtualHostsMap = Collections.emptyMap();
            vmFqdn2HostMap = Collections.emptyMap();
        } else {
            for (CloudServer server : servers) {
                log.debug("loading from {}", server);
                CloudLoader cloudLoader = new CloudLoader(server.getHostsURL(), server.getVmsURL());
                cloudLoader.load();
                log.debug("loaded from {}", server);
                if (physicalHosts == null) {
                    physicalHosts = cloudLoader.getPhysicalHosts();
                } else {
                    physicalHosts.addAll(cloudLoader.getPhysicalHosts());
                }
                if (virtualHosts == null) {
                    virtualHosts = cloudLoader.getVirtualHosts();
                } else {
                    virtualHosts.addAll(cloudLoader.getVirtualHosts());
                }
                if (hostname2HostMap == null) {
                    hostname2HostMap = cloudLoader.getHostname2HostMap();
                } else {
                    hostname2HostMap.putAll(cloudLoader.getHostname2HostMap());
                }
                if (hostName2VirtualHostsMap == null) {
                    hostName2VirtualHostsMap = cloudLoader.getHostName2VirtualHostsMap();
                } else {
                    hostName2VirtualHostsMap.putAll(cloudLoader.getHostName2VirtualHostsMap());
                }
                if (vmFqdn2HostMap == null) {
                    vmFqdn2HostMap = cloudLoader.getVmFqdn2HostMap();
                } else {
                    vmFqdn2HostMap.putAll(cloudLoader.getVmFqdn2HostMap());
                }
            }
        }
        this.physicalHosts = physicalHosts;
        this.virtualHosts = virtualHosts;
        this.hostname2HostMap = hostname2HostMap;
        this.hostName2VirtualHostsMap = hostName2VirtualHostsMap;
        this.vmFqdn2HostMap = vmFqdn2HostMap;
    }

    public List<CloudServer> getServers() {
        return servers;
    }

    public void setServers(List<CloudServer> servers) {
        this.servers = servers;
    }

    @Override
    public List<CloudPhysicalHost> getPhysicalHosts() {
        checkLoad();
        return physicalHosts;
    }

    @Override
    public List<CloudVirtualHost> getVirtualHosts() {
        checkLoad();
        return virtualHosts;
    }

    @Override
    public Map<String, List<CloudVirtualHost>> getHostName2VirtualHostsMap() {
        checkLoad();
        return hostName2VirtualHostsMap;
    }

    @Override
    public Map<String, CloudPhysicalHost> getHostname2HostMap() {
        checkLoad();
        return hostname2HostMap;
    }

    @Override
    public Map<String, CloudPhysicalHost> getVmFqdn2HostMap() {
        checkLoad();
        return vmFqdn2HostMap;
    }


}
