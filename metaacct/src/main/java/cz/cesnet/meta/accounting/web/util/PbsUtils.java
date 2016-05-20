package cz.cesnet.meta.accounting.web.util;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id:$
 */
public class PbsUtils {

    //velikost
    static final long KIBI = 1024l;
    static final long MEBI = KIBI * KIBI;
    static final long GIBI = MEBI * KIBI;
    static final long TEBI = GIBI * KIBI;
    static final long PEBI = TEBI * KIBI;
    static final long EXBI = PEBI * KIBI;
    static final long ROUND_LIMIT = 16l * KIBI;


    /**
     * Formátuje počet bajtů na formát PBS.
     *
     * @param bytes počet bajtů
     * @return počet v jednotkách b,mb,gb,tb,pb
     */
    static public String formatInPbsUnits(long bytes) {
        //nejdřív přesné násobky větších jednotek
        if (bytes == 0) {
            return "0b";
        } else if (bytes % PEBI == 0 && bytes / PEBI < ROUND_LIMIT) {
            return Long.toString(bytes / PEBI) + "pb";
        } else if (bytes % TEBI == 0 && bytes / TEBI < ROUND_LIMIT) {
            return Long.toString(bytes / TEBI) + "tb";
        } else if (bytes % GIBI == 0 && bytes / GIBI < ROUND_LIMIT) {
            return Long.toString(bytes / GIBI) + "gb";
        } else if (bytes % MEBI == 0 && bytes / MEBI < ROUND_LIMIT) {
            return Long.toString(bytes / MEBI) + "mb";
            //pak zaokrouhlování
        } else if (bytes > PEBI) {
            return Long.toString((bytes + PEBI / 2l) / PEBI) + "pb";
        } else if (bytes > TEBI) {
            return Long.toString((bytes + TEBI / 2l) / TEBI) + "tb";
        } else if (bytes > GIBI) {
            return Long.toString((bytes + GIBI / 2l) / GIBI) + "gb";
        } else if (bytes > MEBI) {
            return Long.toString((bytes + MEBI / 2l) / MEBI) + "mb";
        } else {
            return Long.toString(bytes) + "b";
        }
    }

    /**
     * Převádí velikost paměti ve formátu PBS na počet bajtů.
     *
     * @param bytesInPbsUnit řetězec obsahující číslo a jednotku z množiny b,kb,mb,gb,tb,pb
     * @return počet bajtů
     */
    static public long parsePbsBytes(String bytesInPbsUnit) {
        if (bytesInPbsUnit == null) return 0;
        long bytes;
        //prepocitame na bajty
        if (bytesInPbsUnit.endsWith("kb") || bytesInPbsUnit.endsWith("KB")) {
            bytes = Long.parseLong(bytesInPbsUnit.substring(0, bytesInPbsUnit.length() - 2));
            bytes *= KIBI;
        } else if (bytesInPbsUnit.endsWith("mb")) {
            bytes = Long.parseLong(bytesInPbsUnit.substring(0, bytesInPbsUnit.length() - 2));
            bytes *= MEBI;
        } else if (bytesInPbsUnit.endsWith("gb")) {
            bytes = Long.parseLong(bytesInPbsUnit.substring(0, bytesInPbsUnit.length() - 2));
            bytes *= GIBI;
        } else if (bytesInPbsUnit.endsWith("tb")) {
            bytes = Long.parseLong(bytesInPbsUnit.substring(0, bytesInPbsUnit.length() - 2));
            bytes *= TEBI;
        } else if (bytesInPbsUnit.endsWith("pb")) {
            bytes = Long.parseLong(bytesInPbsUnit.substring(0, bytesInPbsUnit.length() - 2));
            bytes *= PEBI;
        } else if (bytesInPbsUnit.endsWith("eb")) {
            bytes = Long.parseLong(bytesInPbsUnit.substring(0, bytesInPbsUnit.length() - 2));
            if (bytes > 7) throw new IllegalArgumentException("Java long can hold up to 2^63 = 7eb");
            bytes *= EXBI;
        } else if (bytesInPbsUnit.endsWith("b")) {
            bytes = Long.parseLong(bytesInPbsUnit.substring(0, bytesInPbsUnit.length() - 1));
        } else {
            bytes = 0;
        }
        return bytes;
    }
}
