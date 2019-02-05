package cz.cesnet.meta.acct.hw.perun;

import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: Machine.java,v 1.2 2009/10/20 11:02:09 makub Exp $
 */
public class Machine {
    private String name;
    private int cpuNum;

    public Machine(String name, int cpuNum) {
        this.name = name;
        this.cpuNum = cpuNum;
    }

    public String getName() {
        return name;
    }

    public int getCpuNum() {
        return cpuNum;
    }

    @Override
    public String toString() {
        return "Machine{" +
                "name='" + name + '\'' +
                ", cpuNum=" + cpuNum +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Machine)) return false;

        Machine machine = (Machine) o;
        return Objects.equals(name, machine.name);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
