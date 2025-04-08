package cz.cesnet.meta.perun.api;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public interface FrontendFinder {

    public boolean isFrontend(String longName);

    public Set<String> getNames();
}
