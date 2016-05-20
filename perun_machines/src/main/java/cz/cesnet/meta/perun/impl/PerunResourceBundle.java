package cz.cesnet.meta.perun.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class PerunResourceBundle extends ResourceBundle {
    final static Logger log = LoggerFactory.getLogger(PerunResourceBundle.class);

    private static int counter = 0;
    private int instNum;

    private static HashMap<Locale, HashMap<String, String>> cache = new HashMap<Locale, HashMap<String, String>>(5);


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
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (map) {
                map.clear();
                readTextsFromDatabase(loc, map);
            }
            log.info("refreshed {} texts",loc);
        }
    }

    private static void readTextsFromDatabase(Locale locale, Map<String, String> map) {
        map.putAll(PerunJsonImpl.getTexty().get(locale.toString()));
    }


//    private static void readTextsFromDatabase(Locale locale, Map<String, String> map) {
//        log.debug("reading {} texts from DB", locale);
//        DataSource pool = getDataSourceFromJNDI();
//        Connection con = null;
//        try {
//            con = pool.getConnection();
//            PreparedStatement st = con.prepareStatement("select id,txt from texts where lang=?");
//            st.setString(1, locale.toString());
//            ResultSet rs = st.executeQuery();
//            while (rs.next()) {
//                String s = rs.getString(2);
//                map.put(rs.getString(1), (s == null) ? "" : s);
//            }
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        } finally {
//            if (con != null) try {
//                con.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private static DataSource getDataSourceFromJNDI() {
//        Context init;
//        try {
//            init = new InitialContext();
//            Context ctx = (Context) init.lookup("java:comp/env");
//            return ((DataSource) ctx.lookup("jdbc/perun"));
//        } catch (NamingException e) {
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        }
//    }

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