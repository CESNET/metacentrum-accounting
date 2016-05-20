<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/include/taglibs.jsp"%>
<stripes:useActionBean var="actionBean" beanclass="cz.cesnet.meta.accounting.web.PbsRecords"/>
<fmt:message var="titlestring" scope="request" key="pbsRecords.title" ><fmt:param value="${actionBean.username}"/></fmt:message>
<c:set var="dateformat" scope="request" value="${pageContext.request.locale == 'cs' ? 'dd.MM.yyyy' : 'MM/dd/yyyy'}" />
<s:layout-render name="/layout.jsp" >
  <s:layout-component name="telo">
  <div id="page">
    
    
    <c:if test="${actionBean.number > 0}">
      <h2><fmt:message key="list.lastNRecords"><fmt:param value="${actionBean.number}"/></fmt:message></h2>          
    </c:if>
    <c:if test="${actionBean.filter.dateTimeFrom != null || actionBean.filter.dateTimeTo != null}">
      <h2>
        <fmt:message key="list.fromTo">
		    <fmt:param><fmt:formatDate  value="${actionBean.filter.dateTimeFrom}" pattern="${dateformat}"/></fmt:param>
		    <fmt:param><fmt:formatDate  value="${actionBean.filter.dateTimeTo}" pattern="${dateformat}"/></fmt:param>
		  </fmt:message>
      </h2>          
    </c:if>
             
    
    <table> 
      <tr>
        <td colspan="3">
          <stripes:form action="/PbsRecords.action">
            <stripes:hidden name="userId">${actionBean.userId}</stripes:hidden>
            <fmt:message key="history.last" />
            <stripes:select name="number">
              <c:forEach var="i" begin="10" end="100" step="10">
                <stripes:option label="${i}" title="${i}" value="${i}"  />
              </c:forEach>    
            </stripes:select>
            <fmt:message key="history.records" />    
            <stripes:submit name="submit"/>
          </stripes:form>
        </td>
      </tr>
      <tr>
        <td>
          <s:form action="/PbsRecords.action">
            <s:hidden name="userId">${actionBean.userId}</s:hidden>
            <input type="hidden" name="periodInDays" value="1">
            <input type="hidden" name="pageSize" value="${actionBean.pbsRecords.objectsPerPage}">
            <button type="submit"><fmt:message key="view24hours"/></button>
          </s:form>      
        </td>
        <td>
          <s:form action="/PbsRecords.action">
            <s:hidden name="userId">${actionBean.userId}</s:hidden>
            <input type="hidden" name="periodInDays" value="7">
            <input type="hidden" name="pageSize" value="${actionBean.pbsRecords.objectsPerPage}">
            <button type="submit"><fmt:message key="viewWeek"/></button>
          </s:form>
        </td>
        <td>
          <s:form action="/PbsRecords.action">
            <s:hidden name="userId">${actionBean.userId}</s:hidden>
            <input type="hidden" name="periodInDays" value="30">
            <input type="hidden" name="pageSize" value="${actionBean.pbsRecords.objectsPerPage}">
            <button type="submit"><fmt:message key="viewMonth"/></button>
          </s:form>
        </td>
      </tr>
    </table>
      <s:form name="filter" action="/PbsRecords.action">
          <stripes:hidden name="userId">${actionBean.userId}</stripes:hidden>
          <input type="hidden" name="pageSize" value="${actionBean.pbsRecords.objectsPerPage}">
          <div class="filtr">
            <h3><fmt:message key="filter.title" /></h3>
            <div class="form">
              <table>
                <tr>
                  <td class="polozka"><fmt:message key="pbs.job" />:</td>
                  <td><s:text name="filter.idString"/><stripes:errors field="filter.idString"/></td>
                  <td class="polozka"><fmt:message key="pbs.name" />:</td>
                  <td><s:text name="filter.jobname"/><stripes:errors field="filter.jobname"/></td>
                </tr>
                <tr>
                  <td class="polozka"><fmt:message key="pbs.queue" />:</td>
                  <td><s:text name="filter.queue"/><stripes:errors field="filter.queue"/></td>
                  <td class="polozka"><fmt:message key="pbs.serverHostname" />:</td>
                  <td><s:text name="filter.pbsServer"/><stripes:errors field="filter.pbsServer"/></td>
                </tr>
				<tr>
                  <td class="polozka"><fmt:message key="pbs.cpus" />&nbsp;<fmt:message key="filter.from" />:</td>
                  <td>
                    <s:text id="filter.cpusFrom" name="filter.cpusFrom" />                    
                  </td>
                  <td class="polozka"><fmt:message key="filter.to" />:</td>
                  <td>
                    <s:text id="filter.cpusTo" name="filter.cpusTo" />                    
                  </td>
                </tr>
                <tr>
                  <td class="polozka"><fmt:message key="pbs.walltime" />&nbsp;<fmt:message key="filter.from" />:</td>
                  <td>
                          <s:text id="filter.walltimeFrom" name="filter.walltimeFrom" /> (H:MM:SS)
                          <stripes:errors field="filter.walltimeFrom"/>
                  </td>
                  <td class="polozka"><fmt:message key="filter.to" />:</td>
                  <td>
                          <s:text id="filter.walltimeTo" name="filter.walltimeTo" /> (H:MM:SS)
                          <stripes:errors field="filter.walltimeTo"/>
                  </td>
                </tr>

                <tr>
                  <td class="polozka"><fmt:message key="pbs.dateTime" />&nbsp;<fmt:message key="filter.from" />:</td>
                  <td>
                    <s:text id="filter.dateTimeFrom" name="filter.dateTimeFrom" formatType="date" formatPattern="${dateformat}"/>
                    <stripes:button id="selectDateTimeFrom" name="select" />
                    <script type="text/javascript">
                      Calendar.setup({
                          inputField     :    "filter.dateTimeFrom",
                          ifFormat       :    "${pageContext.request.locale == 'cs' ? '%d.%m.%Y' : '%m/%d/%Y'}",
                          button         :    "selectDateTimeFrom"
                      });
                    </script>
                  </td>
                  <td class="polozka"><fmt:message key="filter.to" />:</td>
                  <td>
                    <s:text id="filter.dateTimeTo" name="filter.dateTimeTo" formatType="date" formatPattern="${dateformat}"/>
                    <stripes:button id="selectDateTimeTo" name="select" />
                    <script type="text/javascript">
                      Calendar.setup({
                          inputField     :    "filter.dateTimeTo",
                          ifFormat       :    "${pageContext.request.locale == 'cs' ? '%d.%m.%Y' : '%m/%d/%Y'}",
                          button         :    "selectDateTimeTo"
                      });
                    </script>
                  </td>
                </tr>
                <tr>
                  <td class="polozka"><fmt:message key="pbs.createTime" />&nbsp;<fmt:message key="filter.from" />:</td>
                  <td>
                    <s:text id="filter.createTimeFrom" name="filter.createTimeFrom" formatType="date" formatPattern="${dateformat}"/>
                    <stripes:button id="selectCreateTimeFrom" name="select" />
                    <script type="text/javascript">
                      Calendar.setup({
                          inputField     :    "filter.createTimeFrom",
                          ifFormat       :    "${pageContext.request.locale == 'cs' ? '%d.%m.%Y' : '%m/%d/%Y'}",
                          button         :    "selectCreateTimeFrom"
                      });
                    </script>
                  </td>
                  <td class="polozka"><fmt:message key="filter.to" />:</td>
                  <td>
                    <s:text id="filter.createTimeTo" name="filter.createTimeTo" formatType="date" formatPattern="${dateformat}"></s:text>
                    <stripes:button id="selectCreateTimeTo" name="select" />
                    <script type="text/javascript">
                      Calendar.setup({
                          inputField     :    "filter.createTimeTo",
                          ifFormat       :    "${pageContext.request.locale == 'cs' ? '%d.%m.%Y' : '%m/%d/%Y'}",
                          button         :    "selectCreateTimeTo"
                      });
                    </script>
                  </td>
                </tr>
                <tr>
                  <td class="polozka"><fmt:message key="pbs.startTime" />&nbsp;<fmt:message key="filter.from" />:</td>
                  <td>
                    <s:text id="filter.startTimeFrom" name="filter.startTimeFrom" formatType="date" formatPattern="${dateformat}"></s:text>
                    <stripes:button id="selectStartTimeFrom" name="select" />
                    <script type="text/javascript">
                      Calendar.setup({
                          inputField     :    "filter.startTimeFrom",
                          ifFormat       :    "${pageContext.request.locale == 'cs' ? '%d.%m.%Y' : '%m/%d/%Y'}",
                          button         :    "selectStartTimeFrom"
                      });
                    </script>
                  </td>
                  <td class="polozka"><fmt:message key="filter.to" />:</td>
                  <td>
                    <s:text id="filter.startTimeTo" name="filter.startTimeTo" formatType="date" formatPattern="${dateformat}"></s:text>
                    <stripes:button id="selectstartTimeTo" name="select" />
                    <script type="text/javascript">
                      Calendar.setup({
                          inputField     :    "filter.startTimeTo",
                          ifFormat       :    "${pageContext.request.locale == 'cs' ? '%d.%m.%Y' : '%m/%d/%Y'}",
                          button         :    "selectstartTimeTo"
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
    	<c:url value="/Details.action?view" var="detaily"/>
    	<display:table cellpadding="0" cellspacing="0" class="data" requestURI="" name="${actionBean.pbsRecords}" sort="external" id="element">
    	  <display:column property="idString"  href="${detaily}" paramId="pbsIdString" paramProperty="idString" sortable="true" sortName="idString" titleKey="pbs.job" headerClass="data" class="data" />
    	  <display:column property="cpus" sortable="true" titleKey="pbs.cpus" sortName="cpus" headerClass="data" class="data"/>
    	  <display:column property="jobname" sortable="true" sortName="jobname" titleKey="pbs.name" headerClass="data" class="data" />
    	  <display:column property="queue" sortable="true" sortName="queue" titleKey="pbs.queue" headerClass="data" class="data" />
    	  <display:column property="createTime" sortable="true" sortName="createTime" titleKey="pbs.createTime" headerClass="data" class="data" decorator="cz.cesnet.meta.accounting.displaytag.decorator.LongDateWrapper"/>
    	  <display:column property="startTime" sortable="true" sortName="startTime" titleKey="pbs.startTime" headerClass="data" class="data" decorator="cz.cesnet.meta.accounting.displaytag.decorator.LongDateWrapper"/>
    	  <display:column property="endTime" sortable="true" sortName="endTime" titleKey="pbs.endTime" headerClass="data" class="data" decorator="cz.cesnet.meta.accounting.displaytag.decorator.LongDateWrapper"/>
    	  <display:column property="walltime" sortable="true" sortName="walltime" titleKey="pbs.walltime" headerClass="data" class="data" decorator="cz.cesnet.meta.accounting.displaytag.decorator.SecondHundredsTimeWrapper"/>

            <%--
            <display:column property="totalUserTime" titleKey="pbs.totalUserTime" headerClass="data" class="data" decorator="cz.cesnet.meta.accounting.displaytag.decorator.SecondHundredsTimeWrapper"/>
            <display:column property="totalSystemTime" titleKey="pbs.totalSystemTime" headerClass="data" class="data" decorator="cz.cesnet.meta.accounting.displaytag.decorator.SecondHundredsTimeWrapper"/>
            <display:column property="serverHostname" sortable="true" sortName="serverHostname" titleKey="pbs.serverHostname" headerClass="data" class="data" />
            --%>
    	  
    	  <display:footer>
    	  	<form id="formPageSize" action="" method="get" onsubmit="this.action=window.location.href">
    			<fmt:message key="list.pageSize" /> 
    			<select name="pageSize" onchange="document.getElementById('formPageSize').submit()">
    				<option id="20" value="20" <c:if test="${ actionBean.pbsRecords.objectsPerPage == 20 }">selected</c:if> >20</option>
    				<option id="50" value="50" <c:if test="${ actionBean.pbsRecords.objectsPerPage == 50 }">selected</c:if> >50</option>
    				<option id="100" value="100" <c:if test="${ actionBean.pbsRecords.objectsPerPage == 100 }">selected</c:if> >100</option>
    				<option id="200" value="200" <c:if test="${ actionBean.pbsRecords.objectsPerPage == 200 }">selected</c:if> >200</option>
    				<option id="500" value="500" <c:if test="${ actionBean.pbsRecords.objectsPerPage == 500 }">selected</c:if> >500</option>
    			</select>
    			<input type="hidden" name="userId" value="${actionBean.userId}">
    			<input type="hidden" name="number" value="${actionBean.number}">
    			<input type="hidden" name="filter.dateTimeFrom" value='<fmt:formatDate  value="${actionBean.filter.dateTimeFrom}" pattern="${dateformat}"/>'>
    			<input type="hidden" name="filter.dateTimeTo" value='<fmt:formatDate  value="${actionBean.filter.dateTimeTo}" pattern="${dateformat}"/>'>
    		</form>	  
    	  </display:footer>
    	</display:table>
    </div>
  </div>
 </s:layout-component>
</s:layout-render>
