package cz.cesnet.meta.stripes;

import cz.cesnet.meta.pbs.Job;
import cz.cesnet.meta.pbs.Node;
import cz.cesnet.meta.pbs.PbsUtils;
import cz.cesnet.meta.pbs.Pbsky;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.integration.spring.SpringBean;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: JobActionBean.java,v 1.7 2014/12/10 15:42:37 makub Exp $
 */
@UrlBinding("/job/{jobName}")
public class JobActionBean extends BaseActionBean {

    @SpringBean("pbsky")
    protected Pbsky pbsky;

    //parametr
    private String jobName;
    //data
    private Job job;
    private Map<String, Node> nodesMap;

    @DefaultHandler
    public Resolution detail() {
        if (jobName == null || jobName.length() == 0) {
            return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND, "Job name is needed in the URL.");
        }
        job = pbsky.getJobByName(jobName);
        if (job != null) {
            if (job.getExecHostFirst() != null) {
                nodesMap = new HashMap<String, Node>();
                String firstHostName = job.getExecHostFirstName();
                nodesMap.put(firstHostName, job.getPbs().getNodes().get(firstHostName));

                for (String more : job.getExecHostMore()) {
                    String hostName = PbsUtils.substringBefore(more, '/');
                    nodesMap.put(hostName, job.getPbs().getNodes().get(hostName));
                }
            }
            List<Job.NodeSpec> nodeSpecs = job.getScheduledNodeSpecs();
            if(nodeSpecs!=null) {
                if(nodesMap==null) nodesMap = new HashMap<>();
                for (Job.NodeSpec nodeSpec : nodeSpecs) {
                    String hostname = nodeSpec.getHostname();
                    nodesMap.put(hostname,job.getPbs().getNodes().get(hostname));
                }
            }
        }
        return new ForwardResolution("/jobs/job.jsp");
    }


    public Map<String, Node> getNodesMap() {
        return nodesMap;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Job getJob() {
        return job;
    }
}
