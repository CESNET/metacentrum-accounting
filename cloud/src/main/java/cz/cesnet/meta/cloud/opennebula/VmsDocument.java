package cz.cesnet.meta.cloud.opennebula;

import cz.cesnet.meta.cloud.CloudVM;

import java.util.List;

/**
 * Represents JSON document about virtual machines.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class VmsDocument {

    private List<CloudVM> vms;

    public List<CloudVM> getVms() {
        return vms;
    }

    public void setVms(List<CloudVM> vms) {
        this.vms = vms;
    }
}
