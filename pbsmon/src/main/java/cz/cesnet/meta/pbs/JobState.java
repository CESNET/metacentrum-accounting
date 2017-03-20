package cz.cesnet.meta.pbs;

/**
 * Enum of job states to provide sorting order.
 * <p>
 * B Array job: at least one subjob has started.
 * E Job is exiting after having run.
 * F Job is finished.
 * H Job is held.
 * M Job was moved to another server.
 * Q Job is queued.
 * R Job is running.
 * S Job is suspended.
 * T Job is being moved to new location.
 * U Cycle-harvesting job is suspended due to keyboard activity.
 * W Job is waiting for its submitter-assigned start time to be reached.
 * X Subjob has completed execution or has been deleted.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public enum JobState {
    Q, R, E, C, W, H, T, S, X, F, B, M, U
}

