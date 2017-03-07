package cz.cesnet.meta.accounting.server.service;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id:$
 */
public interface OutageManager {

    /**
     * Prepocita vsechny maintenenace a reserved a node down z eventu.
     */
    void computeOutages();

    void saveLogEvents(BufferedReader in, String server, boolean pbspro) throws IOException;
}
