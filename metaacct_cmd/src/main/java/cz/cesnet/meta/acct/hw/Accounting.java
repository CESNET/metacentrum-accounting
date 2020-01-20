package cz.cesnet.meta.acct.hw;

import cz.cesnet.meta.acct.hw.perun.ComputingResource;
import cz.cesnet.meta.perun.api.PerunUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: Accounting.java,v 1.4 2009/10/20 10:18:48 makub Exp $
 */
public interface Accounting {

    void saveComputingResources(List<ComputingResource> computingResources, Set<String> frontends, Set<String> reserved);

    void insertMapping(String virt, String fyz);


    void checkMachinesTablesConsistency();

    void assignNodes(String node, String machine);

    void updateUsers(List<PerunUser> allUsers);

    void fixDuplicateUsers();
}
