package cz.cesnet.meta.pbs;

/**
 * Represents a scheduler in PBSPro.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class Scheduler extends PbsInfoObject {

    public Scheduler(PBS pbs, String name) {
        super(pbs, name);
    }
}
