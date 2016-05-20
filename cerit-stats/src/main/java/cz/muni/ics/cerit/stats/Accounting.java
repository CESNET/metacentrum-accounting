package cz.muni.ics.cerit.stats;

import org.joda.time.DateTime;

import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public interface Accounting {

    /**
     * Stores results of physical hosts check into table physical_hosts_avail as intervals of same availability.
     *
     * @param expectedHosts hosts with filled up availability and reason fields
     */
    void storeHostsAvailability(List<ExpectedHost> expectedHosts);

    /**
     * Computes availability statistics for a given time interval. Does not store anything.
     * <p/>
     * <pre>
     *     up = available - scheduled_downtime
     *     availability = up / total
     *     reliability = up / (total - scheduled_downtime)
     * </pre>
     *
     * @param clusterId       cluster name
     * @param from            start time
     * @param to              end time
     * @param detailedRecords whether to return detailed records
     * @return result
     */
    ResourceAvailability getAvailability(String clusterId, DateTime from, DateTime to, boolean detailedRecords);

    /**
     * Computes statistics for give day, it means from its start to its end.
     * Delegates to {@link #getAvailability(String, org.joda.time.DateTime, org.joda.time.DateTime, boolean)}.
     *
     * @param clusterId cluster name
     * @param day       day
     * @return result
     */
    ResourceAvailability getAvailability(String clusterId, DateTime day);

    /**
     * Stores computed results into table availability_reliability_stats.
     *
     * @param clusterId    cluster name
     * @param day          day
     * @param availability computed results
     */
    void storeStatsForDay(String clusterId, DateTime day, ResourceAvailability availability);


    /**
     * Gets the date when the cluster was created.
     *
     * @param clusterId cluster name
     * @return day
     */
    DateTime getClusterStartDate(String clusterId);
}
