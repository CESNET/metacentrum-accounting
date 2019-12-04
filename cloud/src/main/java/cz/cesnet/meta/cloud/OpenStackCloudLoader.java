package cz.cesnet.meta.cloud;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OpenStackCloudLoader implements CloudLoader {

    private static final Logger log = LoggerFactory.getLogger(OpenStackCloudLoader.class);

    private final String name;
    private final String directory;
    private List<CloudPhysicalHost> physicalHosts;
    private List<CloudVM> virtualHosts;

    public OpenStackCloudLoader(String name, String directory) {
        this.name = name;
        this.directory = directory;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<CloudPhysicalHost> getPhysicalHosts() {
        return physicalHosts;
    }

    @Override
    public List<CloudVM> getVirtualHosts() {
        return virtualHosts;
    }

    @Override
    public void load() {
        log.debug("load()");
        File[] jsonFiles = new File(directory).listFiles((dir1, name1) -> name1.endsWith(".json"));
        if (log.isDebugEnabled()) {
            log.debug("found files {}", Arrays.asList(jsonFiles));
        }
        List<CloudPhysicalHost> physicalHosts = new ArrayList<>();
        List<CloudVM> virtualHosts = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        for (File jsonFile : jsonFiles) {
            try {
                for (JsonNode pnode : objectMapper.readValue(jsonFile, JsonNode.class)) {
                    CloudPhysicalHost cph = new CloudPhysicalHost();
                    String hypervisor = pnode.path("Hypervisor").asText();
                    cph.setFqdn(hypervisor);
                    cph.setId(this.getName() + "-H-" + hypervisor);
                    cph.setCpuAvail(pnode.path("CPUs").asInt());
                    cph.setState("MONITORED");
                    physicalHosts.add(cph);
                    for (JsonNode vnode : pnode.path("VMs")) {
                        CloudVM vm = new CloudVM();
                        virtualHosts.add(vm);
                        String vmname = vnode.path("name").asText();
                        vm.setId(this.getName() + "-VM-" + vmname);
                        vm.setPhysicalHostFqdn(hypervisor);
                        vm.setFqdn(vmname);//WARNING - a VM may not have a FQDN in OpenStack, but so far the name is always its FQDN
                        vm.setName(vmname);
                        vm.setState(vnode.path("instance_state").asText());
                        vm.setCpu_reserved_x100(vnode.path("CPUs").asInt() * 100);
                        vm.setOwner(vnode.path("user_id").asText());
                        try {
                            vm.setStartTime(StdDateFormat.instance.parse(vnode.path("created").asText()));
                        } catch (ParseException e) {
                            log.error("cannot parse time", e);
                        }
                    }
                }
            } catch (IOException e) {
                log.error("cannot parse " + jsonFile, e);
            }
        }
        this.physicalHosts = physicalHosts;
        this.virtualHosts = virtualHosts;
        log.debug("load() end");
    }

}
