package cz.cesnet.meta.perun.impl;

import cz.cesnet.meta.perun.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public abstract class PerunAbstractImpl implements Perun {

    final static Logger log = LoggerFactory.getLogger(PerunAbstractImpl.class);

    /**
     * Najde výpočetní centra, v nich zdroje (clustery a stroje). Spočítá počty CPU.
     * Najde stroje, které jsou v MetaCentru, ale ne v žádném centru.
     *
     * @return složitá struktura
     */
    @Override
    public PhysicalMachines getPhysicalMachines() {
        List<OwnerOrganisation> ownerOrganisations;
        List<PerunMachine> remaining;
        Map<String, Integer> cpuMap;

        List<PerunMachine> perunMachines = this.getPerunMachines();
        Set<PerunMachine> machineSet = new HashSet<>(perunMachines);
        cpuMap = new HashMap<>();

        ownerOrganisations = this.findOwnerOrganisations();
        for (OwnerOrganisation ownerOrganisation : ownerOrganisations) {
            int cpuSumOrg = 0;
            for (PerunComputingResource perunComputingResource : ownerOrganisation.getPerunComputingResources()) {
                if (perunComputingResource.isCluster()) {
                    int cpuSumCluster = 0;
                    for (PerunMachine perunMachine : perunComputingResource.getPerunMachines()) {
                        cpuSumOrg += perunMachine.getCpuNum();
                        cpuSumCluster += perunMachine.getCpuNum();
                        machineSet.remove(perunMachine);
                    }
                    cpuMap.put(perunComputingResource.getId(), cpuSumCluster);
                } else {
                    PerunMachine perunMachine = perunComputingResource.getPerunMachine();
                    if (perunMachine == null) {
                        log.error("computing resource " + perunComputingResource.getName() + " from OwnerOrganisation " + ownerOrganisation.getId() + " has no PerunMachine");
                    } else {
                        cpuSumOrg += perunMachine.getCpuNum();
                        machineSet.remove(perunMachine);
                        cpuMap.put(perunComputingResource.getId(), perunMachine.getCpuNum());
                    }
                }
            }
            cpuMap.put(ownerOrganisation.getId(), cpuSumOrg);
        }

        remaining = new ArrayList<>(machineSet);
        remaining.sort(PerunMachine.NAME_COMPARATOR);

        int cpuSum = 0;
        for (PerunMachine s : remaining) {
            cpuSum += s.getCpuNum();
        }
        cpuMap.put("remaining", cpuSum);
        cpuSum = 0;

        for (PerunMachine s : perunMachines) {
            cpuSum += s.getCpuNum();

        }
        cpuMap.put("all", cpuSum);
        return new PhysicalMachines(ownerOrganisations, remaining, cpuMap);
    }


}
