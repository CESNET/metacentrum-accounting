package cz.cesnet.meta.stripes;

import cz.cesnet.meta.acct.Accounting;
import cz.cesnet.meta.cloud.CloudPhysicalHost;
import cz.cesnet.meta.cloud.CloudVirtualHost;
import cz.cesnet.meta.pbs.Node;
import cz.cesnet.meta.pbsmon.PbsmonUtils;
import cz.cesnet.meta.perun.api.Perun;
import cz.cesnet.meta.perun.api.Stroj;
import cz.cesnet.meta.perun.api.VyhledavacFrontendu;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: MachineActionBean.java,v 1.13 2016/03/04 13:55:03 makub Exp $
 */
@UrlBinding("/machine/{machineName}")
public class MachineActionBean extends BaseActionBean {
    final static Logger log = LoggerFactory.getLogger(MachineActionBean.class);

    private String machineName;

    public String getMachineName() {
        return machineName;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    @SpringBean("perun")
    protected Perun perun;

    @SpringBean("accounting")
    protected Accounting accounting;

    private Stroj perunMachine;
    private List<Node> pbsNodes = new ArrayList<>();
    private CloudPhysicalHost cloudPhysicalHost;
    private List<CloudVirtualHost> cloudVirtualHosts;

    @SuppressWarnings({"UnusedDeclaration"})
    public Resolution show() {
        if (machineName == null || machineName.length() == 0) {
            return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND, "Machine name is needed in the URL.");
        }
        perunMachine = perun.getStrojByName(machineName);
        if (perunMachine == null) {
            log.warn("Machine {} not found ", machineName);
            return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND, "Machine " + machineName + " not found in Perun.");
        }
        if (perunMachine.isVirtual()) {
            return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND, "Machine " + machineName + " is virtual, not physical.");
        }
        //frontendy z peruna - v Perun 3 se nepoužívá
        VyhledavacFrontendu vyhledavacFrontendu = perun.getVyhledavacFrontendu();

        //info z OpenNebuly
        if ((cloudPhysicalHost = cloud.getHostname2HostMap().get(machineName)) != null) {
            cloudVirtualHosts = cloud.getHostName2VirtualHostsMap().get(cloudPhysicalHost.getName());
        }
        //všechny PBS uzly
        pbsNodes = PbsmonUtils.getPbsNodesForPhysicalMachine(perunMachine, pbsky, pbsCache, cloud);


        for (Node pbsNode : pbsNodes) {
            pbsNode.setOutages(accounting.getOutagesForNode(pbsNode));
        }

        return new ForwardResolution("/nodes/machine.jsp");
    }


    public Stroj getPerunMachine() {
        return perunMachine;
    }

    public List<Node> getPbsNodes() {
        return pbsNodes;
    }

    public CloudPhysicalHost getCloudPhysicalHost() {
        return cloudPhysicalHost;
    }

    public List<CloudVirtualHost> getCloudVirtualHosts() {
        return cloudVirtualHosts;
    }
}
