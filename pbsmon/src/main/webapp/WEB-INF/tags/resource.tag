<%@ tag %>
<%@ attribute name="resource" type="cz.cesnet.meta.perun.api.VypocetniZdroj" rtexprvalue="true" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<table class="zakladni resource">
    <tr>
        <th>CPU</th>
        <td><c:out value='${resource.cpuDesc}'/></td>
    </tr>
    <tr>
        <th>RAM</th>
        <td><c:out value='${resource.memory}'/></td>
    </tr>
    <c:if test="${not empty resource.gpuDesc}">
    <tr>
        <th>GPU</th>
        <td><c:out value='${resource.gpuDesc}'/></td>
    </tr>
    </c:if>
    <tr>
        <th>disk</th>
        <td><f:message key="${resource.diskKey}"/></td>
    </tr>
    <tr>
        <th>net</th>
        <td><f:message key="${resource.networkKey}"/></td>
    </tr>
    <tr>
        <th><f:message key="resource.comment"/></th>
        <td><f:message key="${resource.commentKey}"/></td>
    </tr>
    <tr>
        <th><f:message key="resource.owner"/></th>
        <td><f:message key="${resource.ownerKey}"/></td>
    </tr>
</table>

