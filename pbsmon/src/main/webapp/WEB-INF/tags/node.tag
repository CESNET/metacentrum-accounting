<%@ tag %>
<%@ attribute name="node" type="cz.cesnet.meta.pbs.Node" rtexprvalue="true" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<td class="<%=node.getState()%>"><s:link class="<%=node.getState()%>" href='<%="/node/"+node.getName()%>'><%=node.getShortName()%></s:link></td>