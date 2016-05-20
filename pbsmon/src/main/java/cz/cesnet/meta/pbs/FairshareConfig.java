package cz.cesnet.meta.pbs;

/**
* Created with IntelliJ IDEA.
*
* @author Martin Kuba makub@ics.muni.cz
*/
public class FairshareConfig {

    private final String tree;
    private final String metrics;
    private final String id;

    public FairshareConfig(String tree, String metrics, String id) {
        this.tree = tree;
        this.metrics = metrics;
        this.id = id;
    }

    public String getTree() {
        return tree;
    }

    public String getMetrics() {
        return metrics;
    }

    public String getId() {
        return id;
    }
}
