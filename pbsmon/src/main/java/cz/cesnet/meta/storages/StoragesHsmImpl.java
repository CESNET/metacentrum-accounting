package cz.cesnet.meta.storages;

import cz.cesnet.meta.RefreshLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class StoragesHsmImpl extends RefreshLoader implements Storages {

    //final static Logger log = LoggerFactory.getLogger(StoragesHsmImpl.class);
    private String url;
//    private String cerit4File;

    private StoragesInfo storagesInfo;

    public StoragesHsmImpl() {
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public StoragesInfo getStoragesInfo() {
        checkLoad();
        return storagesInfo;
    }

    @Override
    protected void load() {
        List<Storage> storagesList = new ArrayList<>();
        loadDuHsms(storagesList);
        storagesInfo = new StoragesInfo(storagesList);
    }

    private void loadDuHsms(List<Storage> storagesList) {
        log.info("Loading DU storages from {}",url);
        try {
            URL url = new URL(this.url);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String line;
                int lineNum = 0;
                while ((line = in.readLine()) != null) {
                    if (++lineNum == 1) continue;
                    String[] ss = line.split(",");
                    //Storage,Size[TiB],Avail[TiB],Used[TiB]
                    String dir;
                    switch (ss[0]) {
                        case "du4":
                            dir = "/storage/du-cesnet";
                            break;
                        default:
                            continue;
                    }
                    storagesList.add(new Storage(ss[1] + "T", ss[2] + "T", dir));
                }
            } catch (IOException e) {
                log.error("cannot load "+this.url, e);
            }
        } catch (MalformedURLException e) {
            log.error("cannot parse URL " + this.url, e);
        }
        log.info("loaded");
    }
}
