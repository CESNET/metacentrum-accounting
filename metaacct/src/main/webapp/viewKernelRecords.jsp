<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/include/taglibs.jsp"%>
<fmt:message var="titlestring" scope="request" key="kernelRecords.title" />
<s:layout-render name="/layout.jsp" >
 <s:layout-component name="telo">


<div id="main" align="center">
	<display:table cellpadding="0" cellspacing="0" class="data" requestURI="" name="${actionBean.kernelRecords}" sort="external" id="element">
      <display:column property="app" sortable="true" sortName="app" titleKey="kernel.app" headerClass="data" class="data" />	  
	  <display:column property="command" sortable="true" sortName="command" titleKey="kernel.command" headerClass="data" class="data" />	  
	  <display:column property="hostname" sortable="true" sortName="hostname" titleKey="kernel.hostname" headerClass="data" class="data" />
	  <display:column property="createTime" sortable="true" sortName="createTime" titleKey="kernel.createTime" headerClass="data" class="data" decorator="cz.cesnet.meta.accounting.displaytag.decorator.SecondsDateWrapper"/>
	  <display:column property="elapsedTime" sortable="true" sortName="elapsedTime" titleKey="kernel.elapsedTime" headerClass="data" class="data" decorator="cz.cesnet.meta.accounting.displaytag.decorator.SecondHundredsTimeWrapper"/>	  
	  <display:column property="userTime" sortable="true" sortName="userTime" titleKey="kernel.userTime" headerClass="data" class="data" decorator="cz.cesnet.meta.accounting.displaytag.decorator.SecondHundredsTimeWrapper"/>
	  <display:column property="systemTime" sortable="true" sortName="systemTime" titleKey="kernel.systemTime" headerClass="data" class="data" decorator="cz.cesnet.meta.accounting.displaytag.decorator.SecondHundredsTimeWrapper"/>	  
	  <display:column property="exitcode" sortable="true" sortName="exitcode" titleKey="kernel.exitcode" headerClass="data" class="data" />
	  
	  <display:footer>
	  	<form id="formPageSize" action="" method="get" onsubmit="this.action=window.location.href">
			<fmt:message key="list.pageSize" /> 
			<select name="pageSize" onchange="document.getElementById('formPageSize').submit()">
				<option id="10" value="10" <c:if test="${ actionBean.kernelRecords.objectsPerPage == 10 }">selected</c:if> >10</option>
				<option id="20" value="20" <c:if test="${ actionBean.kernelRecords.objectsPerPage == 20 }">selected</c:if> >20</option>
				<option id="30" value="30" <c:if test="${ actionBean.kernelRecords.objectsPerPage == 30 }">selected</c:if> >30</option>
				<option id="50" value="50" <c:if test="${ actionBean.kernelRecords.objectsPerPage == 50 }">selected</c:if> >50</option>
				<option id="100" value="100" <c:if test="${ actionBean.kernelRecords.objectsPerPage == 100 }">selected</c:if> >100</option>				
			</select>
			<input type="hidden" name="pbsIdString" value="${actionBean.pbsIdString}">
		</form>	  
	  </display:footer>
	</display:table>
</div>
 </s:layout-component>
</s:layout-render>
