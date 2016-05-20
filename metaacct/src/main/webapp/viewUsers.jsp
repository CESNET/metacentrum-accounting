<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/include/taglibs.jsp"%>
<fmt:message var="titlestring" scope="request" key="users.title" />
<s:layout-render name="/layout.jsp" menu="internal/acct">
 <s:layout-component name="telo">

<div id="main" align="center">
	<display:table cellpadding="0" cellspacing="0" class="data" requestURI="" name="${actionBean.users}" sort="external" id="element">
	  <display:column property="username" sortable="true" sortName="username" titleKey="users.username" headerClass="data" class="data" />
	  <display:column property="numberOfPbsRecords" sortable="true" sortName="numberOfPbsRecords" titleKey="users.receivedPbs" headerClass="data" class="data"/>
	  <display:column headerClass="data" class="data" >
	  	<stripes:link href="/PbsRecords.action">
          <stripes:param name="userId">${element.id}</stripes:param>          
          <stripes:param name="number">10</stripes:param>          
          (<fmt:message key="users.view" />)
        </stripes:link>
	  </display:column>
	  <display:footer>
	  	<form id="formPageSize" action="" method="get" onsubmit="this.action=window.location.href">
			<fmt:message key="list.pageSize" /> 
			<select name="pageSize" onchange="document.getElementById('formPageSize').submit()">
				<option id="20" value="20" <c:if test="${ actionBean.users.objectsPerPage == 20 }">selected</c:if> >20</option>
				<option id="50" value="50" <c:if test="${ actionBean.users.objectsPerPage == 50 }">selected</c:if> >50</option>
				<option id="100" value="100" <c:if test="${ actionBean.users.objectsPerPage == 100 }">selected</c:if> >100</option>
				<option id="200" value="200" <c:if test="${ actionBean.users.objectsPerPage == 200 }">selected</c:if> >200</option>
				<option id="500" value="500" <c:if test="${ actionBean.users.objectsPerPage == 500 }">selected</c:if> >500</option>
                <option id="1000" value="1000" <c:if test="${ actionBean.users.objectsPerPage == 500 }">selected</c:if> >1000</option>
			</select>
		</form>	  
	  </display:footer>
	</display:table>
</div>
     
</s:layout-component>
</s:layout-render>
