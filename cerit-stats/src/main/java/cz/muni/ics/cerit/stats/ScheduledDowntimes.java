package cz.muni.ics.cerit.stats;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public interface ScheduledDowntimes {

    List<ResourceAtDay> syncDowntimes(InputStream sd) throws IOException, ParseException;
}
