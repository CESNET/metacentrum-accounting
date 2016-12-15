package cz.cesnet.meta.storages;

import cz.cesnet.meta.RefreshLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class StoragesDiskArrayMotdImpl extends RefreshLoader implements Storages {

    final static Logger log = LoggerFactory.getLogger(StoragesDiskArrayMotdImpl.class);
    private String file = "/home/makub/motd.storage";
    private StoragesInfo storagesInfo;

    public StoragesDiskArrayMotdImpl() {
        setDataMaxAgeInMilliseconds(3600 * 1000L);
    }

    public void setFile(String file) {
        this.file = file;
    }

    @Override
    public StoragesInfo getStoragesInfo() {
        checkLoad();
        return storagesInfo;
    }

    @Override
    protected void load() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            List<Storage> storages = new ArrayList<>();

            while ((line = in.readLine()) != null) {
                String[] strings = line.split(" ");
                String total = strings[0];
                String free = strings[1];
                String dir = strings[2];
                if (!dir.contains("archiv")&&!dir.contains("hsm")) {
                    Storage storage = new Storage(total, free, dir);
                    storages.add(storage);
                }
            }
            storagesInfo = new StoragesInfo(storages);
        } catch (FileNotFoundException e) {
            log.error("cannot find file " + file, e);
        } catch (IOException e) {
            log.error("cannot read file " + file, e);
        }
    }
}
