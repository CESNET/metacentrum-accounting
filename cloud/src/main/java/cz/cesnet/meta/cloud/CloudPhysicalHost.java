package cz.cesnet.meta.cloud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OpenNebula physical host.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("UnusedDeclaration")
public class CloudPhysicalHost {

    final static Logger log = LoggerFactory.getLogger(CloudPhysicalHost.class);

    public class ParsedName {
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

    public static Comparator<CloudPhysicalHost> CLOUD_PHYSICAL_HOST_COMPARATOR = new Comparator<CloudPhysicalHost>() {
        @Override
        public int compare(CloudPhysicalHost o1, CloudPhysicalHost o2) {
            ParsedName parsedName1 = o1.getParsedName();
            ParsedName parsedName2 = o2.getParsedName();
            int dc = parsedName1.getDomain().compareTo(parsedName2.getDomain());
            if (dc != 0) return dc;
            dc = parsedName1.getClust().compareTo(parsedName2.getClust());
            if (dc != 0) return dc;
            return parsedName1.getNum() - parsedName2.getNum();
        }
    };

    private static final Pattern HOSTNAME_REGEX = Pattern.compile("([a-z]+)([0-9]+)([^\\.]*)(\\..*)");

    private int id;
    private String name; //muze byt cokoliv, co uzivatel napise, muze byt IP adresa
    private String hostname; //mel by byt hostname
    private String cluster;
    private String state;
    private int cpu_avail_x100; //počet CPU krát 100
    private int cpu_used_x100;  //aktuální load stroje x100
    private int cpu_reserved_x100; //počet CPU rezervovaných pro VM x100
    private long mem_avail_kB;
    private long mem_used_kB;
    private long mem_reserved_kB;
    private int vms_running;
    private String cpu_info;
    private String arch;

    public int getCpuAvail() {
        return cpu_avail_x100 / 100;
    }

    public int getCpuUsed() {
        return cpu_used_x100 / 100;
    }

    public int getCpuReserved() {
        return getCpu_reserved_x100()/ 100;
    }

    public String getCpuReservedString() {
        int percent = getCpu_reserved_x100();
        return (percent%100==0) ? Integer.toString(percent / 100) : String.format(Locale.US,"%.2f", ((double)percent/100d));
    }

    private ParsedName parsedName;

    public ParsedName getParsedName() {
        if (parsedName == null) {
            if (hostname == null) {
                log.warn("hostname for physical host id={} name={} is null, replacing with name", id, name);
                hostname = name;
            }
            Matcher m = HOSTNAME_REGEX.matcher(hostname);
            if (m.matches()) {
                parsedName = new ParsedName(m.group(1) + m.group(3), Integer.parseInt(m.group(2)), m.group(4));
            } else {
                log.warn("cannot parse hostname {}", hostname);
                parsedName = new ParsedName(hostname, 0, "");
            }
        }
        return parsedName;
    }

    /**
     * Should be DNS name. Can be the same for multiple hosts.
     *
     * @return cloud host DNS hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Can be any string the owner used.
     *
     * @return cloud host name
     */
    public String getName() {
        return name;
    }


    //generated below this line
    public CloudPhysicalHost() {
    }

    public String getCluster() {
        return cluster;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * Percent of available CPU, it means for 12-CPU host it would be 1200.
     * @return cpus multiplied by 100
     */
    public int getCpu_avail_x100() {
        return cpu_avail_x100;
    }

    public void setCpu_avail_x100(int cpu_avail_x100) {
        this.cpu_avail_x100 = cpu_avail_x100;
    }

    /**
     * Percent of actually used CPU as measured by the OS.
     * @return used cpus multiplied by 100
     */
    public int getCpu_used_x100() {
        return cpu_used_x100;
    }

    public void setCpu_used_x100(int cpu_used_x100) {
        this.cpu_used_x100 = cpu_used_x100;
    }


    public int getCpu_reserved_x100() {
        return cpu_reserved_x100>cpu_avail_x100?cpu_avail_x100:cpu_reserved_x100;
    }

    public void setCpu_reserved_x100(int cpu_reserved_x100) {
        this.cpu_reserved_x100 = cpu_reserved_x100;
    }

    public long getMem_avail_kB() {
        return mem_avail_kB;
    }

    public void setMem_avail_kB(long mem_avail_kB) {
        this.mem_avail_kB = mem_avail_kB;
    }

    public long getMem_used_kB() {
        return mem_used_kB;
    }

    public void setMem_used_kB(long mem_used_kB) {
        this.mem_used_kB = mem_used_kB;
    }

    public long getMem_reserved_kB() {
        return mem_reserved_kB;
    }

    public void setMem_reserved_kB(long mem_reserved_kB) {
        this.mem_reserved_kB = mem_reserved_kB;
    }

    public int getVms_running() {
        return vms_running;
    }

    public void setVms_running(int vms_running) {
        this.vms_running = vms_running;
    }

    public String getCpu_info() {
        return cpu_info;
    }

    public void setCpu_info(String cpu_info) {
        this.cpu_info = cpu_info;
    }

    public String getArch() {
        return arch;
    }

    public void setArch(String arch) {
        this.arch = arch;
    }

    @Override
    public String toString() {
        return "CloudPhysicalHost{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", hostname='" + hostname + '\'' +
                ", state='" + state + '\'' +
                ", vms_running=" + vms_running +
                ", cpu_info='" + cpu_info + '\'' +
                ", arch='" + arch + '\'' +
                ", parsedName=" + parsedName +
                ", cluster='" + cluster + '\'' +
                ", cpu_avail_x100=" + cpu_avail_x100 +
                ", cpu_used_x100=" + cpu_used_x100 +
                ", cpu_reserved_x100=" + cpu_reserved_x100 +
                ", mem_avail_kB=" + mem_avail_kB +
                ", mem_used_kB=" + mem_used_kB +
                ", mem_reserved_kB=" + mem_reserved_kB +
                '}';
    }
}
