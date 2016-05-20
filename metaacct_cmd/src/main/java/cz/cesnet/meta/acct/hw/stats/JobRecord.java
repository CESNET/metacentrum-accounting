package cz.cesnet.meta.acct.hw.stats;

import java.sql.Timestamp;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: JobRecord.java,v 1.1 2009/12/11 17:37:58 makub Exp $
 */
public class JobRecord {
    String job;
    String host;
    int cpuNum;
    Timestamp start;
    Timestamp end;

    public JobRecord(String job, String host, int cpuNum,Timestamp start, Timestamp end) {
        this.job = job;
        this.host = host;
        this.cpuNum = cpuNum;
        this.start = start;
        this.end = end;
    }

    public String getJob() {
        return job;
    }

    public String getHost() {
        return host;
    }

    public int getCpuNum() {
        return cpuNum;
    }

    public Timestamp getStart() {
        return start;
    }

    public Timestamp getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "JobRecord{" +
                "job='" + job + '\'' +
                ", host='" + host + '\'' +
                ", cpuNum=" + cpuNum +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}
