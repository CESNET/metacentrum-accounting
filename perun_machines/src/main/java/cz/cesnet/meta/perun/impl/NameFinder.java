package cz.cesnet.meta.perun.impl;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public abstract class NameFinder {

    private final Set<String> names;

    protected NameFinder(Set<String> names) {
        this.names = names;
    }

    protected boolean found(String name) {
        return names.contains(name);
    }

    public Set<String> getNames() {
        return names;
    }
}
