package cz.cesnet.meta.perun.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: FyzickeStroje.java,v 1.7 2011/10/31 09:27:49 makub Exp $
 */
public class FyzickeStroje {

    final static Logger log = LoggerFactory.getLogger(FyzickeStroje.class);

    List<VypocetniCentrum> centra;
    List<Stroj> zbyle;
    Map<String, Integer> cpuMap;

    public FyzickeStroje(List<VypocetniCentrum> centra, List<Stroj> zbyle, Map<String, Integer> cpuMap) {
        this.centra = centra;
        this.zbyle = zbyle;
        this.cpuMap = cpuMap;
    }

    public List<VypocetniCentrum> getCentra() {
        return centra;
    }

    public List<Stroj> getZbyle() {
        return zbyle;
    }

    public Map<String, Integer> getCpuMap() {
        return cpuMap;
    }


}
