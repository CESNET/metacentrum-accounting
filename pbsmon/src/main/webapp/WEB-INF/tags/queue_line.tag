<%@ tag %>
<%@ attribute name="queue" type="cz.cesnet.meta.pbs.Queue" rtexprvalue="true" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>

<tr>
    <td style="text-align: right;"><s:link href="/queue/${queue.name}">${queue.name}</s:link></td>
    <td><c:if test="${queue.locked}"><s:link href="/queues/list" anchor="urceni"><img border="0" src="${pageContext.request.contextPath}/img/lock.png" alt="locked" title="<f:message key="queue_line_tag_locked"/>: ${queue.lockedFor}"/></s:link></c:if></td>
    <td>${queue.priority}</td>
    <td> ${(empty queue.walltimeMin) ? 0 : queue.walltimeMin} - ${(empty queue.walltimeMax) ? 0 : queue.walltimeMax }</td>
    <td>${queue.jobsQueued}</td>
    <td>${queue.jobsRunning} / ${queue.maxRunningJobs}</td>
    <td>${queue.jobsCompleted}</td>
    <td>${queue.jobsTotal}</td>
    <td>${queue.maxUserRun}</td>
    <td>${queue.maxUserCPU}</td>
    <td>${queue.fairshareTree=='default'?'':queue.fairshareTree}</td>
</tr>
