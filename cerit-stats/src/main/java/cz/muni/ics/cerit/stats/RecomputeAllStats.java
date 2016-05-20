package cz.muni.ics.cerit.stats;

import cz.cesnet.meta.perun.api.Perun;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.text.ParseException;

import static cz.muni.ics.cerit.stats.ScheduledDowntimeSync.syncScheduledDowntimes;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class RecomputeAllStats {

    public static void main(String[] args) throws IOException, ParseException {
        ApplicationContext springCtx = new ClassPathXmlApplicationContext("spring-context.xml");
        syncScheduledDowntimes(springCtx);
        Perun perun = springCtx.getBean("perun", Perun.class);
        Accounting accounting = springCtx.getBean("acct", Accounting.class);
        Stats.allTimeStats(perun,accounting);
    }

}
