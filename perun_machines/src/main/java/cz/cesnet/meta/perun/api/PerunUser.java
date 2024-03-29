package cz.cesnet.meta.perun.api;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Info about a user.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class PerunUser {

    private String logname;
    private String name;
    private String organization;
    // publications are filled up in PerunJsonImpl.java on the line 236
    private final Map<String,Integer> publications = new HashMap<>(3);
    private final Map<String,Vo> vos = new HashMap<>(3);
    private Vo mainVo;

    public PerunUser() {
    }

    public static class Vo {
        private final String voName;
        private String status;
        private String organization;
        private Date expires;
        private final Map<String,Integer> statsGroups = new HashMap<>(3);

        public Vo(String voName) {
            this.voName = voName;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setOrganization(String organization) {
            this.organization = organization;
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

        public String getOrganization() {
            return organization;
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

    public String getName() {
        return name;
    }

    public String getOrganization() {
        return mainVo != null && mainVo.getOrganization() != null ? mainVo.getOrganization() : organization;
    }

    public void setLogname(String logname) {
        this.logname = logname;
    }

    public void setName(String name) {
        this.name = name;
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
                ", name='" + name + '\'' +
                ", organization='" + organization + '\'' +
                ", publications=" + publications +
                ", vos=" + vos +
                ", mainVo=" + mainVo +
                '}';
    }
}
