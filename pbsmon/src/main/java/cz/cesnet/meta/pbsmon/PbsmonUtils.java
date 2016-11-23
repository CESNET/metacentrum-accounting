package cz.cesnet.meta.pbsmon;

import cz.cesnet.meta.cloud.Cloud;
import cz.cesnet.meta.cloud.CloudPhysicalHost;
import cz.cesnet.meta.cloud.CloudVirtualHost;
import cz.cesnet.meta.pbs.Node;
import cz.cesnet.meta.pbs.Pbsky;
import cz.cesnet.meta.pbscache.PbsCache;
import cz.cesnet.meta.perun.api.Stroj;

import java.util.ArrayList;
import java.util.List;

/**
 * Integrační záležitosti.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class PbsmonUtils {

    /**
     * Získá seznam PBS uzlů pro fyzický stroj.
     * @param perunMachine - informace z Peruna
     * @param pbsky - informace z Torque
     * @param pbsCache - informace z pbs_cache
     * @param cloud - informace z OpenNebula
     * @return seznam PBs uzlů na fyzickém stroji
     */
    public static List<Node> getPbsNodesForPhysicalMachine(Stroj perunMachine, Pbsky pbsky, PbsCache pbsCache, Cloud cloud) {
        String machineName = perunMachine.getName();
        List<Node> pbsNodes = new ArrayList<>();
        //PBS uzel odpovídající jménu fyzického stroje (bez virtualizace nebo dom0)
        Node node = pbsky.getNodeByFQDN(machineName);
        if (node != null && node.isComputingNode()) {
            pbsNodes.add(node);
        }
        //PBS uzly podle pbs_cache (virtualizace přes Magratheu)
        List<String> virtNames = pbsCache.getMapping().getPhysical2virtual().get(machineName);
        if (virtNames != null) {
            for (String virtName : virtNames) {
                Node pbsNode = pbsky.getNodeByFQDN(virtName);
                if (pbsNode != null) {
                    pbsNodes.add(pbsNode);
                }
            }
        }
        //PBS uzly podle OpenNebuly
        CloudPhysicalHost cloudPhysicalHost;
        List<CloudVirtualHost> cloudVirtualHosts;
        if ((cloudPhysicalHost = cloud.getHostname2HostMap().get(machineName)) != null) {
            //do cloudVirtualHosts seznam virtuálů v OpenNebule
            if ((cloudVirtualHosts = cloud.getHostName2VirtualHostsMap().get(cloudPhysicalHost.getName())) != null) {
                for (CloudVirtualHost cloudVirtualHost : cloudVirtualHosts) {
                    Node pbsNode = pbsky.getNodeByFQDN(cloudVirtualHost.getFqdn());
                    if (pbsNode != null) {
                        //virtuál obsahuje PBS Node
                        pbsNodes.add(pbsNode);
                    }
                }
            }
        }
        return pbsNodes;
    }
}
