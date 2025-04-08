package cz.cesnet.meta.perun.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PerunResourceBundle extends ResourceBundle {
    final static Logger log = LoggerFactory.getLogger(PerunResourceBundle.class);

    private static int counter = 0;
    private final int instNum;

    private static final HashMap<Locale, HashMap<String, String>> cache = new HashMap<Locale, HashMap<String, String>>(5);


    Locale locale;
    final HashMap<String, String> map;

    public PerunResourceBundle(Locale loc) {
        instNum = counter++;
        if (log.isDebugEnabled()) log.debug("new PerunResourceBundle(" + loc + ") instNum=" + instNum);
        this.locale = loc;
        HashMap<String, String> c = cache.get(loc);
        if (c == null) {
            map = new HashMap<String, String>();
            readTextsFromDatabase(loc, map);
            cache.put(loc, map);
        } else {
            map = c;
        }
        setParent(ResourceBundle.getBundle("/StripesResources", loc));

    }

    public static void refresh() {
        Set<Locale> locales = cache.keySet();
        for (Locale loc : locales) {
            HashMap<String, String> map = cache.get(loc);
            synchronized (map) {
                map.clear();
                readTextsFromDatabase(loc, map);
            }
            log.info("refreshed {} texts",loc);
        }
    }

    private static void readTextsFromDatabase(Locale locale, Map<String, String> map) {
        map.putAll(PerunJsonImpl.getTexts().get(locale.toString()));
    }

    protected Object handleGetObject(String key) {
        synchronized (map) {
            return this.map.get(key);
        }
    }

    public Enumeration<String> getKeys() {
        synchronized (map) {
            return Collections.enumeration(this.map.keySet());
        }
    }

    public String toString() {
        return "PerunResourceBundle{inst=" + instNum + " locale=" + this.locale + ", map=" + this.map + '}';
    }
}