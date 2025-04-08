package cz.cesnet.meta.pbsmon;

import cz.cesnet.meta.cloud.Cloud;
import cz.cesnet.meta.cloud.CloudPhysicalHost;
import cz.cesnet.meta.cloud.CloudVM;
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
 */
public class MachineStateDecider {

    final static Logger log = LoggerFactory.getLogger(MachineStateDecider.class);

    public static void decideStates(Pbsky pbsky, FrontendFinder frontendy, Mapping mapping, ReservedMachinesFinder vyhrazene, List<OwnerOrganisation> centra, Cloud cloud) {
        log.debug("rozhodniStavy() frontendy={}", frontendy);
        for (OwnerOrganisation c : centra) {
            for (PerunComputingResource z : c.getPerunComputingResources()) {
                if (z.isCluster()) {
                    for (PerunMachine perunMachine : z.getPerunMachines()) {
                        decideState(perunMachine, pbsky, mapping, frontendy, vyhrazene, cloud);
                    }
                } else {
                    PerunMachine perunMachine = z.getPerunMachine();
                    log.debug("rozhodniStavy() perunMachine={}", perunMachine);
                    decideState(perunMachine, pbsky, mapping, frontendy, vyhrazene, cloud);
                }
            }
        }
    }

    public static void decideState(PerunMachine perunMachine, Pbsky pbsky, Mapping mapping, FrontendFinder frontendy, ReservedMachinesFinder reservedMachinesFinder, Cloud cloud) {
        String jmenoStroje = perunMachine.getName();
        log.debug("rozhodniStav({})", jmenoStroje);
        if (reservedMachinesFinder.isMachineReserved(perunMachine)) {
            perunMachine.setState(Node.STATE_JOB_BUSY);
            return;
        }

        if (frontendy.isFrontend(jmenoStroje)) {
            perunMachine.setState(Node.STATE_JOB_BUSY);
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
                    perunMachine.setState(Node.STATE_JOB_BUSY);
                    break;
                case Node.STATE_FREE:
                    perunMachine.setState(Node.STATE_FREE);
                    break;
                case Node.STATE_PARTIALY_FREE:
                    perunMachine.setState(Node.STATE_PARTIALY_FREE);
                    perunMachine.setUsedPercent(physNode.getUsedPercent());
                    break;
                default:
                    perunMachine.setState(Node.STATE_UNKNOWN);
                    break;
            }
            log.debug(" fyzicky {} v PBS, stav={}", jmenoStroje, perunMachine.getState());
        } else {
            //ostatni vezmeme podle stavu virtualnich stroju
            String stav;
            List<String> virtNames = mapping.getPhysical2virtual().get(jmenoStroje);
            if (virtNames != null) {
                //v pbsCache jsou udaje o virtualich strojich (urga)
                List<Node> virtNodes = new ArrayList<>(virtNames.size());
                for (String virtName : virtNames) {
                    if (frontendy.isFrontend(virtName)) {
                        log.debug("fyzicky {} ma virtualni frontend {}", jmenoStroje, virtName);
                        perunMachine.setState(Node.STATE_JOB_BUSY);
                        return;
                    }
                    Node node = pbsky.getNodeByFQDN(virtName);
                    if (node != null) virtNodes.add(node);
                }
                stav = rozhodniStavFyzickehoPodleVirtualnich(perunMachine, virtNodes);
            } else {
                stav = rozhodniCloudovyStroj(perunMachine, pbsky, cloud);
            }
            perunMachine.setState(stav);
            log.debug(" fyzicky {} s vice VM, stav={}", jmenoStroje, stav);
        }
    }

    private static String rozhodniCloudovyStroj(PerunMachine perunMachine, Pbsky pbsky, Cloud cloud) {
        log.debug("rozhodniCloudovyStroj({})", perunMachine.getName());
        CloudPhysicalHost cloudPhysicalHost = cloud.getPhysFqdnToPhysicalHostMap().get(perunMachine.getName());
        if (cloudPhysicalHost == null) {
            return Node.STATE_UNKNOWN;
        }
        perunMachine.setCloudManaged(true);
        //http://docs.opennebula.org/4.14/administration/hosts_and_clusters/host_guide.html#host-life-cycle
        if (!cloudPhysicalHost.getState().endsWith("MONITORED")) {
            return Node.STATE_UNKNOWN;
        }
        List<CloudVM> cloudVMs = cloud.getPhysicalHostToVMsMap().get(cloudPhysicalHost.getFqdn());
        if (cloudVMs != null) {
            int cpusAvailable = cloudPhysicalHost.getCpuAvail();
            //v cloudu jsou udaje o virtualnich strojich
            for (CloudVM cloudVM : cloudVMs) {
                //podle značky z cloudu je to PBS node
                Node node = pbsky.getNodeByFQDN(cloudVM.getFqdn());
                if (node != null) {
                    //PBSka ho zná
                    cloudVM.setNode(node);
                    perunMachine.setCloudPbsHost(true);
                    perunMachine.setPbsName(node.getShortName());
                    if (node.getNoOfCPUInt() >= cpusAvailable) {
                        //fyzický perunMachine plně obsazený PBSnodem
                        perunMachine.setCloudUsable(false);
                        return rozhodniStavFyzickehoPodleVirtualnich(perunMachine, Collections.singletonList(node));
                    }
                }
            }
            perunMachine.setCloudUsable(true);
            if (cloudPhysicalHost.getCpuReserved() >= cpusAvailable) {
                log.debug("cpuReserved>=cpuNum, setting job-busy");
                return Node.STATE_JOB_BUSY;
            } else if (cloudPhysicalHost.getCpuReserved() == 0) {
                log.debug("cpuReserved=0, setting FREE");
                return Node.STATE_FREE;
            } else {
                perunMachine.setUsedPercent(cloudPhysicalHost.getCpuReserved() * 100 / perunMachine.getCpuNum());
                log.debug("setting PARTIALLY_FREE with {}% used ", perunMachine.getUsedPercent());
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
     * @param perunMachine     perunMachine
     * @param virtNodes VM na danem fyzickem stroji
     * @return stav jako Node.STATE_*
     */
    private static String rozhodniStavFyzickehoPodleVirtualnich(PerunMachine perunMachine, List<Node> virtNodes) {
        String jmenoStroje = perunMachine.getName();
        //bez VM je stav neznamy
        if (virtNodes.isEmpty()) {
            log.debug(" fyzicky {} zadne VM", jmenoStroje);
            return Node.STATE_UNKNOWN;
        }
        //pokud aspon jeden z VM pracuje, pak fyzicky pracuje
        for (Node node : virtNodes) {
            if (node.isWorking()) {
                log.debug(" fyzicky {} ma pracujici VM {} ", jmenoStroje, node.getName());
                return Node.STATE_JOB_BUSY;
            }
        }
        //pokud je nejaky z VM free, pak fyzicky je free
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
                perunMachine.setUsedPercent(node.getUsedPercent());
                return Node.STATE_PARTIALY_FREE;
            }
        }
        //nevime :-(
        log.debug(" fyzicky {} nevime stav", jmenoStroje);
        return Node.STATE_UNKNOWN;
    }
}
