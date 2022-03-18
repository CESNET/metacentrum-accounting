package cz.cesnet.meta.stripes;

import cz.cesnet.meta.acct.Accounting;
import cz.cesnet.meta.pbs.*;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: JobsActionBean.java,v 1.33 2014/09/11 11:50:56 makub Exp $
 */
@UrlBinding("/jobs/{$event}/{trideni}")
public class JobsActionBean extends BaseActionBean {

    @SpringBean("accounting")
    protected Accounting accounting;

    final static Logger log = LoggerFactory.getLogger(JobsActionBean.class);
    final static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final long MILLIS_IN_HOUR = 3600000l;
    private static final long MILLIS_IN_15MINS = 10 * 60 * 1000;
    private static final int INTERVALS = 24 * 4;
    int[] runningJobsCountersMins = new int[INTERVALS];
    int[] queuedJobsCountersMins = new int[INTERVALS];
    int[] runningCPUsCountersMins = new int[INTERVALS];
    int[] queuedCPUsCountersMins = new int[INTERVALS];
    int[] completedCountersMins = new int[INTERVALS];
    int[] startedCountersMins = new int[INTERVALS];
    int[] createdCountersMins = new int[INTERVALS];
    Map<Integer, Integer> jobsCPU = new TreeMap<Integer, Integer>();
    Map<Integer, Integer> waitingHoursJobs = new HashMap<Integer, Integer>();
    LinkedHashMap<String, String> warnings;
    Map<String, Job> suspiciousJobs;
    //parametr
    private String trideni;
    //data k zobrazeni
    private List<Job> jobs;
    private JobsInfo jobsInfo;
    private String user;
    private boolean showWarnings = false;

