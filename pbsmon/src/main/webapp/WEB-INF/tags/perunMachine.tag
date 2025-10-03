<%@ tag %>
<%@ attribute name="perunMachine" type="cz.cesnet.meta.perun.api.PerunMachine" rtexprvalue="true" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<td class="node ${perunMachine.state} <c:if test='${perunMachine.cloudUsable}'>cloudusable</c:if>" style="height: ${perunMachine.cpuNum/8}em; padding: 0;">
    <c:choose>
        <c:when test="${perunMachine.state=='partialy-free'}">
            <div class="usedwrapper" style="height: ${perunMachine.cpuNum/8}em;">
                <div class="usedbox" style="height: ${perunMachine.usedPercent}%;"></div>
                <s:link style="height: ${perunMachine.cpuNum/8}em;" href="/machine/${perunMachine.name}"><c:out value="${perunMachine.shortName}"/> (${perunMachine.cpuNum}&nbsp;CPU)</s:link>
            </div>
        </c:when>
        <c:otherwise>
            <s:link style="height: ${perunMachine.cpuNum/8}em;" href="/machine/${perunMachine.name}"><c:out value="${perunMachine.shortName}"/> (${perunMachine.cpuNum}&nbsp;CPU)</s:link>
        </c:otherwise>
    </c:choose>
</td>
