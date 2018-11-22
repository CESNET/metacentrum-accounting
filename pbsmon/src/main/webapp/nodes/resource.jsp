<%@ page pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="h" uri="http://stripes.sourceforge.net/stripes.tld" %>


<s:useActionBean beanclass="cz.cesnet.meta.stripes.ResourceActionBean" var="actionBean"/>
<% if(actionBean.getResource().isCluster()) { %>
   <f:message var="titlestring" key="resource.titul.cluster" scope="request"><f:param value="<%=actionBean.getResourceName()%>"/><f:param value="${actionBean.cpuSum}"/></f:message>
    <% } else { %>
   <f:message var="titlestring" key="resource.titul.machine" scope="request"><f:param value="<%=actionBean.getResourceName()%>"/><f:param value="${actionBean.cpuSum}"/></f:message>
<%} %>
<s:layout-render name="/layout.jsp">
    <s:layout-component name="telo">

        <div id="photo">
            <c:if test="${not empty actionBean.resource.thumbnail}">
            <a href="<c:out value='${actionBean.resource.photo}'/>"><img src="<c:out value='${actionBean.resource.thumbnail}'/>" alt="photo" width="100px"/></a>
            </c:if>
        </div>

        <p><f:message key="${actionBean.resource.popisKey}"/></p>

        <f:message key="${actionBean.resource.specKey}" var="chunk"/>
        <c:if test="${! empty chunk}">
           <p><f:message key="${actionBean.resource.specKey}"/></p>
        </c:if>

        <% if(actionBean.getResource().isCluster()) { %>
           <p><f:message key="resource.cluster.nadpis" ><f:param value="${actionBean.resourceName}"/><f:param value="${fn:length(actionBean.resource.stroje)}"/></f:message></p>
        <% } else { %>

        <%} %>

        <t:resource resource="${actionBean.resource}"/>

        <% if(actionBean.getResource().isCluster()) { %>
        <c:if test="${! empty actionBean.resource.stroje}">
            <table class="nodes" cellspacing="0">
                <tr>
                    <c:forEach items="${actionBean.resource.stroje}" var="stroj" varStatus="i">
                    <t:stroj stroj="${stroj}"/>
                    <c:if test="${i.count%7==0}"></tr><tr></c:if>
                </c:forEach>
            </tr>
            </table>
        </c:if>
        <% } else { %>
          <table class="nodes" cellspacing="0">
              <tr><t:stroj stroj="${actionBean.resource.stroj}"/></tr>
          </table>
        <%} %>



    </s:layout-component>
</s:layout-render>
