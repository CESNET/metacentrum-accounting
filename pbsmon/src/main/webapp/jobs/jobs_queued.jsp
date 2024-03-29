<%@ page import="cz.cesnet.meta.pbs.FairshareConfig" %>
<%@ page import="cz.cesnet.meta.pbs.Job" %>
<%@ page import="cz.cesnet.meta.pbs.PBS" %>
<%@ page import="cz.cesnet.meta.pbs.Queue" %>
<%@ page pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<f:message var="titlestring" key="jobs_queued.titul" scope="request"/>
<s:layout-render name="/layout.jsp">
    <s:layout-component name="telo">
        <style>
            td.job_plan_comment_col {
                width: 25%;
            }

            td.job_res_col {
                max-width: 20em;
                word-break: break-all;
            }

            td.job_tc_time {
                white-space: nowrap;
            }
        </style>
        <s:useActionBean beanclass="cz.cesnet.meta.stripes.QueuesActionBean" var="actionBean"/>

        <!-- rozskok -->
        <c:forEach items="${actionBean.pbs}" var="pbs">
            <h3>Server ${pbs.server.host} - <f:message key="server_${pbs.server.host}"/></h3>

            <ul>
                <c:choose>
                    <c:when test="${pbs.serverConfig.planbased}">
                        <li><a href="#${pbs.server.name}">plan</a></li>
                    </c:when>
                    <c:when test="${pbs.serverConfig.by_queue}">
                        <c:forEach items="${pbs.queuesByPriority}" var="q">
                            <c:if test="${q.jobsQueued>0}">
                                <li><f:message key="queues_queue"/> <a href="#${q.name}">${q.name}</a> : ${q.jobsQueued}
                                </li>
                            </c:if>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <c:forEach items="${pbs.serverConfig.fairshares}" var="fc">
                            <li>fairshare <a href="#${fc.id}"><c:out value="${fc.id}"/></a></li>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
                <li><f:message key="nodes_jsp_waiting_text1"/>
                    <strong><f:message key="nodes_jsp_waiting_jobs"><f:param
                            value="${pbs.jobsQueuedCount}"/></f:message></strong>
                    <f:message key="nodes_jsp_waiting_text3"/></li>
            </ul>
            <!-- duvody cekani -->
            <table class="job">
                <tr>
                    <th><f:message key="jobs_pocet"/></th>
                    <th><f:message key="jobs_queued_duvod"/></th>
                </tr>
                <c:forEach items="${actionBean.duvody[pbs.server.host]}" var="duvod">
                    <tr>
                        <td style="text-align: right;">${duvod.pocet}x</td>
                        <td style="text-align: left;">
                            <c:choose>
                                <c:when test="${fn:length(duvod.text)>125}"><c:out
                                        value="${fn:substring(duvod.text, 0, 125)}"/> ...</c:when>
                                <c:otherwise><c:out value="${duvod.text}"/></c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>

            </table>

        </c:forEach>

        <!-- telo -->
        <% String contextPath = request.getContextPath();
            for (PBS pbs : actionBean.getPbs()) {
                pageContext.setAttribute("pbs", pbs);
        %>
        <hr>
        <h3>Server ${pbs.server.host} - <f:message key="server_${pbs.server.host}"/></h3>

        <% if (pbs.getServerConfig().isPlanbased()) { %>
        <a id="<%=pbs.getServer().getName()%>"></a>
        <table class="job">
            <tr>
                <th><f:message key="jobs_job"/></th>
                <th><f:message key="jobs_ncpu"/></th>
                <th>RAM</th>
                <th><f:message key="job_resource_nodes"/></th>
                <th><f:message key="jobs_user"/></th>
                <c:if test="${param.showFairshare}">
                    <th><s:link href="/users#fairshare">fairshare*</s:link></th>
                </c:if>
                <th colspan="2"><f:message key="jobs_planned_start"/></th>
                <th><f:message key="job_planned_nodes"/></th>
                <th colspan="2"><f:message key="jobs_ctime"/></th>
                <th><f:message key="jobs_waiting_reason"/></th>
            </tr>

            <%
                for (Job job : actionBean.getPlannedJobs().get(pbs.getServer().getHost())) {
            %>
            <tr>
                <td class="job_plan_name_col"><a href="<%=contextPath+"/job/"+job.getName()%>" style="font-size: xx-small"><%=job.getName()%></a></td>
                <td class="job_plan_cpu_col" style="text-align: center;"><%=job.getNoOfUsedCPU()%></td>
                <td class="job_plan_mem_col" style="text-align: right;"><%=job.getReservedMemoryTotal()%></td>
                <td class="job_plan_res_col" style="text-align: left; font-size: xx-small;">
                    <%=(job.getResourceNodes()!=null&&job.getResourceNodes().length()>50)?job.getResourceNodes().substring(0,50)+"...":job.getResourceNodes()%>
                </td>
                <td style="text-align: right;"><a href="<%=contextPath+"/user/"+job.getUser()%>"><%=job.getUser()%></a></td>
                <c:if test="${param.showFairshare}"><td style="text-align: right;"><%=job.getFairshareRank()%></td></c:if>
                <% if (job.getPlannedStart() != null) { %>
                <td style="text-align: right;"><f:formatDate value="<%=job.getPlannedStart()%>" type="date" dateStyle="short"/></td>
                <td style="text-align: right;"><f:formatDate value="<%=job.getPlannedStart()%>" type="time" timeStyle="short"/></td>
                <% } else { %>
                <td colspan="2"></td>
                <% } %>
                <td style="text-align: left;"><%if (job.getPlannedNodes() != null) {%><a
                        href="<%=contextPath+"/job/"+job.getName()%>"><%=job.getPlannedNodesShort()%>
                </a><%}%></td>
                <% if (job.getTimeCreated() != null) { %>
                <td style="text-align: right;"><f:formatDate value="<%=job.getTimeCreated()%>" type="date" dateStyle="short"/></td>
                <td style="text-align: right;"><f:formatDate value="<%=job.getTimeCreated()%>" type="time" timeStyle="short"/></td>
                <% } else { %>
                <td colspan="2"></td>
                <% } %>
                <td class="job_plan_comment_col" style="font-size: xx-small; text-align: left;"><c:out
                        value='<%=(job.getComment()!=null&&job.getComment().length()>60)?job.getComment().substring(0,60)+"...":job.getComment()%>'/></td>
            </tr>
            <%

                }
            %>
        </table>
        <% } else if (pbs.getServerConfig().isBy_queue()) { %>
        <p><f:message key="jobs_queued_byqueuetrue"/></p>
        <c:forEach items="${pbs.queuesByPriority}" var="q">
            <c:if test="${q.jobsQueued>0}">
                <hr/>
                <a name="${q.name}"></a>
                <table class="queue">
                    <t:queue_heading/>
                    <t:queue_line queue="${q}"/>
                </table>

                <table class="job">
                    <tr>
                        <th><f:message key="jobs_job"/></th>
                        <th><f:message key="jobs_ncpu"/></th>
                        <th><f:message key="job_resource_nodes"/></th>
                        <th><f:message key="jobs_mem_reserved"/></th>
                        <th><f:message key="jobs_user"/></th>
                        <th><s:link href="/users#fairshare">fairshare*</s:link></th>
                        <th colspan="2"><f:message key="jobs_ctime"/></th>
                        <th><f:message key="jobs_waiting_reason"/></th>
                    </tr>

                    <%
                        Queue q = (Queue) pageContext.findAttribute("q");
                        for (Job job : actionBean.getQ2qJobs().get(q.getName())) {
                    %>
                    <tr>
                        <td><a href="<%=contextPath+"/job/"+job.getName()%>"
                               style="font-size: xx-small"><%=job.getName()%>
                        </a>
                        <td align="center"><%=job.getNoOfUsedCPU()%>
                        </td>
                        <td class="job_byqueue_res_col" align="left"><%=job.getResourceNodes()%>
                        </td>
                        <td align="right"><%=job.getReservedMemoryTotal()%>
                        </td>
                        <td align="right"><a href="<%=contextPath+"/user/"+job.getUser()%>"><%=job.getUser()%>
                        </a></td>
                        <td align="right"><%=job.getFairshareRank()%>
                        </td>
                        <td align="right"><f:formatDate value="<%=job.getTimeCreated()%>" type="date"
                                                        dateStyle="short"/></td>
                        <td align="right"><f:formatDate value="<%=job.getTimeCreated()%>" type="time"
                                                        timeStyle="short"/></td>
                        <td align="left" style="font-size: xx-small;"><c:out value="<%=job.getComment()%>"/></td>
                    </tr>
                    <%
                        }
                    %>
                </table>
            </c:if>
        </c:forEach>
        <% } else { %>
        <p><f:message key="jobs_queued_byqueuefalse"/></p>

        <% for (FairshareConfig fc : pbs.getServerConfig().getFairshares()) { %>
        <p><a name="<%=fc.getId()%>">fairshare: <%=fc.getId()%>
        </a></p>
        <table class="job">
            <tr>
                <th><f:message key="jobs_job"/></th>
                <th><f:message key="jobs_ncpu"/></th>
                <th><f:message key="job_resource_nodes"/></th>
                <th><f:message key="jobs_mem_reserved"/></th>
                <th><f:message key="jobs_user"/></th>
                <th><f:message key="queues_queue"/></th>
                <th><f:message key="queues_priority"/></th>
                <th><s:link href="/users#fairshare">fairshare*</s:link></th>
                <th colspan="2"><f:message key="jobs_ctime"/></th>
                <th><f:message key="jobs_waiting_reason"/></th>
            </tr>

            <%
                for (Job job : actionBean.getQ2qJobs().get(fc.getId())) {
            %>
            <tr>
                <td><a href="<%=contextPath+"/job/"+job.getName()%>"
                       style="font-size: xx-small"><%=job.getName()%>
                </a>
                <td align="center"><%=job.getNoOfUsedCPU()%>
                </td>
                <td class="job_res_col" align="left"><%=job.getResourceNodes()%>
                </td>
                <td align="right"><%=job.getReservedMemoryTotal()%>
                </td>
                <td align="right"><a href="<%=contextPath+"/user/"+job.getUser()%>"><%=job.getUser()%>
                </a></td>
                <td align="right"><a
                        href="<%=contextPath+"/queue/"+job.getQueueName()%>"><%=job.getQueueShortName()%>
                </a></td>
                <td align="right"><%=job.getQueue().getPriority()%>
                </td>
                <td align="right"><%=job.getFairshareRank()%>
                </td>
                <td class="job_tc_date" align="right"><f:formatDate value="<%=job.getTimeCreated()%>" type="date"
                                                                    dateStyle="short"/></td>
                <td class="job_tc_time" align="right"><f:formatDate value="<%=job.getTimeCreated()%>" type="time"
                                                                    timeStyle="short"/></td>
                <td class="job_comment_col" align="left" style="font-size: xx-small;"><c:out
                        value="<%=job.getComment()%>"/></td>
            </tr>
            <%

                }
            %>
        </table>
        <%
                    }
                }
            }
        %>

    </s:layout-component>
</s:layout-render>
