<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/include/taglibs.jsp"%>
<fmt:message var="titlestring" scope="request" key="pbsRecords.title" ><fmt:param value="${actionBean.username}"/></fmt:message>
<s:layout-render name="/layout.jsp" >
  <s:layout-component name="telo">


<div id="main" align="center">
  <fmt:message key="pbsRecords.emptyUser"/>
</div>
 </s:layout-component>
</s:layout-render>
