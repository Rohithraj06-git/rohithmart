<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Unauthorized | RohithMart" scope="request"/>
<jsp:include page="../common/header.jsp" />
<jsp:include page="../common/navbar.jsp" />

<main class="main-content">
    <div class="error-container">
        <div class="error-code">401</div>
        <h1>Authentication Required</h1>
        <p>You must be signed in to access this page or resource.</p>
        <div class="error-actions">
            <a href="${pageContext.request.contextPath}/auth/login" class="btn btn-primary">Sign In</a>
            <a href="${pageContext.request.contextPath}/home" class="btn btn-outline">Return to Homepage</a>
        </div>
    </div>
</main>

<jsp:include page="../common/footer.jsp" />
