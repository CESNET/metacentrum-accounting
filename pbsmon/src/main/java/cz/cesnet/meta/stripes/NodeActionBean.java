package cz.cesnet.meta.stripes;

import cz.cesnet.meta.cloud.CloudPhysicalHost;
import cz.cesnet.meta.cloud.CloudVirtualHost;
import cz.cesnet.meta.pbs.Node;
import cz.cesnet.meta.pbs.Pbsky;
import cz.cesnet.meta.perun.api.Perun;
import cz.cesnet.meta.pbscache.PbsCache;
import cz.cesnet.meta.acct.OutageRecord;
import cz.cesnet.meta.acct.Accounting;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.integration.spring.SpringBean;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: NodeActionBean.java,v 1.8 2013/10/10 12:18:14 makub Exp $
 */
@UrlBinding("/node/{nodeName}")
public class NodeActionBean extends BaseActionBean {
    final static Logger log = LoggerFactory.getLogger(NodeActionBean.class);

    @SpringBean("perun")
    protected Perun perun;

    @SpringBean("accounting")
    protected Accounting accounting;

    private String nodeName;
    private Node node;
    private boolean virtual;
    private String physicalMachineName;

    @DefaultHandler
    public Resolution show() {
        if(nodeName==null||nodeName.length()==0) {
            return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND,"Node name is needed in the URL.");
        }
        node = pbsky.getNodeByName(nodeName);
        if(node==null) {
            log.warn("Node {} not found.",nodeName);
            return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND,"Node "+nodeName+" not found.");
        }
        log.debug("testing node virtual {}",nodeName);
        String nodeFQDN = node.getFQDN();
        virtual = perun.isNodeVirtual(nodeFQDN);
        log.debug("node virtual={}",virtual);
        if(virtual) {
            //try pbs_cache with mappings
            physicalMachineName = pbsCache.getMapping().getVirtual2physical().get(nodeFQDN);
            //try cloud if not found in previous step
            if(physicalMachineName==null) {
                CloudPhysicalHost physicalHost = cloud.getVmFqdn2HostMap().get(nodeFQDN);
                if(physicalHost!=null) {
                    physicalMachineName = physicalHost.getHostname();
                }
            }
        } else {
            physicalMachineName = nodeFQDN;
        }
        node.setOutages(accounting.getOutagesForNode(node));

        if(log.isDebugEnabled()) {
            log.debug("scratch={}",node.getScratch());
        }
        return new ForwardResolution("/nodes/node.jsp");
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Node getNode() {
        return node;
    }

    public boolean isVirtual() {
        return virtual;
    }

    public String getPhysicalMachineName() {
        return physicalMachineName;
    }


}
