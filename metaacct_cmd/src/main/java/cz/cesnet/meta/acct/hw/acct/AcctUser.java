package cz.cesnet.meta.acct.hw.acct;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class AcctUser {

    private int id;
    private String logname;
    private String organization;
    private String researchGroup;
    private String status;

    public AcctUser(int id, String logname, String organization, String researchGroup,String status) {
        this.id = id;
        this.logname = logname;
        this.organization = organization;
        this.researchGroup = researchGroup;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogname() {
        return logname;
    }

    public void setLogname(String logname) {
        this.logname = logname;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getResearchGroup() {
        return researchGroup;
    }

    public void setResearchGroup(String researchGroup) {
        this.researchGroup = researchGroup;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "AcctUser{" +
                "id=" + id +
                ", logname='" + logname + '\'' +
                ", organization='" + organization + '\'' +
                ", researchGroup='" + researchGroup + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
