<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Seller Dashboard | RohithMart" scope="request"/>
<c:set var="extraCss" value="dashboard" scope="request"/>
<jsp:include page="../common/header.jsp" />
<jsp:include page="../common/navbar.jsp" />
<jsp:include page="../common/toast.jsp" />

<main class="main-content">
    <div class="dashboard-container">
        <div class="dashboard-header">
            <div>
                <h1>Seller Dashboard</h1>
                <p>Manage your product inventory and track incoming buyer orders</p>
            </div>
            <div>
                <a href="${pageContext.request.contextPath}/seller/products/new" class="btn btn-primary">+ Add New Product</a>
            </div>
        </div>

        <div class="metrics-grid">
            <div class="metric-card">
                <h3>Total Listings</h3>
                <div class="metric-value"><c:out value="${totalListings != null ? totalListings : 0}"/></div>
            </div>
            <div class="metric-card">
                <h3>Pending Orders</h3>
                <div class="metric-value"><c:out value="${pendingOrders != null ? pendingOrders : 0}"/></div>
            </div>
            <div class="metric-card">
                <h3>Gross Revenue</h3>
                <div class="metric-value">$<fmt:formatNumber value="${totalRevenue != null ? totalRevenue : 0}" minFractionDigits="2" maxFractionDigits="2"/></div>
            </div>
        </div>

        <div class="dashboard-nav-tabs">
            <a href="${pageContext.request.contextPath}/seller/products" class="tab active">My Products</a>
            <a href="${pageContext.request.contextPath}/seller/orders" class="tab">Incoming Orders</a>
        </div>
    </div>
</main>

<jsp:include page="../common/footer.jsp" />
