package cz.cesnet.meta.pbs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Load data from PBS server using its C interface through Java Native Interface.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class PbsConnectorJNI implements PbsConnector {

    final static Logger log = LoggerFactory.getLogger(PbsConnectorJNI.class);

    static {
        //System.loadLibrary("pbsmon2torque");
        System.loadLibrary("pbsmon2pro");
    }

    //private native void loadInfoTorque();
    private native void loadInfoPro();

    String pbsServer;
    Node[] nodes;
    Job[] jobs;
    Queue[] queues;
    PbsServer[] servers;

    @Override
    public synchronized PBS loadData(PbsServerConfig pbsServerConfig) {
        pbsServer = pbsServerConfig.getHost();
        log.debug("loadData({})", pbsServer);
        if(pbsServerConfig.isTorque()) {
            log.debug("loadInfoTorque({})", pbsServer);
            //loadInfoTorque();
        } else {
            log.debug("loadInfoPro({})", pbsServer);
            loadInfoPro();
        }
         
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
        HashMap<String, T> map = new HashMap<>((int) (array.length * 1.5));
        for (T po : array) map.put(po.getName(), po);
        return map;
    }
}
