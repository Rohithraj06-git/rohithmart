<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Bad Request | RohithMart" scope="request"/>
<jsp:include page="../common/header.jsp" />
<jsp:include page="../common/navbar.jsp" />

<main class="main-content">
    <div class="error-container">
        <div class="error-code">400</div>
        <h1>Invalid Request</h1>
        <p>The request could not be understood by the server due to malformed syntax or invalid parameters.</p>
        <div class="error-actions">
            <a href="${pageContext.request.contextPath}/home" class="btn btn-primary">Return to Homepage</a>
        </div>
    </div>
</main>

<jsp:include page="../common/footer.jsp" />
