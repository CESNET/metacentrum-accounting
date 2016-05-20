<%@ page pageEncoding="utf-8" import="cz.cesnet.meta.stripes.JobsActionBean" %>
<%@ page import="cz.cesnet.meta.pbs.Job" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<s:useActionBean beanclass="cz.cesnet.meta.stripes.JobsActionBean" var="actionBean"/>
<f:message var="titlestring" key="jobs.titul" scope="request"/>
<s:layout-render name="/layout.jsp">
    <s:layout-component name="hlava">
        <script language="javascript" type="text/javascript" src="${pageContext.request.contextPath}/js/prototype.js"></script>
        <!--[if IE]>
            <script language="javascript" type="text/javascript" src="${pageContext.request.contextPath}/js/excanvas.js"></script>
        <![endif]-->
        <script language="javascript" type="text/javascript" src="${pageContext.request.contextPath}/js/ProtoChart.js"></script>

        <script language="javascript" type="text/javascript">
        var jobs_queued = [ <c:forEach items="${actionBean.queuedJobsCountersMins}" var="e" varStatus="i"><c:if test="${! i.first}">,</c:if>[${i.index},${e}]</c:forEach>];
        var jobs_running = [ <c:forEach items="${actionBean.runningJobsCountersMins}" var="e" varStatus="i"><c:if test="${! i.first}">,</c:if>[${i.index},${e}]</c:forEach>];
        var cpus_queued = [ <c:forEach items="${actionBean.queuedCPUsCountersMins}" var="e" varStatus="i"><c:if test="${! i.first}">,</c:if>[${i.index},${e}]</c:forEach>];
        var cpus_running = [ <c:forEach items="${actionBean.runningCPUsCountersMins}" var="e" varStatus="i"><c:if test="${! i.first}">,</c:if>[${i.index},${e}]</c:forEach>];

        var jobs_created_mins = [ <c:forEach items="${actionBean.createdCountersMins}" var="e" varStatus="i"><c:if test="${! i.first}">,</c:if>[${i.index},${e}]</c:forEach>];
        var jobs_started_mins = [ <c:forEach items="${actionBean.startedCountersMins}" var="e" varStatus="i"><c:if test="${! i.first}">,</c:if>[${i.index},${e}]</c:forEach>];
        var jobs_completed_mins = [ <c:forEach items="${actionBean.completedCountersMins}" var="e" varStatus="i"><c:if test="${! i.first}">,</c:if>[${i.index},${e}]</c:forEach>];

        Event.observe(window, 'load', function() {
            new Proto.Chart($('jobschartmins'),
                    [
                        {data: cpus_queued, label: "CPUs of queued jobs" },
                        {data: cpus_running, label: "CPUs of running jobs" },
                        {data: jobs_queued, label: "Queued jobs" },
                        {data: jobs_running, label: "Running jobs" }
                    ],
                    {
                        legend: { show: true},
                        //colors : ["#AD8200", "#0000C9", "#00A800" ],
                        points: {show: true},
                        lines: { show: true},
                        //bars: {show: true, fillOpacity: 1},
                        xaxis: {
                            min: 0,
                            max: 24*4,
                            tickSize: 4,
                            tickFormatter: function(str) { return "-" + (str/4) + "h"; }
                        },
                        yaxis: {
                            min: 0,
                            tickFormatter: function(str) { return " " + str + ""; }
                        }

                    });
        });

        Event.observe(window, 'load', function() {
            new Proto.Chart($('jobschartminssl'),
                    [
                        {data: jobs_created_mins, label: "Created jobs in 15 mins" },
                        {data: jobs_started_mins, label: "Started jobs in 15 mins" },
                        {data: jobs_completed_mins, label: "Completed jobs in 15 mins" }
                    ],
                    {
                        legend: { show: true},
                        //colors : ["#AD8200", "#0000C9", "#00A800" ],
                        //points: {show: true},
                        //lines: { show: true},
                        bars: {show: true, fillOpacity: 1},
                        xaxis: {
                            min: 0,
                            max: 24*4,
                            tickSize: 4,
                            tickFormatter: function(str) { return "-" + (str/4) + "h"; }
                        },
                        yaxis: {
                            min: 0,
                            tickFormatter: function(str) { return " " + str + ""; }
                        }

                    });
        });
        var jobsCPU1 = [ <c:forEach items="${actionBean.jobsCPU}" var="e" varStatus="i"><c:if test="${! i.first}">,</c:if><c:if test="${e.key<=32}">[${e.key},${e.value}]</c:if> </c:forEach>];
        Event.observe(window, 'load', function() {
            new Proto.Chart($('cpuchart1'),
                    [
                        {data: jobsCPU1, label: "jobs by CPU/job" }
                    ],
                    {
                        legend: { show: true},
                        colors: ["#00A8F0"],
                        bars: {show: true},
                        xaxis: {
                            min: 1,
                            tickSize: 1,
                            tickFormatter: function(str) {
                                return "" + str + "";
                            }
                        },
                        yaxis: {
                            min: 0,
                            tickFormatter: function(str) {
                                return "" + str + "";
                            }
                        }

                    });
        });
        var jobsCPU2 = [ <c:forEach items="${actionBean.jobsCPU}" var="e" varStatus="i"><c:if test="${! i.first}">,</c:if><c:if test="${e.key>32}">[${e.key},${e.value}]</c:if></c:forEach>];
        Event.observe(window, 'load', function() {
            new Proto.Chart($('cpuchart2'),
                    [
                        {data: jobsCPU2, label: "jobs by CPU/job" }
                    ],
                    {
                        legend: { show: true},
                        colors: ["#00A8F0"],
                        bars: {show: true},
                        xaxis: {
                            min: 33,
                            tickSize: 1,
                            tickFormatter: function(str) {
                                if(str!=33 && ((str%16)!=0)) return "";
                                return "" + str + "";
                            }
                        },
                        yaxis: {
                            min: 0,
                            tickSize: 1
                        }

                    });
        });

        var waitingJobs = [ <c:forEach items="${actionBean.waitingHoursJobs}" var="e" varStatus="i"><c:if test="${! i.first}">,</c:if>[${e.key},${e.value}]</c:forEach>];
        Event.observe(window, 'load', function() {
            new Proto.Chart($('waitchart'),
                    [
                        {data: waitingJobs, label: "jobs waiting time" }
                    ],
                    {
                        legend: { show: true},
                        //colors: ["#00A8F0"],
                        bars: {show: true},
                        xaxis: {
                            min: 0,
                            tickFormatter: function(str) {
                            return "" + str + "";
                        } },
                        yaxis: {
                            min: 0,
                            tickFormatter: function(str) {
                                return "" + str + "";
                            }
                        }

                    });
        });
    </script>

    </s:layout-component>
    <s:layout-component name="telo">
        <s:useActionBean beanclass="cz.cesnet.meta.stripes.JobsActionBean" var="actionBean"/>

        <t:jobs_info jobsInfo="${actionBean.jobsInfo}"/>

        <br>
        <br>

        <ul>
            <li><s:link href="/jobs/my"><f:message key="jobs.show.zobrazit.moje.ulohy"/></s:link></li>
            <li><s:link href="/jobs/allJobs"><f:message key="jobs.show.zobrazit.vsechny.ulohy"/></s:link> <f:message key="jobs.show.vsechny.komentar"/></li>
            <li><s:link href="/users"><f:message key="jobs.show.zobrazit.ulohy.pro.uzivatele"/></s:link></li>
            <c:if test="${!actionBean.showWarnings}">
               <li><s:link href="/jobs/detail?showWarnings=true"><f:message key="jobs.show.zobrazit.varovani"/></s:link></li>
            </c:if>
        </ul>
        <c:if test="${actionBean.showWarnings}">
            <div style="background-color: #fff0f0; color: black; border: 1px solid red; margin: 10px; padding: 10px;">
                <c:forEach items="${actionBean.warnings}" var="w">
                    <c:set scope="page" var="sj"  value="${actionBean.suspiciousJobs[w.key]}"/>
                    <br><s:link href="/job/${w.key}">${w.key}</s:link> (<s:link href="/user/${sj.user}">${sj.user}</s:link>/<s:link href="/queue/${sj.queueName}">${sj.queueName}</s:link>): <c:out value="${w.value}"/>
                </c:forEach>
                <br>
            </div>
        </c:if>

        <h2><t:i18n cs="Počty úloh a CPU" en="Job and CPU counts"/></h2>
        <!-- legenda zacatek -->
