package cz.cesnet.meta.pbsmon;

import cz.cesnet.meta.cloud.Cloud;
import cz.cesnet.meta.cloud.CloudPhysicalHost;
import cz.cesnet.meta.cloud.CloudVirtualHost;
import cz.cesnet.meta.pbs.Node;
import cz.cesnet.meta.pbs.Pbsky;
import cz.cesnet.meta.pbscache.Mapping;
import cz.cesnet.meta.perun.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Rozhoduje o barve v prehledu fyzickych stroju podle stavu virtualnich stroju.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: RozhodovacStavuStroju.java,v 1.8 2016/03/04 10:35:55 makub Exp $
 */
public class RozhodovacStavuStroju {

    final static Logger log = LoggerFactory.getLogger(RozhodovacStavuStroju.class);

    public static void rozhodniStavy(Pbsky pbsky, VyhledavacFrontendu frontendy, Mapping mapping, VyhledavacVyhrazenychStroju vyhrazene, List<VypocetniCentrum> centra, Cloud cloud) {
        log.debug("rozhodniStavy() frontendy={}", frontendy);
        for (VypocetniCentrum c : centra) {
            for (VypocetniZdroj z : c.getZdroje()) {
                if (z.isCluster()) {
                    for (Stroj stroj : z.getStroje()) {
                        rozhodniStav(stroj, pbsky, mapping, frontendy, vyhrazene, cloud);
                    }
                    for (VypocetniZdroj pz : z.getPodclustery()) {
                        for (Stroj stroj : pz.getStroje()) {
                            rozhodniStav(stroj, pbsky, mapping, frontendy, vyhrazene, cloud);
                        }
                    }
                } else {
                    Stroj stroj = z.getStroj();
                    log.debug("rozhodniStavy() stroj={}", stroj);
                    rozhodniStav(stroj, pbsky, mapping, frontendy, vyhrazene, cloud);
                }
            }
        }
    }

    public static void rozhodniStav(Stroj stroj, Pbsky pbsky, Mapping mapping, VyhledavacFrontendu frontendy, VyhledavacVyhrazenychStroju vyhrazene, Cloud cloud) {
        String jmenoStroje = stroj.getName();
        log.debug("rozhodniStav({})", jmenoStroje);
        if (vyhrazene.jeStrojVyhrazeny(stroj)) {
            log.debug(" fyzicky {} je vyhrazeny", jmenoStroje);
            stroj.setState(Node.STATE_JOB_BUSY);
            return;
        }

        if (frontendy.jeStrojFrontend(jmenoStroje)) {
            log.debug(" fyzicky {} je frontend", jmenoStroje);
            stroj.setState(Node.STATE_JOB_BUSY);
            return;
        }

        Node physNode = pbsky.getNodeByFQDN(jmenoStroje);
        //nevirtualizovane stroje v PBS pouziji svuj stav
        if (physNode != null && !physNode.getState().equals(Node.STATE_CLOUD)) {
            String state = physNode.getState();
            switch (state) {
                case Node.STATE_JOB_BUSY:
                case Node.STATE_JOB_EXCLUSIVE:
                case Node.STATE_JOB_SHARING:
                case Node.STATE_RESERVED:
                case Node.STATE_JOB_FULL:
                    stroj.setState(Node.STATE_JOB_BUSY);
                    break;
                case Node.STATE_FREE:
                    stroj.setState(Node.STATE_FREE);
                    break;
                case Node.STATE_PARTIALY_FREE:
                    stroj.setState(Node.STATE_PARTIALY_FREE);
                    stroj.setUsedPercent(physNode.getUsedPercent());
                    break;
                default:
                    stroj.setState(Node.STATE_UNKNOWN);
                    break;
            }
            log.debug(" fyzicky {} v PBS, stav={}", jmenoStroje, stroj.getState());
        } else {
            //ostatni vezmeme podle stavu virtualnich stroju
            String stav;
            List<String> virtNames = mapping.getPhysical2virtual().get(jmenoStroje);
            if (virtNames != null) {
                //v pbsCache jsou udaje o virtualich strojich (urga)
                List<Node> virtNodes = new ArrayList<>(virtNames.size());
                for (String virtName : virtNames) {
                    if (frontendy.jeStrojFrontend(virtName)) {
                        log.debug("fyzicky {} ma virtualni frontend {}", jmenoStroje, virtName);
                        stroj.setState(Node.STATE_JOB_BUSY);
                        return;
                    }
                    Node node = pbsky.getNodeByFQDN(virtName);
                    if (node != null) virtNodes.add(node);
                }
                stav = rozhodniStavFyzickehoPodleVirtualnich(stroj, virtNodes);
            } else {
                stav = rozhodniCloudovyStroj(stroj, pbsky, cloud);
            }
            stroj.setState(stav);
            log.debug(" fyzicky {} s vice VM, stav={}", jmenoStroje, stav);
        }
    }

