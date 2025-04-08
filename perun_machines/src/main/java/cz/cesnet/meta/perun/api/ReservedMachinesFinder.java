package cz.cesnet.meta.perun.api;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public interface ReservedMachinesFinder {

    boolean isMachineReserved(PerunMachine perunMachine);

    Set<String> getNames();
}
