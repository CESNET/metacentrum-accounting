<%@ page pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<s:useActionBean beanclass="cz.cesnet.meta.stripes.GroupActionBean" var="actionBean"/>
<f:message var="titlestring" key="group.titul" scope="request"><f:param value="${actionBean.groupName}"/><f:param
        value="${actionBean.pbsServerName}"/></f:message>
<s:layout-render name="/layout.jsp">
    <s:layout-component name="telo">

        <table class="users">
            <thead>
                <tr>
                    <th>#</th>
                    <th>logname</th>
                    <th>jméno</th>
                    <th>organizace</th>
                    <th>skupina</th>
                    <th>status</th>
                </tr>
            </thead>
            <tbody>
            <c:forEach items="${actionBean.users}" var="user" varStatus="i">
                <tr>
                    <td>${i.count}</td>
                    <td><s:link href="/user/${user.logname}"><c:out value="${user.logname}"/></s:link></td>
                    <td><c:out value="${user.name}"/></td>
                    <td><c:out value="${user.organization}"/></td>
                    <td><c:out value="${user.researchGroup}"/></td>
                    <td><c:out value="${user.status}"/></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>

    </s:layout-component>
</s:layout-render>
