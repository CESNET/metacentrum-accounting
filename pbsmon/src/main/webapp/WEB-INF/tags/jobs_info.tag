<%@ tag import="cz.cesnet.meta.pbs.JobState" %>
<%@ tag body-content="empty" %>
<%@ attribute name="jobsInfo" type="cz.cesnet.meta.pbs.JobsInfo" rtexprvalue="true" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<table class="job" style="clear: both;">
    <tr>
        <th><f:message key="jobs_state"/></th>
        <% for(JobState s:JobState.values()){String k="jobs_"+s;%> <td class="<%=s%>"><%=s%> - <f:message key="<%=k%>"/></td> <%}%>
        <td><f:message key="jobs_celkem"/></td>
    </tr>
    <tr>
        <th><f:message key="jobs_pocet"/></th>
        <% for(JobState s:JobState.values()){
            Integer num = jobsInfo.getStavy().get(s.toString());if(num==null)num=0;%> <td style="text-align: center;"><%=num%></td> <%}%>
        <td><strong>${jobsInfo.celkemJobs}</strong></td>
    </tr>
    <tr>
        <th><f:message key="jobs_pocet_cpu"/></th>
        <% for(JobState s:JobState.values()){
            Integer numCpu = jobsInfo.getPoctyCpu().get(s.toString());if(numCpu==null)numCpu=0;
        %> <td style="text-align: center;"><%=numCpu%></td> <%}%>
        <td><strong>${jobsInfo.celkemCpu}</strong></td>
    </tr>
</table>