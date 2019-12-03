package cz.cesnet.meta.cloud;

import cz.cesnet.meta.RefreshLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class CloudImpl extends RefreshLoader implements Cloud {

    final static Logger log = LoggerFactory.getLogger(CloudImpl.class);

    private List<CloudLoader> cloudLoaders = Arrays.asList(
            new NebulaCloudLoader("OpenNebula", "http://carach1.ics.muni.cz:12147/exports/hosts.json", "http://carach1.ics.muni.cz:12147/exports/vms.json"),
            new OpenStackCloudLoader("OpenStack", "/home/openstack")
    );

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
    List<CloudVM> virtualHosts;
    Map<String, CloudPhysicalHost> physFqdnToPhysicalHostMap;
    Map<String, List<CloudVM>> physicalHostToVMsMap;
    Map<String, CloudPhysicalHost> vmFqdnToPhysicalHostMap;

    private static final Function<String, List<CloudVM>> NEW_LIST = k -> new ArrayList<>(1);

    @Override
    protected void load() {
        List<CloudPhysicalHost> physicalHosts = null;
        List<CloudVM> virtualHosts = null;
        Map<String, CloudPhysicalHost> physFqdnToPhysicalHostMap;
        Map<String, List<CloudVM>> physicalHostToVMsMap;
        Map<String, CloudPhysicalHost> vmFqdnToPhysicalHostMap;
        if (disabled) {
            log.debug("cloud is disabled, using empty data");
            physicalHosts = Collections.emptyList();
            virtualHosts = Collections.emptyList();
            physFqdnToPhysicalHostMap = Collections.emptyMap();
            physicalHostToVMsMap = Collections.emptyMap();
            vmFqdnToPhysicalHostMap = Collections.emptyMap();
        } else {
            for (CloudLoader cloudLoader : cloudLoaders) {
                log.debug("loading cloud from {}", cloudLoader.getName());
                cloudLoader.load();
                log.debug("loaded from {}", cloudLoader.getName());
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
            }
            //sort
            physicalHosts.sort(CloudPhysicalHost.CLOUD_PHYSICAL_HOST_COMPARATOR);
            //map names of physical hosts to their objects
            physFqdnToPhysicalHostMap = new HashMap<>(physicalHosts.size() * 2);
            for (CloudPhysicalHost physicalHost : physicalHosts) {
                physFqdnToPhysicalHostMap.put(physicalHost.getFqdn(), physicalHost);
            }
            //map physical hosts to virtual hosts
            physicalHostToVMsMap = new HashMap<>(virtualHosts.size() * 2);
            for (CloudVM vm : virtualHosts) {
                physicalHostToVMsMap.computeIfAbsent(vm.getPhysicalHostFqdn(), NEW_LIST).add(vm);
            }
            //map VMs to physical hosts, compute reserved CPUs on each physical host
            vmFqdnToPhysicalHostMap = new HashMap<>(virtualHosts.size() * 2);
            for (CloudPhysicalHost physicalHost : physicalHosts) {
                List<CloudVM> vms = physicalHostToVMsMap.get(physicalHost.getFqdn());
                int cpu_reserved_x100 = 0;
                if (vms != null) {
                    for (CloudVM vm : vms) {
                        vmFqdnToPhysicalHostMap.put(vm.getFqdn(), physicalHost);
                        cpu_reserved_x100 += vm.getCpu_reserved_x100();
                    }
                } else {
                    physicalHostToVMsMap.put(physicalHost.getName(), Collections.emptyList());
                }
                physicalHost.setCpuReserved(cpu_reserved_x100/100);
            }
        }
        this.physicalHosts = physicalHosts;
        this.virtualHosts = virtualHosts;
        this.physFqdnToPhysicalHostMap = physFqdnToPhysicalHostMap;
        this.physicalHostToVMsMap = physicalHostToVMsMap;
        this.vmFqdnToPhysicalHostMap = vmFqdnToPhysicalHostMap;
    }

    @Override
    public List<CloudPhysicalHost> getPhysicalHosts() {
        checkLoad();
        return physicalHosts;
    }

    @Override
    public List<CloudVM> getVirtualHosts() {
        checkLoad();
        return virtualHosts;
    }

    @Override
    public Map<String, List<CloudVM>> getPhysicalHostToVMsMap() {
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
