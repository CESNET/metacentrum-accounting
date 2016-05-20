package cz.cesnet.meta.acct.hw.perun;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: ComputingResource.java,v 1.2 2009/10/20 11:02:09 makub Exp $
 */
public class ComputingResource {
    private String id = null;
    private String name = null;
    private boolean cluster = false;
    private List<Machine> machines;
    private Machine machine;

    public ComputingResource(String id, String name, boolean cluster) {
        this.id = id;
        this.name = name;
        this.cluster = cluster;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isCluster() {
        return cluster;
    }

    public List<Machine> getMachines() {
        return machines;
    }

    public void setMachines(List<Machine> machines) {
        this.machines = machines;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    @Override
    public String toString() {
        return "ComputingResource{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", cluster=" + cluster +
                '}';
    }

    
}
