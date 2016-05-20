package cz.cesnet.meta.accounting.server.servlet;

import cz.cesnet.meta.accounting.server.service.OutageManager;
import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;

/**
 * Servlet for processing data from server log.
 *
 * @see cz.cesnet.meta.accounting.server.service.OutageManagerImpl#saveLogEvents(java.io.BufferedReader, String)
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id:$
 */
public class ProcessPbsServerLog extends HttpServlet {

    static private final Logger log = Logger.getLogger(ProcessPbsServerLog.class);

    @Override
    public void init() throws ServletException {
        log.debug("created");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.warn("GET from " + request.getRemoteHost());
        String all = request.getParameter("all");
        if (all != null) {
            OutageManager om = getOutageManager();
            om.computeOutages();
            sendMessage("Computed all outages", response);
        } else {
            sendError("Use POST method with " + request.getServletPath(), response);
        }
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


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("POST from " + request.getRemoteHost());
        BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream(), "utf-8"));
        try {
            OutageManager om = getOutageManager();
            om.saveLogEvents(in, InetAddress.getByName(request.getRemoteAddr()).getHostName());
            in.close();
            om.computeOutages();
            sendMessage("Log succesfully received", response);
        } catch (RuntimeException ex) {
            log.error("doPost()", ex);
            sendError(ex.getMessage(), response);
        }
    }

    private OutageManager getOutageManager() {
        WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        return (OutageManager) ctx.getBean("outageManager", OutageManager.class);
    }
}
