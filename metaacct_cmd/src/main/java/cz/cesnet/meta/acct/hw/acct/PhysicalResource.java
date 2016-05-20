package cz.cesnet.meta.acct.hw.acct;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: PhysicalResource.java,v 1.1 2009/09/23 15:42:09 makub Exp $
 */
public class PhysicalResource {

    private int id;
    private String name;
    private boolean cluster;

    public PhysicalResource(int id, String name, boolean cluster) {
        this.id = id;
        this.name = name;
        this.cluster = cluster;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isCluster() {
        return cluster;
    }
}
