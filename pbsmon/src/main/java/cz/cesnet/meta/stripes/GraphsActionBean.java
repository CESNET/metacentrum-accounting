package cz.cesnet.meta.stripes;

import cz.cesnet.meta.pbs.Job;
import cz.cesnet.meta.pbs.PBS;
import cz.cesnet.meta.pbs.PbsUtils;
import cz.cesnet.meta.pbs.Pbsky;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: GraphsActionBean.java,v 1.8 2011/09/06 13:00:05 makub Exp $
 */
@UrlBinding("/jobs/graphs")
public class GraphsActionBean extends BaseActionBean {

    final static Logger log = LoggerFactory.getLogger(GraphsActionBean.class);

    //presunuto do JobsActionBean
}
