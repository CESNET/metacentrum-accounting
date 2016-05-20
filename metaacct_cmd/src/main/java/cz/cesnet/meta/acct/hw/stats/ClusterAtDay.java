package cz.cesnet.meta.acct.hw.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: ClusterAtDay.java,v 1.2 2009/12/11 17:37:58 makub Exp $
 */
public class ClusterAtDay {

    final static Logger log = LoggerFactory.getLogger(ClusterAtDay.class);

    //jmeno resource
    String cluster;
    Timestamp date;
    long dateAsLinuxTime;
    Timestamp nextDay;
    long nextDayAsLinuxTime;
    int cpuCount;
    //casy v cpu-milisekundach
    private long maintenanceCpuTime;
    private long reservedCpuTime;
    private long perunReservedCpuTime;
    private long periodMilliseconds;
    //cas v cpu-sekundach
    private long jobsTime;


    public ClusterAtDay(String cluster, Calendar calendar) {
        calendar = (Calendar) calendar.clone();
        this.cluster = cluster;
        date = new Timestamp(calendar.getTimeInMillis());
        calendar.add(Calendar.DATE, 1);
        nextDay = new Timestamp(calendar.getTimeInMillis());
        periodMilliseconds = nextDay.getTime() - date.getTime();
        dateAsLinuxTime = date.getTime() / 1000l;
        nextDayAsLinuxTime = nextDay.getTime() / 1000l;
    }

    public long getAllCpuTime() {
        //prechodne dny jsou jine nez 24h!
        return cpuCount * periodMilliseconds / 1000l; //den v sekundach
    }

    public void setCpuCount(int cpuCount) {
        this.cpuCount = cpuCount;
    }

    public String getCluster() {
        return cluster;
    }


    public int getCpuCount() {
        return cpuCount;
    }

    public long getDateAsLinuxTime() {
        return dateAsLinuxTime;
    }

    public long getNextDayAsLinuxTime() {
        return nextDayAsLinuxTime;
    }

    public Timestamp getDate() {
        return date;
    }

    public Timestamp getNextDay() {
        return nextDay;
    }

    public void setMaintenanceCpuTime(long maintenanceCpuTime) {
        this.maintenanceCpuTime = maintenanceCpuTime;
    }

    public void setReservedCpuTime(long reservedCpuTime) {
        this.reservedCpuTime = reservedCpuTime;
    }

    public long getMaintenanceCpuTime() {
        return maintenanceCpuTime / 1000l;
    }

    public long getReservedCpuTime() {
        return reservedCpuTime / 1000l;
    }

    public void setPerunReservedCpuTime(long perunReservedCpuTime) {
        this.perunReservedCpuTime = perunReservedCpuTime;
    }

    public long getPerunReservedCpuTime() {
        return perunReservedCpuTime / 1000l;
    }

    /**
     * součet ReservedCpu a ReservedPerun
     *
     * @return číslo
     */
    public long getReservedTime() {
        return getReservedCpuTime() + getPerunReservedCpuTime();
    }

    public long getJobsTime() {
        return jobsTime;
    }

    public void setJobsTime(long jobsTime) {
        this.jobsTime = jobsTime;
    }

    public double getUtilizationRatio() {
        long dostupne = getAllCpuTime() - getMaintenanceCpuTime();
        long vyuzite = getReservedCpuTime() + getPerunReservedCpuTime() + getJobsTime();
        double vyuziti;

        if (dostupne == 0) {
            if (vyuzite > 0) {
                log.warn("dostupne==0&&vyuzite>0 cluster:{} date:{}", cluster, date);
            }
            vyuziti = 0d;
        } else {
            vyuziti = (double) vyuzite / (double) dostupne;
        }
        return vyuziti;
    }


    public double getRawUtilizationRatio() {
        long dostupne = getAllCpuTime();
        long vyuzite = getPerunReservedCpuTime() + getJobsTime();
        double vyuziti;
        if (dostupne == 0) {
            vyuziti = 0d;
        } else {
            vyuziti = (double) vyuzite / (double) dostupne;
        }
        return vyuziti;
    }

    public double getMaintenanceRatio() {
        long allCpuTime = getAllCpuTime();
        long maintenanceSecs = getMaintenanceCpuTime();
        if (allCpuTime == 0) {
            if (maintenanceSecs > 0) throw new RuntimeException("allCpuTime==0&&manintenanceSecs>0");
            return 0;
        }
        return ((double) maintenanceSecs) / ((double) allCpuTime);
    }

    public double getReservedRatio() {
        long reservedTime = getReservedTime();
        long allCpuTime = getAllCpuTime();
        if (allCpuTime == 0) {
            if (reservedTime > 0) throw new RuntimeException("allCpuTime==0&&reservedTime>0");
            return 0;
        }
        return ((double) reservedTime) / ((double) allCpuTime);
    }

    public double getJobsRatio() {
        long allCpuTime = getAllCpuTime();
        if(allCpuTime==0) return 0;
        return ((double)getJobsTime()) / (double)allCpuTime;
    }

    @Override
    public String toString() {
        return "ClusterAtDay{" +
                "cluster='" + cluster + '\'' +
                ", date=" + date +
                ", maintenanceCpuTime=" + maintenanceCpuTime +
                ", reservedCpuTime=" + reservedCpuTime +
                ", perunReservedCpuTime=" + perunReservedCpuTime +
                '}';
    }

    public String getId() {
        return ""+cluster+" "+date;
    }
}
