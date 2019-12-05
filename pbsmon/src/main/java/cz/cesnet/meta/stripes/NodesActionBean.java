package cz.cesnet.meta.stripes;

import cz.cesnet.meta.cloud.Cloud;
import cz.cesnet.meta.cloud.CloudPhysicalHost;
import cz.cesnet.meta.cloud.CloudVM;
import cz.cesnet.meta.pbs.Node;
import cz.cesnet.meta.pbscache.Mapping;
import cz.cesnet.meta.pbscache.PbsCache;
import cz.cesnet.meta.pbsmon.PbsmonUtils;
import cz.cesnet.meta.pbsmon.RozhodovacStavuStroju;
import cz.cesnet.meta.perun.api.*;
import cz.cesnet.meta.storages.Storages;
import cz.cesnet.meta.storages.StoragesInfo;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UrlBinding("/nodes/{$event}")
public class NodesActionBean extends BaseActionBean {

    final static Logger log = LoggerFactory.getLogger(NodesActionBean.class);

    @SpringBean("perun")
    protected Perun perun;

    @SpringBean("diskArrays")
    protected Storages diskArraysLoader;

    @SpringBean("hsms")
    protected Storages hsmsLoader;

    StoragesInfo storagesInfo;
    StoragesInfo hsmInfo;
    List<VypocetniCentrum> centra;
    List<Stroj> zbyle;
    Map<String, Integer> cpuMap;
    int jobsQueuedCount;
    int maxVirtual = 0;
    List<Stroj> fyzicke;
    Mapping mapping;
    Map<String, Node> nodeMap;
    Map<String, CloudVM> fqdn2CloudVMMap;
    List<Node> gpuNodes;

    /**
     * Shows physical machines grouped by physical clusters and their owners.
     *
     * @return page /nodes/nodes.jsp
     */
    @SuppressWarnings("unused")
    @DefaultHandler
    public Resolution physical() {
        log.debug("physical({})", ctx.getRequest().getRemoteHost());
        FyzickeStroje fyzickeStroje = perun.getFyzickeStroje();
        centra = fyzickeStroje.getCentra();
        zbyle = fyzickeStroje.getZbyle();
        cpuMap = fyzickeStroje.getCpuMap();
        RozhodovacStavuStroju.rozhodniStavy(pbsky, perun.getVyhledavacFrontendu(),
                pbsCache.getMapping(), perun.getVyhledavacVyhrazenychStroju(), centra, cloud);
        jobsQueuedCount = pbsky.getJobsQueuedCount();
        //diskova pole
        storagesInfo = diskArraysLoader.getStoragesInfo();
        //HSMs
        hsmInfo = hsmsLoader.getStoragesInfo();
        //GPU
        gpuNodes = findGpuNodes();

        return new ForwardResolution("/nodes/nodes.jsp");
    }

    /**
     * Shows PBS nodes grouped by physical clusters.
     * @return page /nodes/pbs.jsp
     */
    public Resolution pbs() {
        log.debug("pbs({})", ctx.getRequest().getRemoteHost());
        FyzickeStroje fyzickeStroje = perun.getFyzickeStroje();
        centra = fyzickeStroje.getCentra();
        cpuMap = fyzickeStroje.getCpuMap();
        for (Stroj stroj : perun.getMetacentroveStroje()) {
            List<Node> nodes = PbsmonUtils.getPbsNodesForPhysicalMachine(stroj, pbsky, pbsCache, cloud);
            if(nodes.size()>0) {
                Node node = nodes.get(0);
                stroj.setPbsName(node.getName());
                stroj.setPbsState(node.getState());
            } else {
                stroj.setPbsName(null);
                stroj.setPbsState(null);
            }
        }
        jobsQueuedCount = pbsky.getJobsQueuedCount();
        //GPU
        gpuNodes = findGpuNodes();
        return new ForwardResolution("/nodes/pbs.jsp");
    }

    public List<Stroj> getMachinesSortedByPbsNodeNames(VypocetniZdroj zdroj) {
        List<Stroj> strojeByPbsNodeNames = new ArrayList<>(zdroj.getStroje());
        strojeByPbsNodeNames.sort((stroj1, stroj2) -> {
            Node node1 = pbsky.getNodeByName(stroj1.getPbsName());
            Node node2 = pbsky.getNodeByName(stroj2.getPbsName());
            if(node1==null) {
                return node2 == null ? 0 : 1;
            } else {
                return node2 == null ? -1 : node1.getNumInCluster() - node2.getNumInCluster();
            }
        });
        return strojeByPbsNodeNames;
    }

