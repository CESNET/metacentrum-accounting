<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/include/taglibs.jsp"%>

<fmt:message var="titlestring" scope="request" key="stats.title" />
<s:layout-render name="/layout.jsp" menu="internal/acct" >
 <s:layout-component name="telo">
     <p>Počítání statistik za delší období může trvat i dlouhé hodiny,
     více dotazů zároveň může server zcela zahltit.
         <b>Prosím používat s rozmyslem.</b></p>
     <p>
         <stripes:link href="/BinariesUserTime.action?view">
             <stripes:param name="periodInDays">1</stripes:param>
             <fmt:message key="stats.binary.title"/>
         </stripes:link>
     </p>
     <p>
         <stripes:link href="/UsernameUserTime.action?view">
             <stripes:param name="periodInDays">1</stripes:param>
             <fmt:message key="stats.usertime.title"/>
         </stripes:link>
     </p>
     <p>
         <stripes:link href="/UsernameJobs.action?view">
             <stripes:param name="periodInDays">1</stripes:param>
             <fmt:message key="stats.userjobs.title"/>
         </stripes:link>
     </p>
     <p>
         <stripes:link href="/ChartStats.action">             
             <fmt:message key="stats.chart.title"/>
         </stripes:link>
     </p>
 </s:layout-component>
</s:layout-render>
