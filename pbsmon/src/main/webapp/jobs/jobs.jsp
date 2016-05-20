<%@ page pageEncoding="utf-8" import="cz.cesnet.meta.stripes.JobsActionBean" %>
<%@ page import="cz.cesnet.meta.pbs.Job" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<f:message var="titlestring" key="jobs.all.titul" scope="request"/>
<s:layout-render name="/layout.jsp">
    <s:layout-component name="telo">
        <s:useActionBean beanclass="cz.cesnet.meta.stripes.JobsActionBean" var="actionBean"/>


        <t:jobs_info jobsInfo="${actionBean.jobsInfo}"/>
  <br>
  <br>
        
<table class="job">
 <tr> 
   <th><s:link href="/jobs/allJobs/Id"><f:message key="jobs_job"/></s:link>
   <c:if test="${actionBean.trideni=='Id'}"><img src="${pageContext.request.contextPath}/img/arrow.jpeg" width="15" height="15" alt="sort"></c:if>
   </th>
   <th><s:link href="/jobs/allJobs/CPU"><f:message key="jobs_ncpu"/></s:link>
   <c:if test="${actionBean.trideni=='CPU' or empty actionBean.trideni}"><img src="${pageContext.request.contextPath}/img/arrow.jpeg" width="15" height="15" alt="sort"></c:if>
   </th>
   <th><s:link href="/jobs/allJobs/ReservedMemTotal"><f:message key="jobs_mem_reserved"/></s:link>
   <c:if test="${actionBean.trideni=='ReservedMemTotal'}"><img src="${pageContext.request.contextPath}/img/arrow.jpeg" width="15" height="15" alt="sort"></c:if>
   </th>
   <th><s:link href="/jobs/allJobs/UsedMem"><f:message key="jobs_mem_used"/></s:link>
   <c:if test="${actionBean.trideni=='UsedMem'}"><img src="${pageContext.request.contextPath}/img/arrow.jpeg" width="15" height="15" alt="sort"></c:if>
   </th>
<%--
   <th><s:link href="/jobs/allJobs/Name"><f:message key="jobs_jobname"/></s:link>
   <c:if test="${actionBean.trideni=='Name'}"><img src="${pageContext.request.contextPath}/img/arrow.jpeg" width="15" height="15" alt="sort"></c:if>
   </th>
--%>
   <th><s:link href="/jobs/allJobs/User"><f:message key="jobs_user"/></s:link>
   <c:if test="${actionBean.trideni=='User'}"><img src="${pageContext.request.contextPath}/img/arrow.jpeg" width="15" height="15" alt="sort"></c:if>
   </th>
   <th><s:link href="/jobs/allJobs/CPUTime"><f:message key="jobs_cputimeused"/></s:link>
   <c:if test="${actionBean.trideni=='CPUTime'}"><img src="${pageContext.request.contextPath}/img/arrow.jpeg" width="15" height="15" alt="sort"></c:if>
   </th>
   <th><s:link href="/jobs/allJobs/WallTime"><f:message key="jobs_walltimeused"/></s:link>
   <c:if test="${actionBean.trideni=='WallTime'}"><img src="${pageContext.request.contextPath}/img/arrow.jpeg" width="15" height="15" alt="sort"></c:if>
   </th>
   <th> <f:message key="jobs_state"/> </th>
   <th><s:link href="/jobs/allJobs/Queue"><f:message key="jobs_queue"/></s:link>
   <c:if test="${actionBean.trideni=='Queue'}"><img src="${pageContext.request.contextPath}/img/arrow.jpeg" width="15" height="15" alt="sort"></c:if>
   </th>
   <th colspan="2"><s:link href="/jobs/allJobs/Ctime"><f:message key="jobs_ctime"/></s:link>
   <c:if test="${actionBean.trideni=='Ctime'}"><img src="${pageContext.request.contextPath}/img/arrow.jpeg" width="15" height="15" alt="sort"></c:if>
   </th>
 </tr>
    <% //přepsáno kvůli efektivitě
      JobsActionBean jbean = (JobsActionBean)pageContext.findAttribute("actionBean");
        String contextPath = request.getContextPath();
        for(Job job : jbean.getJobs()) {
%><tr>
 <td><a href="<%=contextPath+"/job/"+job.getName()%>" style="font-size: xx-small"><%=job.getName()%></a>
 <td align="center"><%=job.getNoOfUsedCPU()%></td>
 <td align="right"><%=job.getReservedMemoryTotal()%></td>
 <td align="right" <%if(job.getMemoryExceeded()){out.print("class=\"memexceeded\"");}%> ><%=job.getUsedMemory()%></td>
 <td align="right"><a href="<%=contextPath+"/user/"+job.getUser()%>"><%=job.getUser()%></a></td>
 <td align="right" <%if(job.isUnderusingCPUs()){out.print("class=\"wasting\"");}%> ><%=job.getCPUTimeUsed()%></td>
 <td align="right"><%=job.getWallTimeUsed()%></td>
 <td align="center" class="<%=job.getState()%>"><%=job.getState()%> - <f:message key='<%="jobs_"+job.getState()%>'/> </td>
 <td align="center"><a href="<%=contextPath+"/queue/"+job.getQueueName()%>"><%=job.getQueueName()%></a>
 <td align="right"><f:formatDate value="<%=job.getTimeCreated()%>" type="date" dateStyle="short" /></td>
 <td align="right"><f:formatDate value="<%=job.getTimeCreated()%>" type="time" timeStyle="short" /></td>
</tr><%
      }
    %>


</table>
    </s:layout-component>
</s:layout-render>
