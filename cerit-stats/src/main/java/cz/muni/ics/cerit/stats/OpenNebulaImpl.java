package cz.muni.ics.cerit.stats;

import cz.cesnet.meta.cloud.CloudLoader;
import cz.cesnet.meta.cloud.opennebula.NebulaCloudLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class OpenNebulaImpl implements OpenNebula {

    final static Logger log = LoggerFactory.getLogger(OpenNebulaImpl.class);

    String physicalHostsURL = "http://carach1.ics.muni.cz:12147/exports/hosts.json";
    public void setPhysicalHostsURL(String physicalHostsURL) {
        this.physicalHostsURL = physicalHostsURL;
    }

    String vmsURL = "http://carach1.ics.muni.cz:12147/exports/vms.json";
    public void setVmsURL(String vmsURL) {
        this.vmsURL = vmsURL;
    }

    @Override
    public CloudLoader getCloud() {
        CloudLoader cloud = new NebulaCloudLoader(physicalHostsURL, vmsURL);
        cloud.load();
        return cloud;
    }

//    @Override
//    public Map<String, OpenNebulaHost> getHostsMap() {
//        Map<String,OpenNebulaHost> hostsMap = new HashMap<String, OpenNebulaHost>();
//        RestTemplate rt = new RestTemplate();
//        long start = System.currentTimeMillis();
//        JsonNode root = rt.getForObject(physicalHostsURL, JsonNode.class);
//        long stop = System.currentTimeMillis();
//        log.debug("loading HwMs ... {}ms",stop-start);
//
//        JsonNode hosts = root.path("hosts");
//        Set<String> states = new HashSet<String>(20);
//        Set<String> clusters = new HashSet<String>(20);
//        for(Iterator<JsonNode> hostsIterator = hosts.elements();hostsIterator.hasNext();) {
//            JsonNode host = hostsIterator.next();
//            String name = host.path("name").asText();
//            String hostname = host.path("hostname").asText();
//            String state = host.path("state").asText();
//            String cluster = host.path("cluster").asText();
//            OpenNebulaHost host1 = hostsMap.get(hostname);
//            if(host1!=null) {
//                //replace only if the previously found host was disabled
//                if("DISABLED".equals(host1.getState())) {
//                    hostsMap.put(hostname, new OpenNebulaHost(name,state,cluster,hostname));
//                }
//            } else {
//                hostsMap.put(hostname, new OpenNebulaHost(name,state,cluster,hostname));
//            }
//            states.add(state);
//            clusters.add(cluster);
//        }
//        if(log.isDebugEnabled()) {
//            log.debug("states: "+states);
//            log.debug("clusters: "+clusters);
//        }
//        return hostsMap;
//    }
//
//    @Override
//    public String getFirstVMName(OpenNebulaHost host) {
//        RestTemplate rt = new RestTemplate();
//        JsonNode root = rt.getForObject(vmURL, JsonNode.class, host.getName());
//        JsonNode vms = root.path("vms");
//        if(vms.isArray()&&vms.size()==1) {
//            return vms.get(0).path("name").asText();
//        } else {
//            return null;
//        }
//    }
//
//    @Override
//    public Map<String,String> getHW2VMMap() {
//        RestTemplate rt = new RestTemplate();
//        long start = System.currentTimeMillis();
//        JsonNode root = rt.getForObject(vmsURL, JsonNode.class);
//        long stop = System.currentTimeMillis();
//        log.debug("loading VMs ... {}ms",stop-start);
//        JsonNode vms = root.path("vms");
//        Map<String,String> map = new HashMap<String, String>();
//        for(Iterator<JsonNode> vmsIter = vms.elements();vmsIter.hasNext();) {
//            JsonNode vm = vmsIter.next();
//            if("ACTIVE".equals(vm.path("state").asText())) {
//                map.put(vm.path("current_host").asText(),vm.path("name").asText());
//            }
//        }
//        return map;
//    }
}
