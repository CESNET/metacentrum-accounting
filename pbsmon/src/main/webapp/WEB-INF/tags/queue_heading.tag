<%@ tag %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
 <tr>
   <th rowspan="2"> <f:message key="queues_queue"/> </th>
   <th rowspan="2"> &nbsp; </th>
   <th rowspan="2"> <f:message key="queues_priority"/> </th>
   <th rowspan="2"> <f:message key="queues_timelimit"/> </th>
   <th rowspan="2"> <f:message key="queues_rprop"/> </th>
   <th colspan="5"> <f:message key="queues_jobs"/> </th>
   <th rowspan="2"> <f:message key="queues_max_user_cpus"/> </th>
   <th rowspan="2"> fairshare </th>
 <tr>
   <th> <f:message key="queues_queued"/> </th>
   <th> <f:message key="queues_running"/>/<f:message key="queues_max_running"/> </th>
   <th> <f:message key="queues_completed"/> </th>
   <th> <f:message key="queues_total"/> </th>
   <th> <f:message key="queues_max_user_run"/> </th>
 </tr>