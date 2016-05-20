package cz.cesnet.meta.cloud;

/**
* VM owner.
*
* @author Martin Kuba makub@ics.muni.cz
*/
public class Owner {
    String name;
    String realm;
    String email;
    String full_name;
    String x509_dn;
    boolean local;

    public Owner() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getX509_dn() {
        return x509_dn;
    }

    public void setX509_dn(String x509_dn) {
        this.x509_dn = x509_dn;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    @Override
    public String toString() {
        return "Owner{" +
                "name='" + name + '\'' +
                ", realm='" + realm + '\'' +
                ", email='" + email + '\'' +
                ", full_name='" + full_name + '\'' +
                ", x509_dn='" + x509_dn + '\'' +
                ", local=" + local +
                '}';
    }
}
