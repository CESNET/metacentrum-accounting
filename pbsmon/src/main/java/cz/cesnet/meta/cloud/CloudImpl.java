package cz.cesnet.meta.cloud;

import cz.cesnet.meta.RefreshLoader;
import cz.cesnet.meta.cloud.opennebula.NebulaCloudLoader;
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
            new CloudServer("OpenNebula", "http://carach1.ics.muni.cz:12147/exports/hosts.json", "http://carach1.ics.muni.cz:12147/exports/vms.json")
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
    Map<String, CloudPhysicalHost> physFqdnToPhysicalHostMap;
    Map<String, List<CloudVirtualHost>> physicalHostToVMsMap;
    Map<String, CloudPhysicalHost> vmFqdnToPhysicalHostMap;

    @Override
    protected void load() {
        List<CloudPhysicalHost> physicalHosts = null;
        List<CloudVirtualHost> virtualHosts = null;
        Map<String, CloudPhysicalHost> physFqdnToPhysicalHostMap = null;
        Map<String, List<CloudVirtualHost>> physicalHostToVMsMap = null;
        Map<String, CloudPhysicalHost> vmFqdnToPhysicalHostMap = null;
        if (disabled) {
            log.debug("cloud is disabled, using empty data");
            physicalHosts = Collections.emptyList();
            virtualHosts = Collections.emptyList();
            physFqdnToPhysicalHostMap = Collections.emptyMap();
            physicalHostToVMsMap = Collections.emptyMap();
            vmFqdnToPhysicalHostMap = Collections.emptyMap();
        } else {
            for (CloudServer server : servers) {
                log.debug("loading cloud from {}", server);
                CloudLoader cloudLoader = new NebulaCloudLoader(server.getName(), server.getHostsURL(), server.getVmsURL());
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
                if (physFqdnToPhysicalHostMap == null) {
                    physFqdnToPhysicalHostMap = cloudLoader.getPhysFqdnToPhysicalHostMap();
                } else {
                    physFqdnToPhysicalHostMap.putAll(cloudLoader.getPhysFqdnToPhysicalHostMap());
                }
                if (physicalHostToVMsMap == null) {
                    physicalHostToVMsMap = cloudLoader.getPhysicalHostToVMsMap();
                } else {
                    physicalHostToVMsMap.putAll(cloudLoader.getPhysicalHostToVMsMap());
                }
                if (vmFqdnToPhysicalHostMap == null) {
                    vmFqdnToPhysicalHostMap = cloudLoader.getVmFqdnToPhysicalHostMap();
                } else {
                    vmFqdnToPhysicalHostMap.putAll(cloudLoader.getVmFqdnToPhysicalHostMap());
                }
            }
        }
        this.physicalHosts = physicalHosts;
        this.virtualHosts = virtualHosts;
        this.physFqdnToPhysicalHostMap = physFqdnToPhysicalHostMap;
        this.physicalHostToVMsMap = physicalHostToVMsMap;
        this.vmFqdnToPhysicalHostMap = vmFqdnToPhysicalHostMap;
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
    public Map<String, List<CloudVirtualHost>> getPhysicalHostToVMsMap() {
        checkLoad();
        return physicalHostToVMsMap;
    }

    @Override
    public Map<String, CloudPhysicalHost> getPhysFqdnToPhysicalHostMap() {
        checkLoad();
        return physFqdnToPhysicalHostMap;
    }

    @Override
    public Map<String, CloudPhysicalHost> getVmFqdnToPhysicalHostMap() {
        checkLoad();
        return vmFqdnToPhysicalHostMap;
    }


}
