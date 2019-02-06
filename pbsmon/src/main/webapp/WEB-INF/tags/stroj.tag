<%@ tag %>
<%@ attribute name="stroj" type="cz.cesnet.meta.perun.api.Stroj" rtexprvalue="true" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<td class="node ${stroj.state} <c:if test='${stroj.openNebulaUsable}'>opennebulausable</c:if>" style="height: ${stroj.cpuNum/8}em; padding: 0;">
    <c:choose>
        <c:when test="${stroj.state=='partialy-free'}">
            <div class="usedwrapper" style="height: ${stroj.cpuNum/8}em;">
                <div class="usedbox" style="height: ${stroj.usedPercent}%;"></div>
                <s:link style="height: ${stroj.cpuNum/8}em;" href="/machine/${stroj.name}"><c:out value="${stroj.shortName}"/> (${stroj.cpuNum}&nbsp;CPU)</s:link>
            </div>
        </c:when>
        <c:otherwise>
            <s:link style="height: ${stroj.cpuNum/8}em;" href="/machine/${stroj.name}"><c:out value="${stroj.shortName}"/> (${stroj.cpuNum}&nbsp;CPU)</s:link>
        </c:otherwise>
    </c:choose>
</td>