<div style="padding: 10px 0px 10px 25px; background-color: white; width: 1025px;">
<div style="margin: 1px 0px 5px -20px;"><t:i18n cs="počet úloh nebo CPU" en="job or CPU count"/></div>
<!-- legenda konec -->
        <div id="jobschartmins" style="width:1000px;height:300px;background-color: #FFFFFF;"></div>
        <!-- legenda zacatek -->
<div style="margin: 5px 0px 0px 950px;"><t:i18n cs="čas" en="time"/></div>
</div>
<!-- legenda konec -->
        <c:choose>
            <c:when test="${pageContext.request.locale=='cs'}">
                <p>
                    Čárový graf zobrazuje počty běžících a čekajících úloh, a jimi požadovaných CPU, v okamžicích po 15 minutách za poslední den.
                    Jde o <b>celkové počty</b> úloh a CPU.
                </p>
            </c:when>
            <c:otherwise>
                <p>
                    The line chart shows the numbers of running and wainting jobs and their CPUs, in 15 minute intervals during the last day.
                    The numbers are <b>total counts</b> of jobs and CPUs.
                </p>
            </c:otherwise>
        </c:choose>

        <h2><t:i18n cs="Změny počtu úloh" en="Changes in job count"/></h2>
        <!-- legenda zacatek -->
