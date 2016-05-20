package cz.muni.ics.cerit.stats;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class ResourceAvailability {

    List<AvailabilityRecord> records;
    private String clusterId;
    private DateTime from;
    private DateTime to;
    private long totalSeconds;
    private double percentAvailable;
    private double percentReliable;

    public ResourceAvailability(String clusterId,
                                DateTime from,
                                DateTime to,
                                List<AvailabilityRecord> records,
                                long totalSeconds,
                                double percentAvailable,
                                double percentReliable) {
        this.clusterId = clusterId;
        this.from = from;
        this.to = to;
        this.records = records;
        this.totalSeconds = totalSeconds;
        this.percentAvailable = percentAvailable;
        this.percentReliable = percentReliable;
    }

    public String getClusterId() {
        return clusterId;
    }

    public DateTime getFrom() {
        return from;
    }

    public DateTime getTo() {
        return to;
    }

    public List<AvailabilityRecord> getRecords() {
        return records;
    }

    public long getTotalSeconds() {
        return totalSeconds;
    }

    public double getPercentAvailable() {
        return percentAvailable;
    }

    public double getPercentReliable() {
        return percentReliable;
    }

    @Override
    public String toString() {
        return "ResourceAvailability{" +
                "clusterId='" + clusterId + '\'' +
                ", from=" + from.toDate() +
                ", to=" + to.toDate() +
                ", totalSeconds=" + totalSeconds +
                ", percentAvailable=" + percentAvailable +
                ", percentReliable=" + percentReliable +
                '}';
    }
}
