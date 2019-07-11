package cz.cesnet.meta.cloud.opennebula;

import cz.cesnet.meta.cloud.CloudVirtualHost;

import java.util.List;

/**
 * Represents JSON document about virtual machines.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class VmsDocument {

    private List<CloudVirtualHost> vms;

    public List<CloudVirtualHost> getVms() {
        return vms;
    }

    public void setVms(List<CloudVirtualHost> vms) {
        this.vms = vms;
    }
}
