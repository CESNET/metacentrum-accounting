package cz.cesnet.meta.perun.api;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public interface Perun {

    // primarni data

    Map<String, String> nactiVsechnyTexty(Locale locale);

    List<VypocetniCentrum> najdiVypocetniCentra();

    List<Stroj> getMetacentroveStroje();

    List<PerunUser> getAllUsers();

    /**
     * Vrací třídu, která rohoduje, zda má být daný stroj označen jako modrý protože je mimo PBS záměrně.
     * @return vyhledavac
     */
    VyhledavacVyhrazenychStroju getVyhledavacVyhrazenychStroju();

    VyhledavacFrontendu getVyhledavacFrontendu();


    //odvozena data

    PerunUser getUserByName(String userName);

    boolean isNodeVirtual(String nodeName);

    Stroj getStrojByName(String machineName);

    FyzickeStroje getFyzickeStroje();

    VypocetniZdroj getVypocetniZdrojByName(String zdrojName);

    boolean isNodePhysical(String nodeName);
}