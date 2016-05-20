package cz.cesnet.meta.perun.impl;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: VyhledavacJmen.java,v 1.1 2011/10/27 14:19:55 makub Exp $
 */
public abstract class VyhledavacJmen {

    private final Set<String> mnozinaJmen;

    protected VyhledavacJmen(Set<String> mnozinaJmen) {
        this.mnozinaJmen = mnozinaJmen;
    }

    protected boolean jeTam(String jmeno) {
        return mnozinaJmen.contains(jmeno);
    }

    public Set<String> getMnozinaJmen() {
        return mnozinaJmen;
    }
}
