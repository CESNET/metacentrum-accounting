<%@ tag body-content="empty" %>
<%@ attribute name="job" type="cz.cesnet.meta.pbs.Job" rtexprvalue="true" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
${job.state} - <f:message key='<%="jobs_"+job.getState()%>'/>
