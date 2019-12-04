<%@ tag pageEncoding="utf-8" %>
<%@ attribute name="vms" type="java.util.List<cz.cesnet.meta.cloud.CloudVM>" rtexprvalue="true" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<h3><t:i18n cs="Využití cloudu" en="Cloud usage"/></h3>
<c:choose>
    <c:when test="${empty vms}">
        <t:i18n cs="žádné VM v cloudu nenalezeny" en="no VMs in cloud"/>
    </c:when>
    <c:otherwise>
        <table class="vms">
            <c:forEach items="${vms}" var="vm">
                <tr>
                    <c:choose>
                        <c:when test="${vm.pbsNode}">
                            <td class="cloud-virt cloudpbshost">
                                <s:link href="/node/${vm.fqdn}"><c:out value="${vm.fqdn}"/>
                                    (<c:out value="${vm.cpuReservedString}"/> CPU)
                                </s:link>
                            </td>
                            <td>
                                PBS node
                            </td>
                        </c:when>
                        <c:when test="${vm.state=='ACTIVE'}">
                            <td class="cloud-virt ACTIVE"><c:out value="${vm.fqdn}"/>
                                (<c:out value="${vm.cpuReservedString}"/> CPU)
                            </td>
                            <td>
                                Cloud node "<c:out value="${vm.name}"/>"
                                running since <f:formatDate value="${vm.startTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
                                at host <s:link href="/machine/${vm.physicalHostFqdn}">${vm.physicalHostFqdn}</s:link>
                            </td>
                        </c:when>
                        <c:otherwise>
                            <td class="cloud-virt ${vm.state}"><c:out value="${vm.fqdn}"/>
                                (<c:out value="${vm.cpuReservedString}"/> CPU)
                            </td>
                            <td>
                                Cloud node "<c:out value="${vm.name}"/>" in state ${vm.state}
                            </td>
                        </c:otherwise>
                    </c:choose>
                </tr>
            </c:forEach>
        </table>
    </c:otherwise>
</c:choose>