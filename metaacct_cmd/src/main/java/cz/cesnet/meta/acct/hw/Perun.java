package cz.cesnet.meta.acct.hw;

import cz.cesnet.meta.acct.hw.perun.ComputingResource;
import cz.cesnet.meta.perun.api.PerunUser;

import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: Perun.java,v 1.2 2009/09/24 11:45:02 makub Exp $
 */
public interface Perun {

    List<ComputingResource> getComputingResources();

    Set<String> getFrontendNames();

    Set<String> getReservedMachinesNames();

    List<PerunUser> getAllUsers();
}
