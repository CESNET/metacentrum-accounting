<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<s:layout-definition>
    <c:set var="titlestring" scope="request">${titlestring}</c:set>
    <c:url var="url" value="/include/style.css" scope="page" />
    <link type="text/css" href="${url}" rel="stylesheet" />
    
    <c:url var="urlJsPrototype" value="/include/prototype.js" scope="page" />
    <script type="text/javascript" src="${urlJsPrototype}"></script>
    <c:url var="urlJsMeta" value="/include/metaacct.js" scope="page" />
    <script type="text/javascript" src="${urlJsMeta}"></script>
    <c:url var="urlJsCalendarCss" value="/include/calendar.css" scope="page" />
    <link rel="stylesheet" type="text/css" href="${urlJsCalendarCss}" />
    <c:url var="urlJsCalendar" value="/include/jscalendar/calendar.js" scope="page" />
    <script type="text/javascript" src="${urlJsCalendar}"></script>
    <c:url var="urlJsCalendarSetup" value="/include/jscalendar/calendar-setup.js" scope="page" />
    <script type="text/javascript" src="${urlJsCalendarSetup}"></script>
    <c:url var="urlJsCalendarLang" value="/include/jscalendar/lang/calendar-${pageContext.request.locale == 'cs' ? 'cs' : 'en'}.js" scope="page" />
    <script type="text/javascript" src="${urlJsCalendarLang}"></script>   
    
    <script type="text/javascript">
      var SHOW_FILTR = '<f:message key="filter.show" />';
      var HIDE_FILTR = '<f:message key="filter.hide" />';
    </script>
    
    <s:layout-component name="hlavicka"/>

    <h1>${titlestring}</h1>
    <s:layout-component name="telo"/>

</s:layout-definition>
