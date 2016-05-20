package cz.cesnet.meta.perun.api;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: VyhledavacFrontendu.java,v 1.2 2011/10/27 14:19:55 makub Exp $
 */
public interface VyhledavacFrontendu {

    public boolean jeStrojFrontend(String dlouheJmeno);

    public Set<String> getMnozinaJmen();
}
