package cz.cesnet.meta.acct.hw.perun;

import cz.cesnet.meta.acct.hw.Perun;
import cz.cesnet.meta.perun.api.PerunMachine;
import cz.cesnet.meta.perun.api.PerunUser;
import cz.cesnet.meta.perun.api.OwnerOrganisation;
import cz.cesnet.meta.perun.api.PerunComputingResource;
import cz.cesnet.meta.perun.impl.PerunJsonImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Používá PerunJsonImpl pro načtení JSON souboru generovaného Perunem. Sdílí s pbsmon2 třídy ve fyzstroje.jar.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id:$
 */
public class PerunHwJsonImpl implements Perun {

    final static Logger log = LoggerFactory.getLogger(PerunHwJsonImpl.class);
    
    PerunJsonImpl perunJson;

    public void setPerunJson(PerunJsonImpl perunJson) {
        this.perunJson = perunJson;
    }

    @Override
    public Set<String> getFrontendNames() {
        return this.perunJson.getFrontendFinder().getNames();
    }

    @Override
    public Set<String> getReservedMachinesNames() {
        return this.perunJson.getReservedMachinesFinder().getNames();
    }

    @Override
    public List<PerunUser> getAllUsers() {
        return this.perunJson.getAllUsers();
    }

    @Override
    public List<ComputingResource> getComputingResources() {
        List<ComputingResource> resources = new ArrayList<ComputingResource>();
        for (OwnerOrganisation centrum : perunJson.findOwnerOrganisations()) {
            for (PerunComputingResource zdroj : centrum.getPerunComputingResources()) {
                ComputingResource cr = new ComputingResource(zdroj.getId(), zdroj.getName(), zdroj.isCluster());
                log.debug("zdroj =  {}", zdroj);
                if (zdroj.isCluster()) {
                    List<PerunMachine> stroje = zdroj.getPerunMachines();
                    List<Machine> machines = new ArrayList<>(stroje.size());
                    for (PerunMachine perunMachine : stroje) {
                        machines.add(new Machine(perunMachine.getName(), perunMachine.getCpuNum()));
                    }
                    cr.setMachines(machines);
                } else {
                    PerunMachine perunMachine = zdroj.getPerunMachine();
                    cr.setMachine(new Machine(perunMachine.getName(), perunMachine.getCpuNum()));
                }
                resources.add(cr);
            }
        }
        return resources;
    }

}
