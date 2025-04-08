package cz.cesnet.meta.stripes;

import cz.cesnet.meta.cloud.Cloud;
import cz.cesnet.meta.cloud.CloudPhysicalHost;
import cz.cesnet.meta.cloud.CloudVM;
import cz.cesnet.meta.pbs.Pbsky;
import cz.cesnet.meta.pbsmon.MachineStateDecider;
import cz.cesnet.meta.perun.api.*;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@UrlBinding("/cloud")
public class CloudActionBean extends BaseActionBean {

    final static Logger log = LoggerFactory.getLogger(CloudActionBean.class);

    @SpringBean("cloud")
    protected Cloud cloud;

    @SpringBean("pbsky")
    protected Pbsky pbsky;

    @SpringBean("perun")
    protected Perun perun;

    List<CloudPhysicalHost> physicalHosts;
    Map<String, List<CloudVM>> vms;
    List<OwnerOrganisation> centra;
    Map<String, Integer> cpuMap;
    Map<String, Boolean> inCloudMap;


    @DefaultHandler
    public Resolution show() {


        //cloud
        this.physicalHosts = cloud.getPhysicalHosts();
        this.vms = cloud.getPhysicalHostToVMsMap();
        //assign PBSnodes
        for (List<CloudVM> hostVMs : vms.values()) {
            for (CloudVM vm : hostVMs) {
                vm.setNode(pbsky.getNodeByFQDN(vm.getName()));
            }
        }

        //hw
        PhysicalMachines physicalMachines = perun.getPhysicalMachines();
        centra = physicalMachines.getOwnerOrganisations();
        cpuMap = new HashMap<>();
        MachineStateDecider.decideStates(pbsky, perun.getFrontendFinder(),
                pbsCache.getMapping(), perun.getReservedMachinesFinder(), centra, cloud);
        inCloudMap = new HashMap<>();
        //zjistit, zda jsou v cloudu
        Set<String> cloudPhysicalHostnames = new HashSet<>();
        for (CloudPhysicalHost cloudPhysicalHost : physicalHosts) {
            cloudPhysicalHostnames.add(cloudPhysicalHost.getFqdn());
        }
        int totalCPU = 0;
        for (OwnerOrganisation centrum : centra) {
            for (PerunComputingResource perunComputingResource : centrum.getPerunComputingResources()) {
                int resourceCPUs = 0;
                boolean anyInCloud = false;
                for (PerunMachine perunMachine : perunComputingResource.getPerunMachines()) {
                    String name = perunMachine.getName();
                    boolean inCloud = cloudPhysicalHostnames.contains(name);
                    anyInCloud |= inCloud;
                    inCloudMap.put(name, inCloud);
                    if (inCloud) {
                        resourceCPUs += perunMachine.getCpuNum();
                        cpuMap.put(perunMachine.getName(), perunMachine.getCpuNum());
                    }
                }
                inCloudMap.put(perunComputingResource.getId(), anyInCloud);
                cpuMap.put(perunComputingResource.getId(), resourceCPUs);
                totalCPU += resourceCPUs;
            }
        }
        cpuMap.put("all",totalCPU);

        return new ForwardResolution("/cloud/cloud.jsp");
    }

    public List<CloudPhysicalHost> getPhysicalHosts() {
        return physicalHosts;
    }

    public Map<String, List<CloudVM>> getVms() {
        return vms;
    }

    public List<OwnerOrganisation> getCentra() {
        return centra;
    }

    public Map<String, Integer> getCpuMap() {
        return cpuMap;
    }

    public Map<String, Boolean> getInCloudMap() {
        return inCloudMap;
    }
}
