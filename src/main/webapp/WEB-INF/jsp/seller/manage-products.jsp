<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Manage Products | RohithMart" scope="request"/>
<c:set var="extraCss" value="dashboard" scope="request"/>
<jsp:include page="../common/header.jsp" />
<jsp:include page="../common/navbar.jsp" />
<jsp:include page="../common/toast.jsp" />

<main class="main-content">
    <div class="dashboard-container">
        <div class="dashboard-header">
            <div>
                <h1>Manage Products</h1>
                <p>Add, edit, or adjust inventory for your marketplace listings</p>
            </div>
            <div>
                <a href="${pageContext.request.contextPath}/seller/products/new" class="btn btn-primary">+ Add New Product</a>
            </div>
        </div>

        <div class="dashboard-nav-tabs">
            <a href="${pageContext.request.contextPath}/seller/dashboard" class="tab">Dashboard Overview</a>
            <a href="${pageContext.request.contextPath}/seller/products" class="tab active">All Listings</a>
            <a href="${pageContext.request.contextPath}/seller/orders" class="tab">Incoming Orders</a>
        </div>

        <div class="table-card">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>ID</th>
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
                                    <td>#<c:out value="${p.id}"/></td>
                                    <td>
                                        <div style="display:flex;align-items:center;gap:0.75rem;">
                                            <img src="<c:out value="${p.imageUrl != null && !p.imageUrl.isEmpty() ? p.imageUrl : 'https://via.placeholder.com/48'}"/>" alt="thumb" class="table-thumbnail">
                                            <a href="${pageContext.request.contextPath}/products/detail?id=${p.id}" target="_blank" style="font-weight:600;color:var(--text-primary);text-decoration:none;">
                                                <c:out value="${p.name}"/>
                                            </a>
                                        </div>
                                    </td>
                                    <td><span class="badge"><c:out value="${p.category}"/></span></td>
                                    <td>$<fmt:formatNumber value="${p.price}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${p.stockQty > 5}">
                                                <span class="badge in-stock"><c:out value="${p.stockQty}"/> in stock</span>
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
                                        <button type="button" class="btn btn-sm btn-outline btn-danger btn-delete-listing"
                                                data-id="${p.id}"
                                                data-name="<c:out value="${p.name}"/>">
                                            Delete
                                        </button>
                                        <form id="delete-listing-form-${p.id}" action="${pageContext.request.contextPath}/seller/products/delete" method="post" style="display:none;">
                                            <input type="hidden" name="_csrf" value="${_csrf_token}">
                                            <input type="hidden" name="id" value="${p.id}">
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="7" class="empty-state">No listings published yet. Click "+ Add New Product" to get started!</td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</main>

<script>
document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.btn-delete-listing').forEach(function(btn) {
        btn.addEventListener('click', function() {
            var id = this.getAttribute('data-id');
            var name = this.getAttribute('data-name');
            RohithMart.Modal.confirm(
                'Delete Listing',
                'Are you sure you want to permanently delete "' + name + '"? This will remove it from search and catalog views.',
                function() {
                    document.getElementById('delete-listing-form-' + id).submit();
                },
                'Delete Listing',
                true
            );
        });
    });
});
</script>

<jsp:include page="../common/footer.jsp" />
