<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<table cellspacing="0" >
    <tr>
        <td align="center" class="state free"><f:message key="nodesjsp_state_free"/></td>
        <td align="center" class="state partialy-free"><f:message key="nodesjsp_state_partialy-free"/></td>
    </tr>
    <tr><td colspan="5" class="spacer"> </td></tr>
    <tr>
        <td align="center" class="state job-busy"><f:message key="nodesjsp_state_job-busy"/></td>
        <td align="center" class="state reserved"><f:message key="nodesjsp_state_reserved"/></td>
        <td align="center" class="state test"><f:message key="nodesjsp_state_test"/></td>
        <td align="center" class="state job-sharing"><f:message key="nodesjsp_state_job-sharing"/></td>
        <td align="center" class="state job-exclusive"><f:message key="nodesjsp_state_job-exclusive"/></td>
    </tr>
    <tr><td colspan="5" class="spacer"> </td></tr>
    <tr>
        <td align="center" class="state maintenance"><f:message key="nodesjsp_state_maintenance"/></td>
        <td align="center" class="state maintenance-busy"><f:message key="nodesjsp_state_maintenance_busy"/></td>
        <td align="center" class="state offline"><f:message key="nodesjsp_state_offline"/></td>
        <td align="center" class="state down"><f:message key="nodesjsp_state_down"/></td>
        <td align="center" class="state state-unknown"><f:message key="nodesjsp_state_state-unknown"/></td>
    </tr>
</table>
