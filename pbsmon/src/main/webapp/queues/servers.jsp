<%@ page pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<f:message var="titlestring" key="servers.titul" scope="request"/>
<s:layout-render name="/layout.jsp">
    <s:layout-component name="telo">
        <s:useActionBean beanclass="cz.cesnet.meta.stripes.QueuesActionBean" var="actionBean"/>
        <c:forEach items="${actionBean.pbs}" var="pbs">
            <h3>Server ${pbs.server.name} - <f:message key="server_${pbs.server.name}"/></h3>

            Server attributes:
            <table class="attributes">
                <c:forEach items="${pbs.server.attributes}" var="entry">
                    <tr>
                        <td align="left" ><c:out value="${entry.key}"/></td>
                        <td align="left"><c:out value="${entry.value}"/></td>
                    </tr>
                </c:forEach>
            </table>


            <c:forEach items="${pbs.schedulers}" var="sch">
                <h3>Scheduler ${sch.key} attributes:</h3>
            <table class="attributes">
                <c:forEach items="${sch.value.attributes}" var="entry">
                    <tr>
                        <td align="left" ><c:out value="${entry.key}"/></td>
                        <td align="left"><c:out value="${entry.value}"/></td>
                    </tr>
                </c:forEach>
            </table>
            </c:forEach>

            <c:forEach items="${pbs.reservations}" var="resv">
                <h3>Reservation ${resv.key} attributes:</h3>
                <table class="attributes">
                    <c:forEach items="${resv.value.attributes}" var="entry">
                        <tr>
                            <td align="left" ><c:out value="${entry.key}"/></td>
                            <td align="left"><c:out value="${entry.value}"/></td>
                        </tr>
                    </c:forEach>
                </table>
            </c:forEach>

            <c:forEach items="${pbs.hooks}" var="he">
                <h3>Hook ${he.key} attributes:</h3>
                <table class="attributes">
                    <c:forEach items="${he.value.attributes}" var="entry">
                        <tr>
                            <td align="left" ><c:out value="${entry.key}"/></td>
                            <td align="left"><c:out value="${entry.value}"/></td>
                        </tr>
                    </c:forEach>
                </table>
            </c:forEach>

            <h3>Resources</h3>
            <table class="attributes">
            <c:forEach items="${pbs.resources}" var="re">
                <tr align="left">
                <td>${re.key}</td>
                <c:forEach items="${re.value.attributes}" var="entry">
                  <td align="left" ><c:out value="${entry.key}"/></td>
                  <td align="left"><c:out value="${entry.value}"/></td>
                </c:forEach>
                </tr>
            </c:forEach>
            </table>



        </c:forEach>
    </s:layout-component>
</s:layout-render>
