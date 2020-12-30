<%@ page contentType="text/html;charset=UTF-8"%>
<%
    // MAPPED TO ROOT("/") BY DEFAULT - REDIRECTING
    // (There is no page for root URL)

    if (session.getAttribute("sessionUser") == null) {
        response.sendRedirect("/signin");
    } else {
        response.sendRedirect("/lobby");
    }
%>