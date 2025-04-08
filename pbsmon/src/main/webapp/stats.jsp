<%@ page pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<s:useActionBean beanclass="cz.cesnet.meta.stripes.StatsActionBean" var="actionBean"/>
<f:message var="titlestring" key="stats.titul" scope="request"/>
<s:layout-render name="/layout.jsp">
    <s:layout-component name="telo">



<t:jobs_info jobsInfo="${actionBean.jobsInfo}"/>

        <p><f:message key="users_jsp_celkem"><f:param value="${fn:length(actionBean.users)}"/></f:message></p>
        TOP10:
        <table class="users">
            <tr>
                <th rowspan="2"><f:message key="jobs_user"/></th>
                <th colspan="2"><f:message key="users_jobs_count"/></th>
                <th colspan="2"><f:message key="users_jobs_ncpu"/></th>
            </tr>
            <c:set var="tokens" value="jobsStateQ,jobsStateR,cpusStateQ,cpusStateR"/>
            <tr><c:forTokens items="${tokens}" delims="," var="P">
                 <th><f:message key="users_jobs_${P}"/></th>
            </c:forTokens>
            </tr>
            <c:forEach items="${actionBean.users}" var="u" varStatus="i">
                <c:if test="${i.index<10}">
                <tr>
                    <td><s:link href="/user/${u.name}">${u.name}</s:link></td>
                    <c:forTokens items="${tokens}" delims="," var="P">
                       <td align="center">${u[P]}</td>
                    </c:forTokens>
                </tr>
                </c:if>
            </c:forEach>
        </table>

<hr/>

<p><f:message key="nodes_jsp_celkem_cpu"/>: ${actionBean.cpuMap['all']}</p>

        <c:forEach items="${actionBean.centra}" var="ownerOrganisation">
            <h3><strong><f:message key="${ownerOrganisation.nameKey}"/></strong>
                <c:set value="${actionBean.cpuCountMap[ownerOrganisation.id]}" var="c"/>
            (<f:message key="stats_vyuziti"/> ${c.used}/${c.total}
                = <f:formatNumber type="percent" maxFractionDigits="1" value="${c.used/c.total}"/>)
            </h3>

            <p style="font-size: smaller; padding-left: 5px;"><f:message key="${ownerOrganisation.specKey}"/></p>
            <table width="90%">
                <c:forEach var="zdr" items="${ownerOrganisation.perunComputingResources}" varStatus="s">
                    <tr>
                        <td colspan="2">
                            ${zdr.name}
                            <c:choose>
                                <c:when test="${zdr.cluster}"><f:message key="stats_cluster"><f:param value="${fn:length(zdr.perunMachines)}"/><f:param value="${actionBean.cpuMap[zdr.id]}"/></f:message></c:when>
                                <c:otherwise><f:message key="stats_samostatny_stroj"><f:param value="${zdr.perunMachine.cpuNum}"/></f:message></c:otherwise>
                            </c:choose>
                            <c:set value="${actionBean.cpuCountMap[zdr.id]}" var="c"/>
                            (<f:message key="stats_vyuziti"/> ${c.used}/${c.total}
                            = <f:formatNumber type="percent" maxFractionDigits="1" value="${c.used/c.total}"/>)

                    <table class="nodes" cellspacing="0">
                            <c:choose>
                            <c:when test="${zdr.cluster}">
                                <tr>
                                <c:forEach items="${zdr.perunMachines}" var="perunMachine" varStatus="i">
                                    <td class="node ${perunMachine.state}"><s:link href="/machine/${perunMachine.name}"><c:out value="${perunMachine.shortName}"/>&nbsp;(${actionBean.cpuCountMap[perunMachine.name].used}/${actionBean.cpuCountMap[perunMachine.name].total})</s:link></td>
                                    <c:if test="${i.count%7==0}"></tr><tr></c:if>
                                </c:forEach>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <tr><td class="node ${zdr.perunMachine.state}"><s:link href="/machine/${zdr.perunMachine.name}"><c:out value="${zdr.perunMachine.shortName}"/>&nbsp;(${actionBean.cpuCountMap[zdr.perunMachine.name].used}/${actionBean.cpuCountMap[zdr.perunMachine.name].total})</s:link></td></tr>
                            </c:otherwise>
                        </c:choose>
                    </table>

                           </td>
                    </tr>
                </c:forEach>
            </table>
        </c:forEach>


    </s:layout-component>
</s:layout-render>