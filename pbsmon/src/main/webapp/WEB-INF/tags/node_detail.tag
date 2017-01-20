<%@ tag pageEncoding="utf-8" %>
<%@ attribute name="node" type="cz.cesnet.meta.pbs.Node" rtexprvalue="true" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>


        <table class="node" cellspacing="0">
            <tr>
                <th class="${node.state}"><f:message key="nodejsp_name"/></th>
                <td colspan="2" class="${node.state}"><c:out value="${node.name}"/></td>
            </tr>
            <tr>
                <th class="${node.state}"><f:message key="nodejsp_state"/></th>
                <td colspan="2" class="${node.state}"><c:out value="${node.state}"/></td>
            </tr>
            <c:if test="${node.state=='down'}">
                <tr>
                    <th class="${node.state}"><f:message key="nodejsp_status_message"/></th>
                    <td colspan="2" class="${node.state}"><c:out value="${node.statusMessage}"/></td>
                </tr>
            </c:if>
            <tr>
                <th class="${node.state}"><f:message key="nodejsp_pbs_state"/></th>
                <td colspan="2" class="${node.state}"><c:out value="${node.pbsState}"/></td>
            </tr>
            <tr>
                <th class="${node.state}"><f:message key="nodejsp_ntype"/></th>
                <td colspan="2" class="${node.state}"><c:out value="${node.ntype}"/></td>
            </tr>
            <tr>
                <th class="${node.state}"><f:message key="nodejsp_properties"/></th>
                <td colspan="2" class="${node.state}">
                    <c:out value="${fn:join(node.properties,', ')}"/>
                </td>
            </tr>
            <c:if test="${! empty node.requiredQueue}">
                <tr>
                    <th class="${node.state}"><f:message key="nodejsp_queue"/></th>
                    <td colspan="2" class="${node.state}"><s:link
                            href="/queue/${node.requiredQueue}">${node.requiredQueue}</s:link></td>
                </tr>
            </c:if>

            <c:if test="${node.scratch.hasFreeSsd}">
            <tr>
                <th class="${node.state}"><f:message key="nodejsp_free_scratch_ssd"/></th>
                <td colspan="2" class="${node.state}"><c:out value="${node.scratch.ssdFreeHuman}"/></td>
            </tr>
            </c:if>
            <c:if test="${node.scratch.hasFreeLocal}">
                <tr>
                    <th class="${node.state}"><f:message key="nodejsp_free_scratch_local"/></th>
                    <td colspan="2" class="${node.state}"><c:out value="${node.scratch.localFreeHuman}"/></td>
                </tr>
            </c:if>
            <c:if test="${node.scratch.hasFreeShared}">
                <tr>
                    <th class="${node.state}"><f:message key="nodejsp_free_scratch_shared"/></th>
                    <td colspan="2" class="${node.state}"><c:out value="${node.scratch.sharedFreeHuman}"/></td>
                </tr>
            </c:if>

        </table>
        <!-- rezervace a vyuziti -->
        <table class="zakladni">
            <tr>
                <th><f:message key="nodejsp_usedcpu"/></th>
                <td>${node.usedCPUPercent}%</td>
                <td>${node.noOfUsedCPU} / ${node.noOfCPU}</td>
            </tr>
            <tr>
                <th><f:message key="nodejsp_usedmem"/></th>
                <td>${node.usedMemoryPercent}%</td>
                <td>${node.usedMemory} / ${node.totalMemory}</td>
            </tr>
            <c:if test="${node.hasGPU}">
            <tr>
                <th><f:message key="nodejsp_usedgpu"/></th>
                <td>${node.usedGPUPercent}%</td>
                <td>${node.noOfUsedGPUInt} / ${node.noOfGPU}</td>
            </tr>
            </c:if>
            <c:if test="${node.scratch.hasSsd}">
                <tr>
                    <th><f:message key="nodejsp_reserved_scratch_ssd"/></th>
                    <td>${node.scratch.ssdReservedPercent}%</td>
                    <td>${node.scratch.ssdReservedInPbsUnits} / ${node.scratch.ssdSizeInPbsUnits}</td>
                </tr>
                <tr>
                    <th><f:message key="nodejsp_used_scratch_ssd"/></th>
                    <td>${node.scratch.ssdUsedPercent}%</td>
                    <td>${node.scratch.ssdUsedInPbsUnits} / ${node.scratch.ssdSizeInPbsUnits}</td>
                </tr>
            </c:if>
            <c:if test="${node.scratch.hasLocal}">
                <tr>
                    <th><f:message key="nodejsp_reserved_scratch_local"/></th>
                    <td>${node.scratch.localReservedPercent}%</td>
                    <td>${node.scratch.localReservedInPbsUnits} / ${node.scratch.localSizeInPbsUnits}</td>
                </tr>
                <tr>
                    <th><f:message key="nodejsp_used_scratch_local"/></th>
                    <td>${node.scratch.localUsedPercent}%</td>
                    <td>${node.scratch.localUsedInPbsUnits} / ${node.scratch.localSizeInPbsUnits}</td>
                </tr>
            </c:if>
            <c:if test="${node.scratch.hasShared}">
                <tr>
                    <th><f:message key="nodejsp_reserved_scratch_shared"/></th>
                    <td>${node.scratch.sharedReservedPercent}%</td>
                    <td>${node.scratch.sharedReservedInPbsUnits} / ${node.scratch.sharedSizeInPbsUnits}</td>
                </tr>
                <tr>
                    <th><f:message key="nodejsp_used_scratch_shared"/></th>
                    <td>${node.scratch.sharedUsedPercent}%</td>
                    <td>${node.scratch.sharedUsedInPbsUnits} / ${node.scratch.sharedSizeInPbsUnits}</td>
                </tr>
            </c:if>
        </table>

        <c:if test="${not empty node.gpuJobMap}">
            <table class="zakladni">
                <tr>
                    <th>GPU</th>
                    <th>job</th>
                </tr>
                <c:forEach items="${node.gpuJobMap}" var="me">
                <tr>
                    <td><c:out value="${me.key}"/></td>
                    <td><c:out value="${me.value}"/></td>
                </tr>
                </c:forEach>
            </table>
        </c:if>

