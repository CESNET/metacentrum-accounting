package cz.cesnet.meta.pbs;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: JobsInfo.java,v 1.2 2014/03/05 14:50:15 makub Exp $
 */
public class JobsInfo {
    private Map<String, Integer> stavy;
    private Map<String, Integer> poctyCpu;
    private int celkemJobs;
    private int celkemCpu;

    private int jobsInStateQ;
    private int jobsInStateR;
    private int jobsInStateC;
    private int jobsInStateOther;
    private int cpusInStateQ;
    private int cpusInStateR;
    private int cpusInStateC;
    private int cpusInStateOther;

    public JobsInfo(List<Job> jobs) {
        stavy = new HashMap<>();
        poctyCpu = new HashMap<>();
        celkemJobs = 0;
        celkemCpu = 0;
        for (Job job : jobs) {
            JobState state = JobState.valueOf(job.getState());
            PbsUtils.updateCount(stavy, state.name(), 1);
            switch (state) {
                case Q: jobsInStateQ++; break;
                case R: jobsInStateR++; break;
                case C:
                case F:
                    jobsInStateC++; break;
                default: jobsInStateOther++;
            }
            int cpu = job.getNoOfUsedCPU();
            PbsUtils.updateCount(poctyCpu, state.name(), cpu);
            switch (state) {
                case Q: cpusInStateQ+=cpu; break;
                case R: cpusInStateR+=cpu; break;
                case C:
                case F:
                    cpusInStateC+=cpu; break;
                default: cpusInStateOther+=cpu;
            }
            celkemJobs++;
            celkemCpu += cpu;
        }
    }

    public Map<String, Integer> getStavy() {
        return stavy;
    }

    public Map<String, Integer> getPoctyCpu() {
        return poctyCpu;
    }

    public int getCelkemJobs() {
        return celkemJobs;
    }

    public int getCelkemCpu() {
        return celkemCpu;
    }

    public int getJobsInStateQ() {
        return jobsInStateQ;
    }

    public int getJobsInStateR() {
        return jobsInStateR;
    }

    /**
     * C in Torque or F in PBSPro.
     * @return number of  finished/completed jobs
     */
    public int getJobsInStateC() {
        return jobsInStateC;
    }

    public int getJobsInStateOther() {
        return jobsInStateOther;
    }

    public int getCpusInStateQ() {
        return cpusInStateQ;
    }

    public int getCpusInStateR() {
        return cpusInStateR;
    }

    public int getCpusInStateC() {
        return cpusInStateC;
    }

    public int getCpusInStateOther() {
        return cpusInStateOther;
    }
}
