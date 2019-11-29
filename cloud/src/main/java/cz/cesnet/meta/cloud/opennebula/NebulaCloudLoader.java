package cz.cesnet.meta.cloud.opennebula;

import com.fasterxml.jackson.databind.JsonNode;
import cz.cesnet.meta.cloud.CloudLoader;
import cz.cesnet.meta.cloud.CloudPhysicalHost;
import cz.cesnet.meta.cloud.CloudVirtualHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implements loading OpenNebula state from given URLs.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class NebulaCloudLoader implements CloudLoader {

    final static Logger log = LoggerFactory.getLogger(NebulaCloudLoader.class);

    private final String name;
    private final String hostsUrl;
    private final String vmsUrl;
    private List<CloudPhysicalHost> physicalHosts;
    private List<CloudVirtualHost> virtualHosts;
    private Map<String, List<CloudVirtualHost>> physicalHostToVMsMap;
    private Map<String, CloudPhysicalHost> physFqdnToPhysicalHostMap;
    private Map<String, CloudPhysicalHost> vmFqdnToPhysicalHostMap;

    public NebulaCloudLoader(String name, String hostsUrl, String vmsUrl) {
        this.name = name;
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
    public Map<String, List<CloudVirtualHost>> getPhysicalHostToVMsMap() {
        return physicalHostToVMsMap;
    }

    @Override
    public Map<String, CloudPhysicalHost> getPhysFqdnToPhysicalHostMap() {
        return physFqdnToPhysicalHostMap;
    }

    @Override
    public Map<String, CloudPhysicalHost> getVmFqdnToPhysicalHostMap() {
        return vmFqdnToPhysicalHostMap;
    }

    @Override
    public void load() {
        log.debug("load()");
        loadPhysicalHosts();
        loadVirtualHosts();
        prepareVMMap();
        checkMissingHosts();
        preparePhysFqdnToPhysicalHostMap();
        prepareVmFqdnToPhysicalHostMap();
        log.debug("load() end");
    }

    private void prepareVmFqdnToPhysicalHostMap() {
        vmFqdnToPhysicalHostMap = new HashMap<>(virtualHosts.size() * 2);
        for (CloudPhysicalHost physicalHost : physicalHosts) {
            List<CloudVirtualHost> vms = physicalHostToVMsMap.get(physicalHost.getFqdn());
            int cpu_reserved_x100 = 0;
            if (vms != null) {
                for (CloudVirtualHost vm : vms) {
                    vmFqdnToPhysicalHostMap.put(vm.getFqdn(), physicalHost);
                    cpu_reserved_x100 += vm.getCpu_reserved_x100();
                }
            } else {
                physicalHostToVMsMap.put(physicalHost.getName(), Collections.emptyList());
            }
            physicalHost.setCpuReserved(cpu_reserved_x100/100);
        }
    }

    private void preparePhysFqdnToPhysicalHostMap() {
        Map<String, CloudPhysicalHost> map = new HashMap<>(physicalHosts.size() * 2);
        physicalHosts.forEach(host -> map.put(host.getFqdn(), host));
        physFqdnToPhysicalHostMap = map;
    }

    private void checkMissingHosts() {
        log.debug("checkMissingHosts()");
        Set<String> hostNamesSet = new HashSet<>(physicalHosts.size() * 2);
        hostNamesSet.addAll(physicalHosts.stream().map(CloudPhysicalHost::getName).collect(Collectors.toList()));
        List<CloudPhysicalHost> fakeHosts = new ArrayList<>();
        for (String hostName : physicalHostToVMsMap.keySet()) {
            if (!hostNamesSet.contains(hostName)) {
                List<CloudVirtualHost> cloudVirtualHosts = physicalHostToVMsMap.get(hostName);
                List<String> vmIds = new ArrayList<>(cloudVirtualHosts.size());
                int cpu_reserved_x100 = 0;
                for (CloudVirtualHost vm : cloudVirtualHosts) {
                    vmIds.add(vm.getIdString());
                    cpu_reserved_x100 += vm.getCpu_reserved_x100();
                }
                log.warn("physical host {} not existing but has VMs {}", hostName, vmIds);
                CloudPhysicalHost fake = new CloudPhysicalHost();
                fake.setFqdn(hostName);
                fake.setState("FAKE");
                fake.getParsedName();
                fake.setCpuReserved(cpu_reserved_x100/100);
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


    private void prepareVMMap() {    // from vm.physicalHost=host.name -> vms
        log.debug("prepareVMMap()");
        this.physicalHostToVMsMap = new HashMap<>(virtualHosts.size() * 2);
        Function<String, List<CloudVirtualHost>> newListLambda = k -> new ArrayList<>(1);
        for (CloudVirtualHost vm : virtualHosts) {
            physicalHostToVMsMap.computeIfAbsent(vm.getPhysicalHost(), newListLambda).add(vm);
        }
    }

    private void loadVirtualHosts() {
        log.debug("loadVirtualHosts({})",vmsUrl);
        RestTemplate rt = new RestTemplate();
        this.virtualHosts = new ArrayList<>();
        //noinspection ConstantConditions
        for(JsonNode v : rt.getForObject(vmsUrl, JsonNode.class).path("vms")) {
            CloudVirtualHost cvh = new CloudVirtualHost();
            cvh.setId(v.path("id").asInt());
            cvh.setName(v.path("name").asText());
            cvh.setFqdn(v.path("fqdn").asText());
            cvh.setCpu_reserved_x100(v.path("cpu_reserved_x100").asInt());
            cvh.setStartTime(new Date(v.path("start_time").asLong()*1000L));
            cvh.setOwner(v.path("owner").path("name").asText());
            cvh.setPhysicalHost(v.path("current_host").asText());
            cvh.setPbsNode("pbs_mom".equals(v.path("role").asText()));
            cvh.setIdString(this.name+" "+cvh.getId());
            String state = v.path("state").asText();
            if("ACTIVE".equals(state)||"POWEROFF".equals(state)||"SUSPENDED".equals(state)) {
                cvh.setState(state);
                this.virtualHosts.add(cvh);
            }
        }
    }

    private void loadPhysicalHosts() {
        log.debug("loadPhysicalHosts({})",hostsUrl);
        //read physical hosts
        try {
            this.physicalHosts = new ArrayList<>();
            RestTemplate rt = new RestTemplate();
            //noinspection ConstantConditions
            for(JsonNode h : rt.getForObject(hostsUrl, JsonNode.class).path("hosts")) {
                CloudPhysicalHost cph = new CloudPhysicalHost();
                cph.setId(h.path("id").asInt());
                cph.setFqdn(h.path("hostname").asText());
                cph.setState(h.path("state").asText());
                cph.setCpuAvail(h.path("cpu_avail_x100").asInt()/100);
                this.physicalHosts.add(cph);
            }
        } catch (Exception ex) {
            log.error("cannot load physical hosts from "+hostsUrl,ex);
        }
    }
}
