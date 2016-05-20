<%@ page pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>

<f:message var="titlestring" key="index.titul" scope="request"/>
<s:layout-render name="/layout.jsp">
    <s:layout-component name="telo">

        <ul>
            <li><s:link beanclass="cz.cesnet.meta.stripes.NodesActionBean"><f:message key="nodes.titul"/></s:link></li>
            <li><s:link beanclass="cz.cesnet.meta.stripes.HardwareActionBean"><f:message key="hardware.titul"/></s:link></li>
            <li><s:link beanclass="cz.cesnet.meta.stripes.NodesActionBean" event="virtual"><f:message key="mapping.titul"/></s:link></li>
            <li><s:link beanclass="cz.cesnet.meta.stripes.QueuesActionBean"><f:message key="queues.titul"/></s:link></li>
            <li><s:link beanclass="cz.cesnet.meta.stripes.QueuesActionBean" event="jobsQueued"><f:message key="jobs_queued.titul"/></s:link></li>
            <li><s:link beanclass="cz.cesnet.meta.stripes.JobsActionBean"><f:message key="jobs.titul"/></s:link></li>
            <li><s:link beanclass="cz.cesnet.meta.stripes.PropsActionBean"><f:message key="props.titul"/></s:link></li>
            <li><s:link beanclass="cz.cesnet.meta.stripes.UsersActionBean"><f:message key="users_headline"/></s:link></li>
            <li><s:link beanclass="cz.cesnet.meta.stripes.PersonActionBean"><f:message key="person_headline"/></s:link></li>
            <li><s:link beanclass="cz.cesnet.meta.stripes.CloudActionBean"><f:message key="cloud.titul"/></s:link></li>
        </ul>

    </s:layout-component>
</s:layout-render> 
