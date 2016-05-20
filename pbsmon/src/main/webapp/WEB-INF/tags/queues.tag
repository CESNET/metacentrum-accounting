<%@ tag import="cz.cesnet.meta.pbs.Queue" %>
<%@ tag import="java.util.List" %>
<%@ tag import="org.slf4j.LoggerFactory" %>
<%@ tag import="org.slf4j.Logger" %>
<%@ tag import="cz.cesnet.meta.stripes.MachineActionBean" %>
<%@ tag %>
<%@ attribute name="queues" type="java.util.List" rtexprvalue="true" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%! Logger log = LoggerFactory.getLogger(MachineActionBean.class); %>

<% long stopky = System.currentTimeMillis();
    long starttime = stopky;
   log.debug("queues.tag start"); %>
<table class="queue">
<tr>
   <th rowspan="2"> <f:message key="queues_queue"/> </th>
    <%
            long ted = System.currentTimeMillis();
            log.debug("f:message queues_queue {}ms", ted - stopky);
            stopky = ted;
    %>
   <th rowspan="2"> &nbsp; </th>
   <th rowspan="2"> <f:message key="queues_priority"/> </th>
     <%
            ted = System.currentTimeMillis();
            log.debug("f:message timelimit {}ms", ted - stopky);
            stopky = ted;
    %>
   <th rowspan="2"> <f:message key="queues_timelimit"/> </th>
   <th rowspan="2"> <f:message key="queues_rprop"/> </th>
   <th colspan="5"> <f:message key="queues_jobs"/> </th>
 <tr>
   <th> <f:message key="queues_queued"/> </th>
   <th> <f:message key="queues_running"/>/<f:message key="queues_max_running"/> </th>
   <th> <f:message key="queues_completed"/> </th>
   <th> <f:message key="queues_total"/> </th>
   <th> <f:message key="queues_max_user_run"/> </th>
 </tr>
    <%
            ted = System.currentTimeMillis();
            log.debug("dalsi 9x f:message  {}ms", ted - stopky);
            stopky = ted;
    %>
<% for(Queue queue : (List<Queue>)queues) { jspContext.setAttribute("queue",queue); String tmp1 = "/queue/"+queue.getName();%>
<tr>
    <td style="text-align: right;"><s:link href="<%=tmp1%>"><%=queue.getName()%></s:link></td>
    <td><% if(queue.isLocked()) { %>
        <s:link href="/queues/list" anchor="urceni"><img border="0" src="<%=((HttpServletRequest)((PageContext)jspContext).getRequest()).getContextPath()%>/img/lock.png" alt="locked" title="<f:message key="queue_line_tag_locked"/>: <%=queue.getLockedFor()%>"/></s:link>
        <% } %></td>
    <td><%=queue.getPriority()%></td>
    <td> <% String wtime_min = queue.getAttributes().get("resources_min.walltime");
        if(wtime_min==null) out.print("0"); else out.print(wtime_min); %> -
         <% String wtime_max = queue.getAttributes().get("resources_max.walltime");
        if(wtime_max==null) out.print("0"); else out.print(wtime_max); %>
    </td>
    <td>
        <% String rp = queue.getRequiredProperty(); if(rp !=null) { %>
        <s:link href="/props" anchor="<%=rp%>"><%=rp%></s:link>
        <% } %>
    </td>
    <td><%=queue.getJobsQueued()%></td>
    <td><%=queue.getJobsRunning()%> / <%=queue.getMaxRunningJobs()==null ? "&infin;" : queue.getMaxRunningJobs() %></td>
    <td><%=queue.getJobsCompleted()%></td>
    <td><%=queue.getJobsTotal()%></td>
    <td><%=queue.getMaxUserRun()==null ? "&infin;" : queue.getMaxUserRun()%></td>
</tr>
<% } %>
</table>
<%
            ted = System.currentTimeMillis();
            log.debug("tabulka front v queues.tag  {}ms", ted - stopky);
            log.debug("queues.tag celkem {}ms", ted - starttime);
            stopky = ted;
    %>