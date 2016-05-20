<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/include/taglibs.jsp"%>
<stripes:useActionBean var="actionBean" beanclass="cz.cesnet.meta.accounting.web.Details"/>
<fmt:message var="titlestring" scope="request" key="detail.title" ><fmt:param value="${actionBean.pbsRecord.idString}"/></fmt:message>
<s:layout-render name="/layout.jsp" >
 <s:layout-component name="telo">

<div id="main" align="center">

<table class="data" cellpadding="0px" cellspacing="0px" width="300px">
  <tr <c:if test="${actionBean.pbsRecord.conflict}">class="conflict"</c:if>>
    <td class="bold"><fmt:message key="pbs.job" /></td>
    <td> ${actionBean.pbsRecord.idString}</td>
  </tr>
  <tr class="second">
    <td class="bold">dateTime</td>
    <td><fmt:formatDate pattern="dd.MM.yyyy, HH:mm:ss"
        value="${actionBean.pbsRecord.dateTime}" /></td>
  </tr>
  <tr>    
    <td class="bold"><fmt:message key="pbs.name" /></td>
    <td>${actionBean.pbsRecord.jobname}</td>
  </tr>
  <tr class="second">
    <td class="bold"><fmt:message key="pbs.queue" /></td>
    <td>${actionBean.pbsRecord.queue}</td>
  </tr>

    <tr>
        <td class="bold"><fmt:message key="pbs.reqNodes" /></td>
        <td>${actionBean.pbsRecord.reqNodes}</td>
    </tr>
    <tr class="second">
        <td class="bold"><fmt:message key="pbs.usedMem" /></td>
        <td>${actionBean.pbsRecord.usedMemFormated}</td>
    </tr>
    <tr>
        <td class="bold"><fmt:message key="pbs.usedNcpus" /></td>
        <td>${actionBean.pbsRecord.usedNcpus}</td>
    </tr>
    <tr class="second">
        <td class="bold"><fmt:message key="pbs.usedCputime" /></td>
        <td>${actionBean.pbsRecord.usedCputime}</td>
    </tr>
    <tr>
        <td class="bold"><fmt:message key="pbs.usedWalltime" /></td>
        <td>${actionBean.pbsRecord.usedWalltime}</td>
    </tr>
    <tr class="second">
        <td class="bold"><fmt:message key="pbs.reqWalltime" /></td>
        <td>${actionBean.pbsRecord.reqWalltime}</td>
    </tr>

  <tr>
    <td class="bold"><fmt:message key="pbs.createTime" /></td>
    <td><fmt:formatDate pattern="dd.MM.yyyy, HH:mm:ss"
        value="${actionBean.pbsRecord.createTime}" /></td>
  </tr>
  <tr class="second">
    <td class="bold"><fmt:message key="pbs.startTime" /></td>
    <td><fmt:formatDate pattern="dd.MM.yyyy, HH:mm:ss"
        value="${actionBean.pbsRecord.startTime}" /></td>
  </tr>
  <tr>    
    <td class="bold"><fmt:message key="pbs.endTime" /></td>
    <td><fmt:formatDate pattern="dd.MM.yyyy, HH:mm:ss"
        value="${actionBean.pbsRecord.endTime}" /></td>
  </tr>
  <tr class="second">
    <td class="bold"><fmt:message key="kernel.exitcode"/></td>      
    <td>${actionBean.pbsRecord.exitStatus}</td>      
  </tr>
  <tr>
    <td class="bold"><fmt:message key="pbs.totalUserTime" /></td>
    <td>${myfn:formatHundredthsToHhMmSs(actionBean.pbsRecord.totalUserTime)}</td>
  </tr>
  <tr class="second">
    <td class="bold"><fmt:message key="pbs.totalSystemTime" /></td>
    <td>${myfn:formatHundredthsToHhMmSs(actionBean.pbsRecord.totalSystemTime)}</td>
  </tr>
  <tr>
    <td class="bold"><fmt:message key="pbs.cpus"/></td>
    <td>
      <c:forEach items="${actionBean.pbsRecord.execHosts}" var="host" varStatus="loop">
        ${host.hostName}/${host.processorNumber}<br/>
      </c:forEach>
    </td>
  </tr>
  <tr class="second">
    <td class="bold"><fmt:message key="pbs.10MostConsumingProcesses"/></td>
    <td>
      <c:choose>
        <c:when test="${empty actionBean.pbsRecord.kernelRecordsOrderedByTotalUserTimeDesc}"><fmt:message key="pbs.notReceivedLogs"/></c:when> 
        <c:otherwise>
          <table class="inner_table">
            <tr>
              <th class="data" ><fmt:message key="kernel.app"/></th>
              <th class="data" ><fmt:message key="kernel.command"/></th>
              <th class="data" ><fmt:message key="kernel.hostname"/></th>
              <th class="data" ><fmt:message key="kernel.createTime"/></th>
              <th class="data" ><fmt:message key="kernel.elapsedTime"/></th>
              <th class="data" ><fmt:message key="kernel.userTime"/></th>
              <th class="data" ><fmt:message key="kernel.systemTime"/></th>
            </tr>
            <c:forEach items="${actionBean.pbsRecord.kernelRecordsOrderedByTotalUserTimeDesc}" var="kernelRecord" varStatus="loop">
              <c:if test="${loop.index <10 }" >
                <tr>
                  <td>${kernelRecord.app}</td>
                  <td>${kernelRecord.command}</td>
                  <td>${kernelRecord.host.hostName}</td>              
                  <td><fmt:formatDate pattern="dd.MM.yyyy, HH:mm:ss"
            value="${myfn:secondsToDate(kernelRecord.createTime)}" /></td>
                  <td>${myfn:formatHundredthsToHhMmSs(kernelRecord.elapsedTime)}</td>
                  <td>${myfn:formatHundredthsToHhMmSs(kernelRecord.userTime)}</td>
                  <td>${myfn:formatHundredthsToHhMmSs(kernelRecord.systemTime)}</td>
                </tr>
              </c:if>
            </c:forEach>
          </table>
        </c:otherwise>
      </c:choose>
    </td>
  </tr>  
  <tr>
    <td colspan="2">
      <c:if test="${not empty actionBean.pbsRecord.kernelRecordsOrderedByTotalUserTimeDesc}">
        <stripes:link href="/KernelRecords.action?view">
          <stripes:param name="pbsIdString">${actionBean.pbsRecord.idString}</stripes:param>
            <fmt:message key="pbs.showAll"/>
        </stripes:link>
      </c:if>
    </td>
  </tr>

</table>

</div>
 </s:layout-component>
</s:layout-render>
