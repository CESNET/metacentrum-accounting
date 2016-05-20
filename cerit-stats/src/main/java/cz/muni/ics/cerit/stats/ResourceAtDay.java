package cz.muni.ics.cerit.stats;

/**
* Tuple.
*
* @author Martin Kuba makub@ics.muni.cz
*/
class ResourceAtDay {

    String resourceName;
    java.sql.Date day;

    ResourceAtDay(String resourceName, java.sql.Date day) {
        this.resourceName = resourceName;
        this.day = day;
    }

    String getResourceName() {
        return resourceName;
    }

    java.sql.Date getDay() {
        return day;
    }

    @Override
    public String toString() {
        return "ResourceAtDay{" +
                "resourceName='" + resourceName + '\'' +
                ", day=" + day +
                '}';
    }
}
