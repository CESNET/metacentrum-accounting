package cz.cesnet.meta.stripes;

import cz.cesnet.meta.cloud.Cloud;
import cz.cesnet.meta.pbsmon.MachineStateDecider;
import cz.cesnet.meta.perun.api.PerunComputingResource;
import cz.cesnet.meta.perun.api.PhysicalMachines;
import cz.cesnet.meta.perun.api.Perun;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

/**
 * Displayes a computing resource, cluster or machine.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: ResourceActionBean.java,v 1.2 2013/10/08 11:31:56 makub Exp $
 */
@UrlBinding("/resource/{resourceName}")
public class ResourceActionBean extends BaseActionBean {

    final static Logger log = LoggerFactory.getLogger(ResourceActionBean.class);

    private String resourceName;
    private PerunComputingResource resource;
    private int cpuSum;

    public String getResourceName() {
        return resourceName;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    @SpringBean("perun")
    protected Perun perun;

    @SpringBean("cloud")
    protected Cloud cloud;

    public Resolution show() {
        if (resourceName == null || resourceName.length() == 0) {
            return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND, "Computing resource name is needed in the URL.");
        }
        resource = perun.getPerunComputingResourceByName(resourceName);
        if (resource == null) {
            log.warn("Computing resource {} not found ", resourceName);
            return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND, "Computing resource " + resourceName + " not found in Perun.");
        }
        PhysicalMachines physicalMachines = perun.getPhysicalMachines();
        if(resource.isCluster()) {
            cpuSum = physicalMachines.getCpuMap().get(resource.getId());
        } else {
            cpuSum = resource.getPerunMachine().getCpuNum();
        }
        MachineStateDecider.decideStates(pbsky, perun.getFrontendFinder(),
                pbsCache.getMapping(), perun.getReservedMachinesFinder(), physicalMachines.getOwnerOrganisations(), cloud);

        return new ForwardResolution("/nodes/resource.jsp");
    }

    public PerunComputingResource getResource() {
        return resource;
    }

    public int getCpuSum() {
        return cpuSum;
    }
}
