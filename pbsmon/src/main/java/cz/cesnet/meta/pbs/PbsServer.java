package cz.cesnet.meta.pbs;

/**
 * Specializace PbsInfoObject na server.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: PbsServer.java,v 1.7 2013/11/15 10:05:35 makub Exp $
 */
public class PbsServer extends PbsInfoObject {

    private PbsServerConfig serverConfig;

    public PbsServer() {
    }

    public PbsServer(String name) {
        super(name);
    }

    public String getVersion() {
        return attrs.get("pbs_version");
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
