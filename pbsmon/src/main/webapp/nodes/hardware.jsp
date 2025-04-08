<%@ page pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="h" uri="http://stripes.sourceforge.net/stripes.tld" %>


<s:useActionBean beanclass="cz.cesnet.meta.stripes.HardwareActionBean" var="actionBean"/>
<f:message var="titlestring" key="hardware.titul" scope="request"/>
<s:layout-render name="/layout.jsp">
    <s:layout-component name="telo">

        <ul>
        <c:forEach items="${actionBean.physicalMachines.centra}" var="ownerOrganisation">
            <li> <strong><f:message key="${ownerOrganisation.nazevKey}"/></strong> (${actionBean.physicalMachines.cpuMap[ownerOrganisation.id]} CPU)
                <ul>
            <c:forEach var="zdr" items="${ownerOrganisation.zdroje}" varStatus="s">
                <li><a href="#<c:out value='${zdr.id}'/>"><c:out value="${zdr.nazev}"/></a>
                    (${actionBean.physicalMachines.cpuMap[zdr.id]} CPU<c:if test="${zdr.cluster}">,
                         <f:message key="hardware.uzlu"><f:param value="${fn:length(zdr.stroje)}"/></f:message></c:if>)
                </li>
            </c:forEach>
                </ul>
            </li>
        </c:forEach>
        </ul>
        <hr>

        <c:forEach items="${actionBean.physicalMachines.centra}" var="ownerOrganisation">

            <c:forEach var="zdr" items="${ownerOrganisation.zdroje}" varStatus="s">
                <div id="<c:out value='${zdr.id}'/>">
                <s:link href="/resource/${zdr.id}"><c:out value="${zdr.nazev}"/></s:link>
                <c:if test="${zdr.cluster}">
                    (${actionBean.physicalMachines.cpuMap[zdr.id]} CPU, <f:message key="hardware.uzlu"><f:param value="${fn:length(zdr.stroje)}"/></f:message> )
                </c:if>
                - <f:message key="${zdr.popisKey}"/><br>
                <table>
                    <tr>
                        <td>
                            <div id="photo">
                                <c:if test="${not empty zdr.thumbnail}">
                                    <s:link href="/resource/${zdr.id}"><img src="<c:out value='${zdr.thumbnail}'/>" alt="photo" width="100px"/></s:link>
                                </c:if>
                            </div>
                        </td>
                        <td>
                            <t:resource resource="${zdr}"/>
                        </td>
                    </tr>
                </table>
                </div>
            </c:forEach>
        </c:forEach>
    </s:layout-component>
</s:layout-render>
