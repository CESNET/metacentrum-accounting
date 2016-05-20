package cz.cesnet.meta.cloud;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class CloudServer {

    private String name;
    private String hostsURL;
    private String vmsURL;

    public CloudServer(String name, String hostsURL, String vmsURL) {
        this.name = name;
        this.hostsURL = hostsURL;
        this.vmsURL = vmsURL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHostsURL() {
        return hostsURL;
    }

    public void setHostsURL(String hostsURL) {
        this.hostsURL = hostsURL;
    }

    public String getVmsURL() {
        return vmsURL;
    }

    public void setVmsURL(String vmsURL) {
        this.vmsURL = vmsURL;
    }

    @Override
    public String toString() {
        return "CloudServer{" +
                "name='" + name + '\'' +
                ", hostsURL='" + hostsURL + '\'' +
                ", vmsURL='" + vmsURL + '\'' +
                '}';
    }
}
