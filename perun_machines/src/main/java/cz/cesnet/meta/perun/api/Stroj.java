package cz.cesnet.meta.perun.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Stroj implements Comparable<Stroj> {

    private static final Pattern whole = Pattern.compile("^([A-Za-z]+)(\\d*)-*([0-9a-z]*)");
    private String name = null;
    private boolean virtual;
    private int cpuNum = 0;
    private String shortName;
    private String clusterName;
    private int numInCluster = 0;
    private VypocetniZdroj vypocetniZdroj;
    private int usedPercent = 0;
    private boolean openNebulaManaged = false; //je v OpenNebule
    private boolean nebulaPbsHost = false; //je v OpenNebule a obsahuje VM ktery je v PBS
    private boolean openNebulaUsable = false; //lze na něm spustit uživatelský VM přes OpenNebulu
    private String pbsName;

    private String state;

    public Stroj(VypocetniZdroj vypocetniZdroj,String name, boolean virtual, int cpuNum) {
        this.vypocetniZdroj = vypocetniZdroj;
        this.name = name;
        this.virtual = virtual; //vzdycky false od pbsmon_json
        this.cpuNum = cpuNum;

        int dot = name.indexOf(46);
        if (dot < 0)
            this.shortName = name;
        else
            this.shortName = name.substring(0, dot);

        Matcher m = whole.matcher(this.shortName);
        if (m.find()) {
            this.clusterName = m.group(1);
            String numInC = m.group(2);
            if ((numInC != null) && (numInC.length() > 0))
                this.numInCluster = Integer.parseInt(numInC);
        } else {
            throw new RuntimeException("name " + this.shortName + " is non-parseable");
        }
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public VypocetniZdroj getVypocetniZdroj() {
        return vypocetniZdroj;
    }

    /**
     * Řazení strojů podle názvu clusteru, pak podle pozice v clusteru.
     *
     * @param s stroj
     * @return int podle compareTo()
     */
    public int compareTo(Stroj s) {
        if (equals(s)) return 0;
        int c = getClusterName().compareTo(s.getClusterName());
        return ((c != 0) ? c : getNumInCluster() - s.getNumInCluster());
    }

    //------- generovano -------

    public String getClusterName() {
        return this.clusterName;
    }

    public String getName() {
        return this.name;
    }

    public int getNumInCluster() {
        return this.numInCluster;
    }

    public String getShortName() {
        return this.shortName;
    }


    public boolean isVirtual() {
        return this.virtual;
    }

    public int getCpuNum() {
        return this.cpuNum;
    }

    public int getUsedPercent() {
        return usedPercent;
    }

    public void setUsedPercent(int usedPercent) {
        this.usedPercent = usedPercent>100?100:usedPercent;
    }

    public boolean isOpenNebulaManaged() {
        return openNebulaManaged;
    }

    public boolean isNebulaPbsHost() {
        return nebulaPbsHost;
    }

    /**
     * Vlasta Holer's definition of cloud host -  is in OpenNebula and is not wholy taken by PBS Node
     * @return
     */
    public boolean isOpenNebulaUsable() {
        return openNebulaUsable;
    }

    public void setOpenNebulaUsable(boolean openNebulaUsable) {
        this.openNebulaUsable = openNebulaUsable;
    }

    public void setNebulaPbsHost(boolean nebulaPbsHost) {
        this.nebulaPbsHost = nebulaPbsHost;
    }

    public void setOpenNebulaManaged(boolean openNebulaManaged) {
        this.openNebulaManaged = openNebulaManaged;
    }

    public String getPbsName() {
        return pbsName;
    }

    public void setPbsName(String pbsName) {
        this.pbsName = pbsName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stroj stroj = (Stroj) o;
        return !(name != null ? !name.equals(stroj.name) : stroj.name != null);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Stroj{" +
                "name='" + name + '\'' +
                ", virtual=" + virtual +
                ", cpuNum=" + cpuNum +
                '}';
    }
}