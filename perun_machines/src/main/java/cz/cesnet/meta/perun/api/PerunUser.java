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
    private String organization;
    private Map<String,Integer> publications = new HashMap<>(3);
    private Map<String,Vo> vos = new HashMap<>(3);
    private Vo mainVo;

    public PerunUser() {
    }

    public static class Vo {
        private String voName;
        private String status;
        private Date expires;
        private Map<String,Integer> statsGroups = new HashMap<>(3);

        public Vo(String voName) {
            this.voName = voName;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setExpires(Date expires) {
            this.expires = expires;
        }

        public String getVoName() {
            return voName;
        }

        public String getStatus() {
            return status;
        }

        public Date getExpires() {
            return expires;
        }

        public Map<String, Integer> getStatsGroups() {
            return statsGroups;
        }

        @Override
        public String toString() {
            return "Vo{" +
                    "voName='" + voName + '\'' +
                    ", status='" + status + '\'' +
                    ", expires=" + expires +
                    ", statsGroups=" + statsGroups +
                    '}';
        }
    }

    public void setMainVo(Vo mainVo) {
        this.mainVo = mainVo;
    }

    public Map<String, Vo> getVos() {
        return vos;
    }

    public String getLogname() {
        return logname;
    }

    public String getOrganization() {
        return organization;
    }

    public void setLogname(String logname) {
        this.logname = logname;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public Map<String, Integer> getPublications() {
        return publications;
    }

    public String getResearchGroup() {
        Map<String, Integer> statsGroups = mainVo.getStatsGroups();
        if(statsGroups !=null&&!statsGroups.isEmpty()) {
            String groupId = (String) statsGroups.keySet().toArray()[0];
            return groupId.startsWith("Evidence:")?groupId.substring(9):groupId;
        } else {
            return "žádná skupina - no group";
        }
    }

    public Map<String, Integer> getStatsGroups() {
        return mainVo.getStatsGroups();
    }

    public String getStatus() {
        return mainVo.getStatus();
    }

    public Date getExpires() {
        return mainVo.getExpires();
    }

    public String getMainVoName() {
        return mainVo.getVoName();
    }

    @Override
    public String toString() {
        return "PerunUser{" +
                "logname='" + logname + '\'' +
                ", organization='" + organization + '\'' +
                ", publications=" + publications +
                ", vos=" + vos +
                ", mainVo=" + mainVo +
                '}';
    }
}
