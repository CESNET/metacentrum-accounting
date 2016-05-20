package cz.cesnet.meta.stripes;

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
 * @version $Id: HardwareActionBean.java,v 1.1 2012/10/12 14:07:12 makub Exp $
 */
@UrlBinding("/hardware")
public class HardwareActionBean extends BaseActionBean {

    final static Logger log = LoggerFactory.getLogger(HardwareActionBean.class);

    @SpringBean("perun")
    protected Perun perun;

    private FyzickeStroje fyzickeStroje;

    public Resolution show() {
        fyzickeStroje = perun.getFyzickeStroje();
        return new ForwardResolution("/nodes/hardware.jsp");
    }

    public FyzickeStroje getFyzickeStroje() {
        return fyzickeStroje;
    }
}
