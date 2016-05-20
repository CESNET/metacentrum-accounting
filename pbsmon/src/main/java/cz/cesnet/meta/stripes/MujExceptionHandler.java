package cz.cesnet.meta.stripes;

import net.sourceforge.stripes.exception.DefaultExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Osetreni jinak nezachycenych vyjimek a chyb.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: MujExceptionHandler.java,v 1.2 2013/10/10 12:18:14 makub Exp $
 */
public class MujExceptionHandler extends DefaultExceptionHandler {

    final static Logger log = LoggerFactory.getLogger(MujExceptionHandler.class);

    @SuppressWarnings({"UnusedDeclaration"})
    public void doslaPamet(OutOfMemoryError ome, HttpServletRequest req, HttpServletResponse res) {
        log.error("dosla pamet: ", ome);
        log.error("nema cenu nic delat, ukoncime Tomcat s navratovym kodem 100");
        System.exit(100);
    }

    public void catchAll(Throwable t, HttpServletRequest req, HttpServletResponse resp) throws Throwable {
        log.error("problem",t);
        throw t;
    }
}
