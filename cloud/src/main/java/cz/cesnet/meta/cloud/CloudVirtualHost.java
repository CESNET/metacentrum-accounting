package cz.cesnet.meta.cloud;


import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * OpenNebula Virtual Machine.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("UnusedDeclaration")
public class CloudVirtualHost {

    private int id;
    private String name; // any string specified by user, i.e. "MyDebian6", for PBS nodes equal to fqdn
    private String fqdn; // FQDN, e.g. DNS name
    private String current_host; // the name of physical node, not the hostname, may be an IP address !
    private Object node; //for PBS Node
    private int cpu_avail_x100; //number of VCPU visible by the guest OS
    private int cpu_used_x100;
    private int cpu_reserved_x100; //number of real CPUs assigned to VM, is in percents, thus may not be an integer
    private Owner owner;
    private String state;
    private long start_time;
    private List<String> hosts;
    private String role;

    /**
     * VCPU - number of CPU available for OS in VM.
     * @return VCPU
     */
    public int getVCPU() {
        return cpu_avail_x100/100;
    }

    public int getCpuUsed() {
        return cpu_used_x100/100;
    }

    public String getCpuReservedString() {
        int percent = cpu_reserved_x100;
        return (percent%100==0) ? Integer.toString(percent / 100) : String.format(Locale.US,"%.2f", ((double)percent/100d));
    }



    public Date getStartTime() {
        return new Date(start_time*1000);
    }

    public String getIdString() {
        return "VM("+id+"/"+name+"/"+fqdn+")";
    }

    /**
     * Decides whether the VM occupies resources at the physical host.
     * @return true for states ACTIVE, POWEROFF and SUSPEND
     */
    public boolean isReserved() {
        return "ACTIVE".equals(state)||"POWEROFF".equals(state)||"SUSPENDED".equals(state);
    }

    //generated below this line

    public CloudVirtualHost() {
    }

    /**
     * Gets the {@link cz.cesnet.meta.cloud.CloudPhysicalHost#getName()} where the VM runs.
     * @return
     */
    public String getCurrent_host() {
        return current_host;
    }

    public void setCurrent_host(String current_host) {
        this.current_host = current_host;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getStart_time() {
        return start_time;
    }

    public void setStart_time(long start_time) {
        this.start_time = start_time;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCpu_avail_x100() {
        return cpu_avail_x100;
    }

    public void setCpu_avail_x100(int cpu_avail_x100) {
        this.cpu_avail_x100 = cpu_avail_x100==0 ? 100 : cpu_avail_x100;
    }

    public int getCpu_used_x100() {
        return cpu_used_x100;
    }

    public void setCpu_used_x100(int cpu_used_x100) {
        this.cpu_used_x100 = cpu_used_x100;
    }

    /**
     * Number of reserved CPUs as percent. It is an integer, but may not be multiple of 100, because OpenNebula allows reserving
     * CPUs in quantums of 1 percent.
     * @return integer between 0 and getCpu_avail_x100()
     */
    public int getCpu_reserved_x100() {
        return cpu_reserved_x100;
    }

    public void setCpu_reserved_x100(int cpu_reserved_x100) {
        this.cpu_reserved_x100 = cpu_reserved_x100;
    }

    public String getFqdn() {
        return fqdn;
    }

    public void setFqdn(String fqdn) {
        this.fqdn = fqdn;
    }

    public String getRole() {
        return role;
    }

    public boolean isPbsNode() {
        return "pbs_mom".equals(role);
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "CloudVirtualHost{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", cpu_avail =" + getVCPU() +
                ", cpu_used =" + getCpuUsed() +
                ", cpu_reserved =" + getCpuReservedString() +
                ", owner = "+ owner +
                ", fqdn='" + fqdn + '\'' +
                ", role = "+ role +
                '}';
    }

    public void setNode(Object node) {
        this.node = node;
    }

    public Object getNode() {
        return node;
    }

}
