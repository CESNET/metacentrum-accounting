<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/include/taglibs.jsp"%>

<fmt:message var="titlestring" scope="request" key="stats.chart.title" />
<s:layout-render name="/layout.jsp" menu="internal/acct" >
  <s:layout-component name="telo">     
  <s:errors action="/ChartStats.action"/>
    <s:form action="/ChartStats.action"  >
    
      <s:select name="userId">
        <c:forEach var="mapItem" items="${actionBean.users}">
          <s:option value="${mapItem.value}">${mapItem.key}</s:option>
        </c:forEach>        
      </s:select>
      <stripes:select name="intervalId" value="${actionBean.intervalId}" >
        <c:forEach var="mapItem" items="${actionBean.intervals}">
          <s:option value="${mapItem.key}">
            <c:if test="${empty mapItem.value.text}">
              <fmt:message key="${mapItem.value.key}"/>  
            </c:if>
            <c:if test="${not empty mapItem.value.text}">
              <c:out value="${mapItem.value.text}"/>  
            </c:if>
          </s:option>
        </c:forEach>
      </stripes:select>
      <s:submit name="chart">Vypsat</s:submit>
    </s:form>
    
    <c:if test="${not empty actionBean.statsUserjobsUrl}">
      <div>
        <img src="${actionBean.statsUserjobsUrl}" alt="stats">
      </div>
    </c:if> 
    
    <c:if test="${not empty actionBean.statsUsertimeUrl}">    
      <div>
        <img src="${actionBean.statsUsertimeUrl}" alt="stats">
      </div>
    </c:if>
       
  </s:layout-component>
</s:layout-render>
