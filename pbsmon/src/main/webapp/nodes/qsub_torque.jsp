<%@ page import="java.util.Date" %>
<%@ page pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<s:useActionBean beanclass="cz.cesnet.meta.stripes.QsubTorqueActionBean" var="actionBean"/>
<f:message var="titlestring" key="qsub_torque_headline" scope="request"/>
<s:layout-render name="/layout.jsp">
    <s:layout-component name="telo">

<p><f:message key="qsub_torque_text1" /></p>
<p><f:message key="qsub_torque_text2" ><f:param value="${actionBean.user}" /></f:message></p>

<div style="border: 1px solid blue; background-color: azure; margin: 10px; padding: 1em;">
    <s:form beanclass="cz.cesnet.meta.stripes.QsubTorqueActionBean" method="post">
        <s:errors/> 
        <strong>qsub
            -l walltime=<s:select name="ww"><c:forEach begin="0" end="4" var="i"><s:option value="${i}">${i}</s:option></c:forEach></s:select>w
                        <s:select name="wd"><c:forEach begin="0" end="30" var="i"><s:option value="${i}">${i}</s:option></c:forEach></s:select>d
                        <s:select name="wh"><c:forEach begin="0" end="24" var="i"><s:option value="${i}">${i}</s:option></c:forEach></s:select>h
                        <s:select name="wm"><c:forEach begin="0" end="59" var="i"><s:option value="${i}">${i}</s:option></c:forEach></s:select>m
                        <s:select name="ws"><c:forEach begin="0" end="59" var="i"><s:option value="${i}">${i}</s:option></c:forEach></s:select>s
            \<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-q <s:select name="fronta"><s:option value="default@wagap.cerit-sc.cz">default@wagap.cerit-sc.cz</s:option>
                <c:forEach items="${actionBean.offerQueues}" var="q"><s:option value="${q.name}">${q.name}</s:option></c:forEach></s:select>
            \<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-l mem=<s:text style="text-align: right;" name="mem" size="5"/><s:select name="memu"><s:option value="mb">mb</s:option><s:option value="gb">gb</s:option></s:select>
            \<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-l scratch=<s:text style="text-align: right;" name="scratch" size="5"/><s:select name="scratchu"><s:option value="mb">mb</s:option><s:option value="gb">gb</s:option></s:select>
            :<s:select name="scratchtype"><s:option value="-"> </s:option><s:option value="ssd">ssd</s:option><s:option value="shared">shared</s:option><s:option value="local">local</s:option><s:option value="first">first</s:option></s:select>
            \<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-l nodes=<s:select name="nodes"><c:forEach begin="1" end="99" var="i"><s:option value="${i}" >${i}</s:option></c:forEach></s:select>
            :ppn=<s:select name="ppn"><c:forEach begin="1" end="384" var="i"><s:option value="${i}">${i}</s:option></c:forEach></s:select>
            <s:select name="prop1"><s:option value=""/><c:forEach items="${actionBean.props}" var="p"><s:option value="${p}">:${p}</s:option></c:forEach></s:select>
            <s:select name="prop2"><s:option value=""/><c:forEach items="${actionBean.props}" var="p"><s:option value="${p}">:${p}</s:option></c:forEach></s:select>
            <s:select name="prop3"><s:option value=""/><c:forEach items="${actionBean.props}" var="p"><s:option value="${p}">:${p}</s:option></c:forEach></s:select>
            \<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-l gpu=<s:select name="gpu"><s:option value="0"/><c:forEach begin="1" end="4" var="i"><s:option value="${i}">${i}</s:option></c:forEach></s:select>
            \<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-l cluster=<s:select name="cluster"><s:option value=""/><c:forEach items="${actionBean.resourceValues.cluster}" var="i"><s:option value="${i}">${i}</s:option></c:forEach></s:select>
            \<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-l city=<s:select name="city"><s:option value=""/><c:forEach items="${actionBean.resourceValues.city}" var="i"><s:option value="${i}">${i}</s:option></c:forEach></s:select>
            \<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-l room=<s:select name="room"><s:option value=""/><c:forEach items="${actionBean.resourceValues.room}" var="i"><s:option value="${i}">${i}</s:option></c:forEach></s:select>
            \<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-l home=<s:select name="home"><s:option value=""/><c:forEach items="${actionBean.resourceValues.home}" var="i"><s:option value="${i}">${i}</s:option></c:forEach></s:select>
            \<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-l infiniband=<s:select name="infiniband"><s:option value=""/><c:forEach items="${actionBean.resourceValues.infiniband}" var="i"><s:option value="${i}">${i}</s:option></c:forEach></s:select>
            \<br> uloha.sh
        </strong>  <br>
        <%--
         <c:forEach items="${actionBean.resourceValues}" var="rv">
             <c:out value="${rv.key}" /> = <c:out value="${rv.value}"/>
         </c:forEach>
        --%>
        <br>
        <f:message key="person_vyznam"/> <a href="https://wiki.metacentrum.cz/wiki/Pl%C3%A1novac%C3%AD_syst%C3%A9m_-_detailn%C3%AD_popis#Vlastnosti_stroj.C5.AF"><f:message key="person_vlastnosti_stroju"/></a>.
        <br><br>
        <input type="submit" name="sestavovac" value="<f:message key="person_nalezt"/>"/>
    </s:form>
