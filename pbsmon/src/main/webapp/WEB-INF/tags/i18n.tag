<%@ tag %><%@ attribute name="cs" type="java.lang.String" rtexprvalue="true" required="true"
        %><%@ attribute name="en" type="java.lang.String" rtexprvalue="true" required="true"
        %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
        %><c:choose><c:when test="${pageContext.request.locale=='cs'}">${cs}</c:when><c:otherwise>${en}</c:otherwise></c:choose>