    private static String rozhodniCloudovyStroj(Stroj stroj, Pbsky pbsky, Cloud cloud) {
        log.debug("rozhodniCloudovyStroj({})", stroj.getName());
        CloudPhysicalHost cloudPhysicalHost = cloud.getPhysFqdnToPhysicalHostMap().get(stroj.getName());
        if (cloudPhysicalHost == null) {
            return Node.STATE_UNKNOWN;
        }
        stroj.setCloudManaged(true);
        //http://docs.opennebula.org/4.14/administration/hosts_and_clusters/host_guide.html#host-life-cycle
        if (!cloudPhysicalHost.getState().endsWith("MONITORED")) {
            return Node.STATE_UNKNOWN;
        }
        List<CloudVirtualHost> cloudVMs = cloud.getPhysicalHostToVMsMap().get(cloudPhysicalHost.getFqdn());
        if (cloudVMs != null) {
            int cpusAvailable = stroj.getCpuNum();
            //v cloudu jsou udaje o virtualnich strojich
            for (CloudVirtualHost cloudVM : cloudVMs) {
                //podle značky z cloudu je to PBS node
                if (cloudVM.isPbsNode()) {
                    stroj.setCloudPbsHost(true);
                    Node node = pbsky.getNodeByFQDN(cloudVM.getFqdn());
                    if (node != null) {
                        //PBSka ho zná
                        stroj.setPbsName(node.getShortName());
                        if (node.getNoOfCPUInt() >= cpusAvailable) {
                            //fyzický stroj plně obsazený PBSnodem
                            stroj.setCloudUsable(false);
                            return rozhodniStavFyzickehoPodleVirtualnich(stroj, Collections.singletonList(node));
                        }
                    } else {
                        //nekonzistence, v OpenNebule je označen jako PBSnode, ale PBSka ho nezná
                        stroj.setPbsName(stroj.getShortName());
                        if (cloudVM.getCpu_reserved_x100() >= cpusAvailable * 100) {
                            //VM plně zabírá stroj
                            stroj.setCloudUsable(false);
                            return Node.STATE_JOB_BUSY;
                        }
                    }
                }
            }
            stroj.setCloudUsable(true);
            if (cloudPhysicalHost.getCpuReserved() >= cpusAvailable) {
                log.debug("cpuReserved>=cpuNum, setting job-busy");
                return Node.STATE_JOB_BUSY;
            } else if (cloudPhysicalHost.getCpuReserved() == 0) {
                log.debug("cpuReserved=0, setting FREE");
                return Node.STATE_FREE;
            } else {
                stroj.setUsedPercent(cloudPhysicalHost.getCpuReserved() * 100 / stroj.getCpuNum());
                log.debug("setting PARTIALLY_FREE with {}% used ", stroj.getUsedPercent());
                return Node.STATE_PARTIALY_FREE;
            }
        } else {
            //fyzicky je v cloudu, nema zadne VM
            log.debug("cloudHost without VMs {}", cloudPhysicalHost);
            return Node.STATE_FREE;
        }
    }

    /**
     * Magie rozhodovani stavu fyzickeho stroje podle virtualnich.
     *
     * @param stroj     stroj
     * @param virtNodes VM na danem fyzickem stroji
     * @return stav jako Node.STATE_*
     */
    private static String rozhodniStavFyzickehoPodleVirtualnich(Stroj stroj, List<Node> virtNodes) {
        String jmenoStroje = stroj.getName();
        //bez VM je stav neznamy
        if (virtNodes.isEmpty()) {
            log.debug(" fyzicky {} zadne VM", jmenoStroje);
            return Node.STATE_UNKNOWN;
        }
        //pokud aspon jeden z VM pracuje, pak fyzicky pracuje
        for (Node node : virtNodes) {
            String state = node.getState();
            if (state.equals(Node.STATE_JOB_BUSY) || state.equals(Node.STATE_JOB_EXCLUSIVE) || state.equals(Node.STATE_JOB_FULL)
                    || state.equals(Node.STATE_JOB_SHARING) || state.equals(Node.STATE_RESERVED)
            ) {
                log.debug(" fyzicky {} ma pracujici VM {} ", jmenoStroje, node.getName());
                return Node.STATE_JOB_BUSY;
            }
        }
        //pokud je nejaky  z VM free, pak fyzicky je free
        for (Node node : virtNodes) {
            String state = node.getState();
            if (state.equals(Node.STATE_FREE)) {
                log.debug(" fyzicky {} ma free VM {} ", jmenoStroje, node.getName());
                return Node.STATE_FREE;
            }
        }
        //pokud je nejaky z VM castecne free, pak fyzicky je free
        for (Node node : virtNodes) {
            String state = node.getState();
            if (state.equals(Node.STATE_PARTIALY_FREE)) {
                log.debug(" fyzicky {} ma partially free VM {} ", jmenoStroje, node.getName());
                stroj.setUsedPercent(node.getUsedPercent());
                return Node.STATE_PARTIALY_FREE;
            }
        }
        //nevime :-(
        log.debug(" fyzicky {} nevime stav", jmenoStroje);
        return Node.STATE_UNKNOWN;
    }
}
