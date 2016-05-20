package cz.cesnet.meta.stripes;

import cz.cesnet.meta.perun.impl.PerunResourceBundle;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import javax.servlet.http.HttpServletResponse;

/**
 * Refreshes text from database.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: RefreshTextsActionBean.java,v 1.1 2010/01/08 14:03:39 makub Exp $
 */
@UrlBinding("/refresh")
public class RefreshTextsActionBean extends BaseActionBean {

    @DefaultHandler
    public Resolution exec() {
        PerunResourceBundle.refresh();
        return new ErrorResolution(HttpServletResponse.SC_OK,"refreshed");
    }
}