</div>

    <c:if test="${actionBean.vyber}">
        <h3><f:message key="person_vysledek"/></h3>
        <f:message key="person_dotaz"/>: qsub -l walltime=<c:if test="${actionBean.ww>0}">${actionBean.ww}w</c:if><c:if test="${actionBean.wd>0}">${actionBean.wd}d</c:if><c:if test="${actionBean.wh>0}">${actionBean.wh}h</c:if><c:if test="${actionBean.wm>0}">${actionBean.wm}m</c:if><c:if test="${actionBean.ws>0}">${actionBean.ws}s</c:if>
        <c:choose>
            <c:when test="${not actionBean.queue.routing}"> -q ${actionBean.fronta} </c:when>
            <c:when test="${actionBean.fronta=='default@wagap.cerit-sc.cz'}"> -q @wagap.cerit-sc.cz </c:when>
            <c:when test="${actionBean.fronta=='default'}"> </c:when>
        </c:choose>
        -l mem=${actionBean.mem}${actionBean.memu}
        -l scratch=${actionBean.scratch}${actionBean.scratchu}<c:if test="${actionBean.scratchtype!='-'}">:${actionBean.scratchtype}</c:if>
        -l nodes=${actionBean.nodes}:ppn=${actionBean.ppn}<c:if test="${! empty actionBean.prop1}">:${actionBean.prop1}</c:if><c:if test="${! empty actionBean.prop2}">:${actionBean.prop2}</c:if><c:if test="${! empty actionBean.prop3}">:${actionBean.prop3}</c:if>
        <c:if test="${actionBean.gpu>0}">-l gpu=${actionBean.gpu}</c:if>
        <c:if test="${! empty actionBean.cluster}"   >-l cluster=${actionBean.cluster}</c:if>
        <c:if test="${! empty actionBean.city}"      >-l city=${actionBean.city}</c:if>
        <c:if test="${! empty actionBean.room}"      >-l room=${actionBean.room}</c:if>
        <c:if test="${! empty actionBean.home}"      >-l home=${actionBean.home}</c:if>
        <c:if test="${! empty actionBean.infiniband}">-l infiniband=${actionBean.infiniband}</c:if>

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

        <h3><f:message key="qsub_torque_accessible_queues"><f:param><c:out value="${actionBean.user}"/></f:param></f:message></h3>

        <table class="queue">
          <t:queue_heading/>
         <c:forEach items="${actionBean.queues}" var="q" >
             <t:queue_line queue="${q}"/>
         </c:forEach>
        </table>



            </s:layout-component>
</s:layout-render>
