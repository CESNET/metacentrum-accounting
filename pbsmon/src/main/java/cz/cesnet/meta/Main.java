package cz.cesnet.meta;

import cz.cesnet.meta.cloud.CloudImpl;
import cz.cesnet.meta.cloud.CloudPhysicalHost;
import cz.cesnet.meta.cloud.HostsDocument;
import cz.cesnet.meta.pbs.*;
import cz.cesnet.meta.pbscache.PbsCache;
import cz.cesnet.meta.pbscache.PbsCacheImpl;
import cz.cesnet.meta.perun.api.Perun;
import cz.cesnet.meta.perun.api.VypocetniCentrum;
import cz.cesnet.meta.perun.api.VypocetniZdroj;
import cz.cesnet.meta.perun.impl.PerunJsonImpl;
import cz.cesnet.meta.storages.Storage;
import cz.cesnet.meta.storages.Storages;
import cz.cesnet.meta.storages.StoragesDiskArrayMotdImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Testy. Zkopiruj  target/pbsmon2/WEB-INF/spring-context.xml do target/classes/ a poedituj ho.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class Main {
    final static Logger log = LoggerFactory.getLogger(Main.class);



    public static void main10(String[] args) {
        Perun perun = new PerunJsonImpl(Arrays.asList("/etc/pbsmon/pbsmon_machines.json"), Collections.emptyList());
        VypocetniZdroj bofurA = perun.getVypocetniZdrojByName("bofur.ics.muni.cz");
        VypocetniZdroj bofurB = najdiZdroj(perun, "bofur.ics.muni.cz");
        System.out.println("bofurA = " + bofurA.getStroje());
        System.out.println("bofurB = " + bofurB.getStroje());

    }

    private static VypocetniZdroj najdiZdroj(Perun perun, String jmeno) {
        for (VypocetniCentrum centrum : perun.getFyzickeStroje().getCentra()) {
            for (VypocetniZdroj zdroj : centrum.getZdroje()) {
                if (zdroj.getId().equals(jmeno)) {
                    return zdroj;
                }
            }
        }
        return null;
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

    public static void main7(String[] args) {
        Pattern p = Pattern.compile("^host=([^:]+):.*:scratch_type=(\\w+):scratch_volume=([0-9]+[kmgp]b)");
        for (String s : Arrays.asList("host=luna43.fzu.cz:ppn=1:mem=409600KB:vmem=137438953472KB:scratch_type=local:scratch_volume=1024mb",
                "host=ramdal.ics.muni.cz:ppn=1:mem=409600KB:vmem=137438953472KB:scratch_type=shared:scratch_volume=1024mb")) {
            Matcher m = p.matcher(s);
            if (m.find()) {
                String hostname = m.group(1);
                String type = m.group(2);
                String volume = m.group(3);
                System.out.println("hostname = " + hostname);
                System.out.println("type = " + type);
                System.out.println("volume = " + volume);
            }
        }
    }

    public static void main6(String[] args) {
        PbsCacheImpl pbsCache = new PbsCacheImpl();
        pbsCache.setPbsServers(Arrays.asList(
                new PbsServerConfig("arien.ics.muni.cz", true, true, true, Collections.<FairshareConfig>emptyList()),
                new PbsServerConfig("wagap.cerit-sc.cz", false, true, true, Collections.<FairshareConfig>emptyList()),
                new PbsServerConfig("wagap-devel.cerit-sc.cz", false, true, false, Collections.<FairshareConfig>emptyList())
        ));

    }

    public static void main5(String[] args) throws InterruptedException {
        CloudImpl cloud = new CloudImpl();
        cloud.setDataMaxAgeInMilliseconds(20000l);
        cloud.setLoadRunTimeInMilliseconds(5000l);
        log.info("start");

        List<Actor> actors = Arrays.asList(
                new Actor(cloud, 1),
                new Actor(cloud, 2),
                new Actor(cloud, 3)
        );

        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (Actor actor : actors) {
            executorService.execute(actor);
        }
        executorService.shutdown();
    }


    static class Actor implements Runnable {
        private Logger log;
        private final CloudImpl cloud;

        Actor(CloudImpl cloud, int n) {
            this.cloud = cloud;
            this.log = LoggerFactory.getLogger("cz.cesnet.meta.Main.Actor" + n);
        }

        @Override
        public void run() {
            try {
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() < start + 90000l) {
                    log.info("loading");
                    cloud.getPhysicalHosts();
                    int refreshTime = cloud.getRefreshTime();
                    log.info("got them, refresh in {}s", refreshTime);
                    try {
                        Thread.sleep(5000l);
                    } catch (InterruptedException e) {
                        log.error("", e);
                    }
                }
            } catch (Throwable t) {
                log.error("in run", t);
            }
        }
    }


    public static void main4(String[] args) {

        Storages storages = new StoragesDiskArrayMotdImpl();
        List<Storage> storageList = storages.getStoragesInfo().getStorages();
        for (Storage storage : storageList) {
            System.out.println("" + storage);
        }
        System.out.println("storages.getStoragesInfo() = " + storages.getStoragesInfo());
    }

    public static void main3(String[] args) {
        log.info("starting");
        RestTemplate rt = new RestTemplate();
        List<CloudPhysicalHost> physicalHosts = rt.getForObject("http://carach1.ics.muni.cz:12147/exports/hosts.json", HostsDocument.class).getHosts();
        for (CloudPhysicalHost physicalHost : physicalHosts) {
            if (physicalHost.getName().equals("ceriha2j.cerit-sc.cz") || physicalHost.getName().equals("hda31.cerit-sc.cz")) {
                CloudPhysicalHost.ParsedName parsedName = physicalHost.getParsedName();
                System.out.println("parsedName = " + parsedName);
            }
        }


    }

    public static void main2(String[] args) {
        ApplicationContext springCtx = new ClassPathXmlApplicationContext("spring-context.xml");
        Pbsky pbsky = springCtx.getBean("pbsky", Pbsky.class);
        PbsCache pbsCache = springCtx.getBean("pbsCache", PbsCache.class);

        Node apollo2 = pbsky.getNodeByName("apollo2.fzu.cz");
        System.out.println("apollo2 = " + apollo2);
        System.out.println("apollo2.getScratch() = " + apollo2.getScratch());
        System.out.println("scratchApollo2 = " + pbsCache.getScratchForNode(apollo2));

        Node apollo1 = pbsky.getNodeByName("apollo1.fzu.cz");
        System.out.println("apollo1 = " + apollo1);
        System.out.println("apollo1.getScratch() = " + apollo1.getScratch());
        System.out.println("scratchApollo1 = " + pbsCache.getScratchForNode(apollo1));

        Node gram2 = pbsky.getNodeByName("gram2.zcu.cz");
        System.out.println("gram2 = " + gram2);
        System.out.println("gram2.getScratch() = " + gram2.getScratch());
    }
}
