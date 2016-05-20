package cz.cesnet.meta.storages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class Storage {

    private int totalGB;
    private int freeGB;
    private int usedGB;
    private String total;
    private int usedPercent;
    private String dir;
    private static Pattern num = Pattern.compile("([0-9.]+)([TGM])");

    static class ParseResult {
        public String nums;
        public int numGB;
        public String unit;
    }


    public Storage(String total, String free, String dir) {
        this.dir = dir;
        ParseResult prTotal = parseNum(total);
        ParseResult prFree = parseNum(free);
        this.totalGB = prTotal.numGB;
        this.total = prTotal.nums;
        this.freeGB = prFree.numGB;
        this.usedGB = this.totalGB - this.freeGB;
        this.usedPercent = Math.round(this.usedGB*100f/this.totalGB);
    }

   static ParseResult parseNum(String s) {
        Matcher m = num.matcher(s);
        if (!m.matches()) throw new RuntimeException("cannot parse '" + s + "'");
        ParseResult pr = new ParseResult();
        String nums = m.group(1);
        float numf = Float.parseFloat(nums);
        pr.unit = m.group(2);
        pr.nums= nums+"\u00A0"+pr.unit+"iB";
       switch (pr.unit) {
           case "G": pr.numGB = Math.round(numf); break;
           case "T": pr.numGB = Math.round(numf * 1024); break;
           case "M": pr.numGB = Math.round(numf / 1024); break;
       }
        return pr;
    }

    public int getTotalGB() {
        return totalGB;
    }

    public int getFreeGB() {
        return freeGB;
    }

    public int getUsedGB() {
        return usedGB;
    }

    public String getTotal() {
        return total;
    }

    public int getUsedPercent() {
        return usedPercent;
    }

    public String getDir() {
        return dir;
    }

    @Override
    public String toString() {
        return "Storage{" +
                "totalGB=" + totalGB +
                ", freeGB=" + freeGB +
                ", usedGB=" + usedGB +
                ", total='" + total + '\'' +
                ", usedPercent=" + usedPercent +
                ", dir='" + dir + '\'' +
                '}';
    }
}
