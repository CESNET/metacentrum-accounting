<%@ page pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<s:useActionBean beanclass="cz.cesnet.meta.stripes.QueueActionBean" var="actionBean"/>
<c:set var="q" value="${actionBean.queue}" scope="request" />
<f:message var="titlestring" key="queue.titul" scope="request"><f:param value="${q.name}"/></f:message>
<s:layout-render name="/layout.jsp">
    <s:layout-component name="telo">



            <c:choose>
                <c:when test="${q.descriptionAvailable}">
                    <p style="display: table-cell;" class="comment">
                    <c:out value="${q.descriptionMap[pageContext.request.locale]}"/><!-- from pbs -->
                    </p>
                </c:when>
                <%--<c:otherwise><f:message key="q_${q.shortName}_txt" /><!-- from ResourceBundle --></c:otherwise>--%>
            </c:choose>


<c:choose>
    <c:when test="${actionBean.queue.routing}">
        <p style="clear: right">
            <t:i18n cs="Fronta je směrovací, rozděluje úlohy podle walltime do následujících front:"
                    en="The queue is routing, it delivers jobs depending on their walltime to the following queues:"/>

        </p>
        <table class="queue">
            <t:queue_heading/>
            <c:forEach items="${actionBean.destinations}" var="dst">
            <t:queue_line queue="${dst}"/>
            </c:forEach>
        </table>
    </c:when>
    <c:when test="${actionBean.queue.reservationQueue}">
        <c:set value="${actionBean.queue.reservation}" var="resv"/>
        <p style="clear: right">
            Fronta je vytvořena pro rezervaci.
        </p>
        <table class="reservations">
            <tr>
                <th>id</th>
                <th>name</th>
                <th>owner</th>
                <th>start</th>
                <th>end</th>
                <th>ctime</th>
                <th>select</th>
            </tr>
            <tr>
                <td><c:out value="${resv.name}"/></td>
                <td><c:out value="${resv.reserveName}"/></td>
                <td><c:out value="${resv.owner}"/></td>
                <td><f:formatDate value="${resv.reserveStart}" dateStyle="medium" timeStyle="short" type="both"/></td>
                <td><f:formatDate value="${resv.reserveEnd}" dateStyle="medium" timeStyle="short" type="both"/></td>
                <td><f:formatDate value="${resv.createdTime}" dateStyle="medium" timeStyle="short" type="both"/></td>
                <td><c:out value="${resv.select}"/></td>
            </tr>
        </table>

        Nodes:
        <t:node_table nodes="${resv.nodes}"/>

        Reservation attributes:
        <table class="attributes">
            <c:forEach items="${resv.attributes}" var="a">
                <tr>
                    <td>${a.key}</td>
                    <td>${a.value}</td>
                </tr>
            </c:forEach>
        </table>
    </c:when>
    <c:otherwise>


<table class="queue">
    <t:queue_heading/>
    <t:queue_line queue="${q}"/>
</table>
<br />
<c:if test="${q.locked}">
    <table class="queue">
 <c:if test="${q.aclUsersEnabled}">
        <tr>
            <th><img src="${pageContext.request.contextPath}/img/lock.png" alt="locked" />  <f:message key="jobs_locked_users"/></th>
            <td><c:forEach var="u" items="${q.aclUsersArray}"><s:link href="/user/${u}"><c:out value="${u}" /></s:link>  </c:forEach></td>
        </tr>
 </c:if>
 <c:if test="${q.aclGroupsEnabled}">
        <tr>
            <th><img src="${pageContext.request.contextPath}/img/lock.png" alt="locked" />  <f:message key="jobs_locked_groups"/></th>
            <td><c:forEach var="group" items="${q.aclGroupsArray}"><s:link href="/group/${q.pbs.host}/${group}">${group}</s:link> </c:forEach></td>
        </tr>
 </c:if>
 <c:if test="${q.aclHostsEnabled}">
        <tr>
            <th><img src="${pageContext.request.contextPath}/img/lock.png" alt="locked" />  <f:message key="jobs_locked_hosts"/></th>
            <td><c:forEach var="host" items="${q.aclHostsArray}"><c:out value="${host}" /> </c:forEach></td>
        </tr>
 </c:if>
    </table>
    <br />
</c:if>






<c:choose>
    <c:when test="${q.maintenance || q.reserved}">
        <table class="nodes">
            <c:forEach items="${actionBean.nodes}" var="node">
                <tr>
                <t:node node="${node}"/>
                <td><c:out value="${node.comment}"/></td>    
                </tr>
            </c:forEach>
        </table>
    </c:when>
    <c:otherwise>
        <h3><f:message key="q_nodes" /></h3>
        <t:node_table nodes="${actionBean.nodes}"/>

        <h3><f:message key="q_machines" /></h3>
        <table class="nodes" cellspacing="0">
            <tr>
                <c:forEach items="${actionBean.machines}" var="perunMachine" varStatus="i">
                <td class="node ${perunMachine.state}"><s:link href="/machine/${perunMachine.name}"><c:out value="${perunMachine.shortName}"/>&nbsp;(${perunMachine.cpuNum}&nbsp;CPU)</s:link></td>
                <c:if test="${i.count%7==0}"></tr><tr></c:if>
            </c:forEach>
        </tr>
        </table>
        <p>

            <f:message key="q_machines_cpus"><f:param value="${fn:length(actionBean.nodes)}"/><f:param value="${fn:length(actionBean.machines)}"/><f:param value="${actionBean.cpus}"/></f:message>
        </p>
    </c:otherwise>
</c:choose>

    </c:otherwise>
</c:choose>

<br />

  <table class="job">
 <tr>
   <th> <f:message key="jobs_job"/> </th>
   <th> <f:message key="jobs_ncpu"/> </th>
   <th> <f:message key="jobs_ngpu"/> </th>
   <th> <f:message key="jobs_jobname"/> </th>
   <th> <f:message key="jobs_user"/> </th>
   <th> <f:message key="jobs_cputimeused"/> </th>
   <th> <f:message key="jobs_walltimeused"/> </th>
   <th> <f:message key="jobs_state"/> </th>
   <th colspan="2"><f:message key="jobs_ctime"/></th>
 </tr>
 <c:forEach items="${actionBean.jobs}" var="job">
     <!-- 2 -->
  <tr>
   <td><s:link href="/job/${job.name}">${job.name}</s:link></td>
   <td align="center">${job.noOfUsedCPU}</td>
   <td align="center">${job.usedGPU}</td>
   <td><c:out value="${job.jobName}"/></td>
   <td align="right"><s:link href="/user/${job.user}">${job.user}</s:link></td>
   <td align="right">${job.CPUTimeUsed}</td>
   <td align="right">${job.wallTimeUsed}</td>
   <td align="center" class="${job.state}"><t:job_state job="${job}"/></td>
   <td align="right"><f:formatDate value="${job.timeCreated}" type="date" dateStyle="short" /></td>
   <td align="right"><f:formatDate value="${job.timeCreated}" type="time" timeStyle="short" /></td>
  </tr>
 </c:forEach>
</table>



<br>
Queue attributes
<table class="attributes">
 <c:forEach items="${q.attributes}" var="a">
 <tr>
  <td>${a.key}</td>
  <td>${a.value}</td>
 </tr>
 </c:forEach>
</table>
            </s:layout-component>
</s:layout-render>
