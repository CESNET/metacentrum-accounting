package cz.cesnet.meta.pbs;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: PbsConnector.java,v 1.2 2011/05/20 07:09:59 makub Exp $
 */
public interface PbsConnector {

    PBS loadData(PbsServerConfig serverConfig);
    
}
