package cz.cesnet.meta.perun.api;

import java.util.List;

public interface Perun {

    // primarni data

    List<OwnerOrganisation> findOwnerOrganisations();

    List<PerunMachine> getPerunMachines();

    List<PerunUser> getAllUsers();

    /**
     * Vrací třídu, která rozhoduje, zda má být daný stroj označen jako modrý protože je mimo PBS záměrně.
     * @return vyhledavac
     */
    ReservedMachinesFinder getReservedMachinesFinder();

    FrontendFinder getFrontendFinder();


    //odvozena data

    PerunUser getUserByName(String userName);

    boolean isNodeVirtual(String nodeName);

    PerunMachine getMachineByName(String machineName);

    PhysicalMachines getPhysicalMachines();

    PerunComputingResource getPerunComputingResourceByName(String name);

    boolean isNodePhysical(String nodeName);
}