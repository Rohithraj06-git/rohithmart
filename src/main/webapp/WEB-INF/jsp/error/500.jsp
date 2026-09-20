<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Server Error | RohithMart" scope="request"/>
<jsp:include page="../common/header.jsp" />
<jsp:include page="../common/navbar.jsp" />

<main class="main-content">
    <div class="error-container">
        <div class="error-code">500</div>
        <h1>Internal Server Error</h1>
        <p>An unexpected condition occurred on the server while processing your request. Our technical team has been alerted.</p>
        <div class="error-actions">
            <a href="${pageContext.request.contextPath}/home" class="btn btn-primary">Return to Homepage</a>
        </div>
    </div>
</main>

<jsp:include page="../common/footer.jsp" />
