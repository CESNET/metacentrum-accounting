package cz.cesnet.meta.perun.api;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: VyhledavacVyhrazenychStroju.java,v 1.2 2011/10/27 14:19:55 makub Exp $
 */
public interface VyhledavacVyhrazenychStroju {

    public boolean jeStrojVyhrazeny(Stroj stroj);

    public Set<String> getMnozinaJmen();
}
