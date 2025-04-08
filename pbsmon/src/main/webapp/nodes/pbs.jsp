<%@ page pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<s:useActionBean beanclass="cz.cesnet.meta.stripes.NodesActionBean" var="actionBean"/>
<f:message var="titlestring" key="nodes_pbs.titul" scope="request"/>
<s:layout-render name="/layout.jsp">
    <s:layout-component name="telo">



        <p><f:message key="nodes_jsp_celkem_cpu"/>: <s:link href="/hardware">${actionBean.cpuMap['all']}</s:link></p>

        <p><f:message key="nodes_jsp_waiting_text1"/>
            <s:link href="/queues/jobsQueued"><strong><f:message key="nodes_jsp_waiting_jobs"><f:param value="${actionBean.jobsQueuedCount}"/></f:message></strong></s:link>
            <f:message key="nodes_jsp_waiting_text3"/>
        </p>

        <c:forEach items="${actionBean.ownerOrganisations}" var="ownerOrganisation">

            <h3><a name="${ownerOrganisation.id}"></a><strong><f:message key="${ownerOrganisation.nameKey}"/></strong> (${actionBean.cpuMap[ownerOrganisation.id]} CPU)</h3>
            <table width="90%">
                <c:forEach var="zdr" items="${ownerOrganisation.perunComputingResources}" varStatus="s">
                    <c:if test="${!s.first}"><tr><td colspan="2">&nbsp;</td></tr></c:if>
                    <tr>
                        <td colspan="2"><a name="${zdr.id}"></a>
                            <s:link href="/resource/${zdr.id}"><c:out value="${zdr.name}"/></s:link>
                            <c:if test="${zdr.cluster}">(${actionBean.cpuMap[zdr.id]} CPU)</c:if>
                            - <f:message key="${zdr.descriptionKey}"/></td>
                    </tr>
                    <f:message key="${zdr.specKey}" var="chunk"/>
                    <c:if test="${! empty chunk}">
                    <tr>
                        <td colspan="2" style="padding-left: 20px; font-size: x-small;"><f:message key="${zdr.specKey}"/></td>
                    </tr>
                    </c:if>
                    <tr>
                        <td colspan="2" style="padding-left: 20px;">

                        <c:choose>
                            <c:when test="${zdr.cluster}">
                                <c:if test="${! empty zdr.perunMachines}">
                                <table class="nodes" cellspacing="0">
                                <tr>
                                <c:forEach items="${actionBean.getMachinesSortedByPbsNodeNames(zdr)}" var="perunMachine" varStatus="i">
                                    <t:stroj_pbs perunMachine="${perunMachine}"/>
                                    <c:if test="${i.count%8==0}"></tr><tr></c:if>
                                </c:forEach>
                                </tr>
                                </table>    
                                </c:if>
                            </c:when>
                            <c:otherwise>
                                <table class="nodes" cellspacing="0">
                                <tr><t:stroj_pbs perunMachine="${zdr.perunMachine}"/></tr>
                                </table>
                            </c:otherwise>
                        </c:choose>


                        </td>
                    </tr>
                </c:forEach>
            </table>

        </c:forEach>

        <h2><a name="gpu"></a><f:message key="nodes_jsp_gpu_title"/></h2>

        <table class="zakladni"><tr>
            <c:forEach items="${actionBean.gpuNodes}" var="node" varStatus="s">
            <td><s:link style="color: black;" href="/node/${node.name}">${node.shortName}</s:link>
                <c:choose>
                    <c:when test="${node.state=='maintenance' or node.state=='maintenance-busy'}">
                        <table><tr>
                            <c:forEach begin="${1}" end="${node.noOfGPUInt}" var="i">
                                <td style="height: 10px; width: 10px; padding: 0; border: 0;" class="maintenance"></td>
                            </c:forEach>
                        </tr></table>
                    </c:when>
                    <c:otherwise>
                        <table><tr>
                            <c:forEach begin="${1}" end="${node.noOfGPUInt}" var="i">
                                <c:choose><c:when test="${i<=node.noOfUsedGPUInt}"><c:set var="cssclass" value="job-busy"/></c:when><c:otherwise><c:set var="cssclass" value="free"/></c:otherwise></c:choose>
                                <td style="height: 10px; width: 10px; padding: 0; border: 0;" class="${cssclass}"></td>
                            </c:forEach>
                        </tr></table>
                    </c:otherwise>
                </c:choose>

            </td>
            <c:if test="${s.count%10==0}"></tr><tr></c:if>
            </c:forEach>
        </table>
    </s:layout-component>
</s:layout-render> 
