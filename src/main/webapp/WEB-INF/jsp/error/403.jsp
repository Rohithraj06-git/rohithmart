<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Access Forbidden | RohithMart" scope="request"/>
<jsp:include page="../common/header.jsp" />
<jsp:include page="../common/navbar.jsp" />

<main class="main-content">
    <div class="error-container">
        <div class="error-code">403</div>
        <h1>Access Forbidden</h1>
        <p>You do not have administrative or seller authorization to access this protected area.</p>
        <div class="error-actions">
            <a href="${pageContext.request.contextPath}/home" class="btn btn-primary">Return to Homepage</a>
        </div>
    </div>
</main>

<jsp:include page="../common/footer.jsp" />
