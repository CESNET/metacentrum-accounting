<%@ page pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<s:useActionBean beanclass="cz.cesnet.meta.stripes.QsubPbsproActionBean" var="actionBean"/>
<f:message var="titlestring" key="qsub_pbspro_headline" scope="request"/>
<s:layout-render name="/layout.jsp">
    <s:layout-component name="telo">

<p><f:message key="qsub_pbspro_text1" /></p>
<p><f:message key="qsub_pbspro_text2" ><f:param value="${actionBean.user}" /></f:message></p>

<div style="border: 1px solid blue; background-color: azure; margin: 10px; padding: 1em;">
    <s:form beanclass="cz.cesnet.meta.stripes.QsubPbsproActionBean" method="post">
        <s:errors/> 
        <strong>qsub
            -l walltime=<s:select name="wh"><c:forTokens items="0,1,2,4,24,48,96,168,336,720" delims="," var="i"><s:option value="${i}">${i}</s:option></c:forTokens></s:select>:<s:select
                    name="wm"><c:forEach begin="0" end="59" var="i"><s:option value="${i}">${i}</s:option></c:forEach></s:select>:<s:select
                    name="ws"><c:forEach begin="0" end="59" var="i"><s:option value="${i}">${i}</s:option></c:forEach></s:select>

            -q <s:select name="fronta">
                <c:forEach items="${actionBean.offerQueues}" var="q">
                    <s:option value="default@arien-pro.ics.muni.cz"> </s:option>
                    <s:option value="${q.name}">${q.name}</s:option></c:forEach></s:select>

            \<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-l select=<s:select name="nodes"><c:forEach begin="1" end="99" var="i"><s:option value="${i}" >${i}</s:option></c:forEach></s:select>
            :ncpus=<s:select name="ncpus"><c:forEach begin="1" end="384" var="i"><s:option value="${i}">${i}</s:option></c:forEach></s:select>
            :ngpus=<s:select name="ngpus"><c:forEach begin="0" end="4" var="i"><s:option value="${i}">${i}</s:option></c:forEach></s:select>
            :mem=<s:text style="text-align: right;" name="mem" size="3"/><s:select name="memu"><s:option value="mb">mb</s:option><s:option value="gb">gb</s:option></s:select>
            :scratch_<s:select name="scratchtype"><s:option value="local">local</s:option><s:option value="ssd">ssd</s:option><s:option value="shared">shared</s:option></s:select>=<s:text style="text-align: right;" name="scratch" size="3"/><s:select name="scratchu"><s:option value="mb">mb</s:option><s:option value="gb">gb</s:option></s:select>
            <c:forEach items="${actionBean.resourceValues}" var="rv">
            <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; :<c:out value="${rv.key}" />=<s:select name="resources.${rv.key}"><s:option value=""> </s:option><c:forEach items="${rv.value}" var="v"><s:option value="${v}">${v}</s:option></c:forEach></s:select>
            </c:forEach>

            \<br> uloha.sh
        </strong>  <br>
        <br><br>
        <input type="submit" name="sestavovac" value="<f:message key="person_nalezt"/>"/>
    </s:form>
</div>

    <c:if test="${actionBean.vyber}">
        <h3><f:message key="person_vysledek"/></h3>
        <f:message key="person_dotaz"/>: qsub -l walltime=${actionBean.wh}:${actionBean.wm}:${actionBean.ws}
        <c:choose>
            <c:when test="${actionBean.fronta=='default@arien-pro.ics.muni.cz'}"> </c:when>
            <c:otherwise> -q ${actionBean.fronta} </c:otherwise>
        </c:choose>
        -l select=${actionBean.nodes}:ncpus=${actionBean.ncpus}:mem=${actionBean.mem}${actionBean.memu}:scratch_${actionBean.scratchtype}=${actionBean.scratch}${actionBean.scratchu}

        <c:choose>
            <c:when test="${fn:length(actionBean.potencialni)<actionBean.nodes}">
                <div class="warning1">
                    <div class="label1"><f:message key="warning1_title"/></div>
                    <div class="content1">
                        <f:message key="person_malo_potencialnich"><f:param value="${actionBean.nodes}" /><f:param value="${fn:length(actionBean.potencialni)}" /></f:message>
                    </div>
                </div>
                <c:if test="${fn:length(actionBean.potencialni)>0}">
                    <h4><f:message key="person_potencialni"/></h4>
                    <t:node_table_free nodes="${actionBean.potencialni}"/>
                </c:if>
            </c:when>
            <c:otherwise>
                <c:choose>
                    <c:when test="${fn:length(actionBean.tedVolne)<actionBean.nodes}">
                      <div class="warning2">
                        <div class="label2"><f:message key="warning2_label"/></div>
                        <div class="content2"><f:message key="person_malo_volnych"><f:param value="${actionBean.nodes}" /><f:param value="${fn:length(actionBean.tedVolne)}" /></f:message></div>
                      </div>
                      <c:if test="${fn:length(actionBean.potencialni)>0}">
                        <h4><f:message key="person_potencialni"/></h4>
                        <t:node_table_free nodes="${actionBean.potencialni}"/>
                      </c:if>
                    </c:when>
                    <c:otherwise>
                        <div class="warning3">
                            <div class="label3"><f:message key="warning3_label"/></div>
                            <div class="content3"><f:message key="person_dost_volnych"><f:param value="${actionBean.nodes}" /><f:param value="${fn:length(actionBean.tedVolne)}" /><f:param value="${fn:length(actionBean.potencialni)}"/></f:message></div>
                        </div>
                        <h4><f:message key="person_tedvolne"/></h4>
                        <t:node_table_free nodes="${actionBean.tedVolne}"/>
                        <c:if test="${fn:length(actionBean.potencialni)>0}">
                            <h4><f:message key="person_potencialni"/></h4>
                            <t:node_table_free nodes="${actionBean.potencialni}"/>
                        </c:if>
                    </c:otherwise>
                </c:choose>
            </c:otherwise>
        </c:choose>

        <hr/>
        <br>
        <br>
    </c:if>

        <h3><f:message key="qsub_pbspro_accessible_queues"><f:param><c:out value="${actionBean.user}"/></f:param></f:message></h3>

        <table class="queue">
          <t:queue_heading/>
         <c:forEach items="${actionBean.queues}" var="q" >
             <t:queue_line queue="${q}"/>
         </c:forEach>
        </table>



            </s:layout-component>
</s:layout-render>