    @SuppressWarnings({"UnusedDeclaration"})
    public String getUser() {
        return user;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setUser(String user) {
        this.user = user;
    }

    @DefaultHandler
    public Resolution detail() {
        jobsInfo = pbsky.getJobsInfo();

        long nowtime = System.currentTimeMillis();
        //kontrola
        int runningJobs = 0;
        int runningCPUs = 0;
        int queuedJobs = 0;
        int queuedCPUs = 0;
        int holdJobs = 0;
        int holdCPUs = 0;
        warnings = new LinkedHashMap<>(40);
        suspiciousJobs = new HashMap<>(40);
        //ziskame ulohy
        for (PBS pbs : pbsky.getListOfPBS()) {
            for (Job job : pbs.getJobsById()) {
                //pocty CPU uloh
                PbsUtils.updateCount(jobsCPU, job.getNoOfUsedCPU(), 1);

                //vytvorene ulohy
                Date created = job.getTimeCreated();
                if (created == null) {
                    String w = "Úloha nemá ctime !";
                    warnings.put(job.getId(), w);
                    suspiciousJobs.put(job.getId(), job);
                    continue;
                } else {
                    int mins = (int) ((nowtime - created.getTime()) / MILLIS_IN_15MINS);
                    if (mins < INTERVALS) {
                        createdCountersMins[mins]++;
                    }

                }
                //spustene ulohy
                Date started = job.getTimeStarted();
                if (started != null) {
                    int mins = (int) ((nowtime - started.getTime()) / MILLIS_IN_15MINS);
                    if (mins < INTERVALS) {
                        startedCountersMins[mins]++;
                    }
                }
                //doba cekani uloh
                //je treba brat od etime, protoze od toho casu teprve mohla byt uloha spustena
                //rozdil etime oproti ctime je pro ulohy ve stavu H - cekajici na jine ulohy
                String jobState = job.getState();
                Date eligible = job.getTimeEligible();
                if (eligible != null) {
                    int cekala_hodin;
                    if (started != null) {
                        cekala_hodin = (int) ((started.getTime() - eligible.getTime()) / MILLIS_IN_HOUR);
                    } else {
                        cekala_hodin = (int) ((nowtime - eligible.getTime()) / MILLIS_IN_HOUR);
                    }
                    if (cekala_hodin > 1000) {
                        String w = "Úloha čekala už " + cekala_hodin + " hodin.";
                        warnings.put(job.getId(), w);
                        suspiciousJobs.put(job.getId(), job);
                    }
                    PbsUtils.updateCount(waitingHoursJobs, cekala_hodin, 1);
                }

                //dokoncene ulohy
                Date comp = null;
                if ("C".equals(jobState)||"F".equals(jobState)) {
                    comp = job.getTimeCompleted();
                    if (comp != null) {
                        //ulohy ve stavu C bez casu dokonceni nebyly ani spusteny
                        int mins = (int) ((nowtime - comp.getTime()) / MILLIS_IN_15MINS);
                        if(mins>=0) {
                            if (mins < INTERVALS) {
                                completedCountersMins[mins]++;
                            }
                        } else {
                            log.warn("mins={} for job {}",mins,job.getId());
                        }
                    }
                }

                //prepocitani pro grafy minulosti
                //bezici a cekajici v urcitych casech
                for (int i = 0; i < INTERVALS; i++) {
                    //casovyokamzik, pro ktery pocitame
                    long time = nowtime - (i * MILLIS_IN_15MINS);
                    if (comp != null && comp.getTime() < time) {
                        continue; //uz byla dokoncena, nepocitat
                    }
                    if (started != null && started.getTime() < time) {
                        runningJobsCountersMins[i]++;
                        runningCPUsCountersMins[i] += job.getNoOfUsedCPU();
                        continue; //uz byla spustena, bezela
                    }
                    if (eligible != null && eligible.getTime() < time) {
                        queuedJobsCountersMins[i]++;
                        queuedCPUsCountersMins[i] += job.getNoOfUsedCPU();
                        //byla vytvorena, cekala

                    }
                }
                //kontrola na walltime remaining
                if ("R".equals(jobState) || "E".equals(jobState)) {
                    Duration walltimeRemaining = job.getWalltimeRemaining();
                    if (walltimeRemaining == null) {
                        String w = "Úloha je ve stavu " + jobState + " a nemá atribut Walltime.Remaining ";
                        warnings.put(job.getId(), w);
                        suspiciousJobs.put(job.getId(), job);
                    } else if (walltimeRemaining.isNegative()) {
                        Date expectedEnd = job.getTimeExpectedEnd();
                        String w = "Úloha je ve stavu " + jobState + " a má zápornou hodnotu atributu Walltime.Remaining, měla skončit "
                                + (expectedEnd != null ? sdf.format(expectedEnd) : "???");
                        warnings.put(job.getId(), w);
                        suspiciousJobs.put(job.getId(), job);
                    }
                }
                //kontroly na stav neodpovidajici nastavenym casum
                if (comp != null && comp.getTime() < nowtime) {
                    //má čas dokončení, měla by být ve stavu C
                    if (!("C".equals(jobState)||"F".equals(jobState))) {
                        String w = "Úloha má být ve stavu C/F (protože má comp_time), ale je ve stavu " + jobState + ".";
                        warnings.put(job.getId(), w);
                        suspiciousJobs.put(job.getId(), job);
                    }
                } else if (started != null && started.getTime() < nowtime) {
                    //nemá čas dokončení, má čas startu, měla by být R nebo E
                    if (!"R".equals(jobState)) {
                        Date modified = job.getTimeModified();
                        if ("E".equals(jobState) && modified != null) {
                            if (modified.getTime() < nowtime - MILLIS_IN_HOUR) {

                                String w = "Úloha je ve stavu E už moc dlouho, od " + sdf.format(modified);
                                warnings.put(job.getId(), w);
                                suspiciousJobs.put(job.getId(), job);
                            }
                        } else {
                            String w = "Úloha má být ve stavu R (protože má start_time a nemá comp_time), ale je ve stavu " + jobState;
                            warnings.put(job.getId(), w);
                            suspiciousJobs.put(job.getId(), job);
                        }
                    }
                    runningJobs++;
                    runningCPUs += job.getNoOfUsedCPU();
                } else if (eligible != null && eligible.getTime() < nowtime) {
                    //nemá čas dokončení ani startu, má čas eligible (způsobilosti k běhu), měla by být Q
                    if ("Q".equals(jobState)) {
                        queuedJobs++;
                        queuedCPUs += job.getNoOfUsedCPU();
                    } else {
                        if ("C".equals(jobState)||"F".equals(jobState)) {
                            //byla deleted, t je v pořádku
                        } else {
                            String cas = "";
                            if (job.getTimeModified() != null) {
                                cas = " od " + sdf.format(job.getTimeModified());
                            }
                            String w = "Úloha má být ve stavu Q nebo C (protože má etime a nemá comp_time ani start_time), ale je ve stavu " + jobState + cas;
                            warnings.put(job.getId(), w);
                            suspiciousJobs.put(job.getId(), job);
                        }
                    }
                } else if (created.getTime() < nowtime) {
                    //T - Transported/přesunována na stroj, stav mezi Q a R, typicky čekání na postavení virtuálního stroje
                    //W - waiting/čeká na stagin/stageout nebo než nastane okamžik nastavený v Execution_Time
                    if (!"H".equals(jobState)) {
                        if ("W".equals(jobState)) {
                            if (job.getExecutionTime() == null) {
                                String cas = "";
                                if (job.getTimeModified() != null) {
                                    cas = " od " + sdf.format(job.getTimeModified());
                                }
                                String w = "Úloha je ve stavu W, ale nemá Execution_Time a čeká už od " + cas;
                                warnings.put(job.getId(), w);
                                suspiciousJobs.put(job.getId(), job);
                            } else if (job.getExecutionTime().getTime() <= nowtime) {
                                String w = "Úloha je ve stavu W, ale Execution_Time je v minulosti (" + sdf.format(job.getExecutionTime()) + ")";
                                warnings.put(job.getId(), w);
                                suspiciousJobs.put(job.getId(), job);
                            }
                        } else {
                            String cas = "";
                            if (job.getTimeModified() != null) {
                                cas = " od " + sdf.format(job.getTimeModified());
                            }
                            String w = "Úloha má být ve stavu H (protože nemá etime), ale je ve stavu " + jobState + cas;
                            warnings.put(job.getId(), w);
                            suspiciousJobs.put(job.getId(), job);
                        }
                    }
                    //uz existovala, ale jeste nebyla eligible, takze byla nejspis H-Hold nebo W-waiting
                    holdJobs++;
                    holdCPUs += job.getNoOfUsedCPU();
                }
            }
        }
//        log.warn("Spocitano:");
//        log.warn("Hold: " + holdJobs + "/" + holdCPUs);
//        log.warn("Queued: " + queuedJobs + "/" + queuedCPUs);
//        log.warn("Running: " + runningJobs + "/" + runningCPUs);
//        log.warn("Ma byt:");
//        log.warn("Hold:" + jobsInfo.getStavy().get("H") + "/" + jobsInfo.getPoctyCpu().get("H"));
//        log.warn("Queued:" + jobsInfo.getStavy().get("Q") + "/" + jobsInfo.getPoctyCpu().get("Q"));
//        log.warn("Running:" + jobsInfo.getStavy().get("R") + "/" + jobsInfo.getPoctyCpu().get("R"));
        return new ForwardResolution("/jobs/show.jsp");
    }

    public int[] getRunningJobsCountersMins() {
        return runningJobsCountersMins;
    }

    public int[] getQueuedJobsCountersMins() {
        return queuedJobsCountersMins;
    }

    public int[] getRunningCPUsCountersMins() {
        return runningCPUsCountersMins;
    }

    public int[] getQueuedCPUsCountersMins() {
        return queuedCPUsCountersMins;
    }

    public int[] getCompletedCountersMins() {
        return completedCountersMins;
    }

    public int[] getStartedCountersMins() {
        return startedCountersMins;
    }

    public int[] getCreatedCountersMins() {
        return createdCountersMins;
    }

    public Map<Integer, Integer> getJobsCPU() {
        return jobsCPU;
    }

    public Map<Integer, Integer> getWaitingHoursJobs() {
        return waitingHoursJobs;
    }

    public Map<String, Job> getSuspiciousJobs() {
        return suspiciousJobs;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public Resolution my() throws UnsupportedEncodingException {
        HttpServletRequest request = ctx.getRequest();
        HttpSession session = request.getSession(true);
        if (user != null) {
            //explicitně zadaný uživatel, nastavit
            session.setAttribute(PersonActionBean.PERSON, user);
        } else {
            user = (String) session.getAttribute(PersonActionBean.PERSON);
            if (user == null) {
                String remoteUser = request.getRemoteUser();
                log.info("REMOTE_USER {}", remoteUser);
                if(remoteUser==null||remoteUser.isEmpty()) {
                    return new ErrorResolution(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "REMOTE_USER not set");
                }
                user = remoteUser;
                session.setAttribute(PersonActionBean.PERSON, user);
            }
        }
        return new RedirectResolution(UserActionBean.class).addParameter("userName", user);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public Resolution allJobs() {
        if (trideni == null) trideni = JobsSortOrder.Id.toString();
        jobsInfo = pbsky.getJobsInfo();
        jobs = pbsky.getSortedJobs(JobsSortOrder.valueOf(trideni));
        return new ForwardResolution("/jobs/jobs.jsp");
    }

    public Resolution missingStarted() {
        HashSet<String> startedJobIdsSet = new HashSet<>(accounting.getStartedJobIds());
        for (PBS pbs : pbsky.getListOfPBS()) {
            for (Job job : pbs.getJobsById()) {
                startedJobIdsSet.remove(job.getId());
            }
        }
        return (req, resp) -> {
            resp.setContentType("text/plain");
            PrintWriter out = resp.getWriter();
            startedJobIdsSet.stream().forEach(out::println);
            out.close();
        };
    }

    public String getTrideni() {
        return trideni;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setTrideni(String trideni) {
        this.trideni = trideni;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public JobsInfo getJobsInfo() {
        return jobsInfo;
    }

    public boolean isShowWarnings() {
        return showWarnings;
    }

    public void setShowWarnings(boolean showWarnings) {
        this.showWarnings = showWarnings;
    }

    public LinkedHashMap<String, String> getWarnings() {
        return warnings;
    }
}
