package cz.cesnet.meta.pbs;

import java.util.TreeMap;

/**
 * Formát dat vracený od PBS. PBS hlásí data ve formě objektů, kde každý objekt
 * má jméno a atributy.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class PbsInfoObject {


    //primary data
    protected String name = null;
    protected TreeMap<String, String> attrs;
    protected PBS pbs;

    public void clear() {
        name = null;
        pbs = null;
        if(attrs!=null) attrs.clear();
        attrs = null;
    }

    //pouužíván JSON Tools
    public PbsInfoObject() {
    }

    //používán JNI
    public PbsInfoObject(String name) {
        this.name = name;
        this.attrs = new TreeMap<String, String>();
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets name of the info object.
     *
     * @return name of the info object
     */
    public String getName() {
        return name;
    }

    /**
     * Gets map of all attributes and their values as reported by PBS.
     *
     * @return map of all attributes and their values
     */
    public TreeMap<String, String> getAttributes() {
        return attrs;
    }

    public void setAttributes(TreeMap<String, String> attrs) {
        this.attrs = attrs;
    }

    public PBS getPbs() {
        return pbs;
    }

    public void setPbs(PBS pbs) {
        this.pbs = pbs;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + name + "," + attrs + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PbsInfoObject)) return false;

        PbsInfoObject that = (PbsInfoObject) o;

        return !(name != null ? !name.equals(that.name) : that.name != null);

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
