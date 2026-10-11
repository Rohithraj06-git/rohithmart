<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Listing Moderation | Admin | RohithMart" scope="request"/>
<c:set var="extraCss" value="dashboard" scope="request"/>
<jsp:include page="../common/header.jsp" />
<jsp:include page="../common/navbar.jsp" />
<jsp:include page="../common/toast.jsp" />

<main class="main-content">
    <div class="dashboard-container">
        <div class="dashboard-header">
            <div>
                <h1>Product Moderation</h1>
                <p>Review and moderate seller product listings</p>
            </div>
            <div>
                <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-outline">&larr; Back to Admin</a>
            </div>
        </div>

        <div class="table-card">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Seller ID</th>
                        <th>Product Title</th>
                        <th>Category</th>
                        <th>Price</th>
                        <th>Stock</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="p" items="${listings}">
                        <tr>
                            <td>#<c:out value="${p.id}"/></td>
                            <td>Seller #<c:out value="${p.sellerId}"/></td>
                            <td><c:out value="${p.name}"/></td>
                            <td><c:out value="${p.category}"/></td>
                            <td>$<fmt:formatNumber value="${p.price}" minFractionDigits="2" maxFractionDigits="2"/></td>
                            <td><c:out value="${p.stockQty}"/></td>
                            <td>
                                <button type="button" class="btn btn-sm btn-danger btn-moderate-listing"
                                        data-id="${p.id}"
                                        data-name="<c:out value="${p.name}"/>">
                                    Remove Listing
                                </button>
                                <form id="moderate-form-${p.id}" action="${pageContext.request.contextPath}/admin/listings/delete" method="post" style="display:none;">
                                    <input type="hidden" name="_csrf" value="${_csrf_token}">
                                    <input type="hidden" name="id" value="${p.id}">
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</main>

<script>
document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.btn-moderate-listing').forEach(function(btn) {
        btn.addEventListener('click', function() {
            var id = this.getAttribute('data-id');
            var name = this.getAttribute('data-name');
            RohithMart.Modal.confirm(
                'Moderate Product Listing',
                'Are you sure you want to remove the listing for "' + name + '"? This will permanently delete the item.',
                function() {
                    document.getElementById('moderate-form-' + id).submit();
                },
                'Remove Listing',
                true
            );
        });
    });
});
</script>

<jsp:include page="../common/footer.jsp" />
