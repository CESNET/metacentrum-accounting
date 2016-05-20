package cz.muni.ics.cerit.stats;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Čte soubor formátu uvedeného dále a synchronizuje jeho obsah se záznamy plánovaných výpadků v databázi.
 * <p/>
 * Formát:
 * <pre>
 * 2013-10-14,,maintenance,"reseni nestability, HW",hdc33.cerit-sc.cz,zapat33.cerit-sc.cz
 * 2013-10-01,2013-11-24,reserved,"testy MM/CEZ",hda1.cerit-sc,hda2.cerit-sc,cz,zapat1.cerit-sc.cz,zapat2.cerit-sc.cz
 * </pre>
 * <ul>
 * <li>první sloupec je čas začátku, ve tvaru datum nebo datum a čas; pokud není uveden čas, je to od začátku dne</li>
 * <li>druhý sloupce je čas konce, ve tvaru datum nebo datum a čas nebo prázdná hodnota; pokud nejsou uvedeny hodiny, je to do konce dne</li>
 * <li>třetí sloupce je druh, bereme ho v potaz jen pokud obsahuje slovo "scheduled", např. "scheduled maintenance"</li>
 * <li>čtvrtý soupce je komentář</li>
 * <li>další sloupce obsahují id clusterů nebo strojů z Peruna, případně virtuální stroje, které se ignorují</li>
 * </ul>
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class ScheduledDowntimeSync {

    final static Logger log = LoggerFactory.getLogger(ScheduledDowntimeSync.class);

    public static void main(String[] args) throws IOException, ParseException {
        //just parse the file to see how it works
        InputStream sd = new URL("https://raw.github.com/CERIT-SC/cerit-maintenance/master/maintenance.csv").openStream();
        List<ScheduledDowntimesImpl.Line> lines = ScheduledDowntimesImpl.parseCsvFile(sd);
        System.out.println();
        for(ScheduledDowntimesImpl.Line line : lines) {
            System.out.println("line = " + line);
        }
    }

    public static void syncScheduledDowntimes(ApplicationContext springCtx) throws IOException, ParseException {
        ScheduledDowntimes scheduledDowntimes = springCtx.getBean("scheduledDowntimes", ScheduledDowntimes.class);
        Accounting accounting = springCtx.getBean("acct", Accounting.class);

        InputStream sd = new URL("https://raw.github.com/CERIT-SC/cerit-maintenance/master/maintenance.csv").openStream();
        //InputStream sd = new FileInputStream("scheduled.csv");
        List<ResourceAtDay> removedStats = scheduledDowntimes.syncDowntimes(sd);

        for (ResourceAtDay rat : removedStats) {
            DateTime day = new DateTime(rat.getDay());
            log.info("recomputing stats for {} on {}", rat.getResourceName(), rat.getDay());
            ResourceAvailability a = accounting.getAvailability(rat.getResourceName(), day);
            accounting.storeStatsForDay(rat.getResourceName(), day, a);
        }
    }


}
