package cz.cesnet.meta.acct.hw.stats;

import cz.cesnet.meta.acct.hw.stats.ClusterAtDay;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: Stats.java,v 1.2 2009/12/11 17:37:58 makub Exp $
 */
public interface Stats {

    void computeStats(ClusterAtDay c);


    /**
     * Gets names of clusters that were enabled between the specified dates.
     * @param start date
     * @param end date
     * @return list of cluster names
     */
    List<String> getClusters(GregorianCalendar start, GregorianCalendar end);

    void saveStats(ClusterAtDay c);

    void checkReceiveLogs(Calendar start,Calendar end);

    List<WorkloadRecord> getWorkload(String cluster, Calendar start, Calendar end);

    boolean wasClusterActive(ClusterAtDay c);
}
