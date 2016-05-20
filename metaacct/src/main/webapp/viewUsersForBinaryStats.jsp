<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/include/taglibs.jsp"%>
<fmt:message var="titlestring" scope="request" key="usersForBinaryStats.title" />
<c:set var="dateformat" scope="request" value="${pageContext.request.locale == 'cs' ? 'dd.MM.yyyy' : 'MM/dd/yyyy'}" />
<s:layout-render name="/layout.jsp" menu="internal/acct">
 <s:layout-component name="telo">

<div id="main" align="center">
  	<display:table cellpadding="0" cellspacing="0" class="data" requestURI="" name="${actionBean.usersForBinaryStats}" sort="external" id="element">	  
	  <display:column property="username" sortable="true" sortName="username" titleKey="users.username" headerClass="data" class="data" />	  
	  <display:column property="elapsedTimeSum" sortable="true" sortName="elapsedTimeSum" titleKey="binariesStats.elapsedTimeSum" headerClass="data" class="data" decorator="cz.cesnet.meta.accounting.displaytag.decorator.SecondHundredsTimeWrapper"/>
	  <display:column property="userTimeSum" sortable="true" sortName="userTimeSum" titleKey="binariesStats.userTimeSum" headerClass="data" class="data" decorator="cz.cesnet.meta.accounting.displaytag.decorator.SecondHundredsTimeWrapper"/>
	  <display:column property="systemTimeSum" sortable="true" sortName="systemTimeSum" titleKey="binariesStats.systemTimeSum" headerClass="data" class="data" decorator="cz.cesnet.meta.accounting.displaytag.decorator.SecondHundredsTimeWrapper"/>
	  
	  <display:footer>
	  	<form id="formPageSize" action="" method="get" onsubmit="this.action=window.location.href">
			<fmt:message key="list.pageSize" /> 
			<select name="pageSize" onchange="document.getElementById('formPageSize').submit()">
				<option id="20" value="20" <c:if test="${ actionBean.usersForBinaryStats.objectsPerPage == 20 }">selected</c:if> >20</option>
				<option id="50" value="50" <c:if test="${ actionBean.usersForBinaryStats.objectsPerPage == 50 }">selected</c:if> >50</option>
				<option id="100" value="100" <c:if test="${ actionBean.usersForBinaryStats.objectsPerPage == 100 }">selected</c:if> >100</option>
				<option id="200" value="200" <c:if test="${ actionBean.usersForBinaryStats.objectsPerPage == 200 }">selected</c:if> >200</option>
				<option id="500" value="500" <c:if test="${ actionBean.usersForBinaryStats.objectsPerPage == 500 }">selected</c:if> >500</option>				
			</select>
			<input type="hidden" name="command" value="${actionBean.command}">
			<input type="hidden" name="fromDate" value="${actionBean.fromDate}">
			<input type="hidden" name="toDate" value='${actionBean.toDate}'>
		</form>	  
	  </display:footer>
	</display:table>
</div>
 </s:layout-component>
</s:layout-render>
