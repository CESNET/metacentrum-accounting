package cz.cesnet.meta.acct.hw.perun;

import cz.cesnet.meta.acct.hw.Perun;
import cz.cesnet.meta.perun.api.PerunUser;
import cz.cesnet.meta.perun.api.Stroj;
import cz.cesnet.meta.perun.api.VypocetniCentrum;
import cz.cesnet.meta.perun.api.VypocetniZdroj;
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
        return this.perunJson.getVyhledavacFrontendu().getMnozinaJmen();
    }

    @Override
    public Set<String> getReservedMachinesNames() {
        return this.perunJson.getVyhledavacVyhrazenychStroju().getMnozinaJmen();
    }

    @Override
    public List<PerunUser> getAllUsers() {
        return this.perunJson.getAllUsers();
    }

    @Override
    public List<ComputingResource> getComputingResources() {
        List<ComputingResource> resources = new ArrayList<ComputingResource>();
        for (VypocetniCentrum centrum : perunJson.najdiVypocetniCentra()) {
            for (VypocetniZdroj zdroj : centrum.getZdroje()) {
                ComputingResource cr = new ComputingResource(zdroj.getId(), zdroj.getNazev(), zdroj.isCluster());
                log.debug("zdroj =  {}", zdroj);
                if (zdroj.isCluster()) {
                    List<Stroj> stroje = zdroj.getStroje();
                    List<Machine> machines = new ArrayList<>(stroje.size());
                    for (Stroj stroj : stroje) {
                        machines.add(new Machine(stroj.getName(), stroj.getCpuNum()));
                    }
                    cr.setMachines(machines);
                } else {
                    Stroj stroj = zdroj.getStroj();
                    cr.setMachine(new Machine(stroj.getName(), stroj.getCpuNum()));
                }
                resources.add(cr);
            }
        }
        return resources;
    }

}
