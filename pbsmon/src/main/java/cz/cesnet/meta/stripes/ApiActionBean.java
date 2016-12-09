package cz.cesnet.meta.stripes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import cz.cesnet.meta.pbs.Node;
import cz.cesnet.meta.pbs.User;
import cz.cesnet.meta.pbscache.Mapping;
import cz.cesnet.meta.perun.api.FyzickeStroje;
import cz.cesnet.meta.perun.api.Perun;
import cz.cesnet.meta.perun.api.PerunUser;
import cz.cesnet.meta.perun.api.Stroj;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@UrlBinding("/api/{$event}")
public class ApiActionBean extends BaseActionBean {

    final static Logger log = LoggerFactory.getLogger(ApiActionBean.class);

    @SpringBean("perun")
    protected Perun perun;

    private List<String> users;
    private boolean pretty = false;

    public Resolution machines() {
        List<Stroj> vsechnyStroje = perun.getMetacentroveStroje();
        List<String> pbsNodeNames = new ArrayList<>(vsechnyStroje.size()*2);
        Mapping mapping  = NodesActionBean.makeUnifiedMapping(this.pbsCache, this.cloud);
        for (Stroj s : vsechnyStroje) {
            String strojName = s.getName();
            //PBs uzel primo na fyzickem - nevirtualizovane
            Node pbsNode = pbsky.getNodeByFQDN(strojName);
            if (pbsNode != null && !pbsNode.isDown()) {
                pbsNodeNames.add(strojName);
            }
            //virtualni podle mappingu
            List<String> virtNames = mapping.getPhysical2virtual().get(strojName);
            if (virtNames != null) {
                for (String virtName : virtNames) {
                    Node vn = pbsky.getNodeByFQDN(virtName);
                    if (vn != null && !vn.isDown()) {
                        pbsNodeNames.add(virtName);
                    }
                }
            }
        }
        Collections.sort(pbsNodeNames);
        return (request, response) -> {
            response.setContentType("text/plain;charset=utf-8");
            PrintWriter out = response.getWriter();
            for (String pbsNode : pbsNodeNames) {
                out.println(pbsNode);
            }
            out.close();
        };
    }

    @DefaultHandler
    public Resolution users() {
        log.debug("users() users={}", users);
        if (users == null || users.isEmpty()) {
            return new ErrorResolution(HttpServletResponse.SC_BAD_REQUEST, "Specify users parameter (multivalued, i.e. users=joe&users=john");
        }
        final Map<String,Map<String,Object>> usersData = new HashMap<String, Map<String, Object>>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (String userName : users) {
            Map<String, Object> userData = new HashMap<String, Object>();
            try {
                PerunUser perunUser = perun.getUserByName(userName);
                usersData.put(perunUser.getLogname(), userData);
                userData.put("logname", perunUser.getLogname());
                userData.put("fullname", perunUser.getName());
                userData.put("expires", sdf.format(perunUser.getExpires()));
            } catch (Exception ex) {
                log.warn("Nemohu nacist PerunUser {} kvuli {} ", userName, ex.getMessage());
                return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND, "User " + userName + " not found.");
            }
            User user = pbsky.getUserByName(userName);
            if (user != null) {
                Map<String,Object> jobs = new HashMap<String,Object>();
                userData.put("jobs", jobs);
                jobs.put("total", user.getJobsTotal());
                jobs.put("stateQ", user.getJobsStateQ());
                jobs.put("stateR", user.getJobsStateR());
                jobs.put("stateC", user.getJobsStateC());
                jobs.put("stateOther", user.getJobsOther());
                //List<Job> userJobs = pbsky.getUserJobs(userName);
            } else {
                Map<String,Object> jobs = new HashMap<String,Object>();
                userData.put("jobs", jobs);
                jobs.put("total", 0);
                jobs.put("stateQ", 0);
                jobs.put("stateR", 0);
                jobs.put("stateC", 0);
                jobs.put("stateOther", 0);
            }
        }
        return new Resolution() {
            @Override
            public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
                response.setContentType("application/json");
                ObjectMapper objectMapper = new ObjectMapper();
                if (pretty) objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                objectMapper.writeValue(response.getOutputStream(), usersData);
            }
        };

    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public boolean isPretty() {
        return pretty;
    }

    public void setPretty(boolean pretty) {
        this.pretty = pretty;
    }
}
