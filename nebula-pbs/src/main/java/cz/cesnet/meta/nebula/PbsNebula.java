package cz.cesnet.meta.nebula;

import java.util.List;

public interface PbsNebula {

    void fixNodeAssignment();

    void markNodes(List<Main.VMHost> vmHosts);
}
