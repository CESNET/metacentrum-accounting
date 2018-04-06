<%@ page pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="h" uri="http://stripes.sourceforge.net/stripes.tld" %>

<s:useActionBean beanclass="cz.cesnet.meta.stripes.PropsActionBean" var="actionBean"/>
<f:message var="titlestring" key="property_nodes_title" scope="request"><f:param value="${actionBean.property}"/></f:message>
<s:layout-render name="/layout.jsp">
    <s:layout-component name="telo">

<table class="prop2node">
 <tr>
     <th><f:message key="props_prop2node_node"/></th>
 </tr>
 <tr id="${actionBean.property}">
  <td>
  <c:set var="numinrow" value="${0}"/>
  <table class="nodes" cellspacing="0" border="0">
  <tr>
  <c:forEach items="${actionBean.propsMap[actionBean.property]}" var="node">
     <c:if test="${numinrow==8}">
         <c:set var="numinrow" value="${0}"/>
         </tr><tr>
     </c:if>
     <td class="${node.state}"> <h:link class="${node.state}" href="/node/${node.name}">${node.shortName}</h:link></td>
     <c:set var="numinrow" value="${numinrow+1}"/>
 </c:forEach>
 </tr>
 </table>
 </td>
</table>

  </s:layout-component>
</s:layout-render>
