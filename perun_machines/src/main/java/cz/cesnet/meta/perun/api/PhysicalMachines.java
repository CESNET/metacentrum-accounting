package cz.cesnet.meta.perun.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class PhysicalMachines {

    final static Logger log = LoggerFactory.getLogger(PhysicalMachines.class);

    List<OwnerOrganisation> ownerOrganisations;
    List<PerunMachine> remaining;
    Map<String, Integer> cpuMap;

    public PhysicalMachines(List<OwnerOrganisation> ownerOrganisations, List<PerunMachine> remaining, Map<String, Integer> cpuMap) {
        this.ownerOrganisations = ownerOrganisations;
        this.remaining = remaining;
        this.cpuMap = cpuMap;
    }

    public List<OwnerOrganisation> getOwnerOrganisations() {
        return ownerOrganisations;
    }

    public List<PerunMachine> getRemaining() {
        return remaining;
    }

    public Map<String, Integer> getCpuMap() {
        return cpuMap;
    }


}
