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
                <p>Overview of your store inventory, order processing, and sales volume</p>
            </div>
            <div>
                <a href="${pageContext.request.contextPath}/seller/products/new" class="btn btn-primary">+ Add New Product</a>
            </div>
        </div>

        <!-- 4 Statistics Cards -->
        <div class="metrics-grid">
            <div class="metric-card">
                <h3>Total Products</h3>
                <div class="metric-value"><c:out value="${totalProducts != null ? totalProducts : 0}"/></div>
            </div>
            <div class="metric-card">
                <h3>Total Orders</h3>
                <div class="metric-value"><c:out value="${totalOrders != null ? totalOrders : 0}"/></div>
            </div>
            <div class="metric-card">
                <h3>Gross Revenue</h3>
                <div class="metric-value">$<fmt:formatNumber value="${totalRevenue != null ? totalRevenue : 0}" minFractionDigits="2" maxFractionDigits="2"/></div>
            </div>
            <div class="metric-card">
                <h3>Low-Stock Items</h3>
                <div class="metric-value" style="color: ${lowStockCount > 0 ? 'var(--warning-color)' : 'var(--primary-color)'};">
                    <c:out value="${lowStockCount != null ? lowStockCount : 0}"/>
                </div>
            </div>
        </div>

        <!-- Low Stock Warning if any -->
        <c:if test="${not empty lowStockProducts}">
            <div style="background:#fffbeb;border:1px solid #fef3c7;border-left:4px solid var(--warning-color);padding:1rem 1.25rem;border-radius:var(--radius-md);margin-bottom:2rem;">
                <h4 style="color:#92400e;margin-bottom:0.25rem;">Attention: ${lowStockProducts.size()} listing(s) have low stock remaining</h4>
                <p style="color:#b45309;font-size:0.9rem;">
                    Items: 
                    <c:forEach var="lsp" items="${lowStockProducts}" varStatus="status">
                        <strong><c:out value="${lsp.name}"/></strong> (${lsp.stockQty} left)${!status.last ? ', ' : ''}
                    </c:forEach>
                </p>
            </div>
        </c:if>

        <div class="dashboard-nav-tabs">
            <a href="${pageContext.request.contextPath}/seller/dashboard" class="tab active">Dashboard Overview</a>
            <a href="${pageContext.request.contextPath}/seller/products" class="tab">All Listings</a>
            <a href="${pageContext.request.contextPath}/seller/orders" class="tab">Incoming Orders (${pendingOrders != null ? pendingOrders : 0})</a>
        </div>

        <!-- Section: Product Management Table -->
        <div class="section-container">
            <div class="section-header">
                <h2>Product Inventory (${products.size()})</h2>
                <a href="${pageContext.request.contextPath}/seller/products" class="view-all-link">Manage Full Catalog &rarr;</a>
            </div>
            <div class="table-card">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Product</th>
                            <th>Category</th>
                            <th>Price</th>
                            <th>Stock</th>
                            <th>Rating</th>
                            <th style="text-align:right;">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty products}">
                                <c:forEach var="p" items="${products}">
                                    <tr>
                                        <td>
                                            <div style="display:flex;align-items:center;gap:0.75rem;">
                                                <img src="<c:out value="${p.imageUrl != null && !p.imageUrl.isEmpty() ? p.imageUrl : 'https://via.placeholder.com/48'}"/>" class="table-thumbnail" alt="<c:out value="${p.name}"/>">
                                                <div>
                                                    <a href="${pageContext.request.contextPath}/products/detail?id=${p.id}" target="_blank" style="font-weight:600;color:var(--text-primary);text-decoration:none;">
                                                        <c:out value="${p.name}"/>
                                                    </a>
                                                    <div style="font-size:0.8rem;color:var(--text-secondary);">ID: #${p.id}</div>
                                                </div>
                                            </div>
                                        </td>
                                        <td><span class="badge"><c:out value="${p.category}"/></span></td>
                                        <td>$<fmt:formatNumber value="${p.price}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${p.stockQty > 5}">
                                                    <span class="badge in-stock"><c:out value="${p.stockQty}"/> units</span>
                                                </c:when>
                                                <c:when test="${p.stockQty > 0}">
                                                    <span class="badge status-pending"><c:out value="${p.stockQty}"/> low</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge out-of-stock">0 units</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>★ <fmt:formatNumber value="${p.averageRating}" minFractionDigits="1" maxFractionDigits="1"/></td>
                                        <td style="text-align:right;">
                                            <a href="${pageContext.request.contextPath}/seller/products/edit?id=${p.id}" class="btn btn-sm btn-outline" style="margin-right:0.25rem;">
                                                Edit
                                            </a>
                                            <button type="button" class="btn btn-sm btn-outline btn-danger" onclick="confirmDeleteProduct(${p.id}, '<c:out value="${p.name}"/>')">
                                                Delete
                                            </button>
                                            <form id="delete-form-${p.id}" action="${pageContext.request.contextPath}/seller/products/delete" method="post" style="display:none;">
                                                <input type="hidden" name="_csrf" value="${_csrf_token}">
                                                <input type="hidden" name="id" value="${p.id}">
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="6" class="empty-state">No listings published yet. Click "+ Add New Product" to start selling.</td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Section: Recent Orders -->
        <div class="section-container" style="margin-top:2.5rem;">
            <div class="section-header">
                <h2>Recent Incoming Orders</h2>
                <a href="${pageContext.request.contextPath}/seller/orders" class="view-all-link">View All Orders &rarr;</a>
            </div>
            <div class="table-card">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Order ID</th>
                            <th>Date</th>
                            <th>Buyer ID</th>
                            <th>Total</th>
                            <th>Status</th>
                            <th>Action</th>
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
                                        <td>
                                            <a href="${pageContext.request.contextPath}/seller/orders" class="btn btn-sm btn-outline">
                                                Update Status
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="6" class="empty-state">No incoming buyer orders yet.</td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</main>

<script>
function confirmDeleteProduct(id, name) {
    RohithMart.Modal.confirm(
        'Delete Listing',
        'Are you sure you want to permanently delete "' + name + '"? This cannot be undone.',
        function() {
            document.getElementById('delete-form-' + id).submit();
        },
        'Delete Listing',
        true
    );
}
</script>

<jsp:include page="../common/footer.jsp" />
