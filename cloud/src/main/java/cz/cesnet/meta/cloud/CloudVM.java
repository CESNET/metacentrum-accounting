package cz.cesnet.meta.cloud;


import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * Cloud Virtual Machine.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("UnusedDeclaration")
public class CloudVM {

    private int id;
    private String idString; //unique identifier across multiple cloud instances
    private String name; // any string specified by user, i.e. "MyDebian6", for PBS nodes equal to fqdn
    private String fqdn; // DNS name of the virtual host
    private String physicalHostFqdn; // DNS name of its physical host
    private int cpu_reserved_x100; //number of real CPUs assigned to VM, is in percents, thus may not be an integer
    private String owner;
    private String state;
    private Date startTime;
    private boolean pbsNode;
    private Object node; //for PBS Node

    public String getCpuReservedString() {
        int percent = cpu_reserved_x100;
        return (percent%100==0) ? Integer.toString(percent / 100) : String.format(Locale.US,"%.2f", ((double)percent/100d));
    }

    public String getIdString() {
        return idString;
    }

    public void setIdString(String idString) {
        this.idString = idString;
    }

    public CloudVM() {
    }

    /**
     * Gets the {@link cz.cesnet.meta.cloud.CloudPhysicalHost#getName()} where the VM runs.
     */
    public String getPhysicalHostFqdn() {
        return physicalHostFqdn;
    }

    public void setPhysicalHostFqdn(String physicalHostFqdn) {
        this.physicalHostFqdn = physicalHostFqdn;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
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

    public boolean isPbsNode() {
        return pbsNode;
    }

    public void setPbsNode(boolean pbsNode) {
        this.pbsNode = pbsNode;
    }

    @Override
    public String toString() {
        return "CloudVirtualHost{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", cpu_reserved =" + getCpuReservedString() +
                ", owner = "+ owner +
                ", fqdn='" + fqdn + '\'' +
                '}';
    }

    public void setNode(Object node) {
        this.node = node;
    }

    public Object getNode() {
        return node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CloudVM cloudVM = (CloudVM) o;
        return idString.equals(cloudVM.idString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idString);
    }
}
