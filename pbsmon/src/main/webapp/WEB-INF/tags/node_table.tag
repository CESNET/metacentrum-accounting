<%@ tag import="cz.cesnet.meta.pbs.Node" %>
<%@ tag import="java.util.List" %>
<%@ tag %>
<%@ attribute name="nodes" type="java.util.List" rtexprvalue="true" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<table class="nodes" cellspacing="0" border="0">
    <tr>
        <%
            int numinrow = 0;
            for (Node node : (List<Node>) nodes) {
                if (numinrow == 7) {
                    numinrow = 0;
                    out.println("</tr><tr>");
                }
                %><td class="<%=node.getState()%>"><s:link class="<%=node.getState()%>" href='<%="/node/"+node.getName()%>'><%=node.getShortName()%> (<%=node.getNoOfUsedCPU()%>/<%=node.getNoOfCPU()%>)</s:link></td>
        <%
                numinrow+=1;
            }
        %>
    </tr>
</table>