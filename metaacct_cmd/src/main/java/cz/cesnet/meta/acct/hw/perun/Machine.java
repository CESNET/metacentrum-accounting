package cz.cesnet.meta.acct.hw.perun;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: Machine.java,v 1.2 2009/10/20 11:02:09 makub Exp $
 */
public class Machine {
    private String name = null;
    private boolean virtual;
    private int cpuNum = 0;

    public Machine(String name, boolean virtual, int cpuNum) {
        this.name = name;
        this.virtual = virtual;
        this.cpuNum = cpuNum;
        if(virtual) throw new RuntimeException("machine "+name+" is virtual");
    }

    public String getName() {
        return name;
    }

    public boolean isVirtual() {
        return virtual;
    }

    public int getCpuNum() {
        return cpuNum;
    }

    @Override
    public String toString() {
        return "Machine{" +
                "name='" + name + '\'' +
                ", virtual=" + virtual +
                ", cpuNum=" + cpuNum +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Machine)) return false;

        Machine machine = (Machine) o;

        if (name != null ? !name.equals(machine.name) : machine.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
