<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/include/taglibs.jsp"%>
<fmt:message var="titlestring" scope="request" key="receiveLog.title" />
<s:layout-render name="/layout.jsp" menu="internal/acct/log">
  <s:layout-component name="telo">
    <div id="page">
      
      <s:form name="filter" action="/ReceiveLog.action">
        <input type="hidden" name="pageSize" value="${actionBean.logs.objectsPerPage}">
        <div class="filtr">
          <h3><fmt:message key="filter.title" /></h3>
          <div class="form">          
            <table>
              <tr>
                <td class="polozka"><fmt:message key="receiveLog.host"/>:</td>
                <td><s:text name="filter.hostname"></s:text></td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
              </tr>
              <tr>
                <td class="polozka"><fmt:message key="receiveLog.receiveTime" />&nbsp;<fmt:message key="filter.from" />:</td>
                <td>
                  <s:text id="filter.receiveTimeFrom" name="filter.receiveTimeFrom" formatType="date" formatPattern="${dateformat}"></s:text>
                  <stripes:button id="selectReceiveTimeFrom" name="select" />
                  <script type="text/javascript">
                    Calendar.setup({
                        inputField     :    "filter.receiveTimeFrom",
                        ifFormat       :    "${pageContext.request.locale == 'cs' ? '%d.%m.%Y' : '%m/%d/%Y'}",
                        button         :    "selectReceiveTimeFrom"
                    });
                  </script>
                </td>
                <td class="polozka"><fmt:message key="filter.to" />:</td>
                <td>
                  <s:text id="filter.receiveTimeTo" name="filter.receiveTimeTo" formatType="date" formatPattern="${dateformat}"></s:text>
                  <stripes:button id="selectReceiveTimeTo" name="select" />
                  <script type="text/javascript">
                    Calendar.setup({
                        inputField     :    "filter.receiveTimeTo",
                        ifFormat       :    "${pageContext.request.locale == 'cs' ? '%d.%m.%Y' : '%m/%d/%Y'}",
                        button         :    "selectReceiveTimeTo"
                    });
                  </script>
                </td>
              </tr>
              <tr>
                <td colspan="4" class="akce"><s:submit name="view" ><fmt:message key="filter.filter" /></s:submit><s:submit name="clear" ><fmt:message key="filter.clear" /></s:submit></td>
              </tr>
            </table>
          </div>
        </div>
      </s:form>
      
      <div id="main" align="center">      
        <display:table class="data" requestURI="" name="${actionBean.logs}" sort="external" id="element">
          <display:column property="receiveTime" sortable="true" sortName="receiveTime" titleKey="receiveLog.receiveTime" headerClass="data" class="data" decorator="cz.cesnet.meta.accounting.displaytag.decorator.LongDateWrapper"/>
          <display:column property="hostname" sortable="true" sortName="hostname" titleKey="receiveLog.host" headerClass="data" class="data" />
          <display:column property="minimalTime" sortable="true" sortName="minimalTime" titleKey="receiveLog.minimalTime" headerClass="data" class="data" decorator="cz.cesnet.meta.accounting.displaytag.decorator.LongDateWrapper">
            <p style="text-align: right"><fmt:message key="receiveLog.noKernelRecords" /></p>
          </display:column>	  
          <display:column property="maximalTime" sortable="true" sortName="maximalTime" titleKey="receiveLog.maximalTime" headerClass="data" class="data" decorator="cz.cesnet.meta.accounting.displaytag.decorator.LongDateWrapper">
            &nbsp;
          </display:column>
        
          <display:footer>
            <form id="formPageSize" action="" method="get" onsubmit="this.action=window.location.href">
              <fmt:message key="list.pageSize" /> 
              <select name="pageSize" onchange="document.getElementById('formPageSize').submit()">
                <option id="20" value="20" <c:if test="${ actionBean.logs.objectsPerPage == 20 }">selected</c:if> >20</option>
                <option id="50" value="50" <c:if test="${ actionBean.logs.objectsPerPage == 50 }">selected</c:if> >50</option>
                <option id="100" value="100" <c:if test="${ actionBean.logs.objectsPerPage == 100 }">selected</c:if> >100</option>
                <option id="200" value="200" <c:if test="${ actionBean.logs.objectsPerPage == 200 }">selected</c:if> >200</option>
                <option id="500" value="500" <c:if test="${ actionBean.logs.objectsPerPage == 500 }">selected</c:if> >500</option>
              </select>
            </form>	  
          </display:footer>
        </display:table>
      	
      </div>
    </div>
  </s:layout-component>
</s:layout-render>
