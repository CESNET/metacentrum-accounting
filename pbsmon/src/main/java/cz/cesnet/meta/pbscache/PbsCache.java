package cz.cesnet.meta.pbscache;

import cz.cesnet.meta.TimeStamped;
import cz.cesnet.meta.pbs.FairshareConfig;
import cz.cesnet.meta.pbs.Node;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PbsCache extends TimeStamped {

    /**
     * Returns only static mappings for ungu/urga after Magrathea was dismantled.
     * @return mapping from physical to virtual machines
     */
    Mapping getMapping();

    /**
     * Used for Torque only, PBSPro does not use pbs_cache for scratches.
     * @param node computing node
     * @return data about scratch
     */
    Scratch getScratchForNode(Node node);

    PbsAccess getUserAccess(String userName);

    List<FairshareConfig> getFairshareConfigs();

    Map<String,Integer> getRankMapForFairshareIdAndExistingUsers(String pbsServer, Set<String> userNames);

    Map<String,String> getGpuAlloc(Node node);
}