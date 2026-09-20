<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Admin Panel | RohithMart" scope="request"/>
<c:set var="extraCss" value="dashboard" scope="request"/>
<jsp:include page="../common/header.jsp" />
<jsp:include page="../common/navbar.jsp" />
<jsp:include page="../common/toast.jsp" />

<main class="main-content">
    <div class="dashboard-container">
        <div class="dashboard-header">
            <div>
                <h1>Platform Administration</h1>
                <p>Global oversight of marketplace users, product listings, and platform transactions</p>
            </div>
        </div>

        <div class="metrics-grid">
            <div class="metric-card">
                <h3>Registered Users</h3>
                <div class="metric-value"><c:out value="${userCount != null ? userCount : 0}"/></div>
            </div>
            <div class="metric-card">
                <h3>Active Listings</h3>
                <div class="metric-value"><c:out value="${listingCount != null ? listingCount : 0}"/></div>
            </div>
            <div class="metric-card">
                <h3>Total Orders</h3>
                <div class="metric-value"><c:out value="${orderCount != null ? orderCount : 0}"/></div>
            </div>
        </div>

        <div class="dashboard-nav-tabs">
            <a href="${pageContext.request.contextPath}/admin/users" class="tab">User Management</a>
            <a href="${pageContext.request.contextPath}/admin/listings" class="tab">Product Moderation</a>
            <a href="${pageContext.request.contextPath}/admin/orders" class="tab">All Platform Orders</a>
        </div>
    </div>
</main>

<jsp:include page="../common/footer.jsp" />
