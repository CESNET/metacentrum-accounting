<%@ page import="java.util.Date" %>
<%@ page pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<s:useActionBean beanclass="cz.cesnet.meta.stripes.PersonActionBean" var="actionBean"/>
<f:message var="titlestring" key="person_headline" scope="request"/>
<s:layout-render name="/layout.jsp">
    <s:layout-component name="telo">

<p><f:message key="person_text" ><f:param value="${actionBean.user}" /></f:message></p>

<h3><f:message key="person_jobs" ><f:param value="${actionBean.user}" /></f:message></h3>

    <table class="users">
         <tr>
             <th rowspan="2"><f:message key="jobs_user"/></th>
             <th colspan="5"><f:message key="users_jobs_count"/></th>
             <th colspan="5"><f:message key="users_jobs_ncpu"/></th>
         </tr>
         <c:set var="tokens"
                value="jobsTotal,jobsStateQ,jobsStateR,jobsStateC,jobsOther,cpusTotal,cpusStateQ,cpusStateR,cpusStateC,cpusOther"/>
         <tr>
             <c:forTokens items="${tokens}" delims="," var="P">
                 <th><f:message key="users_jobs_${P}"/></th>
             </c:forTokens>
         </tr>
         <tr>
             <td><s:link href="/user/${actionBean.jobUser.name}">${actionBean.jobUser.name}</s:link></td>
             <c:forTokens items="${tokens}" delims="," var="P">
                 <td align="center">${actionBean.jobUser[P]}</td>
             </c:forTokens>
         </tr>
     </table>

    <p><s:link href="/user/${actionBean.user}"><f:message key="person_vypis_uloh" /></s:link></p>

    <p><a href="https://metavo.metacentrum.cz/${pageContext.request.locale}/myaccount/kvoty"><f:message key="nodes_jsp_person_quotas_link"/></a>.</p>


     <h3><t:i18n cs="Sestavovače qsub" en="Qsub assemblers"/></h3>
     <ul>
         <li><s:link href="/qsub_torque?user=${actionBean.user}"><t:i18n cs="Sestavovač qsub pro Torque" en="Qsub assembler for Torque"/></s:link></li>
         <li><s:link href="/qsub_pbspro?user=${actionBean.user}"><t:i18n cs="Sestavovač qsub pro PBSPro" en="Qsub assembler for PBSPro"/></s:link></li>
     </ul>
<h3><f:message key="person_accessible_queues"/> <c:out value="${actionBean.user}" /></h3>

        <table class="queue">
          <t:queue_heading/>
         <c:forEach items="${actionBean.queues}" var="q" >
             <t:queue_line queue="${q}"/>
         </c:forEach>
        </table>


            </s:layout-component>
</s:layout-render>
