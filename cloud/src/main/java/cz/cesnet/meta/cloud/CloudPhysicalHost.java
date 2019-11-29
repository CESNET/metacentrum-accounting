package cz.cesnet.meta.cloud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cloud physical host.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("UnusedDeclaration")
public class CloudPhysicalHost {

    final static Logger log = LoggerFactory.getLogger(CloudPhysicalHost.class);

    public static class ParsedName {
        private String clust;
        private int num;
        private String domain;

        public ParsedName(String clust, int num, String domain) {
            this.clust = clust;
            this.num = num;
            this.domain = domain;
        }

        public String getClust() {
            return clust;
        }

        public int getNum() {
            return num;
        }

        public String getDomain() {
            return domain;
        }

        @Override
        public String toString() {
            return "ParsedName{" +
                    "clust='" + clust + '\'' +
                    ", num=" + num +
                    ", domain='" + domain + '\'' +
                    '}';
        }
    }

    public static Comparator<CloudPhysicalHost> CLOUD_PHYSICAL_HOST_COMPARATOR = (o1, o2) -> {
        ParsedName parsedName1 = o1.getParsedName();
        ParsedName parsedName2 = o2.getParsedName();
        int dc = parsedName1.getDomain().compareTo(parsedName2.getDomain());
        if (dc != 0) return dc;
        dc = parsedName1.getClust().compareTo(parsedName2.getClust());
        if (dc != 0) return dc;
        return parsedName1.getNum() - parsedName2.getNum();
    };

    private static final Pattern HOSTNAME_REGEX = Pattern.compile("(\\D)([0-9]+)([^.]*)(\\..*)");

    private int id;
    private String fqdn; //mel by byt hostname
    private String state;
    private int cpuAvail;
    private int cpuReserved;

    private ParsedName parsedName;

    public ParsedName getParsedName() {
        if (parsedName == null) {
            Matcher m = HOSTNAME_REGEX.matcher(fqdn);
            if (m.matches()) {
                parsedName = new ParsedName(m.group(1) + m.group(3), Integer.parseInt(m.group(2)), m.group(4));
            } else {
                log.warn("cannot parse hostname {}", fqdn);
                parsedName = new ParsedName(fqdn, 0, "");
            }
        }
        return parsedName;
    }

    /**
     * Should be DNS name. Can be the same for multiple hosts.
     *
     * @return cloud host DNS hostname
     */
    public String getFqdn() {
        return fqdn;
    }

    /**
     * Same as FQDN.
     *
     * @return cloud host name
     */
    public String getName() {
        return fqdn;
    }


    //generated below this line
    public CloudPhysicalHost() {
    }

    public int getCpuAvail() {
        return cpuAvail;
    }

    public void setCpuAvail(int cpuAvail) {
        this.cpuAvail = cpuAvail;
    }

    public int getCpuReserved() {
        return cpuReserved;
    }

    public void setCpuReserved(int cpuReserved) {
        this.cpuReserved = cpuReserved;
    }

    public void setFqdn(String fqdn) {
        this.fqdn = fqdn;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "CloudPhysicalHost{" +
                "id=" + id +
                ", fqdn='" + fqdn + '\'' +
                ", state='" + state + '\'' +
                ", cpuAvail=" + cpuAvail +
                ", cpuReserved=" + cpuReserved +
                ", parsedName=" + parsedName +
                '}';
    }
}
