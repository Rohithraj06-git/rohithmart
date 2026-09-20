<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Page Not Found | RohithMart" scope="request"/>
<jsp:include page="../common/header.jsp" />
<jsp:include page="../common/navbar.jsp" />

<main class="main-content">
    <div class="error-container">
        <div class="error-code">404</div>
        <h1>Page Not Found</h1>
        <p>The page, listing, or resource you are looking for does not exist or has been moved.</p>
        <div class="error-actions">
            <a href="${pageContext.request.contextPath}/home" class="btn btn-primary">Return to Homepage</a>
            <a href="${pageContext.request.contextPath}/products" class="btn btn-outline">Explore Products</a>
        </div>
    </div>
</main>

<jsp:include page="../common/footer.jsp" />
