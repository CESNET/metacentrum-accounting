<%@ page pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<f:message var="titlestring" key="queues.titul" scope="request"/>
<s:layout-render name="/layout.jsp">
    <s:layout-component name="telo">
        <s:useActionBean beanclass="cz.cesnet.meta.stripes.QueuesActionBean" var="actionBean"/>

        <c:forEach items="${actionBean.pbs}" var="pbs">
            <h3>Server ${pbs.server.host} - <f:message key="server_${pbs.server.host}"/> </h3>
            <table class="queue">
                <t:queue_heading/>
                <c:forEach items="${pbs.queuesByPriority}" var="q">
                    <t:queue_line queue="${q}"/>
                </c:forEach>
            </table>
            <c:if test="${not empty pbs.reservations}">
                Reservations:
                <table class="reservations">
                    <tr>
                        <th>id</th>
                        <th>name</th>
                        <th>owner</th>
                        <th>start</th>
                        <th>end</th>
                        <th>select</th>
                    </tr>
                <c:forEach items="${pbs.reservations}" var="resve">
                    <tr>
                        <td><s:link href="/queue/${resve.value.queue.name}">${resve.value.name}</s:link></td>
                        <td><c:out value="${resve.value.reserveName}"/></td>
                        <td><c:out value="${resve.value.owner}"/></td>
                        <td><f:formatDate value="${resve.value.reserveStart}" dateStyle="medium" timeStyle="short" type="both"/></td>
                        <td><f:formatDate value="${resve.value.reserveEnd}" dateStyle="medium" timeStyle="short" type="both"/></td>
                        <td><c:out value="${resve.value.select}"/></td>
                    </tr>
                </c:forEach>
                </table>
            </c:if>
        </c:forEach>



        <h2><a name="urceni"></a><f:message key="queues_list_urceni_front"/></h2>
        <p><f:message key="queues_list_urceni_pokec1"/></p>
        <p><f:message key="queues_list_urceni_pokec2"/></p>
        <table class="queue">
            <tr>
                <th><f:message key="queues_queue"/></th>
                <th><f:message key="queues_list_popis"/></th>
                <th><img src="${pageContext.request.contextPath}/img/lock.png" alt="locked" /> <f:message key="queues_list_omezeni"/></th>
            </tr>
        <c:forEach items="${actionBean.pbs}" var="pbs">
            <c:forEach items="${pbs.queuesByPriority}" var="q">
                <tr><td style="text-align: right;"><s:link href="/queue/${q.name}">${q.name}</s:link></td>
                    <td style="text-align: left;">
                        <c:choose>
                            <c:when test="${q.descriptionAvailable}"><c:out value="${q.descriptionMap[pageContext.request.locale]}"/><!-- from pbs --></c:when>
                            <c:otherwise><f:message key="q_${q.shortName}_txt" /><!-- from ResourceBundle --></c:otherwise>
                        </c:choose>
                    </td>
                    <td style="text-align: left;">
                        <c:if test="${q.aclUsersEnabled}">
                            <f:message key="queues_list_locked_for_users"/>:
                            <c:forEach items="${q.aclUsersArray}" var="user"><s:link href="/user/${user}">${user}</s:link> </c:forEach>
                        </c:if>
                        <c:if test="${q.aclGroupsEnabled}">
                            <f:message key="queues_list_locked_for_groups"/>:
                            <c:forEach items="${q.aclGroupsArray}" var="group"><s:link href="/group/${pbs.host}/${group}">${group}</s:link> </c:forEach>
                        </c:if>
                        <c:if test="${q.aclHostsEnabled}">
                            <f:message key="queues_list_locked_for_hosts"/>:
                            <c:forEach items="${q.aclHostsArray}" var="host">${host} </c:forEach>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
        </c:forEach>
        </table>


        <h2><f:message key="servers.titul"/></h2>
         <c:forEach items="${actionBean.pbs}" var="pbs">
            <h3><a name="${pbs.server.host}"></a> Server ${pbs.server.host} - <f:message key="server_${pbs.server.host}"/></h3>

            <table class="attributes">
                <c:forEach items="${pbs.server.attributes}" var="entry">
                    <tr>
                        <td align="left" ><c:out value="${entry.key}"/></td>
                        <td align="left"><c:out value="${entry.value}"/></td>
                    </tr>
                </c:forEach>
            </table>
             <br>

           <t:node_table nodes="${pbs.nodesByName}"/>

        </c:forEach>

        <h2 id="queue_nodes"><f:message key="queues_list_queueus_nodes_headline"/></h2>
        <c:forEach items="${actionBean.pbs}" var="pbs">
            <c:forEach items="${pbs.queuesByPriority}" var="q">
                <table class="queue">
                    <tr><td><s:link href="/queue/${q.name}">${q.name}</s:link></td></tr>
                </table>
                <c:choose>
                    <c:when test="${not empty q.nodes}">
                        <t:node_table nodes="${q.nodes}"/>
                    </c:when>
                    <c:otherwise>
                        <f:message key="queues_list_no_nodes_in_queue"/><br>
                    </c:otherwise>
                </c:choose>
                <br>
            </c:forEach>
        </c:forEach>

    </s:layout-component>
</s:layout-render>
