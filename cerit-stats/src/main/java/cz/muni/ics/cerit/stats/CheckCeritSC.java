package cz.muni.ics.cerit.stats;

import cz.cesnet.meta.cloud.CloudLoader;
import cz.cesnet.meta.cloud.CloudPhysicalHost;
import cz.cesnet.meta.cloud.CloudVirtualHost;
import cz.cesnet.meta.perun.api.Perun;
import cz.cesnet.meta.perun.api.Stroj;
import cz.cesnet.meta.perun.api.VypocetniCentrum;
import cz.cesnet.meta.perun.api.VypocetniZdroj;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class CheckCeritSC {

    final static Logger log = LoggerFactory.getLogger(CheckCeritSC.class);

    public static void main(String[] args) throws UnknownHostException {
        ApplicationContext springCtx = new ClassPathXmlApplicationContext("spring-context.xml");
        Pbs pbs = springCtx.getBean("pbs", Pbs.class);
        OpenNebula nebula = springCtx.getBean("nebula", OpenNebula.class);
        Perun perun = springCtx.getBean("perun", Perun.class);
        Accounting accounting = springCtx.getBean("acct", Accounting.class);

        Map<String, PbsNode> pbsNodesMap = pbs.getPbsNodesMap();
        CloudLoader cloud = nebula.getCloud();

        VypocetniCentrum cerit = getCeritSC(perun);
        List<ExpectedHost> expectedHosts = getExpectedHosts(cerit);

        checkPbsNodes(expectedHosts, pbsNodesMap);
        checkCloudHosts(expectedHosts, pbsNodesMap, cloud);
        checkUV(expectedHosts, pbsNodesMap,"ungu.cerit-sc.cz","ungu1.cerit-sc.cz");
        checkUV(expectedHosts, pbsNodesMap,"urga.cerit-sc.cz","urga1.cerit-sc.cz");

        accounting.storeHostsAvailability(expectedHosts);

    }

    private static void checkUV(List<ExpectedHost> expectedHosts, Map<String, PbsNode> pbsNodesMap,String host,String vm) {
        for (ExpectedHost expectedHost : expectedHosts) {
            if (expectedHost.getHostname().equals(host)) {
                PbsNode pbsNode = pbsNodesMap.get(vm);
                if (pbsNode != null) {
                    //je v PBS
                    boolean pbsNodeAvailable = pbsNode.isAvailable();
                    log.debug(expectedHost.getHostname() + " with PBS node " + vm + " available: " + pbsNodeAvailable);
                    expectedHost.setAvailable(pbsNodeAvailable);
                    expectedHost.setReason("in pbs " + pbsNode.getReason());
                    if (!pbsNodeAvailable) {
                        log.info("host {} has unavailable PBS {} node for reason '{}'", expectedHost.getHostname(), pbsNode.getName(), pbsNode.getReason());
                    }
                }
                break;
            }
        }
    }

    private static void checkCloudHosts(List<ExpectedHost> expectedHosts, Map<String, PbsNode> pbsNodesMap, CloudLoader cloud) {
        //projet OpenNebulu
        for (ExpectedHost expectedHost : expectedHosts) {
            if (expectedHost.isInPbs()) continue;
            String hostname = expectedHost.getHostname();
            CloudPhysicalHost host = cloud.getHostname2HostMap().get(hostname);
            if (host == null) {
                log.info("{} not found in OpenNebula hosts ", hostname);
                expectedHost.setAvailable(false);
                expectedHost.setReason("not in OpenNebula");
            } else {
                List<CloudVirtualHost> vms = cloud.getHostName2VirtualHostsMap().get(host.getName());
                String firstVMName = (vms != null && vms.size() > 0) ? vms.get(0).getFqdn() : "";
                PbsNode pbsNode = pbsNodesMap.get(firstVMName);
                if (pbsNode != null) {
                    //je v PBS
                    boolean pbsNodeAvailable = pbsNode.isAvailable();
                    log.debug(hostname + " with PBS node " + firstVMName + " available: " + pbsNodeAvailable);
                    expectedHost.setAvailable(pbsNodeAvailable);
                    expectedHost.setReason("in pbs " + pbsNode.getReason());
                    if (!pbsNodeAvailable) {
                        log.info("host {} has unavailable PBS {} node for reason '{}'", hostname, pbsNode.getName(), pbsNode.getReason());
                    }
                } else {
                    //neni v PBS
                    //http://docs.opennebula.org/4.14/administration/hosts_and_clusters/host_guide.html#host-life-cycle
                    String state = host.getState();
                    if ("MONITORING_MONITORED".equals(state)) {
                        state = "MONITORED";
                    }
                    boolean available = "MONITORED".equals(state);
                    log.debug("{} in cloud available: {}", hostname, available);
                    expectedHost.setAvailable(available);
                    expectedHost.setReason("in state " + state);
                    if (!available) {
                        log.info("host {} is unavailable because its state is {}", hostname, state);
                    }
                }
            }
        }
    }

    private static void checkPbsNodes(List<ExpectedHost> expectedHosts, Map<String, PbsNode> pbsNodesMap) {
        //projet Torque uzly
        for (ExpectedHost expectedHost : expectedHosts) {
            PbsNode pbsNode = pbsNodesMap.get(expectedHost.getHostname());
            if (pbsNode != null) {
                expectedHost.setInPbs(true);
                boolean pbsNodeAvailable = pbsNode.isAvailable();
                if (log.isDebugEnabled()) {
                    log.debug(expectedHost.getHostname() + ": " + pbsNode.getState() + " " + pbsNode.getQueue() + " available:" + pbsNodeAvailable);
                }
                expectedHost.setAvailable(pbsNodeAvailable);
                expectedHost.setReason("in pbs " + pbsNode.getReason());
                if (!pbsNodeAvailable) {
                    log.info("host {} is unavailable PBS node for reason '{}'", expectedHost.getHostname(), pbsNode.getReason());
                }
            }
        }
    }

    static VypocetniCentrum getCeritSC(Perun perun) {
        List<VypocetniCentrum> centra = perun.getFyzickeStroje().getCentra();
        VypocetniCentrum cerit = null;
        for (VypocetniCentrum centrum : centra) {
            if (centrum.getId().equals("CERIT-SC")) {
                cerit = centrum;
                break;
            }
        }
        if (cerit == null) {
            log.error("CERIT-SC not found");
            System.exit(1);
        }
        return cerit;
    }

    private static List<ExpectedHost> getExpectedHosts(VypocetniCentrum cerit) {
        List<ExpectedHost> expectedHosts = new ArrayList<ExpectedHost>(220);
        for (VypocetniZdroj resource : cerit.getZdroje()) {
            log.info("resource.getId() = {}", resource.getId());
            List<Stroj> stroje = resource.isCluster() ? resource.getStroje() : Arrays.asList(resource.getStroj());
            for (Stroj stroj : stroje) {
                log.debug("stroj = {}", stroj.getName());
                expectedHosts.add(new ExpectedHost(stroj.getName()));
            }
        }
        return expectedHosts;
    }

}
