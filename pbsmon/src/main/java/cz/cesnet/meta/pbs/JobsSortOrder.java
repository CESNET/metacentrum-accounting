package cz.cesnet.meta.pbs;

/**
 * Řazení úloh.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: JobsSortOrder.java,v 1.2 2014/03/05 14:50:15 makub Exp $
 */
public enum JobsSortOrder {
    Id, CPU, ReservedMemTotal, UsedMem, Name, User, State, CPUTime, WallTime, Queue, Ctime
}