    private  List<Node> findGpuNodes() {
        return centra.stream()
                .flatMap(centrum -> centrum.getZdroje().stream())
                .flatMap(zdroj -> zdroj.isCluster()?zdroj.getStroje().stream():Stream.of(zdroj.getStroj()))
                .flatMap(stroj -> PbsmonUtils.getPbsNodesForPhysicalMachine(stroj,pbsky, pbsCache, cloud).stream())
                .filter(Node::getHasGPU)
                .collect(Collectors.toList());
    }

    public StoragesInfo getStoragesInfo() {
        return storagesInfo;
    }

    public StoragesInfo getHsmInfo() {
        return hsmInfo;
    }

    public Map<String, Integer> getCpuMap() {
        return cpuMap;
    }

    public List<VypocetniCentrum> getCentra() {
        return centra;
    }

    public List<Stroj> getZbyle() {
        return zbyle;
    }

    public int getJobsQueuedCount() {
        return jobsQueuedCount;
    }

    public List<Node> getGpuNodes() {
        return gpuNodes;
    }

    /**
     * Shows physical nodes with all mapped virtual machines and p
     * @return page /nodes/mapping.jsp
     */
    @SuppressWarnings("unused")
    public Resolution virtual() {
        log.debug("virtual()");
        fyzicke = this.perun.getMetacentroveStroje();
        //mapa z hostname PBS uzlu na PBS uzel
        nodeMap = new HashMap<>(fyzicke.size());
        //mapovani z jmen virtualnich stroju na jmena fyzickych stroju a naopak
        mapping = makeUnifiedMapping(this.pbsCache, this.cloud);
        //mapa VM z cloudu
        List<CloudVM> virtualHosts = cloud.getVirtualHosts();
        fqdn2CloudVMMap = new HashMap<>(virtualHosts.size() * 2);
        for (CloudVM vm : virtualHosts) {
            fqdn2CloudVMMap.put(vm.getFqdn(), vm);
        }
        //pripravit
        for (Stroj s : fyzicke) {
            String strojName = s.getName();
            //PBs uzel primo na fyzickem - nevirtualizovane
            Node pbsNode = pbsky.getNodeByFQDN(strojName);
            if (pbsNode != null) nodeMap.put(strojName, pbsNode);
            // pro vsechny fyzicke vytahat virtualni s PBS uzly
            List<String> virtNames = mapping.getPhysical2virtual().get(s.getName());
            if (virtNames != null) {
                for (String virtName : virtNames) {
                    Node vn = pbsky.getNodeByFQDN(virtName);
                    if (vn != null) {
                        nodeMap.put(vn.getName(), vn);
                    }
                }
            }
        }

        maxVirtual = 0;
        for (List<String> list : mapping.getPhysical2virtual().values()) {
            int size = list.size();
            if (size > maxVirtual) maxVirtual = size;
        }

        //rozhodni stav podle virtualnich
        for (Stroj stroj : fyzicke) {
            RozhodovacStavuStroju.rozhodniStav(stroj, pbsky, pbsCache.getMapping(), perun.getVyhledavacFrontendu(), perun.getVyhledavacVyhrazenychStroju(), cloud);
        }

        return new ForwardResolution("/nodes/mapping.jsp");
    }

    public static Mapping makeUnifiedMapping(PbsCache pbsCache, Cloud cloud) {
        Mapping m = new Mapping();
        Map<String, List<String>> physical2virtual = new HashMap<>();
        Map<String, String> virtual2physical = new HashMap<>();
        m.setPhysical2virtual(physical2virtual);
        m.setVirtual2physical(virtual2physical);
        //Mapping of ungu and urga partitions
        Mapping unguMapping = pbsCache.getMapping();
        physical2virtual.putAll(unguMapping.getPhysical2virtual());
        virtual2physical.putAll(unguMapping.getVirtual2physical());
        //Cloud
        Map<String, List<CloudVM>> hostName2VirtualHostsMap = cloud.getPhysicalHostToVMsMap();
        for (CloudPhysicalHost host : cloud.getPhysicalHosts()) {
            List<String> vmFqdns = new ArrayList<>();
            for (CloudVM vm : hostName2VirtualHostsMap.get(host.getName())) {
                vmFqdns.add(vm.getFqdn());
                virtual2physical.put(vm.getFqdn(), host.getFqdn());
            }
            physical2virtual.put(host.getFqdn(), vmFqdns);
        }
        return m;
    }

    public List<Stroj> getFyzicke() {
        return fyzicke;
    }

    public Mapping getMapping() {
        return mapping;
    }

    public int getMaxVirtual() {
        return maxVirtual;
    }

//    public Date getPbsTimeLoaded() {
//        return pbsky.getTimeLoaded();
//    }

    public Map<String, Node> getNodeMap() {
        return nodeMap;
    }

    public Map<String, CloudVM> getFqdn2CloudVMMap() {
        return fqdn2CloudVMMap;
    }

}
