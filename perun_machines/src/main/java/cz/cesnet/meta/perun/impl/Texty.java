package cz.cesnet.meta.perun.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Zdanlive zbytecne, ale resi problem kdyz browser nespecifikuje zadny jazyk.
 * Kdyz totiz request nema hlavicku Accept-Language, tak sice request.getLocale() vraci "en"
 * a Stripes vyberou nejaky jazyk, ale trida org.apache.taglibs.standard.tag.common.core.Util
 * v metode getRequestLocales() znovu zkouma hlavicku Accept-Language a pokud neni,
 * vraci prazdnou kolekci, nacez org.apache.taglibs.standard.tag.common.fmt.BundleSupport
 * v metode getLocalizationContext() zkusi ziskat ResourceBundle pro prazdne Locale,
 * coz zkusi nacist tuto tridu. Pokud by neexistovala, tak se budou dit divne veci, zejmena
 * vznikne spousta zbytecnych instanci tridy Texty_en, i kdyz nevim presne proc.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: Texty.java,v 1.1 2009/08/20 13:32:37 makub Exp $
 */
public class Texty extends ResourceBundle {

    final static Logger log = LoggerFactory.getLogger(Texty.class);

    public Texty() {
        log.debug("new Texty()");
        setParent(ResourceBundle.getBundle("/StripesResources", Locale.ENGLISH));
    }

    @Override
    protected Object handleGetObject(String key) {
        return null;
    }

    @Override
    public Enumeration<String> getKeys() {
        return Collections.enumeration(Collections.<String>emptyList());
    }
}
