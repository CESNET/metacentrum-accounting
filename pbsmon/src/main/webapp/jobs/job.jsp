<%@ page pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<s:useActionBean beanclass="cz.cesnet.meta.stripes.JobActionBean" var="actionBean"/>
<f:message var="titlestring" key="job.titul" scope="request"><f:param value="${actionBean.jobName}"/></f:message>
<s:layout-render name="/layout.jsp">
    <s:layout-component name="telo">

<c:choose>
 <c:when test="${empty actionBean.job}">
    <f:message key="job_headline"/> <strong><c:out value="${actionBean.jobName}"/></strong> <f:message key="job_skoncil"/>
 </c:when>
 <c:otherwise>
    <c:set var="job" value="${actionBean.job}" scope="request" />


     <c:choose>
         <c:when test="${job.killedBySignal}">
             <p class="killedjob"><f:message key="job_killedby"><f:param value="${job.exitValueOrSignal}"/></f:message></p>
         </c:when>
         <c:when test="${job.killedForOverLimit}">
             <p class="killedjob"><f:message key="job_killed_over_limit"/>
                 <br/><br/>"<c:out value="${job.comment}"/>"
             </p>
         </c:when>
         <c:when test="${job.exitValueOrSignal>0}">
             <p class="killedjob"><f:message key="job_exited_error"><f:param value="${job.exitValueOrSignal}"/></f:message></p>
         </c:when>
     </c:choose>
     <c:if test="${job.memoryExceeded}">
         <br>
         <p class="jobwarning"><f:message key="job_memory_exceeded"/></p>
     </c:if>
     <c:if test="${job.underusingCPUs}">
         <br>
         <p class="jobwarning"><f:message key="job_underusing_cpus"/></p>
     </c:if>
     <c:if test="${job.exceedsCPUTime}">
         <br>
         <p class="jobwarning"><f:message key="job_exceeds_cputime"/></p>
     </c:if>

    <c:if test="${job.consumingResources}">
        <table class="zakladni">
            <tr><th colspan="3"><f:message key="job_used_resources"/></th></tr>
            <tr>
                <th>RAM</th>
                <td>${job.usedMemPercent}%</td>
                <td>${job.usedMemory} / ${job.reservedMemoryTotal}</td>
            </tr>
            <tr>
                <th>CPU</th>
                <td>${job.usedCpuTimePercent}%</td>
                <td>${job.CPUTimeUsed} / (${job.noOfUsedCPU} * ${job.wallTimeUsed})</td>
            </tr>
            <tr>
                <th>walltime</th>
                <td>${job.usedWalltimePercent}%</td>
                <td>${job.wallTimeUsed} / ${job.walltimeReservedString}</td>
            </tr>
        </table>
    </c:if>


    <a name="tblVars"></a><h3><f:message key="tabled_variables"/></h3>


   <table class="job">
    <tr>
      <th> <f:message key="jobs_job"/> </th>
      <th> <f:message key="jobs_ncpu"/> </th>
      <th> <f:message key="jobs_mem_reserved"/> </th>
      <th> <f:message key="jobs_mem_used"/> </th>  
      <th> <f:message key="jobs_jobname"/> </th>
      <th> <f:message key="jobs_user"/> </th>
      <th> <f:message key="jobs_cputimeused"/> </th>
      <th> <f:message key="jobs_walltimeused"/> </th>
      <th> <f:message key="jobs_state"/> </th>
      <th> <f:message key="jobs_queue"/> </th>
    </tr>
       
    <tr>
      <td>${job.name}</td>
      <td align="center">${job.noOfUsedCPU}</td>
      <td align="right">${job.reservedMemoryTotal}</td>
      <td align="right" <c:if test="${job.memoryExceeded}">class="memexceeded"</c:if> >${job.usedMemory}</td>
      <td>${job.jobName}</td>
      <td align="right"><s:link href="/user/${job.user}">${job.user}</s:link></td>
      <td align="right" <c:if test="${job.underusingCPUs || job.exceedsCPUTime}">class="wasting"</c:if>>${job.CPUTimeUsed}</td>
      <td align="right">${job.wallTimeUsed}</td>
      <td align="center" class="${job.state}"><t:job_state job="${job}"/></td>
      <td align="center"><s:link beanclass="cz.cesnet.meta.stripes.QueueActionBean"><s:param name="queueName" value="${job.queueName}"/>${job.queueName}</s:link></td>
    </tr>

    <tr>
      <th> <f:message key="job_resource_nodes"/> </th>
      <td colspan="9"><c:out value="${job.resourceNodes}"/></td>
    </tr>
    <!-- casy -->
    <tr>
      <th> <f:message key="job_ctime"/> </th>
      <td colspan="9"><f:formatDate value="${job.timeCreated}" type="both" dateStyle="full" /></td>
    </tr>

    <c:if test="${! empty job.executionTime}">
    <tr>
      <th> <f:message key="job_exectime"/> </th>
      <td colspan="9"><f:formatDate value="${job.executionTime}" type="both" dateStyle="full" /></td>
    </tr>
    </c:if>
    <c:if test="${! empty job.timeEligible}">
    <tr>
      <th> <f:message key="job_etime"/> </th>
      <td colspan="9"><f:formatDate value="${job.timeEligible}" type="both" dateStyle="full" /></td>
    </tr>
    </c:if>
    <c:if test="${job.state=='Q' and ! empty job.plannedStart}">
      <tr>
         <th> <f:message key="jobs_planned_start"/> </th>
         <td colspan="9"><f:formatDate value="${job.plannedStart}" type="both" dateStyle="full" /></td>
      </tr>
    </c:if>
       <c:if test="${job.state=='Q' and ! empty job.plannedNodes}">
           <tr>
               <th> <f:message key="job_planned_nodes"/> </th>
               <td colspan="9">
                   <c:forEach items="${job.plannedNodesNames}" var="nodeName">
                       <s:link href="/node/${nodeName}"><c:out value="${nodeName}"/></s:link>
                   </c:forEach>
               </td>
           </tr>
       </c:if>
    <c:if test="${! empty job.timeStarted}">
    <tr>
      <th> <f:message key="job_start_time"/> </th>
      <td colspan="9"><f:formatDate value="${job.timeStarted}" type="both" dateStyle="full" /></td>
    </tr>
    </c:if>
    <c:if test="${! empty job.timeExpectedEnd}">
    <tr>
       <th> <f:message key="job_expected_endtime"/> </th>
       <td colspan="9" <c:if test="${job.overRun}">class="memexceeded"</c:if> >
           <f:formatDate value="${job.timeExpectedEnd}" type="both" dateStyle="full" /></td>
    </tr>
    </c:if>
    <c:if test="${! empty job.timeCompleted}">
    <tr>
      <th> <f:message key="job_comp_time"/> </th>
      <td colspan="9"><f:formatDate value="${job.timeCompleted}" type="both" dateStyle="full" /></td>
    </tr>
    </c:if>
    <tr>
      <th> <f:message key="job_mtime"/> </th>
      <td colspan="9"><f:formatDate value="${job.timeModified}" type="both" dateStyle="full" /></td>
    </tr>

    <tr>
      <th> <f:message key="job_comment"/> </th>
      <td colspan="9"><c:out value="${job.comment}"/></td>
    </tr>

    <c:if test="${!empty job.submitDir}">
     <tr>
      <th> <f:message key="job_submitdir"/> </th>
      <td colspan="9"><c:out value="${job.submitDir}" /></td>
    </tr>
   </c:if>

    <c:if test="${!empty job.workDir}">
     <tr>
      <th> <f:message key="job_workdir"/> </th>
      <td colspan="9"><c:out value="${job.workDir}" /></td>
     </tr>
    </c:if>

   <c:if test="${!empty job.scratchType}">
      <tr>
        <th>SCRATCHDIR</th>
        <td colspan="9"><c:out value="${job.scratchDir}" /></td>
      </tr>
   </c:if>

   <tr><th><f:message key="job_variable_list"/></th>
       <td colspan="9"><c:forEach items="${job.variables}" var="vl">
           ${vl.key}=<c:out value="${fn:substring(vl.value,0,75)}"/><br>
       </c:forEach>
     </td></tr>

   <c:if test="${! empty job.chunks}">
       <tr>
           <th><f:message key="job_scheduled_nodespec"/></th>
           <td colspan="9">
               <table class="zakladni">
                   <tr>
                       <th><f:message key="job_nodespec_node"/></th>
                       <th>CPU</th>
                       <th>GPU</th>
                       <th>RAM</th>
                       <th>scratch</th>
                   </tr>
                   <c:forEach items="${job.chunks}" var="chunk">
                       <tr>
                           <td><s:link href="/node/${chunk.nodeName}"><c:out value="${actionBean.nodesMap[chunk.nodeName].shortName}"/></s:link></td>
                           <td align="center"><c:out value="${chunk.ncpus}"/></td>
                           <td align="center"><c:out value="${chunk.ngpus}"/></td>
                           <td align="center"><c:out value="${chunk.mem}"/></td>
                           <td><c:out value="${chunk.scratchType}"/> <c:out value="${chunk.scratchVolume}"/> </td>
                       </tr>
                   </c:forEach>
               </table>

           </td>
       </tr>
   </c:if>    
   </table>

   <br>
   <a name="allVars"></a><h3><f:message key="all_variables"/></h3>

   <table class="attributes">
   <c:forEach var="oat" items="${job.orderedAttributes}">
    <tr><td>${oat.key}</td><td><c:out value="${oat.value}"/></td></tr>
   </c:forEach>
   </table>
</c:otherwise>
</c:choose>

   </s:layout-component>
</s:layout-render>
