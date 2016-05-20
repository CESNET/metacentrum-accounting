package cz.cesnet.meta.pbscache;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: PbsCacheEntry.java,v 1.2 2014/04/09 14:02:47 makub Exp $
 */
public class PbsCacheEntry {

    String key;
    String value;
    long timestamp;

    public PbsCacheEntry(String key, String value, long timestamp) {
        this.key = key;
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "PbsCacheEntry{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
