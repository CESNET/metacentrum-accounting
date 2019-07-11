package cz.cesnet.meta.cloud.opennebula;

import cz.cesnet.meta.cloud.CloudLoader;
import cz.cesnet.meta.cloud.CloudPhysicalHost;
import cz.cesnet.meta.cloud.CloudVirtualHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements loading OpenNebula state from given URLs.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class NebulaCloudLoader implements CloudLoader {

    final static Logger log = LoggerFactory.getLogger(NebulaCloudLoader.class);

    private final String hostsUrl;
    private final String vmsUrl;
    private List<CloudPhysicalHost> physicalHosts;
    private List<CloudVirtualHost> virtualHosts;
    private Map<String, List<CloudVirtualHost>> host2VMsMap;
    private Map<String, CloudPhysicalHost> hostname2HostMap;
    private Map<String, CloudPhysicalHost> vmFqdn2HostMap;

    public NebulaCloudLoader(String hostsUrl, String vmsUrl) {
        this.hostsUrl = hostsUrl;
        this.vmsUrl = vmsUrl;
    }

    @Override
    public List<CloudPhysicalHost> getPhysicalHosts() {
        return physicalHosts;
    }

    @Override
    public List<CloudVirtualHost> getVirtualHosts() {
        return virtualHosts;
    }

    @Override
    public Map<String, List<CloudVirtualHost>> getHostName2VirtualHostsMap() {
        return host2VMsMap;
    }

    @Override
    public Map<String, CloudPhysicalHost> getHostname2HostMap() {
        return hostname2HostMap;
    }

    @Override
    public Map<String, CloudPhysicalHost> getVmFqdn2HostMap() {
        return vmFqdn2HostMap;
    }

    @Override
    public void load() {
        log.debug("load()");
        loadPhysicalHosts();
        loadVirtualHosts();
        prepareVMMap();
        checkMissingHosts();
        prepareHostMap();
        prepareVmFqdn2HostMap();
        log.debug("load() end");
    }

    private void prepareVmFqdn2HostMap() {
        vmFqdn2HostMap = new HashMap<>(virtualHosts.size() * 2);
        for (CloudPhysicalHost host : physicalHosts) {
            List<CloudVirtualHost> vms = host2VMsMap.get(host.getName());
            if (vms != null) {
                for (CloudVirtualHost vm : vms) {
                    vmFqdn2HostMap.put(vm.getFqdn(), host);
                }
            } else {
                host2VMsMap.put(host.getName(), Collections.emptyList());
            }
        }
    }

    private void prepareHostMap() {
        Map<String, CloudPhysicalHost> map = new HashMap<>(physicalHosts.size() * 2);
        for (CloudPhysicalHost host : physicalHosts) {
            map.put(host.getHostname(), host);
        }
        hostname2HostMap = map;
    }

    private void checkMissingHosts() {
        log.debug("checkMissingHosts()");
        Set<String> hostNamesSet = new HashSet<>(physicalHosts.size() * 2);
        hostNamesSet.addAll(physicalHosts.stream().map(CloudPhysicalHost::getName).collect(Collectors.toList()));
        List<CloudPhysicalHost> fakeHosts = new ArrayList<>();
        for (String hostName : host2VMsMap.keySet()) {
            if (!hostNamesSet.contains(hostName)) {
                List<CloudVirtualHost> cloudVirtualHosts = host2VMsMap.get(hostName);
                List<String> vmIds = new ArrayList<>(cloudVirtualHosts.size());
                int cpu_reserved_x100 = 0;
                for (CloudVirtualHost vm : cloudVirtualHosts) {
                    vmIds.add(vm.getIdString());
                    cpu_reserved_x100 += vm.getCpu_reserved_x100();
                }
                log.warn("physical host {} not existing but has VMs {}", hostName, vmIds);
                CloudPhysicalHost fake = new CloudPhysicalHost();
                fake.setName(hostName);
                fake.setHostname(hostName);
                fake.setVms_running(cloudVirtualHosts.size());
                fake.setCpu_avail_x100(cpu_reserved_x100);
                fake.setCpu_reserved_x100(cpu_reserved_x100);
                fake.setState("FAKE");
                fake.getParsedName();
                fakeHosts.add(fake);
                log.debug("creating fake host {}", fake);
            }
        }
        //add FAKE hosts to real physical machines
        if (!fakeHosts.isEmpty()) {
            log.warn("adding {} fake physical hosts: {}", fakeHosts.size(), fakeHosts);
            physicalHosts.addAll(fakeHosts);
        }
        physicalHosts.sort(CloudPhysicalHost.CLOUD_PHYSICAL_HOST_COMPARATOR);
    }


    private void prepareVMMap() {    // from vm.current_host=host.name -> vms
        log.debug("prepareVMMap()");
        Map<String, List<CloudVirtualHost>> map = new HashMap<>(virtualHosts.size() * 2);
        for (CloudVirtualHost vm : virtualHosts) {
            List<CloudVirtualHost> hostVMsList = map.computeIfAbsent(vm.getCurrent_host(), k -> new ArrayList<>(1));
            hostVMsList.add(vm);
        }
        this.host2VMsMap = map;
    }

    private void loadVirtualHosts() {
        log.debug("loadVirtualHosts({})",vmsUrl);
        RestTemplate rt = new RestTemplate();
        List<CloudVirtualHost> virtualHosts1 = rt.getForObject(vmsUrl, VmsDocument.class).getVms();
        List<CloudVirtualHost> activeHosts = new ArrayList<>(virtualHosts1.size());
        for (CloudVirtualHost vm : virtualHosts1) {
            if (vm.isReserved()) {
                activeHosts.add(vm);
            } else {
                log.debug("skipping {} in state {}", vm.getIdString(), vm.getState());
            }
        }
        this.virtualHosts = activeHosts;
    }

    private void loadPhysicalHosts() {
        log.debug("loadPhysicalHosts({})",hostsUrl);
        //read physical hosts
        try {
            RestTemplate rt = new RestTemplate();
            this.physicalHosts = rt.getForObject(hostsUrl, HostsDocument.class).getHosts();
        } catch (RestClientException ex) {
            log.error("cannot load physical hosts from "+hostsUrl,ex);
        }
    }
}
