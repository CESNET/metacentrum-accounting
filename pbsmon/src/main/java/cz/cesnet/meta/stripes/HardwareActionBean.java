package cz.cesnet.meta.stripes;

import cz.cesnet.meta.perun.api.PhysicalMachines;
import cz.cesnet.meta.perun.api.Perun;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displayes a computing resource, cluster or machine.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: HardwareActionBean.java,v 1.1 2012/10/12 14:07:12 makub Exp $
 */
@UrlBinding("/hardware")
public class HardwareActionBean extends BaseActionBean {

    final static Logger log = LoggerFactory.getLogger(HardwareActionBean.class);

    @SpringBean("perun")
    protected Perun perun;

    private PhysicalMachines physicalMachines;

    public Resolution show() {
        physicalMachines = perun.getPhysicalMachines();
        return new ForwardResolution("/nodes/hardware.jsp");
    }

    public PhysicalMachines getPhysicalMachines() {
        return physicalMachines;
    }
}
