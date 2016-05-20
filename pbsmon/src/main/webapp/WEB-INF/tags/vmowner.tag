<%@ tag %>
<%@ attribute name="owner" type="cz.cesnet.meta.cloud.Owner" rtexprvalue="true" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<% if(owner.isLocal()) { %>
<span title="<c:out value='${owner.x509_dn}'/>">&quot;<c:out value="${owner.full_name}"/>&quot;</span>
<% } else { %>
<s:link href="/user/${owner.name}"><c:out value="${owner.name}"/></s:link>
<% } %>
