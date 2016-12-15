<%@ page pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="h" uri="http://stripes.sourceforge.net/stripes.tld" %>


<s:useActionBean beanclass="cz.cesnet.meta.stripes.MachineActionBean" var="actionBean"/>
<f:message var="titlestring" key="machine.titul" scope="request"><f:param
        value="${actionBean.machineName}"/></f:message>
<s:layout-render name="/layout.jsp">
    <s:layout-component name="hlava">
        <style type="text/css">
            td.ACTIVE {
                background-color: #6060FF;
            }

            td.POWEROFF, td.SUSPENDED {
                background-color: #C0C0C0;
            }

            td.cloud-virt {
                font-weight: bold;
            }
        </style>
    </s:layout-component>
    <s:layout-component name="telo">

        <%-- variantní hláška o clusteru nebo SMP stroji --%>
        <% if (actionBean.getPerunMachine().getVypocetniZdroj().isCluster()) {%>
        <f:message key="machine_jsp_soucast_clusteru1"/>
        <s:link href="/resource/${actionBean.perunMachine.vypocetniZdroj.id}"><c:out
                value="${actionBean.perunMachine.vypocetniZdroj.nazev}"/></s:link>
        <f:message key="machine_jsp_soucast_clusteru2"/>
        <% } else { %>
        <f:message key="machine_jsp_smp_stroj1"/>
        <s:link href="/resource/${actionBean.perunMachine.vypocetniZdroj.id}"><c:out
                value="${actionBean.perunMachine.vypocetniZdroj.nazev}"/></s:link>
        <f:message key="machine_jsp_smp_stroj2"/>
        <% } %>

        <%-- tabulka s popisem hardware z Peruna --%>
        <t:resource resource="${actionBean.perunMachine.vypocetniZdroj}"/>


        <%-- OpenNebula --%>
        <% if (actionBean.getCloudPhysicalHost() != null) { %>
        <h1>OpenNebula cloud <img src="${pageContext.request.contextPath}/img/cloud.png" alt="in OpenNebula"></h1>

        <p><f:message key="machine_jsp_in_cloud"><f:param
                value="${actionBean.cloudPhysicalHost.hostname}"/></f:message></p>
        <table class="zakladni">
            <tr>
                <th>Stav v OpenNebule</th>
                <td>${actionBean.cloudPhysicalHost.state}</td>
            </tr>
            <tr>
                <th>CPU v Perunovi</th>
                <td>${actionBean.perunMachine.cpuNum} CPU</td>
            </tr>
            <tr>
                <th>CPU v OpenNebula</th>
                <td>${actionBean.cloudPhysicalHost.cpuAvail} CPU</td>
            </tr>
            <tr>
                <th>Rezervovaných CPU</th>
                <td>${actionBean.cloudPhysicalHost.cpuReservedString} CPU</td>
            </tr>
            <tr>
                <th>CPU info</th>
                <td>${actionBean.cloudPhysicalHost.cpu_info}</td>
            </tr>
        </table>


        <% if (actionBean.getCloudVirtualHosts() != null && !actionBean.getCloudVirtualHosts().isEmpty()) { %>

        <p><f:message key="machine_jsp_cloud_vms"/></p>
        <table>
            <c:forEach items="${actionBean.cloudVirtualHosts}" var="vm">
                <tr>
                    <c:choose>
                        <c:when test="${vm.pbsNode}">
                            <td class="cloud-virt nebulapbshost">
                                <s:link href="/node/${vm.fqdn}"><c:out value="${vm.fqdn}"/>
                                (<c:out value="${vm.cpuReservedString}"/> CPU<c:if
                                        test="${vm.cpu_reserved_x100!=vm.cpu_avail_x100}">/${vm.VCPU}VCPU</c:if>)
                                </s:link>
                            </td>
                            <td>
                                PBS node
                            </td>
                        </c:when>
                        <c:when test="${vm.state=='ACTIVE'}">
                            <td class="cloud-virt ACTIVE"><c:out value="${vm.fqdn}"/>
                                (<c:out value="${vm.cpuReservedString}"/> CPU<c:if
                                        test="${vm.cpu_reserved_x100!=vm.cpu_avail_x100}">/${vm.VCPU}VCPU</c:if>)
                            </td>
                            <td>
                                Cloud node "<c:out value="${vm.name}"/>" of user <t:vmowner owner="${vm.owner}"/>
                                started <f:formatDate value="${vm.startTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
                            </td>
                        </c:when>
                        <c:otherwise>
                            <td class="cloud-virt ${vm.state}"><c:out value="${vm.fqdn}"/>
                                (<c:out value="${vm.cpuReservedString}"/> CPU<c:if
                                        test="${vm.cpu_reserved_x100!=vm.cpu_avail_x100}">/${vm.VCPU}VCPU</c:if>)
                            </td>
                            <td>
                                Cloud node "<c:out value="${vm.name}"/>" of user <t:vmowner owner="${vm.owner}"/> in
                                state ${vm.state}
                            </td>
                        </c:otherwise>
                    </c:choose>
                </tr>
            </c:forEach>
        </table>

        <% } %>
        <% } %>


        <%-- PBS nodes --%>
        <c:if test="${fn:length(actionBean.pbsNodes)>0}">
            <c:forEach items="${actionBean.pbsNodes}" var="virt">
                <h2><a name="${virt.name}"> </a><f:message key="machine_jsp_virtual"/> ${virt.name}</h2>
                <t:node_detail node="${virt}"/>
            </c:forEach>
        </c:if>

    </s:layout-component>
</s:layout-render>