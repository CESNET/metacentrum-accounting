package cz.cesnet.meta.perun.impl;

import cz.cesnet.meta.perun.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: PerunAbstractImpl.java,v 1.1 2011/10/27 14:19:55 makub Exp $
 */
public abstract class PerunAbstractImpl implements Perun {

    final static Logger log = LoggerFactory.getLogger(PerunAbstractImpl.class);

    /**
     * Najde výpočetní centra, v nich zdroje (clustery a stroje). Spočítá počty CPU.
     * Najde stroje, které jsou v MetaCentru, ale ne v žádném centru.
     *
     * @return složitá struktura
     */
    @Override
    public FyzickeStroje getFyzickeStroje() {
        List<VypocetniCentrum> centra;
        List<Stroj> zbyle;
        Map<String, Integer> cpuMap;

        List<Stroj> vsechnyStroje = this.getMetacentroveStroje();
        Set<Stroj> fyzickeSet = new HashSet<>(vsechnyStroje);
        cpuMap = new HashMap<>();

        centra = this.najdiVypocetniCentra();
        for (VypocetniCentrum c : centra) {
            int cpuSumCentrum = 0;
            for (VypocetniZdroj z : c.getZdroje()) {
                if (z.isCluster()) {
                    int cpuSumCluster = 0;
                    for (Stroj stroj : z.getStroje()) {
                        cpuSumCentrum += stroj.getCpuNum();
                        cpuSumCluster += stroj.getCpuNum();
                        fyzickeSet.remove(stroj);
                    }
                    cpuMap.put(z.getId(), cpuSumCluster);
                } else {
                    Stroj stroj = z.getStroj();
                    if (stroj == null) {
                        log.error("zdroj " + z.getNazev() + " z VypocentiCentrum " + c.getId() + " nema Stroj, takze nevime pocet CPU");
                    } else {
                        cpuSumCentrum += stroj.getCpuNum();
                        fyzickeSet.remove(stroj);
                        cpuMap.put(z.getId(),stroj.getCpuNum());
                    }
                }
            }
            cpuMap.put(c.getId(), cpuSumCentrum);
        }

        zbyle = new ArrayList<>(fyzickeSet);
        zbyle.sort(Stroj.NAME_COMPARATOR);

        int cpuSum = 0;
        for (Stroj s : zbyle) {
            cpuSum += s.getCpuNum();
        }
        cpuMap.put("zbyle", cpuSum);
        cpuSum = 0;

        for (Stroj s : vsechnyStroje) {
            cpuSum += s.getCpuNum();

        }
        cpuMap.put("vsechny", cpuSum);
        return new FyzickeStroje(centra, zbyle, cpuMap);
    }


}
