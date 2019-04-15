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

    public PbsServer(PBS pbs, String name) {
        super(pbs, name);
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
        return serverConfig.getShortName();
    }

    public void setServerConfig(PbsServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public PbsServerConfig getServerConfig() {
        return serverConfig;
    }
}
