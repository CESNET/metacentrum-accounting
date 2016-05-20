package cz.cesnet.meta.pbs;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: User.java,v 1.6 2014/03/05 17:03:53 makub Exp $
 */
public class User {

    private String name;
    private int jobsTotal;
    private int jobsStateQ;
    private int jobsStateR;
    private int jobsStateC;
    private int jobsOther;
    private int cpusTotal;
    private int cpusStateQ;
    private int cpusStateR;
    private int cpusStateC;
    private int cpusOther;
    private Map<String,Integer> fairShares = new HashMap<String, Integer>(3);

    public User(String name) {
        this.name = name;
    }

    /**
     * Sečte dva stejné uživatele do jednoho.
     *
     * @param u1 user
     * @param u2 user
     */
    public User(User u1, User u2) {
        if (u1 == null) throw new IllegalArgumentException("u1==null");
        if (u2 == null) throw new IllegalArgumentException("u2==null");
        if (!u1.name.equals(u2.name))
            throw new IllegalArgumentException("users " + u1.name + " and " + u2.name + " cannot be added together");
        name = u1.name;
        jobsTotal = u1.jobsTotal + u2.jobsTotal;
        jobsStateQ = u1.jobsStateQ + u2.jobsStateQ;
        jobsStateR = u1.jobsStateR + u2.jobsStateR;
        jobsStateC = u1.jobsStateC + u2.jobsStateC;
        jobsOther = u1.jobsOther + u2.jobsOther;
        cpusTotal = u1.cpusTotal + u2.cpusTotal;
        cpusStateQ = u1.cpusStateQ + u2.cpusStateQ;
        cpusStateR = u1.cpusStateR + u2.cpusStateR;
        cpusStateC = u1.cpusStateC + u2.cpusStateC;
        cpusOther = u1.cpusOther + u2.cpusOther;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getJobsTotal() {
        return jobsTotal;
    }

    public int getJobsStateQ() {
        return jobsStateQ;
    }

    public void incJobsStateQ() {
        this.jobsStateQ++;
        this.jobsTotal++;
    }

    public int getJobsStateR() {
        return jobsStateR;
    }

    public void incJobsStateR() {
        this.jobsStateR++;
        this.jobsTotal++;
    }

    public int getJobsStateC() {
        return jobsStateC;
    }

    public void incJobsStateC() {
        this.jobsStateC++;
        this.jobsTotal++;
    }

    public int getJobsOther() {
        return jobsOther;
    }

    public void incJobsOther() {
        this.jobsOther++;
        this.jobsTotal++;
    }

    public int getCpusTotal() {
        return cpusTotal;
    }

    public int getCpusStateQ() {
        return cpusStateQ;
    }

    public void addCpusStateQ(int cpusStateQ) {
        this.cpusStateQ += cpusStateQ;
        this.cpusTotal += cpusStateQ;
    }

    public int getCpusStateR() {
        return cpusStateR;
    }

    public void addCpusStateR(int cpusStateR) {
        this.cpusStateR += cpusStateR;
        this.cpusTotal += cpusStateR;
    }

    public int getCpusStateC() {
        return cpusStateC;
    }

    public void addCpusStateC(int cpusStateC) {
        this.cpusStateC += cpusStateC;
        this.cpusTotal += cpusStateC;
    }

    public int getCpusOther() {
        return cpusOther;
    }

    public void addCpusOther(int cpusOther) {
        this.cpusOther += cpusOther;
        this.cpusTotal += cpusOther;
    }

    public Integer getFairshareRank(String fairshareId) {
        return fairShares.get(fairshareId);
    }

    public void setFairshareRank(String fairshareId, Integer fairshareRank) {
        if (fairshareRank != null)
            this.fairShares.put(fairshareId,fairshareRank);
    }

    public Map<String, Integer> getFairshares() {
        return fairShares;
    }
}
