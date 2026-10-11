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
                <p>Global governance over multi-seller listings, buyer accounts, and escrow transactions</p>
            </div>
            <div>
                <a href="${pageContext.request.contextPath}/admin/listings" class="btn btn-outline btn-sm">Moderation Queue</a>
            </div>
        </div>

        <!-- 4 Key Platform Metric Cards -->
        <div class="metrics-grid">
            <div class="metric-card">
                <h3>Total Platform Users</h3>
                <div class="metric-value"><c:out value="${userCount != null ? userCount : 0}"/></div>
                <div style="font-size:0.8rem;color:var(--text-secondary);margin-top:0.4rem;">
                    ${sellerCount} Sellers &bull; ${buyerCount} Buyers
                </div>
            </div>
            <div class="metric-card">
                <h3>Active Listings</h3>
                <div class="metric-value"><c:out value="${listingCount != null ? listingCount : 0}"/></div>
                <div style="font-size:0.8rem;color:var(--text-secondary);margin-top:0.4rem;">
                    Across all verified sellers
                </div>
            </div>
            <div class="metric-card">
                <h3>Total Orders</h3>
                <div class="metric-value"><c:out value="${orderCount != null ? orderCount : 0}"/></div>
                <div style="font-size:0.8rem;color:var(--text-secondary);margin-top:0.4rem;">
                    ${pendingCount} Pending Processing
                </div>
            </div>
            <div class="metric-card">
                <h3>Gross Platform Volume</h3>
                <div class="metric-value">$<fmt:formatNumber value="${totalRevenue != null ? totalRevenue : 0}" minFractionDigits="2" maxFractionDigits="2"/></div>
                <div style="font-size:0.8rem;color:var(--text-secondary);margin-top:0.4rem;">
                    Excludes cancelled orders
                </div>
            </div>
        </div>

        <div class="dashboard-nav-tabs">
            <a href="${pageContext.request.contextPath}/admin/dashboard" class="tab active">Overview</a>
            <a href="${pageContext.request.contextPath}/admin/users" class="tab">User Accounts (${userCount})</a>
            <a href="${pageContext.request.contextPath}/admin/listings" class="tab">Product Catalog (${listingCount})</a>
            <a href="${pageContext.request.contextPath}/admin/orders" class="tab">All Orders (${orderCount})</a>
        </div>

        <!-- Simple Visual Order Distribution Chart -->
        <div class="chart-card">
            <h3>Order Status Distribution</h3>
            <div class="chart-bars">
                <div class="chart-bar-item">
                    <span class="chart-bar-label">Delivered</span>
                    <div class="chart-bar-track">
                        <div class="chart-bar-fill success" style="width: ${orderCount > 0 ? (deliveredCount * 100 / orderCount) : 0}%;"></div>
                    </div>
                    <span class="chart-bar-value">${deliveredCount}</span>
                </div>
                <div class="chart-bar-item">
                    <span class="chart-bar-label">Shipped</span>
                    <div class="chart-bar-track">
                        <div class="chart-bar-fill" style="width: ${orderCount > 0 ? (shippedCount * 100 / orderCount) : 0}%;"></div>
                    </div>
                    <span class="chart-bar-value">${shippedCount}</span>
                </div>
                <div class="chart-bar-item">
                    <span class="chart-bar-label">Confirmed / Processing</span>
                    <div class="chart-bar-track">
                        <div class="chart-bar-fill accent" style="width: ${orderCount > 0 ? (confirmedCount * 100 / orderCount) : 0}%;"></div>
                    </div>
                    <span class="chart-bar-value">${confirmedCount}</span>
                </div>
                <div class="chart-bar-item">
                    <span class="chart-bar-label">Pending</span>
                    <div class="chart-bar-track">
                        <div class="chart-bar-fill" style="background:#fbbf24;width: ${orderCount > 0 ? (pendingCount * 100 / orderCount) : 0}%;"></div>
                    </div>
                    <span class="chart-bar-value">${pendingCount}</span>
                </div>
                <div class="chart-bar-item">
                    <span class="chart-bar-label">Cancelled</span>
                    <div class="chart-bar-track">
                        <div class="chart-bar-fill" style="background:#f87171;width: ${orderCount > 0 ? (cancelledCount * 100 / orderCount) : 0}%;"></div>
                    </div>
                    <span class="chart-bar-value">${cancelledCount}</span>
                </div>
            </div>
        </div>

        <!-- Section: Recent Platform Orders -->
        <div class="section-container">
            <div class="section-header">
                <h2>Recent Platform Orders</h2>
                <a href="${pageContext.request.contextPath}/admin/orders" class="view-all-link">Manage All Orders &rarr;</a>
            </div>
            <div class="table-card">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Order ID</th>
                            <th>Date</th>
                            <th>Buyer ID</th>
                            <th>Total Amount</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty recentOrders}">
                                <c:forEach var="ro" items="${recentOrders}">
                                    <tr>
                                        <td><strong>#<c:out value="${ro.id}"/></strong></td>
                                        <td><fmt:formatDate value="${ro.createdAt}" pattern="MMM dd, yyyy HH:mm"/></td>
                                        <td>Buyer #<c:out value="${ro.buyerId}"/></td>
                                        <td style="font-weight:600;">$<fmt:formatNumber value="${ro.totalAmount}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                        <td>
                                            <span class="badge status-<c:out value="${ro.status.name().toLowerCase()}"/>">
                                                <c:out value="${ro.status}"/>
                                            </span>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="5" class="empty-state">No platform orders recorded yet.</td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Section: Recent Registered Users -->
        <div class="section-container" style="margin-top:2rem;">
            <div class="section-header">
                <h2>Recent Registered Users</h2>
                <a href="${pageContext.request.contextPath}/admin/users" class="view-all-link">Manage All Users &rarr;</a>
            </div>
            <div class="table-card">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>User ID</th>
                            <th>Name</th>
                            <th>Email Address</th>
                            <th>Role</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty recentUsers}">
                                <c:forEach var="u" items="${recentUsers}">
                                    <tr>
                                        <td>#<c:out value="${u.id}"/></td>
                                        <td><strong><c:out value="${u.name}"/></strong></td>
                                        <td><c:out value="${u.email}"/></td>
                                        <td>
                                            <span class="badge ${u.role == 'ADMIN' ? 'badge-navy' : (u.role == 'SELLER' ? 'badge-gold' : '')}">
                                                <c:out value="${u.role}"/>
                                            </span>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="4" class="empty-state">No users registered yet.</td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</main>

<jsp:include page="../common/footer.jsp" />
