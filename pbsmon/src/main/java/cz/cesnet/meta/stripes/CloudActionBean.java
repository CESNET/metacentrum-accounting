package cz.cesnet.meta.stripes;

import cz.cesnet.meta.cloud.Cloud;
import cz.cesnet.meta.cloud.CloudPhysicalHost;
import cz.cesnet.meta.cloud.CloudVirtualHost;
import cz.cesnet.meta.pbs.Pbsky;
import cz.cesnet.meta.pbsmon.RozhodovacStavuStroju;
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
    Map<String, List<CloudVirtualHost>> vms;
    List<VypocetniCentrum> centra;
    Map<String, Integer> cpuMap;
    Map<String, Boolean> inCloudMap;


    @DefaultHandler
    public Resolution show() {


        //cloud
        this.physicalHosts = cloud.getPhysicalHosts();
        this.vms = cloud.getHostName2VirtualHostsMap();
        //assign PBSnodes
        for (List<CloudVirtualHost> hostVMs : vms.values()) {
            for (CloudVirtualHost vm : hostVMs) {
                vm.setNode(pbsky.getNodeByFQDN(vm.getName()));
            }
        }

        //hw
        FyzickeStroje fyzickeStroje = perun.getFyzickeStroje();
        centra = fyzickeStroje.getCentra();
        cpuMap = new HashMap<>();
        RozhodovacStavuStroju.rozhodniStavy(pbsky, perun.getVyhledavacFrontendu(),
                pbsCache.getMapping(), perun.getVyhledavacVyhrazenychStroju(), centra, cloud);
        inCloudMap = new HashMap<>();
        //zjistit, zda jsou v cloudu
        Set<String> cloudPhysicalHostnames = new HashSet<>();
        for (CloudPhysicalHost cloudPhysicalHost : physicalHosts) {
            cloudPhysicalHostnames.add(cloudPhysicalHost.getHostname());
        }
        int celkemCPU = 0;
        for (VypocetniCentrum centrum : centra) {
            for (VypocetniZdroj zdroj : centrum.getZdroje()) {
                int zdrojCPU = 0;
                boolean anyInCloud = false;
                for (Stroj stroj : zdroj.getStroje()) {
                    String name = stroj.getName();
                    boolean inCloud = cloudPhysicalHostnames.contains(name);
                    anyInCloud |= inCloud;
                    inCloudMap.put(name, inCloud);
                    if (inCloud) {
                        zdrojCPU += stroj.getCpuNum();
                        cpuMap.put(stroj.getName(), stroj.getCpuNum());
                    }
                }
                inCloudMap.put(zdroj.getId(), anyInCloud);
                cpuMap.put(zdroj.getId(), zdrojCPU);
                celkemCPU += zdrojCPU;
            }
        }
        cpuMap.put("vsechny",celkemCPU);

        return new ForwardResolution("/cloud/cloud.jsp");
    }

    public List<CloudPhysicalHost> getPhysicalHosts() {
        return physicalHosts;
    }

    public Map<String, List<CloudVirtualHost>> getVms() {
        return vms;
    }

    public List<VypocetniCentrum> getCentra() {
        return centra;
    }

    public Map<String, Integer> getCpuMap() {
        return cpuMap;
    }

    public Map<String, Boolean> getInCloudMap() {
        return inCloudMap;
    }
}
