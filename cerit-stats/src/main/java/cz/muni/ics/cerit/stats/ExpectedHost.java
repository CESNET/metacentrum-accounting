package cz.muni.ics.cerit.stats;

/**
* Host expected to exist.
*
* @author Martin Kuba makub@ics.muni.cz
*/
class ExpectedHost {

    String hostname;
    boolean inPbs;
    boolean available;
    String reason;

    ExpectedHost(String hostname) {
        this.hostname = hostname;
    }

    String getHostname() {
        return hostname;
    }

    boolean isInPbs() {
        return inPbs;
    }

    void setInPbs(boolean inPbs) {
        this.inPbs = inPbs;
    }


    boolean isAvailable() {
        return available;
    }

    void setAvailable(boolean available) {
        this.available = available;
    }

    String getReason() {
        return reason;
    }

    void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "ExpectedHost{" +
                "hostname='" + hostname + '\'' +
                ", inPbs=" + inPbs +
                ", available=" + available +
                ", reason='" + reason + '\'' +
                '}';
    }
}
