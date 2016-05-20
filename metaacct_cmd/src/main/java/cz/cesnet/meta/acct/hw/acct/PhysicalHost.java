package cz.cesnet.meta.acct.hw.acct;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: PhysicalHost.java,v 1.1 2009/09/23 15:42:09 makub Exp $
 */
public class PhysicalHost {
    int id;
    String name;

    public PhysicalHost(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
