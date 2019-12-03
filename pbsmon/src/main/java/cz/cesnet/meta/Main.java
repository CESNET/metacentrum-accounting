package cz.cesnet.meta;

import cz.cesnet.meta.cloud.CloudPhysicalHost;
import cz.cesnet.meta.cloud.CloudVM;
import cz.cesnet.meta.cloud.OpenStackCloudLoader;
import cz.cesnet.meta.pbs.*;
import cz.cesnet.meta.perun.api.Perun;
import cz.cesnet.meta.perun.api.Stroj;
import cz.cesnet.meta.perun.api.VypocetniZdroj;
import cz.cesnet.meta.perun.impl.PerunJsonImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Testy. Zkopiruj  target/pbsmon2/WEB-INF/spring-context.xml do target/classes/ a poedituj ho.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class Main {
    final static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        OpenStackCloudLoader loader = new OpenStackCloudLoader("Stack","/home/openstack");
        loader.load();
        List<CloudPhysicalHost> physicalHosts = loader.getPhysicalHosts();
        physicalHosts.sort(CloudPhysicalHost.CLOUD_PHYSICAL_HOST_COMPARATOR);
        for (CloudPhysicalHost physicalHost : physicalHosts) {
            System.out.println("physicalHost = " + physicalHost);
        }
        for (CloudVM virtualHost : loader.getVirtualHosts()) {
            System.out.println("virtualHost = " + virtualHost);
        }
    }
    public static void main2(String[] args) {
        Perun perun = new PerunJsonImpl(Arrays.asList("/etc/pbsmon/pbsmon_machines.json"), Collections.emptyList());
        VypocetniZdroj bofurA = perun.getVypocetniZdrojByName("cerit-hde-ostack.priv.cloud.muni.cz");
        List<Stroj> stroje = bofurA.getStroje();
        for (Stroj stroj : stroje) {
            System.out.println("stroj = " + stroj);
        }

    }

     public static void main9(String[] args) {
        MessageFormat mf = new MessageFormat("Začátek odstávky je naplánován na {0,time,d.M.yyyy HH:mm:ss}");
        System.out.println(mf.format(new Object[]{new Date()}));
    }

    public static void main8(String[] args) {
        for (String s : new String[]{"0mb", "2gb", "2200mb", "1024tb", "7815829540kb", "543905024kb", "57076422mb", "4097mb",
                "4096mb", "4095mb", "4612mb", "8193mb", "8192mb", "8191mb", "8705mb", "16385mb", "6154397092kb"}) {
//        Random random = new Random();
//        String[] units = new String[] { "mb", "gb", "tb"};
//        for(int i =0; i<10; i++) {
//            String s = random.nextInt(3000) + units[random.nextInt(units.length)];
            long bytes = PbsUtils.parsePbsBytes(s);
            System.out.printf("%15s -> %10s  %s%n", s, PbsUtils.formatInPbsUnits(bytes), PbsUtils.formatInHumanUnits(bytes));
        }
    }


}
