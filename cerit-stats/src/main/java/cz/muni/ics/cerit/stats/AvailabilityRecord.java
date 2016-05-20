package cz.muni.ics.cerit.stats;

import java.util.Date;

/**
* Created with IntelliJ IDEA.
*
* @author Martin Kuba makub@ics.muni.cz
*/
public class AvailabilityRecord {
    public long id;
    public long ph_id;
    public String hostname;
    public boolean available;
    public String reason;
    public Date start_time;
    public Date end_time;

    public AvailabilityRecord(long id, long ph_id, boolean available, String reason, Date start_time, Date end_time) {
        this.id = id;
        this.ph_id = ph_id;
        this.available = available;
        this.reason = reason;
        this.start_time = start_time;
        this.end_time = end_time;
    }

    public AvailabilityRecord(long id, String hostname, boolean available, String reason, Date start_time, Date end_time) {
        this.id = id;
        this.hostname = hostname;
        this.available = available;
        this.reason = reason;
        this.start_time = start_time;
        this.end_time = end_time;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public String toString() {
        return "AvailabilityRecord{" +
                "id=" + id +
                ", ph_id=" + ph_id +
                ", hostname='" + hostname + '\'' +
                ", available=" + available +
                ", reason='" + reason + '\'' +
                ", start_time=" + start_time +
                ", end_time=" + end_time +
                '}';
    }
}
