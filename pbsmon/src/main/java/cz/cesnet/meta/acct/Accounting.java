package cz.cesnet.meta.acct;

import cz.cesnet.meta.pbs.Node;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: Accounting.java,v 1.3 2014/09/11 11:50:56 makub Exp $
 */
public interface Accounting {

    UserInfo getUserInfoByName(String userName);

    List<OutageRecord> getOutagesForNode(Node node);

    List<String> getStartedJobIds();

    List<Map<String, Object>> getCanonicalOrgNames();
}
