package cz.cesnet.meta.pbs;

import com.sdicons.json.mapper.JSONMapper;
import com.sdicons.json.parser.JSONParser;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.io.BufferedInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: PbsConnectorJson.java,v 1.2 2011/05/20 07:09:59 makub Exp $
 */
public class PbsConnectorJson implements PbsConnector {
    final static Logger log = LoggerFactory.getLogger(PbsConnectorJson.class);

    @Override
    public PBS loadData(PbsServerConfig serverConfig) {
        String server = serverConfig.getHost();
        log.debug("loadData({})", server);
        HttpURLConnection uc;
        try {
            uc = (HttpURLConnection) new URL("http://"+server+":6666/pbs").openConnection();
            InputStream inputStream = uc.getInputStream();
            BufferedInputStream buf = new BufferedInputStream(inputStream,1000000);
            PBS pbs = (PBS) JSONMapper.toJava(new JSONParser(buf).nextValue(), PBS.class);
            buf.close();
            return pbs;
        } catch (Exception ex) {
            log.error("loadPBS() cannot read from " + server, ex);
        }
        return null;
    }
}
