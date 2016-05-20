package cz.cesnet.meta.acct.hw.stats;

import java.sql.Date;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: WorkloadRecord.java,v 1.1 2009/12/11 17:37:58 makub Exp $
 */
public class WorkloadRecord {

    Date date;
    int cpuCount;
    long allCpuTime;
    long maintenanceTime;
    long queueReservedTime;
    long perunReservedTime;
    long jobsTime;
    double maintenanceRatio;
    double reservedRatio;
    double utilizationRatio;
    double rawRatio;

    public WorkloadRecord(Date date, int cpuCount, long allCpuTime, long maintenanceTime, long queueReservedTime, long perunReservedTime, long jobsTime, double maintenanceRatio, double reservedRatio, double utilizationRatio, double rawRatio) {
        this.date = date;
        this.cpuCount = cpuCount;
        this.allCpuTime = allCpuTime;
        this.maintenanceTime = maintenanceTime;
        this.queueReservedTime = queueReservedTime;
        this.perunReservedTime = perunReservedTime;
        this.jobsTime = jobsTime;
        this.maintenanceRatio = maintenanceRatio;
        this.reservedRatio = reservedRatio;
        this.utilizationRatio = utilizationRatio;
        this.rawRatio = rawRatio;
    }

    public Date getDate() {
        return date;
    }

    public int getCpuCount() {
        return cpuCount;
    }

    public long getAllCpuTime() {
        return allCpuTime;
    }

    public long getMaintenanceTime() {
        return maintenanceTime;
    }

    public long getQueueReservedTime() {
        return queueReservedTime;
    }

    public long getPerunReservedTime() {
        return perunReservedTime;
    }

    public long getJobsTime() {
        return jobsTime;
    }

    public double getMaintenanceRatio() {
        return maintenanceRatio;
    }

    public double getReservedRatio() {
        return reservedRatio;
    }

    public double getUtilizationRatio() {
        return utilizationRatio>100d?100d:utilizationRatio;
    }

    public double getRawRatio() {
        return rawRatio;
    }

    public double getJobsRatio() {
        if(allCpuTime==0) return 0;
        return (((double)jobsTime) / (double)allCpuTime)*100d;
    }
}
