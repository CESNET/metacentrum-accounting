<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/include/taglibs.jsp"%>
<fmt:message var="titlestring" scope="request" key="apps.list.title" />
<s:layout-render name="/layout.jsp" menu="internal/acct">
 <s:layout-component name="telo">

<div id="main" align="center">
    <p><stripes:link href="/apps/Edit.action"><fmt:message key="apps.list.new" /></stripes:link></p>
	<table class="data">
		<tr>
			<th><fmt:message key="apps.edit.order" /></th>
			<th><fmt:message key="apps.edit.name" /></th>
			<th><fmt:message key="apps.edit.regex" /></th>
		</tr>
		<c:forEach items="${actionBean.apps}" var="item" varStatus="status">
			<tr>
				<td><c:out value="${item.order}"/></td>
				<td><c:out value="${item.name}"/></td>
				<td><c:out value="${item.regex}"/></td>
				<td>
					<!-- <a>presunout vyse</a> | 
					<a>presunout nize</a> | --> 
					<stripes:link href="/apps/Edit.action">
          				<stripes:param name="id">${item.id}</stripes:param>
          				<fmt:message key="apps.list.edit" />
        			</stripes:link> | 
					<stripes:link href="/apps/Delete.action">
          				<stripes:param name="id">${item.id}</stripes:param>
          				<fmt:message key="apps.list.delete" />
        			</stripes:link>
				</td>
			</tr>				
		</c:forEach>	

	</table>
</div>
     
</s:layout-component>
</s:layout-render>
