<%@ page import="cz.cesnet.meta.pbs.Job" %>
<%@ page import="cz.cesnet.meta.stripes.UserActionBean" %>
<%@ page import="org.apache.taglibs.standard.util.EscapeXML" %>
<%@ page pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<s:useActionBean beanclass="cz.cesnet.meta.stripes.UserActionBean" var="actionBean"/>
<f:message var="titlestring" key="user.titul" scope="request"><f:param value="${actionBean.userName}"/></f:message>
<s:layout-render name="/layout.jsp">
    <s:layout-component name="telo">

<c:if test="${(! empty actionBean.perunUser) and (! empty actionBean.userInfo)}">
    <p><f:message key="user_jsp_hlaska_kdo">
        <f:param value="${actionBean.perunUser.name}"/>
        <f:param value="${actionBean.perunUser.organization}"/>
        <f:param value="${actionBean.perunUser.researchGroup}"/>
        <f:param value="${actionBean.perunUser.mainVoName}"/>
        <f:param value="${actionBean.perunUser.expires}"/>
    </f:message>
    <f:message key="user_jsp_hlaska_propocital">
        <f:param value="${actionBean.userInfo.jobCount}"/>
        <f:param value="${actionBean.userInfo.cpuDaysUsed}"/>
    </f:message>
    <f:message key="user_jsp_hlaska_publikace">
      <f:param value="${actionBean.perunUser.publications['MetaCentrum']}"/>
      <f:param value="${actionBean.perunUser.publications['CERIT-SC']}"/>
    </f:message>
    </p>
    <table class="zakladni">
        <tr><th><f:message key="user.rok"/></th><th><f:message key="jobs_pocet"/></th><th><f:message key="user.cpudny"/></th></tr>
    <c:forEach items="${actionBean.userInfo.usages}" var="rok">
        <tr>
            <td><c:out value="${rok.year}"/></td>
            <td><c:out value="${rok.jobs}"/></td>
            <td style="text-align: right;"><f:formatNumber maxFractionDigits="1" minFractionDigits="1" value="${rok.cpuDays}"/></td>
        </tr>
    </c:forEach>
    </table>
</c:if>

        <t:vms_list vms="${actionBean.userVMs}"/>

        <h2><f:message key="user_jsp_ulohy_v_PBS"/></h2>

