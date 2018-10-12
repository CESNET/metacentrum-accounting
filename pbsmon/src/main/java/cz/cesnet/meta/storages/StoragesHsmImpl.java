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

    public StoragesHsmImpl() throws NoSuchAlgorithmException, IOException, KeyManagementException {
        //doNotVerifyCertificates();
    }

    public void setUrl(String url) {
        this.url = url;
    }

//    public void setCerit4File(String cerit4File) {
//        this.cerit4File = cerit4File;
//    }

    @Override
    public StoragesInfo getStoragesInfo() {
        checkLoad();
        return storagesInfo;
    }

    @Override
    protected void load() {
        List<Storage> storagesList = new ArrayList<>();
        loadDuHsms(storagesList);
        //loadCeritHsms(storagesList);
        storagesInfo = new StoragesInfo(storagesList);
    }

//    private void loadCeritHsms(List<Storage> storagesList) {
//        log.info("loading CERIT storages from {}",cerit4File);
//        try (BufferedReader in = new BufferedReader(new FileReader(cerit4File))) {
//            String line;
//            int sizeGB = 0;
//            int freeGB = 0;
//            while ((line = in.readLine()) != null) {
//                String[] ss = line.split(" ", 2);
//                sizeGB += Storage.parseNum(ss[0]).numGB;
//                freeGB += Storage.parseNum(ss[1]).numGB;
//            }
//            storagesList.add(new Storage((sizeGB / 1024) + "T", (freeGB / 1024) + "T", "/storage/brno4-cerit-hsm"));
//        } catch (IOException e) {
//            log.error("cannot read file " + cerit4File, e);
//        }
//        log.info("loaded");
//    }

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
                        case "du1":
                            dir = "/storage/plzen2-archive";
                            break;
                        case "du2":
                            dir = "/storage/jihlava2-archive";
                            break;
                        case "du3":
                            dir = "/storage/brno5-archive";
                            break;
                        case "du4":
                            dir = "/storage/ostrava2-archive";
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


    void doNotVerifyCertificates() throws IOException, KeyManagementException, NoSuchAlgorithmException {
        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, new TrustManager[]{new AllTrustManager()}, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        HttpsURLConnection.setDefaultHostnameVerifier((urlHostName, session) -> {
            String peerHost = session.getPeerHost();
            if(!urlHostName.equals(peerHost)) log.warn("hostname {} and sslPeerHost {} do not match");
            return true;
        });

    }

    class AllTrustManager implements X509TrustManager {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) {
            if(certs!=null&&certs.length>0) {
                log.info("not checking server cert {}", certs[0].getSubjectDN().toString());
            } else {
                log.error("checkServerTrusted() certs empty !");
            }
        }
    }
}
