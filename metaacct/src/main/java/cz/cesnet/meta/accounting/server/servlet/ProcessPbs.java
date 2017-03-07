package cz.cesnet.meta.accounting.server.servlet;

import cz.cesnet.meta.accounting.server.service.PbsRecordManager;
import cz.cesnet.meta.accounting.server.util.PBSReader;
import cz.cesnet.meta.accounting.server.data.PBSRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final static Logger log = LoggerFactory.getLogger(ProcessPbs.class);

    public void doPost(HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException {
        String remoteHost = InetAddress.getByName(request.getRemoteAddr()).getHostName();
        log.info("received request from {}", remoteHost);
        try {
            long startTimeNanos = System.nanoTime();

            log.debug("start: " + startTimeNanos / 1000000);

            PbsRecordManager pbsRecordManager =
                    WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean(PbsRecordManager.class);



            Date limit = new Date(System.currentTimeMillis() - 720L * 3600_000L);

            List<PBSRecord> records;
            try {
                records = PBSReader.readPBSFile(request.getInputStream(), limit, "/Pro".equalsIgnoreCase(request.getPathInfo()));
            } catch (ParseException e) {
                log.error("cannot parse pbs records", e);
                throw new ServletException(e);
            }

            List<String> unsavedIdStrings = pbsRecordManager.saveRecords(records, remoteHost);

            log.debug("elapsed time: " + (System.nanoTime() - startTimeNanos) / 1000000);

            PrintWriter out = response.getWriter();
            for (String s : unsavedIdStrings) {
                out.println("not used: " + s);
            }
            out.println("total not used job records: " + unsavedIdStrings.size());
            out.close();
        } catch (Exception ex) {
            log.error("Cannot process request from "+remoteHost,ex);
            throw ex;
        }
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        sendError("Use POST method with " + request.getContextPath() + request.getServletPath(), response);
    }


    private void sendError(String error, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        out.println("<html><head><title>400 BAD REQUEST</title></head><body><h1>400 BAD REQUEST</h1>");
        out.println(error + "</body></html>");
        out.close();
    }

    private void sendMessage(String error, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();
        response.setStatus(HttpServletResponse.SC_OK);
        out.println("<html><head><title>200 OK</title></head><body><h1>200 OK</h1>");
        out.println(error + "</body></html>");
        out.close();
    }
}