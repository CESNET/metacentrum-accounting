<%
response.setHeader("Cache-Control", "no-cache");
%>
<%@ page contentType="text/html;chartset=utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>

<%--<s:useActionBean beanclass="cz.cesnet.meta.stripes.BaseActionBean" var="actionBean"/>--%>

<s:layout-definition>
    <c:set var="locale" scope="request" value="${sessionScope['MujLocalePicker.locale']}"/>
    
   <c:import context="/look" url="/metavo/dil1.jsp"/> 

    <link type="text/css" href="${pageContext.request.contextPath}/pbsmon.css?version=201712131446" rel="stylesheet"/>
    <s:layout-component name="hlava"/>
   <c:import context="/look" url="/metavo/dil2.jsp"/> 
<%--<style>
    #cesnet_linker img {vertical-align: middle;}
    #cesnet_linker_placeholder { min-height: 45px; }
</style>
<div id="cesnet_linker_placeholder" data-lang="${locale}"
 data-lang-cs-href="${pageContext.request.contextPath}/locale/cs?kam=${requestScope['javax.servlet.forward.request_uri']}"
 data-lang-en-href="${pageContext.request.contextPath}/locale/en?kam=${requestScope['javax.servlet.forward.request_uri']}" ></div>--%>
   <c:import context="/look" url="/metavo/dil2b.jsp"/> 

    <c:import charEncoding="utf-8" context="/"
              url="/export/sites/meta/${pageContext.request.locale == 'cs' ? 'cs' : 'en'}/${initParam.menuName}/menu.jsp.html"/>

   <c:import context="/look" url="/metavo/dil3.jsp"/> 
    <div style="float: right;">
        <table>
        <c:forEach items="${actionBean.timesLoaded}" var="tl">
            <tr <c:if test="${tl.old}">style="color: red;" </c:if> >
                <td style="text-align: right;"><f:message key="layout.data.from" ><f:param value="${tl.service}"/></f:message> <c:out value="${tl.server}"/>:</td>
                <td><f:formatDate value="${tl.time}" type="both"/></td>
            </tr>
        </c:forEach>
            <tr>
                <td style="text-align: right;"><f:message key="layout.displayed"/>:</td>
                <td><f:formatDate value="${actionBean.now}" type="both"/></td>
            </tr>

            </table>
    </div>
    <h1><c:out value="${titlestring}"/></h1>
    <s:layout-component name="telo"/>

   <c:import context="/look" url="/metavo/dil4.jsp"/> 
</s:layout-definition>
