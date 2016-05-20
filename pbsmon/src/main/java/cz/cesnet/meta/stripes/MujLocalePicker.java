package cz.cesnet.meta.stripes;

import net.sourceforge.stripes.localization.DefaultLocalePicker;
import net.sourceforge.stripes.localization.LocalePicker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Locale;

/**
 * Umoznuje vnutit lokale nastavenim klice v session.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: MujLocalePicker.java,v 1.3 2011/10/31 09:27:49 makub Exp $
 */
public class MujLocalePicker extends DefaultLocalePicker implements LocalePicker {

    public static final String SESSION_KEY = "MujLocalePicker.locale";

    /**
     * Pokusi se najit lokale v session, jinak deleguje na DefaultLocalePicker.
     *
     * @param request http request
     * @return selected locale
     */
    @Override
    public Locale pickLocale(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Locale loc = (Locale) session.getAttribute(SESSION_KEY);
            if (loc != null) {
                if (this.locales.contains(loc)) {
                    return loc;
                }
            }
        }
        return super.pickLocale(request);
    }

    /**
     * Nastavi locale do session pod prislusnym klicem.
     *
     * @param loc     locale
     * @param request http request
     */
    public static void setLocale(Locale loc, HttpServletRequest request) {
        request.getSession().setAttribute(SESSION_KEY, loc);
    }
}
