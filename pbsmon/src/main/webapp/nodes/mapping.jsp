<%@ page pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<s:useActionBean beanclass="cz.cesnet.meta.stripes.NodesActionBean" var="actionBean"/>
<f:message var="titlestring" key="mapping.titul" scope="request"/>
<s:layout-render name="/layout.jsp" titulklic="mapping.titul">
    <s:layout-component name="telo">
<style type="text/css">
    table.mapping { empty-cells: show;  border-collapse: collapse; margin-left: 10px;}
    table.mapping td { border: 2px solid black; padding: 5px; }
    table.mapping td.name { border-right: 0; }
    table.mapping td.cpu {  border-left: 0; border-right: 0; }
    table.mapping td.pbs1 {  border-left: 0; border-right: 0; }
    table.mapping td.pbs2 {  border-left: 0; border-right: 0; }
    table.mapping td.pbs3 {  border-left: 0;  }
    table.mapping td.v1,table.mapping td.v2,table.mapping td.v3,table.mapping td.v4 { border: 1px solid gray; }
    table.mapping td.v1 {  border-right: 0; }
    table.mapping td.v2 {  border-left: 0; border-right: 0; }
    table.mapping td.v3 {  border-left: 0; border-right: 0; }
    table.mapping td.v4 {  border-left: 0; }
    table.mapping td.e1 {  border: 0; }
    table.mapping td.cpuDesc { border-left: 0; border-right: 0;}
    table.mapping td.nonpbs { border: 1px solid gray; }

    td.v2, td.v3, td.v4, td.pbs1, td.pbs2, td.pbs3 { font-size: x-small; }
</style>

 
        
     <table class="mapping">
         <thead>
           <tr>
            <th colspan="5"><f:message key="mapping_jsp_fyzicke"/></th>
            <th colspan="${actionBean.maxVirtual*4}"><f:message key="mapping_jsp_virtualni"/></th>
           </tr>
         </thead>
         <tbody>
         <c:forEach items="${actionBean.fyzicke}" var="stroj" varStatus="i">
             <tr>
                 <td>${i.count}</td>
                 <td class="stroj name"><span class="${stroj.state}">&nbsp;</span>
                     <a name="${stroj.name}"> </a><s:link href="/machine/${stroj.name}">${stroj.shortName}</s:link>
                 </td>
                 <td class="stroj cpuDesc">${stroj.cpuNum}&nbsp;CPU</td>
                 <c:set var="snode" value="${actionBean.nodeMap[stroj.name]}"/>
                 <c:choose>
                     <c:when test="${! empty snode}">
                         <td class="node pbs1 ${snode.state}"><s:link href="/node/${snode.name}" class="${snode.state}">${snode.noOfCPU} CPU</s:link></td>
                         <td class="node pbs2 ${snode.state}"><s:link href="/node/${snode.name}" class="${snode.state}">${snode.totalMemoryB} RAM</s:link></td>
                         <td class="node pbs3 ${snode.state}"><s:link href="/node/${snode.name}" class="${snode.state}">${snode.scratch.anySizeInHumanUnits} HDD</s:link></td>
                     </c:when>
                     <c:otherwise>
                         <td class="pbs1"> </td>
                         <td class="pbs2"> </td>
                         <td class="pbs3"> </td>
                     </c:otherwise>
                 </c:choose>


                 <c:forEach items="${actionBean.mapping.physical2virtual[stroj.name]}" var="jmenoVirtualniho">
                     <c:set var="vnode" value="${actionBean.nodeMap[jmenoVirtualniho]}"/>
                     <c:choose>
                         <c:when test="${! empty vnode}">

                             <td class="node v1 ${vnode.state}"><s:link href="/node/${vnode.name}">${vnode.shortName}</s:link> </td>
                             <td class="node v2 ${vnode.state}">${vnode.noOfCPU} CPU</td>
                             <td class="node v3 ${vnode.state}">${vnode.totalMemoryB} RAM</td>
                             <td class="node v4 ${vnode.state}">${vnode.scratch.anySizeInHumanUnits} HDD
                                 <c:if test="${! empty param.states}">(${vnode.pbsState})</c:if>
                             </td>
                         </c:when>
                         <c:otherwise>
                             <td class="nonpbs job-exclusive" colspan="4">${jmenoVirtualniho}
                                 <c:if test="${not empty actionBean.fqdn2CloudVMMap[jmenoVirtualniho]}">
                                     ${actionBean.fqdn2CloudVMMap[jmenoVirtualniho].cpuReservedString} CPU
                                 </c:if>
                             </td>
                         </c:otherwise>
                     </c:choose>

                 </c:forEach>
                 <c:forEach begin="1" end="${actionBean.maxVirtual - fn:length(actionBean.mapping.physical2virtual[stroj.name])}">
                     <td colspan="4" class="e1"> </td>
                 </c:forEach>

             </tr>
         </c:forEach>
         </tbody>
     </table>

    <h2><f:message key="nodes_jsp_barvy"/></h2>
    <jsp:include page="node_states_table.jsp" />

        <br>
    </s:layout-component>
</s:layout-render> 
