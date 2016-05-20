package cz.cesnet.meta.stripes;

import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import java.util.Locale;

/**
 * Akce pro uzivatelem vynucenou zmenu nastaveni jazyka.
 * Za normalnich okolnosti je jazyk vybran podle nastaveni prohlizece,
 * ale tato akce umoznuje vybrat jiny jazyk.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: LocaleActionBean.java,v 1.3 2014/06/13 14:41:39 makub Exp $
 */
@UrlBinding("/locale/{jazyk}")
public class LocaleActionBean extends BaseActionBean {

    private String kam;
    private String jazyk;


    public String getKam() {
        return kam;
    }

    public void setKam(String kam) {
        this.kam = kam;
    }

    public String getJazyk() {
        return jazyk;
    }

    public void setJazyk(String jazyk) {
        this.jazyk = jazyk;
    }

    public Resolution nastav() {
        MujLocalePicker.setLocale(new Locale(jazyk), getContext().getRequest());
        String url = kam != null ? kam : "/";
        return new RedirectResolution(url,!url.startsWith(getContext().getRequest().getContextPath()));
    }
}