<div style="padding: 10px 0px 10px 25px; background-color: white; width: 1025px;">
<div style="margin: 1px 0px 5px -20px;"><t:i18n cs="počet úloh" en="job count"/></div>
<!-- legenda konec -->
        <div id="jobschartminssl" style="width:1000px;height:300px;background-color: #FFFFFF;"></div>
        <!-- legenda zacatek -->
<div style="margin: 5px 0px 0px 950px;"><t:i18n cs="čas" en="time"/></div>
</div>
<!-- legenda konec -->
        <c:choose>
            <c:when test="${pageContext.request.locale=='cs'}">
                <p>Sloupcový graf zobrazuje počty vytvořených/spuštěných/dokončených úloh během každých 15 minut za poslední den.
                    Jde tedy o <b>změny</b> počtu běžících a čekajících úloh.
                </p>
            </c:when>
            <c:otherwise>
                <p>
                   The column chart shows the numbers of created/started/completed jobs during 15 minute intervals during the last day.
                   The numbers are <b>changes</b> in the counts of running and waiting jobs.
                </p>
            </c:otherwise>
        </c:choose>




    <h2><t:i18n cs="Víceprocesorovost úloh" en="Multi-CPU jobs"/></h2>
        <table>
            <tr>
                <td>
                    <div style="padding: 10px 0px 10px 25px; background-color: white; width: 500px; margin: 2px;">
                        <div style="margin: 1px 0px 5px -20px;"><t:i18n cs="počet úloh" en="job count"/></div>
                        <div id="cpuchart1" style="width:475px;height:300px;background-color: #FFFFFF;"></div>
                        <div style="margin: 5px 0px 0px 375px;"><t:i18n cs="počet CPU úlohy" en="CPUs"/></div>
                    </div>
                </td>
                <td>
                    <div style="padding: 10px 0px 10px 25px; background-color: white; width: 500px; margin: 2px;">
                        <div style="margin: 1px 0px 5px -20px;"><t:i18n cs="počet úloh" en="job count"/></div>
                        <div id="cpuchart2" style="width:475px;height:300px;background-color: #FFFFFF;"></div>
                        <div style="margin: 5px 0px 0px 375px;"><t:i18n cs="počet CPU úlohy" en="CPUs"/></div>
                    </div>
                </td>
            </tr>
        </table>
        <c:choose>
            <c:when test="${pageContext.request.locale=='cs'}">
                <p>Sloupcový graf zobrazuje počty všech úloh (držených v systému, tedy za posledních 24 hodin)
                    využívajících daný počet CPU.</p>
            </c:when>
            <c:otherwise>
                <p>
                    The column chart shows the numbers of jobs requiring the given number of CPUs.
                </p>
            </c:otherwise>
        </c:choose>

    <h2><t:i18n cs="Doba čekání úloh" en="Job waiting times"/></h2>

        <div style="padding: 10px 0px 10px 25px; background-color: white; width: 1025px;">
            <div style="margin: 1px 0px 5px -20px;"><t:i18n cs="počet úloh" en="job count"/></div>
            <div id="waitchart" style="width:1000px;height:300px;background-color: #FFFFFF;"></div>
            <div style="margin: 5px 0px 0px 930px;"><t:i18n cs="hodiny čekání" en="hours of waiting"/></div>
        </div>
        <c:choose>
            <c:when test="${pageContext.request.locale=='cs'}">
                <p>Sloupcový graf zobrazuje počty úloh, které čekají nebo čekaly daný počet hodin.</p>
                <p>(grafy fungují v moderních prohlížečích s podporou HTML5, tedy Firefox, Chrome, Safari, MSIE9+; nefungují v MSIE8 a starších)</p>
            </c:when>
            <c:otherwise>
                <p>The column chart shows the numbers of jobs that are waiting or waited the given number of hours.</p>
                <p>(charts work in moder browsers with HTML5 support, i. e. Firefox, Chrome, Safari, MSIE9+; they do not work in MSIE8 and older)</p>
            </c:otherwise>
        </c:choose>


    </s:layout-component>
</s:layout-render>
