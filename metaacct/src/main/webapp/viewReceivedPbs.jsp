<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/include/taglibs.jsp"%>
<fmt:message var="titlestring" scope="request" key="receivedPbs.title" />
<s:layout-render name="/layout.jsp" menu="internal/acct/log">
 <s:layout-component name="telo">
 
<div id="main" align="center">
	<display:table cellpadding="0" cellspacing="0" class="data" requestURI="" name="${actionBean.logs}" sort="external" id="element">	  
	  <display:column property="receiveTime" sortable="true" sortName="receiveTime" titleKey="receiveLog.receiveTime" headerClass="data" class="data" decorator="cz.cesnet.meta.accounting.displaytag.decorator.LongDateWrapper"/>	  
	  <display:column property="minimalTime" sortable="true" sortName="minimalTime" titleKey="receiveLog.minimalTime" headerClass="data" class="data" decorator="cz.cesnet.meta.accounting.displaytag.decorator.LongDateWrapper">
	  	<p style="text-align: right"><fmt:message key="receiveLog.noKernelRecords" /></p>
	  </display:column>
	  <display:column property="maximalTime" sortable="true" sortName="maximalTime" titleKey="receiveLog.maximalTime" headerClass="data" class="data" decorator="cz.cesnet.meta.accounting.displaytag.decorator.LongDateWrapper">
	  	&nbsp;
	  </display:column>
	  <display:column property="serverHostname" sortable="true" sortName="serverHostname" titleKey="receiveLog.serverHostname" headerClass="data" class="data"/>  
    
	  <display:footer>
	  	<form id="formPageSize" action="" method="get" onsubmit="this.action=window.location.href">
			<fmt:message key="list.pageSize" /> 
			<select name="pageSize" onchange="document.getElementById('formPageSize').submit()">
				<option id="20" value="20" <c:if test="${ actionBean.logs.objectsPerPage == 20 }">selected</c:if> >20</option>
				<option id="50" value="50" <c:if test="${ actionBean.logs.objectsPerPage == 50 }">selected</c:if> >50</option>
				<option id="100" value="100" <c:if test="${ actionBean.logs.objectsPerPage == 100 }">selected</c:if> >100</option>
				<option id="200" value="200" <c:if test="${ actionBean.logs.objectsPerPage == 200 }">selected</c:if> >200</option>
				<option id="500" value="500" <c:if test="${ actionBean.logs.objectsPerPage == 500 }">selected</c:if> >500</option>
			</select>
		</form>	  
	  </display:footer>
	</display:table>
	
  
  <%--<c:forEach items="${actionBean.logs}" var="log" varStatus="loop">
    <tr>
      <td class="data" ><fmt:formatDate pattern="dd.MM.yyyy, HH:mm:ss"
        value="${log.receiveTime}" /></td>
      <c:choose>
        <c:when test="${log.minimalTime == null}">
          <td class="data" colspan="2"><fmt:message key="receiveLog.noKernelRecords" /></td>
        </c:when>
        <c:otherwise>
          <td class="data" ><fmt:formatDate pattern="dd.MM.yyyy, HH:mm:ss"
          value="${log.minimalTime}" /></td>
          <td class="data" ><fmt:formatDate pattern="dd.MM.yyyy, HH:mm:ss"
          value="${log.maximalTime}" /></td>          
        </c:otherwise>
      </c:choose>
    </tr>
  </c:forEach>
</table>--%>
</div>
 </s:layout-component>
</s:layout-render>
