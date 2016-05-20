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

<p><f:message key="nodes_jsp_celkem_cpu"/>: ${actionBean.cpuMap['vsechny']}</p>

        <c:forEach items="${actionBean.centra}" var="centrum">
            <h3><strong><f:message key="${centrum.nazevKey}"/></strong>
                <c:set value="${actionBean.cpuCountMap[centrum.id]}" var="c"/>
            (<f:message key="stats_vyuziti"/> ${c.used}/${c.total}
                = <f:formatNumber type="percent" maxFractionDigits="1" value="${c.used/c.total}"/>)
            </h3>

            <p style="font-size: smaller; padding-left: 5px;"><f:message key="${centrum.specKey}"/></p>
            <table width="90%">
                <c:forEach var="zdr" items="${centrum.zdroje}" varStatus="s">
                    <tr>
                        <td colspan="2">
                            ${zdr.nazev}
                            <c:choose>
                                <c:when test="${zdr.cluster}"><f:message key="stats_cluster"><f:param value="${fn:length(zdr.stroje)}"/><f:param value="${actionBean.cpuMap[zdr.id]}"/></f:message></c:when>
                                <c:otherwise><f:message key="stats_samostatny_stroj"><f:param value="${zdr.stroj.cpuNum}"/></f:message></c:otherwise>
                            </c:choose>
                            <c:set value="${actionBean.cpuCountMap[zdr.id]}" var="c"/>
                            (<f:message key="stats_vyuziti"/> ${c.used}/${c.total}
                            = <f:formatNumber type="percent" maxFractionDigits="1" value="${c.used/c.total}"/>)

                    <table class="nodes" cellspacing="0">
                            <c:choose>
                            <c:when test="${zdr.cluster}">
                                <tr>
                                <c:forEach items="${zdr.stroje}" var="stroj" varStatus="i">
                                    <td class="node ${stroj.state}"><s:link href="/machine/${stroj.name}"><c:out value="${stroj.shortName}"/>&nbsp;(${actionBean.cpuCountMap[stroj.name].used}/${actionBean.cpuCountMap[stroj.name].total})</s:link></td>
                                    <c:if test="${i.count%7==0}"></tr><tr></c:if>
                                </c:forEach>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <tr><td class="node ${zdr.stroj.state}"><s:link href="/machine/${zdr.stroj.name}"><c:out value="${zdr.stroj.shortName}"/>&nbsp;(${actionBean.cpuCountMap[zdr.stroj.name].used}/${actionBean.cpuCountMap[zdr.stroj.name].total})</s:link></td></tr>
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