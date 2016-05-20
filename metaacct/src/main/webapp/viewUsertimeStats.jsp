<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/include/taglibs.jsp"%>
<fmt:message var="titlestring" scope="request" key="stats.usertime.title" />
<c:set var="dateformat" scope="request" value="${pageContext.request.locale == 'cs' ? 'dd.MM.yyyy' : 'MM/dd/yyyy'}" />
<s:layout-render name="/layout.jsp"  menu="internal/acct" >
 <s:layout-component name="telo">

<h2>
  <fmt:message key="list.fromTo">
    <fmt:param><fmt:formatDate  value="${actionBean.fromDate}" pattern="${dateformat}"/></fmt:param>
    <fmt:param><fmt:formatDate  value="${actionBean.toDate}" pattern="${dateformat}"/></fmt:param>
  </fmt:message>
</h2>

<table>
  <tr>
    <td>
      <s:form name="day" action="/UsernameUserTime.action">
        <input type="hidden" name="periodInDays" value="1">
        <input type="hidden" name="pageSize" value="${actionBean.userStats.objectsPerPage}">
        <button type="submit"><fmt:message key="view24hours"/></button>
      </s:form>
    </td>
    <td>
      <s:form name="week" action="/UsernameUserTime.action">
        <input type="hidden" name="periodInDays" value="7">
        <input type="hidden" name="pageSize" value="${actionBean.userStats.objectsPerPage}">
        <button type="submit"><fmt:message key="viewWeek"/></button>
      </s:form>
    </td>
    <td>
      <s:form name="month" action="/UsernameUserTime.action">
        <input type="hidden" name="periodInDays" value="30">
        <input type="hidden" name="pageSize" value="${actionBean.userStats.objectsPerPage}">
        <button type="submit"><fmt:message key="viewMonth"/></button>
      </s:form>
    </td>
    <td>
      <s:form name="3months" action="/UsernameUserTime.action">
          <input type="hidden" name="periodInDays" value="90">
          <input type="hidden" name="pageSize" value="${actionBean.userStats.objectsPerPage}">
          <button type="submit"><fmt:message key="view3Months"/></button>
      </s:form>
    </td>
    <td>
      <s:form name="6months" action="/UsernameUserTime.action">
          <input type="hidden" name="periodInDays" value="180">
          <input type="hidden" name="pageSize" value="${actionBean.userStats.objectsPerPage}">
          <button type="submit"><fmt:message key="view6Months"/></button>
      </s:form>
    </td>
    <td>
      <s:form name="year" action="/UsernameUserTime.action">
          <input type="hidden" name="periodInDays" value="365">
          <input type="hidden" name="pageSize" value="${actionBean.userStats.objectsPerPage}">
          <button type="submit"><fmt:message key="viewYear"/></button>
      </s:form>
    </td>
  </tr>
  <tr>
    <td colspan="6">
      <stripes:form action="/UsernameUserTime.action">
        <stripes:errors/>
        <fmt:message key="history.fromDate" />&nbsp;
        <stripes:text id="fromDate" name="fromDate" formatType="date" formatPattern="${dateformat}"/>
        <stripes:button id="selectFrom" name="select" />
          <script type="text/javascript">
          Calendar.setup({
              inputField     :    "fromDate",
              ifFormat       :    "${pageContext.request.locale == 'cs' ? '%d.%m.%Y' : '%m/%d/%Y'}",
              button         :    "selectFrom"
          });
        </script>
        
        
        <fmt:message key="history.toDate" />&nbsp;
        <stripes:text id="toDate" name="toDate" formatType="date" formatPattern="${dateformat}"/>
        <stripes:button id="selectTo" name="select" />
          <script type="text/javascript">
          Calendar.setup({
              inputField     :    "toDate",
              ifFormat       :    "${pageContext.request.locale == 'cs' ? '%d.%m.%Y' : '%m/%d/%Y'}",
              button         :    "selectTo"
          });
        </script>
        <input type="hidden" name="pageSize" value="${actionBean.userStats.objectsPerPage}">
        <stripes:submit name="submit"/>
      </stripes:form>
    </td>
  </tr>
</table>

<div id="main" align="center">
	<display:table cellpadding="0" cellspacing="0" class="data" requestURI="" name="${actionBean.userStats}" sort="external" id="element">	  
	  <display:column property="username" sortable="true" sortName="username" titleKey="users.username" headerClass="data" class="data" />	  
	  <%--<display:column property="elapsedTimeSum" sortable="true" sortName="elapsedTimeSum" titleKey="binariesStats.elapsedTimeSum" headerClass="data" class="data" decorator="cz.cesnet.meta.accounting.displaytag.decorator.SecondHundredsTimeWrapper"/>--%>
	  <display:column property="userTimeSum" sortable="true" sortName="userTimeSum" titleKey="binariesStats.userTimeSum" headerClass="data" class="data" decorator="cz.cesnet.meta.accounting.displaytag.decorator.SecondHundredsTimeWrapper"/>
	  <%--<display:column property="systemTimeSum" sortable="true" sortName="systemTimeSum" titleKey="binariesStats.systemTimeSum" headerClass="data" class="data" decorator="cz.cesnet.meta.accounting.displaytag.decorator.SecondHundredsTimeWrapper"/>--%>
	  
	  <display:footer>
	  	<form id="formPageSize" action="" method="get" onsubmit="this.action=window.location.href">
			<fmt:message key="list.pageSize" /> 
			<select name="pageSize" onchange="document.getElementById('formPageSize').submit()">
				<option id="20" value="20" <c:if test="${ actionBean.userStats.objectsPerPage == 20 }">selected</c:if> >20</option>
				<option id="50" value="50" <c:if test="${ actionBean.userStats.objectsPerPage == 50 }">selected</c:if> >50</option>
				<option id="100" value="100" <c:if test="${ actionBean.userStats.objectsPerPage == 100 }">selected</c:if> >100</option>
				<option id="200" value="200" <c:if test="${ actionBean.userStats.objectsPerPage == 200 }">selected</c:if> >200</option>
				<option id="500" value="500" <c:if test="${ actionBean.userStats.objectsPerPage == 500 }">selected</c:if> >500</option>				
			</select>			
			<input type="hidden" name="fromDate" value='<fmt:formatDate  value="${actionBean.fromDate}" pattern="${dateformat}"/>'>
			<input type="hidden" name="toDate" value='<fmt:formatDate  value="${actionBean.toDate}" pattern="${dateformat}"/>'>
		</form>	  
	  </display:footer>
	</display:table>
</div>
 </s:layout-component>
</s:layout-render>
