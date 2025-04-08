<%@ page pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<s:useActionBean beanclass="cz.cesnet.meta.stripes.NodesActionBean" var="actionBean"/>
<f:message var="titlestring" key="nodes.titul" scope="request"/>
<s:layout-render name="/layout.jsp">
    <s:layout-component name="hlava">
        <style type="text/css">
            span.volne {
                background-color: green;
                padding-left: 3px;
                padding-right: 3px;
            }
            span.uzite {
                background-color: #6060FF;;
                padding-left: 3px;
                padding-right: 3px;
            }
        </style>
    </s:layout-component>
    <s:layout-component name="telo">



        <h2><a name="storages"></a><f:message key="nodes_jsp_storages"/></h2>

        <h3><a name="storages_scratch"></a><f:message key="nodes_jsp_storage_scratch_title"/></h3>
            <p><f:message key="nodes_jsp_storage_scratch_text"/></p>
            <p><f:message key="nodes_jsp_storage_scratch_warn"/></p>

        <h3><a name="storages_arrays"></a><f:message key="nodes_jsp_storage_arrays_title"/></h3>

        <table class="zakladni">
            <tr>
                <th><f:message key="nodes_jsp_storages_dir"/></th>
                <th><f:message key="nodes_jsp_storages_usage"/> -
                    <span class="uzite"><f:message key="nodes_jsp_storages_used"/></span>
                    <span class="volne"><f:message key="nodes_jsp_storages_free"/></span></th>
                <th><f:message key="nodes_jsp_storages_size"/></th>
            </tr>
            <c:forEach items="${actionBean.storagesInfo.storages}" var="s">
                <tr>
                    <td><c:out value="${s.dir}"/></td>
                    <td style="text-align: right;"><div style="width: ${600*s.totalGB/4000000}px; background-color: green;">
                        <div style="width: ${600*s.usedGB/4000000}px; background-color: #6060FF;">
                            <div style="padding: 2px;"><b>${s.usedPercent}%</b></div></div>
                    </div>
                    </td>
                    <td style="text-align: right;"><c:out value="${s.total}"/></td>
                </tr>
            </c:forEach>
            <tr>
                <td colspan="2"><f:message key="nodes_jsp_storages_totalSize"/></td>
                <td style="text-align: right;"><b>${actionBean.storagesInfo.totalSize}</b></td>
            </tr>
            <tr>
                <td colspan="2"><f:message key="nodes_jsp_storages_totalUsed"/></td>
                <td style="text-align: right;">${actionBean.storagesInfo.totalUsed}</td>
            </tr>
            <tr>
                <td colspan="2"><f:message key="nodes_jsp_storages_totalFree"/></td>
                <td style="text-align: right;">${actionBean.storagesInfo.totalFree}</td>
            </tr>
        </table>
        <p><f:message key="nodes_jsp_storage_arrays_text"/></p>
        <p><f:message key="nodes_jsp_storage_arrays_warn"/></p>


        <h3><a name="storages_hsm"></a><f:message key="nodes_jsp_storage_hsm_title"/></h3>

        <table class="zakladni">
            <tr>
                <th><f:message key="nodes_jsp_storages_dir"/></th>
                <th><f:message key="nodes_jsp_storages_usage"/> -
                    <span class="uzite"><f:message key="nodes_jsp_storages_used"/></span>
                    <span class="volne"><f:message key="nodes_jsp_storages_free"/></span></th>
                <th><f:message key="nodes_jsp_storages_size"/></th>
            </tr>
            <c:forEach items="${actionBean.hsmInfo.storages}" var="s">
                <tr>
                    <td><c:out value="${s.dir}"/></td>
                    <td style="text-align: right;"><div style="width: ${600*s.totalGB/16000000}px; background-color: green;">
                        <div style="width: ${600*s.usedGB/16000000}px; background-color: #6060FF;">
                            <div style="padding: 2px;"><b>${s.usedPercent}%</b></div></div>
                    </div>
                    </td>
                    <td style="text-align: right;"><c:out value="${s.total}"/></td>
                </tr>
            </c:forEach>
            <tr>
                <td colspan="2"><f:message key="nodes_jsp_storages_totalSize"/></td>
                <td style="text-align: right;"><b>${actionBean.hsmInfo.totalSize}</b></td>
            </tr>
            <tr>
                <td colspan="2"><f:message key="nodes_jsp_storages_totalUsed"/></td>
                <td style="text-align: right;">${actionBean.hsmInfo.totalUsed}</td>
            </tr>
            <tr>
                <td colspan="2"><f:message key="nodes_jsp_storages_totalFree"/></td>
                <td style="text-align: right;">${actionBean.hsmInfo.totalFree}</td>
            </tr>
        </table>
        <p><f:message key="nodes_jsp_storage_hsm_text"/></p>
        <p><f:message key="nodes_jsp_storage_hsm_warn"/></p>


        <h2><a name="computing_machines"></a><f:message key="nodes_jsp_computing_machines"/></h2>

        <p><f:message key="nodes_jsp_celkem_cpu"/>: <s:link href="/hardware">${actionBean.cpuMap['all']}</s:link></p>

        <p><f:message key="nodes_jsp_waiting_text1"/>
            <s:link href="/queues/jobsQueued"><strong><f:message key="nodes_jsp_waiting_jobs"><f:param value="${actionBean.jobsQueuedCount}"/></f:message></strong></s:link>
            <f:message key="nodes_jsp_waiting_text3"/>
        </p>

        <p><f:message key="nodes_jsp_barvy"/> -
                <span style="padding: 2px;" class="free"><f:message key="nodesjsp_state_free"/></span>
            <span class="partialy-free" style="display: inline-block; position: relative; padding: 2px;"><!-- blue absolutely positioned div with procentual height-->
                <span style="position: absolute; bottom: 0; left: 0; background-color: #6060FF; width: 100%; height: 50%; z-index: 1"></span>
                <span style="z-index: 10; position: relative;"><f:message key="nodesjsp_state_partialy-free"/></span>
            </span>
                <span style="padding: 2px;" class="job-busy"><f:message key="nodesjsp_state_blue"/></span>
                <span style="padding: 2px;" class="maintenance"><f:message key="nodesjsp_state_gray"/></span>
        </p>

        <c:forEach items="${actionBean.ownerOrganisations}" var="ownerOrganisation">

            <h3><a name="${ownerOrganisation.id}"></a><strong><f:message key="${ownerOrganisation.nameKey}"/></strong> (${actionBean.cpuMap[ownerOrganisation.id]} CPU)</h3>
            <%--<p style="font-size: smaller; padding-left: 5px;"><f:message key="${centrum.specKey}"/></p>--%>
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
                                <c:forEach items="${zdr.perunMachines}" var="perunMachine" varStatus="i">
                                    <t:perunMachine perunMachine="${perunMachine}"/>
                                    <c:if test="${i.count%8==0}"></tr><tr></c:if>
                                </c:forEach>
                                </tr>
                                </table>    
                                </c:if>
                            </c:when>
                            <c:otherwise>
                                <table class="nodes" cellspacing="0">
                                <tr><t:perunMachine perunMachine="${zdr.perunMachine}"/></tr>
                                </table>
                            </c:otherwise>
                        </c:choose>


                        </td>
                    </tr>
                </c:forEach>
            </table>

        </c:forEach>
<c:if test="${fn:length(actionBean.zbyle)>0}">
        <h2><f:message key="nodes_jsp_nezarazene"/> (${actionBean.cpuMap['zbyle']} CPU)</h2>
        <table class="nodes">
            <tr><c:set var="numinrow" value="${1}"/>
                <c:forEach items="${actionBean.zbyle}" var="perunMachine">
                <td><c:out value="${perunMachine.shortName}"/> (${perunMachine.cpuNum} CPU)</td>
                <c:if test="${numinrow%6==0}"></tr>
            <tr></c:if>
                    <c:set var="numinrow" value="${numinrow+1}"/>
                </c:forEach>
        </table>
</c:if>

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
