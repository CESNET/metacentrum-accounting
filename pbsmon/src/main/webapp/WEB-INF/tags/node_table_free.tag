<%@ tag %>
<%@ attribute name="nodes" type="java.util.List<cz.cesnet.meta.pbs.Node>" rtexprvalue="true" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>

<c:set var="numinrow" value="${0}"/>
  <table class="nodes" cellspacing="0" border="0">
  <tr>
  <c:forEach items="${nodes}" var="node">
     <c:if test="${numinrow==5}">
         <c:set var="numinrow" value="${0}"/>
         </tr><tr>
     </c:if>
     <td class="${node.state}"> <s:link class="${node.state}" href="/node/${node.name}">${node.shortName}
     <c:if test="${node.noOfFreeCPUInt>0}"><span style="font-size: xx-small;">(${node.noOfFreeCPU}&nbsp;CPU, ${node.freeMemoryB} RAM, ${node.scratch.anySizeInHumanUnits} HDD)</span></c:if></s:link></td>
     <c:set var="numinrow" value="${numinrow+1}"/>
 </c:forEach>
      </tr>
  </table>