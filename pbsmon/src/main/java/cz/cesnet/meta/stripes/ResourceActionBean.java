package cz.cesnet.meta.stripes;

import cz.cesnet.meta.cloud.Cloud;
import cz.cesnet.meta.pbsmon.RozhodovacStavuStroju;
import cz.cesnet.meta.perun.api.FyzickeStroje;
import cz.cesnet.meta.perun.api.Perun;
import cz.cesnet.meta.perun.api.VypocetniZdroj;
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
    private VypocetniZdroj resource;
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
        resource = perun.getVypocetniZdrojByName(resourceName);
        if (resource == null) {
            log.warn("Computing resource {} not found ", resourceName);
            return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND, "Computing resource " + resourceName + " not found in Perun.");
        }
        FyzickeStroje fyzickeStroje = perun.getFyzickeStroje();
        if(resource.isCluster()) {
            cpuSum = fyzickeStroje.getCpuMap().get(resource.getId());
        } else {
            cpuSum = resource.getStroj().getCpuNum();
        }
        RozhodovacStavuStroju.rozhodniStavy(pbsky, perun.getVyhledavacFrontendu(),
                pbsCache.getMapping(), perun.getVyhledavacVyhrazenychStroju(), fyzickeStroje.getCentra(), cloud);

        return new ForwardResolution("/nodes/resource.jsp");
    }

    public VypocetniZdroj getResource() {
        return resource;
    }

    public int getCpuSum() {
        return cpuSum;
    }
}
