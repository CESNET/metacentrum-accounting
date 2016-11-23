package cz.cesnet.meta.perun.api;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Info about a user.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: PerunUser.java,v 1.2 2011/10/27 14:19:55 makub Exp $
 */
public class PerunUser {

    private String logname;
    private String name;
    private String organization;
    private String researchGroup;
    private String lang;
    private String status;
    private String mail;
    private Date expires;
    private Map<String,Integer> publications = new HashMap<String, Integer>(3);


    public PerunUser() {
    }

    public PerunUser(String logname, String name, String organization, Map<String, Integer> publications, String researchGroup, String lang, String status, String mail, Date expires) {
        this.logname = logname;
        this.name = name;
        this.organization = organization;
        this.publications = publications;
        this.researchGroup = researchGroup;
        this.lang = lang;
        this.status = status;
        this.mail = mail;
        this.expires = expires;
    }

    public String getLogname() {
        return logname;
    }

    public void setLogname(String logname) {
        this.logname = logname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public Map<String, Integer> getPublications() {
        return publications;
    }

    public void setPublications(Map<String, Integer> publications) {
        this.publications = publications;
    }

    public String getResearchGroup() {
        return researchGroup;
    }

    public void setResearchGroup(String researchGroup) {
        this.researchGroup = researchGroup;
    }

//    public String getLang() {
//        return lang;
//    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    @Override
    public String toString() {
        return "PerunUser{" +
                "logname='" + logname + '\'' +
                ", name='" + name + '\'' +
                ", organization='" + organization + '\'' +
                ", publications=" + publications +
                ", researchGroup='" + researchGroup + '\'' +
                ", lang='" + lang + '\'' +
                ", status='" + status + '\'' +
                ", mail='" + mail + '\'' +
                ", expires=" + expires +
                '}';
    }
}
