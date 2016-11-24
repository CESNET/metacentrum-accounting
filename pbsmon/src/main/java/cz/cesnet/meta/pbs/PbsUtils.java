package cz.cesnet.meta.pbs;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: PbsUtils.java,v 1.12 2014/12/10 15:42:37 makub Exp $
 */
public class PbsUtils {
    public static final String MAINTENANCE = "maintenance";
    public static final String RESERVED = "reserved";


    public static boolean isIn(String s, String[] pole) {
        for (String x : pole) {
            if (x.equals(s)) return true;
        }
        return false;
    }

    public static String substringBefore(String string, char ch) {
        int pos = string.indexOf(ch);
        if (pos == -1) return string;
        return string.substring(0, pos);
    }

    public static String substringAfter(String string, char ch) {
        int pos = string.indexOf(ch);
        if (pos == -1) return string;
        return string.substring(pos + 1);
    }

    /**
     * Makes translation from a property String with Unix time in seconds since the Epoch
     * to Java time in milliseconds since the Epoch.
     *
     * @param unixTime Unix time in seconds since the Epoc
     * @return Java time in milliseconds since the Epoch
     */
    public static Date getJavaTime(String unixTime) {
        if (unixTime == null) return null;
        long time = Long.parseLong(unixTime);
        time *= 1000L;
        return new Date(time);
    }



    //velikost
    static final long KIBI = 1024L;
    static final long MEBI = KIBI * KIBI;
    static final long GIBI = MEBI * KIBI;
    static final long TEBI = GIBI * KIBI;
    static final long PEBI = TEBI * KIBI;
    static final long EXBI = PEBI * KIBI;
    static final long KILO = 1000;
    static final long MEGA = KILO * KILO;
    static final long GIGA = MEGA * KILO;
    static final long ROUND_LIMIT = 16L*KIBI;

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
        if (bytesInPbsUnit.endsWith("kb")||bytesInPbsUnit.endsWith("KB")) {
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
     * Formátuje počet bajtů na lidský formát - s jedním desetinným místem v binárních jednotkách.
     *
     * @param bytes počet bajtů
     * @return počet v binární jednotce s jedním desetinným místem
     */
    static public String formatInHumanUnits(long bytes) {
        if (bytes >= PEBI) {
            return oneDigitFrac(bytes, PEBI) + " PiB";
        } else if (bytes >= TEBI) {
            return oneDigitFrac(bytes, TEBI) + " TiB";
        } else if (bytes >= GIBI) {
            return oneDigitFrac(bytes, GIBI) + " GiB";
        } else {
            return oneDigitFrac(bytes, MEBI) + " MiB";
        }
    }

    static private String oneDigitFrac(long bytes, long unit) {
        double d = ((double) bytes) / ((double) unit);
        return String.format(Locale.US, "%.1f", d);
    }

    /**
     * Zaokrouhluje na zvolenou jednotku. Od poloviny jednotky včetně zaokrouhluje nahoru.
     *
     * @param bytes počet bajtů
     * @param unit  jednotka tj. MEBI, GIBI atd.
     * @return zaokrouhlený počet jednotek
     */
    static public long round(long bytes, long unit) {
        long celaCast = bytes / unit;
        long zbytek = bytes % unit;
        if (zbytek >= (unit / 2)) celaCast++;
        return celaCast;
    }

    /**
     * Zvyšuje čítač.
     *
     * @param map mapa z klíčú na počty
     * @param key klíč pro čítač
     * @param inc o kolik se má zvednout čítač
     */
    public static <T> void updateCount(Map<T, Integer> map, T key, int inc) {
        Integer count = map.get(key);
        if (count == null) count = 0;
        count += inc;
        map.put(key, count);
    }

    /**
     * Parses walltime specified with w for week, d for days, h for hours, m for minutes, s for seconds
     * @param walltime walltime
     * @return number of seconds
     */
    public static long parseWalltime(String walltime) {
        Pattern p = Pattern.compile("(\\d+)([wdhms])");
        Matcher m = p.matcher(walltime);
        long seconds = 0L;
        while (m.find()) {
            int num = Integer.parseInt(m.group(1));
            switch (m.group(2)) {
                case "w":
                    seconds += num * (7L * 24L * 3600L);
                    break;
                case "d":
                    seconds += num * (24L * 3600L);
                    break;
                case "h":
                    seconds += num * (3600L);
                    break;
                case "m":
                    seconds += num * (60L);
                    break;
                case "s":
                    seconds += num;
                    break;
            }
        }
        return seconds;
    }

    /**
     * Parses time to seconds.
     * @param time in format hours:minutes:seconds
     * @return number of seconds
     */
    public static Duration parseTime(String time) {
        if(time==null) return null;
        String[] c = time.split(":");
        return Duration.of(Long.parseLong(c[0]) * 3600 + Long.parseLong(c[1]) * 60 + Long.parseLong(c[2]), ChronoUnit.SECONDS);
    }
}
