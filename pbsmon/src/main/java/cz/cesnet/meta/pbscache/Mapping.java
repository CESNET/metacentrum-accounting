package cz.cesnet.meta.pbscache;

import java.util.List;
import java.util.Map;

/**
 * JavaBean utvořený tak, aby ho JSON Tools mohly rovnou použít.
 */
public class Mapping {

    private Map<String, String> virtual2physical;
    private Map<String, List<String>> physical2virtual;

    public Map<String, String> getVirtual2physical() {
        return this.virtual2physical;
    }

    public void setVirtual2physical(Map<String, String> virtual2physical) {
        this.virtual2physical = virtual2physical;
    }

    public Map<String, List<String>> getPhysical2virtual() {
        return this.physical2virtual;
    }

    public void setPhysical2virtual(Map<String, List<String>> physical2virtual) {
        this.physical2virtual = physical2virtual;
    }
}