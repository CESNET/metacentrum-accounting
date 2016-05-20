<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/include/taglibs.jsp"%>
<fmt:message var="titlestring" scope="request" key="hosts.title" />
<s:layout-render name="/layout.jsp" menu="internal/acct/log">
  <s:layout-component name="telo" >
    <div id="page">
 
    
      <s:form name="filter" action="/Host.action">
        <input type="hidden" name="pageSize" value="${actionBean.hosts.objectsPerPage}">
        <div class="filtr">
          <h3><fmt:message key="filter.title" /></h3>
          <div class="form">          
            <table>
              <tr>
                <td class="polozka"><fmt:message key="hosts.hostname"/>:</td>
                <td><s:text name="filter.hostname"></s:text></td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
              </tr>
              <%-- <tr>
                <td>Number of kernel logs from:</td>
                <td><s:text name="filter.kernelLogsFrom"></s:text></td>
                <td>to: </td>
                <td><s:text name="filter.kernelLogsTo"></s:text></td>
              </tr>
              <tr>
                <td>Last log date from:</td>
                <td><s:text name="filter.lastLogDateFrom"></s:text></td>
                <td>to: </td>
                <td><s:text name="filter.lastLogDateTo"></s:text></td>
              </tr>
              --%><tr>
                <td colspan="4" class="akce"><s:submit name="view" ><fmt:message key="filter.filter" /></s:submit><s:submit name="clear" ><fmt:message key="filter.clear" /></s:submit></td>
              </tr>
            </table>
          </div>
        </div>
      </s:form>
    
 
 
      <div id="main" align="center">
      	<display:table cellpadding="0" cellspacing="0" class="data" requestURI="" name="${actionBean.hosts}" sort="external" id="element">
      	  <display:column property="hostName" sortable="true" sortName="hostName" titleKey="hosts.hostname" headerClass="data" class="data" />
      	  <display:column property="kernelLogsCount" sortable="true" sortName="kernelLogsCount" titleKey="hosts.sentLogs" headerClass="data" class="data"/>
      	  <display:column property="lastLogDate" sortable="true" sortName="lastLogDate" titleKey="hosts.lastLogDate" headerClass="data" class="data" decorator="cz.cesnet.meta.accounting.displaytag.decorator.LongDateWrapper">
      	  	<fmt:message key="hosts.never" />
      	  </display:column>
      	  <display:footer>
      	  	<form id="formPageSize" name="filter" action="" method="get" onsubmit="this.action=window.location.href">
      			<fmt:message key="list.pageSize" /> 
      			<select name="pageSize" onchange="document.getElementById('formPageSize').submit()">
      				<option id="20" value="20" <c:if test="${ actionBean.hosts.objectsPerPage == 20 }">selected</c:if> >20</option>
      				<option id="50" value="50" <c:if test="${ actionBean.hosts.objectsPerPage == 50 }">selected</c:if> >50</option>
      				<option id="100" value="100" <c:if test="${ actionBean.hosts.objectsPerPage == 100 }">selected</c:if> >100</option>
      				<option id="200" value="200" <c:if test="${ actionBean.hosts.objectsPerPage == 200 }">selected</c:if> >200</option>
      				<option id="500" value="500" <c:if test="${ actionBean.hosts.objectsPerPage == 500 }">selected</c:if> >500</option>
      			</select>
      		</form>	  
      	  </display:footer>
      	</display:table>
      </div>
    </div>
  </s:layout-component>
</s:layout-render>

