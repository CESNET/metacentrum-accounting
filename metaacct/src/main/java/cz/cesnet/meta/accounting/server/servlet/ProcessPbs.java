package cz.cesnet.meta.accounting.server.servlet;

import cz.cesnet.meta.accounting.server.service.PbsRecordManager;
import cz.cesnet.meta.accounting.server.util.PBSReader;
import cz.cesnet.meta.accounting.server.data.PBSRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.List;
import java.util.Date;

/**
 * Servlet implementation class for Servlet: ProcessKernel
 */
public class ProcessPbs extends HttpServlet {
    //private static Logger logger = Logger.getLogger(ProcessPbs.class);
    final static Logger log = LoggerFactory.getLogger(ProcessPbs.class);
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected void processRequest(HttpServletRequest request,
                                  HttpServletResponse response) throws ServletException, IOException {
        long startTimeNanos = System.nanoTime();

        log.debug("start: " + startTimeNanos / 1000000);

        PrintWriter out = response.getWriter();
        log.debug("printing xml:");


        WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        PbsRecordManager pbsRecordManager = (PbsRecordManager) ctx.getBean("pbsRecordManager");
        String remoteHost = InetAddress.getByName(request.getRemoteAddr()).getHostName();


        Date limit = new Date(System.currentTimeMillis()-720*3600000l);
        List<PBSRecord> records = null;
        try {
            records = PBSReader.readPBSFile(request.getInputStream(), limit);
        } catch (ParseException e) {
            log.error("cannot parse pbs records",e);
            throw new ServletException(e);
        }
        List<String> unsavedIdStrings = pbsRecordManager.saveRecords(records, remoteHost);

        log.debug("elapsed time: " + (System.nanoTime() - startTimeNanos) / 1000000);

        for (String s : unsavedIdStrings) {
            out.write("not used: " + s + "\n");
        }
        out.write("total not used job records: " + unsavedIdStrings.size());

        out.close();
    }

    /*
    * (non-Java-doc)
    *
    * @see javax.servlet.http.HttpServlet#HttpServlet()
    */
    public ProcessPbs() {
        super();
    }

    /*
    * (non-Java-doc)
    *
    * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request,
    *      HttpServletResponse response)
    */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /*
    * (non-Java-doc)
    *
    * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request,
    *      HttpServletResponse response)
    */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}