package cz.cesnet.meta.cloud.opennebula;

import cz.cesnet.meta.cloud.CloudPhysicalHost;

import java.util.List;

/**
 * Represents JSON document about hosts.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class HostsDocument {

    private List<CloudPhysicalHost> hosts;

    public HostsDocument() {
    }

    public List<CloudPhysicalHost> getHosts() {
        return hosts;
    }

    public void setHosts(List<CloudPhysicalHost> hosts) {
        this.hosts = hosts;
    }


}
