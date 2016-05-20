package cz.cesnet.meta.pbs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: PbsConnectorJNI.java,v 1.39 2014/03/05 17:03:53 makub Exp $
 */
public class PbsConnectorJNI implements PbsConnector {

    final static Logger log = LoggerFactory.getLogger(PbsConnectorJNI.class);

    static {
        System.loadLibrary("pbsmon2torque");
        //System.loadLibrary("pbsmon2pro");
    }

    String pbsServer;
    Node[] nodes;
    Job[] jobs;
    Queue[] queues;
    PbsServer[] servers;

    @Override
    public synchronized PBS loadData(PbsServerConfig pbsServerConfig) {
        pbsServer = pbsServerConfig.getHost();
        log.debug("loadData({})", pbsServer);
        loadInfoTorque();
        PBS pbs = new PBS(pbsServerConfig);
        PbsServer server = servers[0];
        server.setServerConfig(pbsServerConfig);
        pbs.setServer(server);
        pbs.setNodes(makeMap(nodes));
        pbs.setJobs(makeMap(jobs));
        pbs.setQueues(makeMap(queues));
        return pbs;
    }

    private <T extends PbsInfoObject> Map<String, T> makeMap(T[] array) {
        HashMap<String, T> map = new HashMap<String, T>((int) (array.length * 1.5));
        for (T po : array) map.put(po.getName(), po);
        return map;
    }

    //private native void loadInfoPro();

    private native void loadInfoTorque();

    public static void main(String[] args) {
        System.out.println("start");
        log.debug("main");
        PbsConnectorJNI p = new PbsConnectorJNI();
        call(p, new PbsServerConfig("torque.cerit-sc.cz",true,true, true,Collections.<FairshareConfig>emptyList()));
        //call(p, new PbsServerConfig("arien.ics.muni.cz",true,true));

    }

    static PBS call(PbsConnectorJNI p, PbsServerConfig server) {
        long start = System.currentTimeMillis();
        PBS pbs = p.loadData(server);
        long end = System.currentTimeMillis();
        pbs.uprav();
        System.out.println();
        System.out.println();
        System.out.println("pbs.getServer() = " + pbs.getServer());
        System.out.println("time " + (end - start));
        System.out.println("pbs.getServer().getShortName() = " + pbs.getServer().getShortName());

//        for(Job job: pbs.getJobsById()) {
//            System.out.println("-------");
//            System.out.println("job = " + job);
//            for(Map.Entry<String,String> entry : job.getAttributes().entrySet()) {
//                System.out.println(entry.getKey()+" = " + entry.getValue());
//            }
//
//        }
//        for(Queue q : pbs.getQueuesByPriority()) {
//            System.out.println();
//            System.out.println("queue = " + q.getOrigToString());
//            System.out.println("queue.name = " + q.getName());
//        }
        System.out.println("-------");
//        for (Node node : pbs.getNodesByName()) {
//            if(!node.getName().equals("skirit50-1.ics.muni.cz")) continue;
//            System.out.println("node.getName() = " + node.getName());
//            TreeMap<String, String> attributes = node.getAttributes();
//            for (Map.Entry<String, String> e : attributes.entrySet()) {
//                System.out.println(e.getKey() + "=" + e.getValue());
//            }
//            System.out.println();
//            System.out.println("node.getNoOfCPUInt() = " + node.getNoOfCPUInt());
//            System.out.println("node.getNoOfFreeCPUInt() = " + node.getNoOfFreeCPUInt());
//            System.out.println("node.getNoOfUsedCPUInt() = " + node.getNoOfUsedCPUInt());
//            System.out.println("node.getNoOfHTCoresInt() = " + node.getNoOfHTCoresInt());
//            System.out.println("node.getTotalMemory() = " + node.getTotalMemory());
//            System.out.println("node.getFreeMemory() = " + node.getFreeMemory());
//            System.out.println("node.getUsedMemory() = " + node.getUsedMemory());
//            for(Job job : node.getJobs()) {
//                System.out.println();
//                System.out.println("----------------------job = " + job);
//            }
//            break;
//        }
        return pbs;
    }
}