<!-- resources -->
<c:if test="${node.pbs.PBSPro}">
    <table class="zakladni">
        <tr>
            <th><t:i18n cs="zdroj" en="resource"/></th>
            <th><t:i18n cs="dostupné" en="available"/></th>
            <th><t:i18n cs="použito" en="assigned"/></th>
        </tr>
        <c:forEach items="${node.nodeResources}" var="res">
            <tr>
                <td><c:out value="${res.name}"/></td>
                <td>
                    <c:choose>
                        <c:when test="${fn:length(res.available)>25}"><c:out
                                value="${fn:substring(res.available, 0, 25)}"/> ...</c:when>
                        <c:otherwise><c:out value="${res.available}"/></c:otherwise>
                    </c:choose>
                </td>
                <td><c:out value="${res.assigned}"/></td>
            </tr>
        </c:forEach>
    </table>
</c:if>

        <c:if test="${node.maintenance}">
            <p class="maintenance"><f:message key="nodejsp_maintenance"><f:param
                    value="${node.comment}"/></f:message></p>
        </c:if>
        <c:if test="${node.plannedOutage}">
            <p class="maintenance">
                <f:message key="nodejsp_planned_outage_note"><f:param value="${node.comment}"/></f:message>
                <c:if test="${not empty node.availableBefore}"><f:message key="nodejsp_planned_outage_start"><f:param value="${node.availableBefore}"/></f:message></c:if>
                <c:if test="${not empty node.availableAfter}"><f:message key="nodejsp_planned_outage_end"><f:param value="${node.availableAfter}"/></f:message></c:if>
            </p>
        </c:if>
        <c:if test="${node.reserved}">
            <p class="comment"><f:message key="nodejsp_reserved"><f:param value="${node.comment}"/></f:message></p>
        </c:if>



        <!-- ulohy -->
        <br>
        <c:if test="${fn:length(node.jobs)>0 or fn:length(node.plannedJobs)>0}">
            <table class="job">
                <tr>
                    <th rowspan="2" align="center"><f:message key="nodejsp_jobs"/></th>
                    <th rowspan="2" align="center"><f:message key="jobs_user"/></th>
                    <th colspan="4" align="center"><f:message key="nodejsp_on_node"/></th>
                    <th rowspan="2" align="center"><f:message key="jobs_jobname"/></th>
                    <th rowspan="2" align="center"><f:message key="jobs_state"/></th>
                    <th rowspan="2" align="center" colspan="2"><f:message key="job_start_time"/></th>
                    <th rowspan="2" align="center" colspan="2"><f:message key="job_expected_endtime"/></th>
                    <th rowspan="2" align="center"><f:message key="jobs_queue"/></th>
                </tr>
                <tr>
                    <th align="center">RAM</th>
                    <th align="center" colspan="2">scratch</th>
                    <th align="center">CPU</th>
                </tr>
                <c:forEach items="${node.jobs}" var="job" varStatus="j">
                <tr>
                <td><s:link href="/job/${job.id}">${job.id}</s:link>
                <td align="right"><s:link href="/user/${job.user}">${job.user}</s:link></td>
                <td align="right">${job.nodeName2reservedResources[node.name].mem}</td>
                <td>${job.nodeName2reservedResources[node.name].scratchType} </td>
                <td>${job.nodeName2reservedResources[node.name].volume}</td>
                <td align="center">${job.nodeName2reservedResources[node.name].cpus}</td>
                <td>${job.jobName}</td>
                <td align="center" class="${job.state}">${job.state} - <f:message key='jobs_${job.state}'/></td>
                    <td align="right"><f:formatDate value="${job.timeStarted}" pattern="d.M."/></td>
                    <td align="right"><f:formatDate value="${job.timeStarted}" pattern="H:mm"/></td>
                    <td align="right"><f:formatDate value="${job.timeExpectedEnd}" pattern="d.M."/></td>
                    <td align="right"><f:formatDate value="${job.timeExpectedEnd}" pattern="H:mm"/></td>
                    <td align="center"><s:link beanclass="cz.cesnet.meta.stripes.QueueActionBean"><s:param name="queueName" value="${job.queueName}"/>${job.queueName}</s:link></td>
                </tr>
                </c:forEach>
                <c:forEach items="${node.plannedJobs}" var="job" varStatus="j">
                    <tr>
                        <td><s:link href="/job/${job.id}">${job.id}</s:link>
                        <td align="right"><s:link href="/user/${job.user}">${job.user}</s:link></td>
                        <td align="right">${job.nodeName2reservedResources[node.name].mem}</td>
                        <td>${job.nodeName2reservedResources[node.name].scratchType} </td>
                        <td>${job.nodeName2reservedResources[node.name].volume}</td>
                        <td align="center">${job.nodeName2reservedResources[node.name].cpus}</td>
                        <td>${job.jobName}</td>
                        <td align="center" class="${job.state}">${job.state} - <f:message key='jobs_${job.state}'/></td>
                        <c:choose>
                            <c:when test="${not empty job.plannedStart}">
                                <td align="right"><f:formatDate value="${job.plannedStart}" pattern="d.M."/></td>
                                <td align="right"><f:formatDate value="${job.plannedStart}" pattern="H.mm"/></td>
                            </c:when>
                            <c:otherwise>
                                <td colspan="2"></td>
                            </c:otherwise>
                        </c:choose>
                        <c:choose>
                            <c:when test="${not empty job.plannedEnd}">
                                <td align="right"><f:formatDate value="${job.plannedEnd}" pattern="d.M."/></td>
                                <td align="right"><f:formatDate value="${job.plannedEnd}" pattern="H.mm"/></td>
                            </c:when>
                            <c:otherwise>
                                <td colspan="2"></td>
                            </c:otherwise>
                        </c:choose>
                        <td align="center"><s:link beanclass="cz.cesnet.meta.stripes.QueueActionBean"><s:param name="queueName" value="${job.queueName}"/>${job.queueName}</s:link></td>
                    </tr>
                </c:forEach>
            </table>
            <c:if test="${not empty node.lastJobEndTime}">
            <br>
            <p>
                <f:message key="jobs_last_job_ends_time"><f:param value="${node.lastJobEndTime}"/><f:param value="${node.lastJobEndInMinutes}"/></f:message>
            </p>
            </c:if>
        </c:if>

        <!-- fronty -->
        <br>
        <f:message key="node_detail_fronty"/> <s:link href="/queues/list" anchor="${node.pbs.server.name}"><c:out
            value="${node.pbs.server.shortName}"/></s:link>:
        <t:queues queues="${node.queues}"/>

        <c:if test="${! empty node.maxWalltime}">
            <div class="walltimelimit">
                <f:message key="node_detail_max_walltime"><f:param value="${node.maxWalltime}"/></f:message>
            </div>
        </c:if>
        <c:if test="${! empty node.minWalltime}">
            <div class="walltimelimit">
                <f:message key="node_detail_min_walltime"><f:param value="${node.minWalltime}"/></f:message>
            </div>
        </c:if>



        <!-- atributy -->
        <br>
        <table class="attributes">
            <c:forEach items="${node.attributes}" var="met">
                <tr>
                    <td align="left">${met.key}</td>
                    <td align="left"><c:choose>
                        <c:when test="${fn:length(met.value)>70}"><c:out value="${fn:substring(met.value, 0, 70)}"/> ...</c:when>
                        <c:otherwise><c:out value="${met.value}"/></c:otherwise>
                    </c:choose></td>
                </tr>
            </c:forEach>
        </table>
        <br>

        <h4><f:message key="node_detail_tag_outages"/></h4>
        <c:choose>
            <c:when test="${! empty node.outages}">
                <table class="zakladni">
                    <tr>
                        <th><f:message key="node_detail_tag_typ"/></th>
                        <th colspan="2"><f:message key="node_detail_tag_start"/></th>
                        <th colspan="2"><f:message key="node_detail_tag_end"/></th>
                        <th><f:message key="node_detail_tag_comment"/></th>
                    </tr>
                    <c:forEach items="${node.outages}" var="outage" varStatus="i">
                        <tr>
                            <td><c:out value="${outage.type}"/></td>
                            <td style="text-align: right; border-right: 0;"><f:formatDate type="date"
                                                                                          dateStyle="default"
                                                                                          value="${outage.start}"/></td>
                            <td style="text-align: right; border-left: 0;"><f:formatDate type="time" timeStyle="default"
                                                                                         value="${outage.start}"/></td>
                            <td style="text-align: right; border-right: 0;"><f:formatDate type="date"
                                                                                          dateStyle="default"
                                                                                          value="${outage.end}"/></td>
                            <td style="text-align: right; border-left: 0;"><f:formatDate type="time" timeStyle="default"
                                                                                         value="${outage.end}"/></td>
                            <td><c:out value="${outage.comment}"/></td>
                        </tr>
                    </c:forEach>
                </table>
            </c:when>
            <c:otherwise>
                <f:message key="node_detail_tag_none_found"/>
            </c:otherwise>
        </c:choose>
