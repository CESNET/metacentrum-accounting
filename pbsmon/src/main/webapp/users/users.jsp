<%@ page import="java.util.Arrays" %>
<%@ page pageEncoding="utf-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<f:message var="titlestring" key="users_headline" scope="request"/>
<s:layout-render name="/layout.jsp">
    <s:layout-component name="telo">
        <s:useActionBean beanclass="cz.cesnet.meta.stripes.UsersActionBean" var="actionBean"/>

        <p><f:message key="users_jsp_celkem"><f:param value="${fn:length(actionBean.users)}"/></f:message></p>

        <table class="users">
            <tr>
                <th rowspan="2"><s:link href="/users/name"><f:message key="jobs_user"/></s:link>
                    <c:if test="${actionBean.trideni=='name'}"><img src="${pageContext.request.contextPath}/img/arrow.jpeg" width="15" height="15" alt="sort"></c:if>
                </th>
                <th colspan="${fn:length(actionBean.fairshares)}">fairshare</th>
                <th colspan="5"><f:message key="users_jobs_count"/></th>
                <th colspan="5"><f:message key="users_jobs_ncpu"/></th>
            </tr>
            <tr>
                <c:forEach items="${actionBean.fairshares}" var="fairsh">
                    <th><s:link href="/users/fairshare/${fairsh.id}">${fairsh.id}</s:link><c:if test="${actionBean.srv==fairsh}"><img src="${pageContext.request.contextPath}/img/arrow.jpeg" width="15" height="15" alt="sort"></c:if></th>
                </c:forEach>
            <c:set var="tokens" value="jobsTotal,jobsStateQ,jobsStateR,jobsStateC,jobsOther,cpusTotal,cpusStateQ,cpusStateR,cpusStateC,cpusOther"/>
            <c:forTokens items="${tokens}" delims="," var="P">
                 <th><s:link href="/users/${P}"><f:message key="users_jobs_${P}"/></s:link><c:if test="${actionBean.trideni==P}"><img src="${pageContext.request.contextPath}/img/arrow.jpeg" width="15" height="15" alt="sort"></c:if></th>
            </c:forTokens>
            </tr>

            <c:forEach items="${actionBean.users}" var="u">
                <tr>
                    <td><s:link href="/user/${u.name}">${u.name}</s:link></td>
                    <c:forEach items="${actionBean.fairshares}" var="fairsh">
                        <td align="center">${u.fairshares[fairsh.id]}</td>
                    </c:forEach>
                    <c:forTokens items="${tokens}" delims="," var="P">
                       <td align="center">${u[P]}</td>
                    </c:forTokens>
                </tr>
            </c:forEach>
        </table>

    <p><a name="fairshare"></a><b>fairshare</b> - úlohy jsou v
plánovači řazeny podle fairshare uživatele, přednost je dávána uživatelům, kteří zatím 
propočítali méně, aby se dostalo na všechny. Číslo v této tabulce udává prioritu uživatele
podle fairshare, tj.  čím vyšší číslo, tím vyšší má uživatel prioritu, protože zatím méně propočítal.
Uživatel s číslem 1 má nejnižší prioritu při spouštění úloh. 
Do fairshare se počítají úlohy za posledních 30 dnů, s váhou klesající s postupem času,
tedy vyšší váhu mají nedávné úlohy. </p>

    <p>Informace z této stránky jsou dostupné i ve formátu JSON, např.
        <s:link beanclass="cz.cesnet.meta.stripes.ApiActionBean" event="users">
            <s:param name="pretty" value="true"/>
            <s:param name="users" value="<%=Arrays.asList(actionBean.getUsers().get(0).getName(),actionBean.getUsers().get(1).getName(),actionBean.getUsers().get(2).getName())%>" />
            první tři uživatelé
        </s:link>
    </p>
    </s:layout-component>
</s:layout-render>
