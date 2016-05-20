<%@ page pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="h" uri="http://stripes.sourceforge.net/stripes.tld" %>

<s:useActionBean beanclass="cz.cesnet.meta.stripes.NodeActionBean" var="actionBean"/>
<f:message var="titlestring" key="node.titul" scope="request"><f:param value="${actionBean.nodeName}"/></f:message>
<s:layout-render name="/layout.jsp">
    <s:layout-component name="telo">
        <p>
            <c:choose>
                <c:when test="${actionBean.virtual}">Stroj je virtuální a </c:when>
                <c:otherwise>Výpočetní uzel </c:otherwise>
            </c:choose>
            <c:if test="${not empty actionBean.physicalMachineName}">
                je na fyzickém stroji
                <s:link href="/machine/${actionBean.physicalMachineName}">${actionBean.physicalMachineName}</s:link>
            </c:if>.
        </p>

        <t:node_detail node="${actionBean.node}"/>

    </s:layout-component>
</s:layout-render>