package cz.cesnet.meta.stripes;

import cz.cesnet.meta.cloud.CloudPhysicalHost;
import cz.cesnet.meta.pbs.Job;
import cz.cesnet.meta.pbs.Node;
import cz.cesnet.meta.pbs.Queue;
import cz.cesnet.meta.pbsmon.MachineStateDecider;
import cz.cesnet.meta.perun.api.Perun;
import cz.cesnet.meta.perun.api.PerunMachine;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Detail jedne fronty.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: QueueActionBean.java,v 1.8 2014/01/24 15:29:38 makub Exp $
 */
@UrlBinding("/queue/{queueName}")
public class QueueActionBean extends BaseActionBean {
    final static Logger log = LoggerFactory.getLogger(QueueActionBean.class);

    @SpringBean("perun")
    protected Perun perun;

    //parametr
    private String queueName;
    //data k zobrazeni
    private Queue queue;
    private List<Node> nodes;
    private List<Job> jobs;
    private List<PerunMachine> machines;
    private int cpus;
    private List<Queue> destinations;

    @DefaultHandler
    public Resolution detail() {
        if (queueName == null || queueName.length() == 0) {
            log.warn("Queue name is empty, agent is {}", ctx.getRequest().getHeader("User-Agent"));
            return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND, "Queue name is needed in the URL.");
        }
        queue = pbsky.getQueueByName(queueName);
        if (queue == null) {
            log.warn("Queue {} not found.", queueName);
            return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND, "Queue " + queueName + " not found.");
        }
        jobs = queue.getJobs();
        if (queue.isRouting()) {
            destinations = new ArrayList<>();
            for(String dstName : queue.getDestQueueNames()) {
                destinations.add(pbsky.getQueueByName(dstName));
            }
        } else {
            nodes = queue.getNodes();
            findMachinesForNodes();
        }
        return new ForwardResolution("/queues/queue.jsp");
    }

    private void findMachinesForNodes() {
        Set<String> machineNames = new HashSet<String>(nodes.size());
        machines = new ArrayList<>(nodes.size());
        for (Node node : nodes) {
            String nodeFQDN = node.getFQDN();
            //try mapping
            String machineName = pbsCache.getMapping().getVirtual2physical().get(nodeFQDN);
            //try Cloud
            if (machineName == null) {
                CloudPhysicalHost physicalHost = cloud.getVmFqdnToPhysicalHostMap().get(nodeFQDN);
                if (physicalHost != null) {
                    machineName = physicalHost.getFqdn();
                }
            }
            //assume non-virtualised host
            if (machineName == null) machineName = nodeFQDN;
            machineNames.add(machineName);
        }
        cpus = 0;
        for (String machineName : machineNames) {
            PerunMachine perunMachine = perun.getMachineByName(machineName);
            if (perunMachine != null) {
                machines.add(perunMachine);
                cpus += perunMachine.getCpuNum();
            } else {
                log.warn("findMachinesForNodes() no machine for machineName {}", machineName);
            }
        }
        //sort physical machines by name
        machines.sort(PerunMachine.NAME_COMPARATOR);
        //update their state
        for (PerunMachine perunMachine : machines) {
            MachineStateDecider.decideState(perunMachine, pbsky, pbsCache.getMapping(), perun.getFrontendFinder(), perun.getReservedMachinesFinder(), cloud);
        }
    }

    public List<Queue> getDestinations() {
        return destinations;
    }

    public List<PerunMachine> getMachines() {
        return machines;
    }

    public int getCpus() {
        return cpus;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public Queue getQueue() {
        return queue;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Job> getJobs() {
        return jobs;
    }
}
