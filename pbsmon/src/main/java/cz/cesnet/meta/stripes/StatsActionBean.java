package cz.cesnet.meta.stripes;

import cz.cesnet.meta.cloud.Cloud;
import cz.cesnet.meta.pbs.*;
import cz.cesnet.meta.pbscache.Mapping;
import cz.cesnet.meta.pbscache.PbsCache;
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
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: StatsActionBean.java,v 1.6 2013/10/08 11:31:56 makub Exp $
 */
@UrlBinding("/stats")
public class StatsActionBean extends BaseActionBean {

    final static Logger log = LoggerFactory.getLogger(StatsActionBean.class);

    @SpringBean("perun")
    protected Perun perun;

    @SpringBean("pbsCache")
    protected PbsCache pbsCache;

    @SpringBean("pbsky")
    protected Pbsky pbsky;

    @SpringBean("cloud")
    protected Cloud cloud;

    private List<User> users;
    private JobsInfo jobsInfo;

    private List<OwnerOrganisation> centra;
    private List<PerunMachine> zbyle;
    private Map<String, Integer> cpuMap;
    private Map<String, CpuCount> cpuCountMap;

    @DefaultHandler
    public Resolution stats() {
        log.debug("stats()");
        users = pbsky.getSortedUsers(UsersSortOrder.cpusStateR);
        jobsInfo = pbsky.getJobsInfo();

        PhysicalMachines physicalMachines = perun.getPhysicalMachines();
        centra = physicalMachines.getOwnerOrganisations();
        zbyle = physicalMachines.getRemaining();
        cpuMap = physicalMachines.getCpuMap();
        ReservedMachinesFinder vyhrazene = perun.getReservedMachinesFinder();
        FrontendFinder frontendy = perun.getFrontendFinder();
        Mapping mapping = pbsCache.getMapping();

        MachineStateDecider.decideStates(pbsky, frontendy, mapping, vyhrazene, centra, cloud);

        cpuCountMap = new HashMap<String, CpuCount>();

        for (OwnerOrganisation centrum : centra) {
            int centrumCpuTotal = 0;
            int centrumCpuUsed = 0;
            for (PerunComputingResource zdroj : centrum.getPerunComputingResources()) {
                int zdrojCpuTotal = 0;
                int zdrojCpuAvailable = 0;
                int zdrojCpuUsed = 0;
                List<PerunMachine> stroje = zdroj.isCluster() ? zdroj.getPerunMachines() : Collections.singletonList(zdroj.getPerunMachine());
                for (PerunMachine perunMachine : stroje) {
                    int strojCpuTotal = perunMachine.getCpuNum();
                    int strojCpuUsed = 0;
                    //vyhrazene a fyzicke frontendy zapocitat jako plne vytizene
                    if (vyhrazene.isMachineReserved(perunMachine) || frontendy.isFrontend(perunMachine.getName())) {
                        strojCpuUsed = strojCpuTotal;
                    } else {
                        //shromazdit vsechny nody z PBSky
                        String jmenoStroje = perunMachine.getName();
                        List<Node> nodes = new ArrayList<Node>(2);
                        Node physNode = pbsky.getNodeByFQDN(jmenoStroje);
                        if (physNode != null) nodes.add(physNode);
                        List<String> virtNames = mapping.getPhysical2virtual().get(jmenoStroje);
                        if (virtNames != null) {
                            for (String virtName : virtNames) {
                                if (frontendy.isFrontend(virtName)) {
                                    strojCpuUsed = strojCpuTotal;
                                    continue;
                                }
                                Node virtNode = pbsky.getNodeByFQDN(virtName);
                                if (virtNode != null) nodes.add(virtNode);
                            }
                        }
                        //secist pouzite ve vsech nodech (fyzicky i virtualni)
                        boolean allReserved = true;
                        for (Node node : nodes) {
                            if (node.isWorking()) {
                                strojCpuUsed += node.getNoOfUsedCPUInt();
                                allReserved = false;
                            } else if(node.isReserved()) {
                                strojCpuUsed += node.getNoOfCPUInt();
                            } else {
                                allReserved = false;
                            }
                        }
                        if (strojCpuUsed > strojCpuTotal) {
                            if (log.isWarnEnabled()&& !allReserved)
                                log.warn(jmenoStroje + " ma " + strojCpuTotal + " CPU ale pouzitych je " + strojCpuUsed);
                            strojCpuUsed = strojCpuTotal;
                        }
                    }
                    cpuCountMap.put(perunMachine.getName(), new CpuCount(strojCpuTotal, strojCpuUsed));
                    zdrojCpuTotal += strojCpuTotal;
                    zdrojCpuUsed += strojCpuUsed;
                }
                cpuCountMap.put(zdroj.getId(), new CpuCount(zdrojCpuTotal, zdrojCpuUsed));
                centrumCpuTotal += zdrojCpuTotal;
                centrumCpuUsed += zdrojCpuUsed;
            }
            cpuCountMap.put(centrum.getId(), new CpuCount(centrumCpuTotal, centrumCpuUsed));
        }

        return new ForwardResolution("/stats.jsp");
    }


    //-------------

    public Map<String, CpuCount> getCpuCountMap() {
        return cpuCountMap;
    }

    public List<User> getUsers() {
        return users;
    }

    public JobsInfo getJobsInfo() {
        return jobsInfo;
    }

    public List<OwnerOrganisation> getCentra() {
        return centra;
    }

    public List<PerunMachine> getZbyle() {
        return zbyle;
    }

    public Map<String, Integer> getCpuMap() {
        return cpuMap;
    }

    public static class CpuCount {
        int total;
        int used;

        public CpuCount(int total, int used) {
            this.total = total;
            this.used = used;
        }

        public int getTotal() {
            return total;
        }


        public int getUsed() {
            return used;
        }
    }
}
