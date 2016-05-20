<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/include/taglibs.jsp"%>
<fmt:message var="titlestring" scope="request" key="apps.edit.title" />
<s:layout-render name="/layout.jsp" menu="internal/acct">
 <s:layout-component name="telo">

<div id="main" align="center">
	<s:form action="/apps/Edit.action" method="post" focus="app.order" >
		<s:errors/>
		<s:hidden name="app.id"/>
		<table class="data">
			<tr>
				<th><fmt:message key="apps.edit.order" /></th>
				<td><s:text name="app.order" /></td>
                <td><fmt:message key="apps.edit.order.hint"/></td>
			</tr>
			<tr>
				<th><fmt:message key="apps.edit.name" /></th>
				<td><s:text name="app.name"/></td>
                <td><fmt:message key="apps.edit.name.hint"/></td>
			</tr>
			<tr>
				<th><fmt:message key="apps.edit.regex" /></th>
				<td><s:text name="app.regex"/></td>
                <td><fmt:message key="apps.edit.regex.hint"/></td>
			</tr>
			<tr>				
                <td>&nbsp;</td>
				<td><s:submit name="save" ><fmt:message key="apps.edit.save" /></s:submit>&nbsp;
                    <s:submit name="back" ><fmt:message key="apps.edit.back" /></s:submit></td>
                <td>&nbsp;</td>
			</tr>
		</table>
	</s:form>
</div>
     
</s:layout-component>
</s:layout-render>
