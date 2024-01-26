package cz.cesnet.meta.pbs;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: PbsServerConfig.java,v 1.7 2014/07/17 10:40:27 makub Exp $
 */
public class PbsServerConfig {

    private String shortName;
    private String host;
    private boolean main = false;
    private boolean torque = true;
    private boolean by_queue;
    private List<FairshareConfig> fairshares;
    private String groupFile;
    private String pbsCaller = "pbsprocaller";
    /**
     * If true, server uses plan-based scheduler, thus waiting jobs are sorted by planned_start instead of by queue priority and fairshare.
     */
    private boolean planbased = false;

    public PbsServerConfig() {
    }

    public PbsServerConfig(String shortName, String host, boolean main, boolean torque, boolean by_queue,List<FairshareConfig> fairshares) {
        this.shortName = shortName;
        this.host = host;
        this.main = main;
        this.torque = torque;
        this.by_queue = by_queue;
        this.fairshares = fairshares;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isMain() {
        return main;
    }

    public void setMain(boolean main) {
        this.main = main;
    }

    public boolean isTorque() {
        return torque;
    }

    public void setTorque(boolean torque) {
        this.torque = torque;
    }



    public boolean isBy_queue() {
        return by_queue;
    }

    public void setBy_queue(boolean by_queue) {
        this.by_queue = by_queue;
    }

    public List<FairshareConfig> getFairshares() {
        return fairshares;
    }

    public void setFairshares(List<FairshareConfig> fairshares) {
        this.fairshares = fairshares;
    }

    public String getGroupFile() {
        return groupFile;
    }

    public void setGroupFile(String groupFile) {
        this.groupFile = groupFile;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getPbsCaller() {
        return pbsCaller;
    }

    public void setPbsCaller(String pbsCaller) {
        this.pbsCaller = pbsCaller;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PbsServerConfig that = (PbsServerConfig) o;

        if (host != null ? !host.equals(that.host) : that.host != null) return false;

        return true;
    }

    public boolean isPlanbased() {
        return planbased;
    }

    public void setPlanbased(boolean planbased) {
        this.planbased = planbased;
    }

    @Override
    public int hashCode() {
        return host != null ? host.hashCode() : 0;
    }

    @Override
    public String
    toString() {
        return "PbsServerConfig{" +
                "host='" + host + '\'' +
                ", main=" + main +
                ", torque=" + torque +
                ", by_queue=" + by_queue +
                ", fairshares=" + fairshares +
                '}';
    }
}
