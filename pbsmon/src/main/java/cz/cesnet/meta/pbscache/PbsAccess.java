package cz.cesnet.meta.pbscache;

import java.util.List;
import java.util.Map;


/**
 * Trida pro namapovani JSON zpravy pomoci JSON Tools.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: PbsAccess.java,v 1.1 2009/05/14 07:33:15 makub Exp $
 */
public class PbsAccess {
    private List<String> queues;
    private Map<String,String> all_queues;
    private Map<String,List<String>> queueToHostsMap;


    public Map<String, String> getAll_queues() {
        return all_queues;
    }

    public void setAll_queues(Map<String, String> all_queues) {
        this.all_queues = all_queues;
    }


    public Map<String, List<String>> getQueueToHostsMap() {
        return queueToHostsMap;
    }

    public void setQueueToHostsMap(Map<String, List<String>> queueToHostsMap) {
        this.queueToHostsMap = queueToHostsMap;
    }

    public List<String> getQueues() {
        return queues;
    }

    public void setQueues(List<String> queues) {
        this.queues = queues;
    }
}
