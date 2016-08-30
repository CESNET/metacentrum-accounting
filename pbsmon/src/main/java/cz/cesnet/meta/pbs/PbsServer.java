package cz.cesnet.meta.pbs;

/**
 * Class representing data about a PBS server.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class PbsServer extends PbsInfoObject {

    //used attribute names
    private static final String ATTRIBUTE_PBS_VERSION = "pbs_version";

    private PbsServerConfig serverConfig;

    public PbsServer() {
    }

    public PbsServer(String name) {
        super(name);
    }

    public String getVersion() {
        return attrs.get(ATTRIBUTE_PBS_VERSION);
    }

    /**
     * Returns getServerConfig().getHost().
     * @return host name
     */
    public String getHost() {
        return serverConfig.getHost();
    }

    public String getShortName() {
        return getHost().split("\\.")[0];
    }

    public boolean isTorque() {
        return !getVersion().startsWith("PBSPro");
    }

    public void setServerConfig(PbsServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public PbsServerConfig getServerConfig() {
        return serverConfig;
    }
}
