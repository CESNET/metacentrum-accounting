package cz.cesnet.meta.perun.api;


import java.util.Comparator;

public class Stroj {

    private String name;
    private int cpuNum;
    private String shortName;
    private VypocetniZdroj vypocetniZdroj;
    private int usedPercent = 0;
    private boolean cloudManaged = false; //je v cloudu
    private boolean cloudPbsHost = false; //je v cloudu a obsahuje VM ktery je v PBS
    private boolean cloudUsable = false; //lze na něm spustit další uživatelský VM přes cloud
    private String pbsName;
    private String pbsState;

    private String state;

    public Stroj(VypocetniZdroj vypocetniZdroj, String name, int cpuNum) {
        this.vypocetniZdroj = vypocetniZdroj;
        this.name = name;
        this.cpuNum = cpuNum;

        int dot = name.indexOf(46);
        if (dot < 0)
            this.shortName = name;
        else
            this.shortName = name.substring(0, dot);
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public VypocetniZdroj getVypocetniZdroj() {
        return vypocetniZdroj;
    }

    public String getName() {
        return this.name;
    }

    public String getShortName() {
        return this.shortName;
    }

    public int getCpuNum() {
        return this.cpuNum;
    }

    public int getUsedPercent() {
        return usedPercent;
    }

    public void setUsedPercent(int usedPercent) {
        this.usedPercent = Math.min(usedPercent, 100);
    }

    public boolean isCloudManaged() {
        return cloudManaged;
    }

    public boolean isCloudPbsHost() {
        return cloudPbsHost;
    }

    /**
     * Vlasta Holer's definition of cloud host -  is in cloud and is not wholy taken by PBS Node
     */
    public boolean isCloudUsable() {
        return cloudUsable;
    }

    public void setCloudUsable(boolean cloudUsable) {
        this.cloudUsable = cloudUsable;
    }

    public void setCloudPbsHost(boolean cloudPbsHost) {
        this.cloudPbsHost = cloudPbsHost;
    }

    public void setCloudManaged(boolean cloudManaged) {
        this.cloudManaged = cloudManaged;
    }

    public String getPbsName() {
        return pbsName;
    }

    public void setPbsName(String pbsName) {
        this.pbsName = pbsName;
    }

    public String getPbsState() {
        return pbsState;
    }

    public void setPbsState(String pbsState) {
        this.pbsState = pbsState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stroj stroj = (Stroj) o;
        return !(name != null ? !name.equals(stroj.name) : stroj.name != null);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Stroj{" +
                "name='" + name + '\'' +
                ", cpuNum=" + cpuNum +
                '}';
    }

    /**
     * Compares two machines using their names. It recursively splits the names into non-digit-only
     * and digit-only strings, and compares non-digit-only strings as strings and digit-only strings as numbers.
     */
    public static final NameComparator NAME_COMPARATOR = new NameComparator();

    public static class NameComparator implements Comparator<Stroj> {

        @Override
        public int compare(Stroj stroj1, Stroj stroj2) {
            return smartCompare(stroj1.getName(), stroj2.getName());
        }

        public static int smartCompare(String s1, String s2) {
            String p1 = parseNotDigitPrefix(s1);
            String p2 = parseNotDigitPrefix(s2);

            int compare = p1.compareToIgnoreCase(p2);
            if (compare == 0 && p1.length() != s1.length() && p2.length() != s2.length()) {
                return smartDigitCompare(s1.substring(p1.length()), s2.substring(p2.length()));
            }
            return compare;
        }

        private static int smartDigitCompare(String s1, String s2) {
            String p1 = parseDigitPrefix(s1);
            String p2 = parseDigitPrefix(s2);

            int compare;
            if (p1.length() > 0 && p2.length() > 0) {
                compare = Integer.compare(Integer.parseInt(p1), Integer.parseInt(p2));
            } else {
                compare = p1.compareToIgnoreCase(p2);
            }
            if (compare == 0 && p1.length() != s1.length() && p2.length() != s2.length()) {
                return smartCompare(s1.substring(p1.length()), s2.substring(p2.length()));
            }
            return compare;
        }

        private static String parsePrefix(String s, boolean digits) {
            StringBuilder p = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (((!digits) && (c >= '0' && c <= '9')) || (digits && !(c >= '0' && c <= '9'))) {
                    break;
                }
                p.append(c);
            }
            return p.toString();
        }

        private static String parseDigitPrefix(String s) {
            return parsePrefix(s, true);
        }

        private static String parseNotDigitPrefix(String s) {
            return parsePrefix(s, false);
        }
    }
}