<c:choose>
    <c:when test="${! empty actionBean.user}">

   <!-- celkem -->
     <table class="users">
         <tr>
             <th rowspan="2"><f:message key="queues_queue"/></th>
             <th colspan="5"><f:message key="users_jobs_count"/></th>
             <th colspan="5"><f:message key="users_jobs_ncpu"/></th>
         </tr>

         <c:set var="tokens"
                value="jobsTotal,jobsStateQ,jobsStateR,jobsStateC,jobsOther,cpusTotal,cpusStateQ,cpusStateR,cpusStateC,cpusOther"/>
         <tr>
             <c:forTokens items="${tokens}" delims="," var="P">
                 <th><f:message key="users_jobs_${P}"/></th>
             </c:forTokens>
         </tr>

         <c:forEach items="${actionBean.usedQueueNames}" var="qname">
             <tr>
                 <td><c:out value="${qname}"/></td>
                 <td align="center">${actionBean.jobInfosByQueue[qname].celkemJobs}</td>
                 <td align="center">${actionBean.jobInfosByQueue[qname].jobsInStateQ}</td>
                 <td align="center">${actionBean.jobInfosByQueue[qname].jobsInStateR}</td>
                 <td align="center">${actionBean.jobInfosByQueue[qname].jobsInStateC}</td>
                 <td align="center">${actionBean.jobInfosByQueue[qname].jobsInStateOther}</td>
                 <td align="center">${actionBean.jobInfosByQueue[qname].celkemCpu}</td>
                 <td align="center">${actionBean.jobInfosByQueue[qname].cpusInStateQ}</td>
                 <td align="center">${actionBean.jobInfosByQueue[qname].cpusInStateR}</td>
                 <td align="center">${actionBean.jobInfosByQueue[qname].cpusInStateC}</td>
                 <td align="center">${actionBean.jobInfosByQueue[qname].cpusInStateOther}</td>
             </tr>
         </c:forEach>
         <tr>
             <td><b><f:message key="users_jobs_jobsTotal"/></b></td>
             <c:forTokens items="${tokens}" delims="," var="P">
                 <td align="center"><b>${actionBean.user[P]}</b></td>
             </c:forTokens>
         </tr>
     </table>



     <hr>
     <!-- tabulka uloh -->
     <table class="job">
         <tr>
             <th><s:link href="/user/${actionBean.userName}/Id"><f:message key="jobs_job"/></s:link>
                 <c:if test="${actionBean.sort=='Id'}"><img src="${pageContext.request.contextPath}/img/arrow.jpeg" width="15" height="15" alt="sort"></c:if>
             </th>
             <th><s:link href="/user/${actionBean.userName}/CPU"><f:message key="jobs_ncpu"/></s:link>
                 <c:if test="${actionBean.sort=='CPU' or empty actionBean.sort}"><img src="${pageContext.request.contextPath}/img/arrow.jpeg" width="15" height="15" alt="sort"></c:if>
             </th>
             <th><s:link href="/user/${actionBean.userName}/ReservedMemTotal"><f:message key="jobs_mem_reserved"/></s:link>
                 <c:if test="${actionBean.sort=='ReservedMemTotal'}"><img src="${pageContext.request.contextPath}/img/arrow.jpeg" width="15" height="15" alt="sort"></c:if>
             </th>
             <th><s:link href="/user/${actionBean.userName}/UsedMem"><f:message key="jobs_mem_used"/></s:link>
                 <c:if test="${actionBean.sort=='UsedMem'}"><img src="${pageContext.request.contextPath}/img/arrow.jpeg" width="15" height="15" alt="sort"></c:if>
             </th>
             <th><s:link href="/user/${actionBean.userName}/Name"><f:message key="jobs_jobname"/></s:link>
                 <c:if test="${actionBean.sort=='Name'}"><img src="${pageContext.request.contextPath}/img/arrow.jpeg" width="15" height="15" alt="sort"></c:if>
             </th>
             <th><s:link href="/user/${actionBean.userName}/CPUTime"><f:message key="jobs_cputimeused"/></s:link>
                 <c:if test="${actionBean.sort=='CPUTime'}"><img src="${pageContext.request.contextPath}/img/arrow.jpeg" width="15" height="15" alt="sort"></c:if>
             </th>
             <th><s:link href="/user/${actionBean.userName}/WallTime"><f:message key="jobs_walltimeused"/></s:link>
                 <c:if test="${actionBean.sort=='WallTime'}"><img src="${pageContext.request.contextPath}/img/arrow.jpeg" width="15" height="15" alt="sort"></c:if>
             </th>
             <th><s:link href="/user/${actionBean.userName}/State"><f:message key="jobs_state"/></s:link>
                 <c:if test="${actionBean.sort=='State'}"><img src="${pageContext.request.contextPath}/img/arrow.jpeg" width="15" height="15" alt="sort"></c:if>
             </th>
             <th><f:message key="job_first_host"/></th>
             <th><s:link href="/user/${actionBean.userName}/Queue"><f:message key="jobs_queue"/></s:link>
                 <c:if test="${actionBean.sort=='Queue'}"><img src="${pageContext.request.contextPath}/img/arrow.jpeg" width="15" height="15" alt="sort"></c:if>
             </th>
             <th colspan="2"><s:link href="/user/${actionBean.userName}/Ctime"><f:message key="jobs_ctime"/></s:link>
                 <c:if test="${actionBean.sort=='Ctime'}"><img src="${pageContext.request.contextPath}/img/arrow.jpeg" width="15" height="15" alt="sort"></c:if>
             </th>
         </tr>         
         
         
         
    <% //přepsáno kvůli efektivitě
      UserActionBean jbean = (UserActionBean)pageContext.findAttribute("actionBean");
        String contextPath = request.getContextPath();
        for(Job job : jbean.getJobs()) {
%>
         <tr>
         <td><a href="<%=contextPath+"/job/"+job.getName()%>" style="font-size: xx-small"><%=job.getName()%></a>
         <td align="center"><%=job.getNoOfUsedCPU()%></td>
         <td align="right"><%=job.getReservedMemoryTotal()%></td>
         <td align="right" <%if(job.getMemoryExceeded()){out.print("class=\"memexceeded\"");}%> ><%=job.getUsedMemory()%></td>
         <td align="right"><%=job.getJobName()%></td>
         <td align="right" <%if(job.isUnderusingCPUs()){out.print("class=\"wasting\"");}%> ><%=job.getCPUTimeUsed()%></td>
         <td align="right"><%=job.getWallTimeUsed()%></td>
         <td align="center" class="<%=job.getState()%>"><%=job.getState()%> - <f:message key='<%="jobs_"+job.getState()%>'/>
             <% if("F".equals(job.getState())) {
                 String jobComment = job.getComment();
                 if(jobComment!=null) jobComment = EscapeXML.escape(jobComment);
                 out.print("<span title='"+ jobComment+"'>(exit " + job.getExitStatus() + ")</span>");
             }%>
         </td>
         <td align="left">
     <%if("R".equals(job.getState())&&job.getExecHostFirst()!=null) { pageContext.setAttribute("job",job);%>

     <s:link href="/node/${job.execHostFirstName}">${actionBean.nodesShortNamesMap[job.execHostFirstName]}/${job.execHostFirstCPU}</s:link>
     <%}%>
 </td>
 <td align="center"><a href="<%=contextPath+"/queue/"+job.getQueueName()%>"><%=job.getQueueName()%></a>
 <td align="right"><f:formatDate value="<%=job.getTimeCreated()%>" type="date" dateStyle="short" /></td>
 <td align="right"><f:formatDate value="<%=job.getTimeCreated()%>" type="time" timeStyle="short" /></td>
</tr><%
      }
    %>


</table>

 </c:when>
 <c:otherwise>
  <p><f:message key="user_headline"/> <strong><c:out value="${actionBean.userName}"/></strong> <f:message key="user_skoncil"/></p>
 </c:otherwise>
</c:choose>

   </s:layout-component>
</s:layout-render>
