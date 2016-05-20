package cz.cesnet.meta.acct;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: UserInfo.java,v 1.2 2010/01/05 12:48:22 makub Exp $
 */
public class UserInfo {

    int jobCount;
    long cpuSecondsUsed;
    private List<Usage> usages;

    public UserInfo(int jobCount, long cpuSecondsUsed) {
        this.jobCount = jobCount;
        this.cpuSecondsUsed = cpuSecondsUsed;
    }

    public int getJobCount() {
        return jobCount;
    }

    public long getCpuSecondsUsed() {
        return cpuSecondsUsed;
    }

    public double getCpuDaysUsed() {
        return ((double)cpuSecondsUsed)/(86400d);
    }

    public void setUsages(List<Usage> usages) {
        this.usages = usages;
    }

    public List<Usage> getUsages() {
        return usages;
    }

    public static class Usage {
        int year;
        long cpuSeconds;
        long jobs;

        public Usage(int year,long jobs, long cpuSeconds) {
            this.year = year;
            this.jobs = jobs;
            this.cpuSeconds = cpuSeconds;
        }

        public int getYear() {
            return year;
        }

        public long getCpuSeconds() {
            return cpuSeconds;
        }

        public double getCpuDays() {
            return ((double)cpuSeconds)/(3600d*24d);
        }

        public long getJobs() {
            return jobs;
        }
    }
}
