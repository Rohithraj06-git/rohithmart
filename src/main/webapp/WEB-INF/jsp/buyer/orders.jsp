<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="My Orders | RohithMart" scope="request"/>
<jsp:include page="../common/header.jsp" />
<jsp:include page="../common/navbar.jsp" />
<jsp:include page="../common/toast.jsp" />

<main class="main-content">
    <div class="orders-container">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:2rem;">
            <div>
                <h1 style="font-size:2rem;color:var(--primary-color);">My Order History</h1>
                <p style="color:var(--text-secondary);font-size:0.95rem;">Review past purchases, current statuses, and delivery receipts</p>
            </div>
            <a href="${pageContext.request.contextPath}/products" class="btn btn-outline btn-sm">
                Browse Marketplace
            </a>
        </div>

        <c:choose>
            <c:when test="${not empty orders}">
                <div class="orders-list">
                    <c:forEach var="o" items="${orders}">
                        <div class="order-card">
                            <div class="order-header">
                                <div>
                                    <span class="order-id">Order #<c:out value="${o.id}"/></span>
                                    <span class="order-date">
                                        Placed on <fmt:formatDate value="${o.createdAt}" pattern="MMM dd, yyyy 'at' hh:mm a"/>
                                    </span>
                                </div>
                                <div style="display:flex;align-items:center;gap:1rem;">
                                    <span class="badge status-<c:out value="${o.status.name().toLowerCase()}"/>">
                                        <c:out value="${o.status}"/>
                                    </span>
                                    <span class="order-total">
                                        $<fmt:formatNumber value="${o.totalAmount}" minFractionDigits="2" maxFractionDigits="2"/>
                                    </span>
                                </div>
                            </div>

                            <div class="order-items-preview">
                                <c:forEach var="item" items="${o.items}">
                                    <div class="order-item-row">
                                        <div>
                                            <a href="${pageContext.request.contextPath}/products/detail?id=${item.productId}" style="font-weight:600;color:var(--text-primary);text-decoration:none;">
                                                <c:out value="${item.productName}"/>
                                            </a>
                                            <span style="color:var(--text-secondary);font-size:0.85rem;margin-left:0.5rem;">
                                                &times; <c:out value="${item.quantity}"/>
                                            </span>
                                        </div>
                                        <span style="font-weight:600;color:var(--text-primary);">
                                            $<fmt:formatNumber value="${item.unitPrice}" minFractionDigits="2" maxFractionDigits="2"/>
                                        </span>
                                    </div>
                                </c:forEach>
                            </div>

                            <div class="order-actions">
                                <c:choose>
                                    <c:when test="${o.status == 'PENDING'}">
                                        <form id="cancel-form-${o.id}" action="${pageContext.request.contextPath}/orders/cancel" method="post" style="display:inline;">
                                            <input type="hidden" name="_csrf" value="${_csrf_token}">
                                            <input type="hidden" name="orderId" value="${o.id}">
                                            <button type="button" class="btn btn-sm btn-outline btn-danger" onclick="cancelOrder(${o.id})">
                                                Cancel Order
                                            </button>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        <span style="font-size:0.85rem;color:var(--text-light);">
                                            Order is finalized (<c:out value="${o.status}"/>)
                                        </span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="empty-state">
                    <div class="empty-state-icon">📦</div>
                    <h3>No order records found</h3>
                    <p>You haven't placed any purchases yet. Explore our verified multi-seller collection.</p>
                    <a href="${pageContext.request.contextPath}/products" class="btn btn-primary">
                        Start Shopping
                    </a>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</main>

<script>
function cancelOrder(orderId) {
    RohithMart.Modal.confirm(
        'Cancel Order #' + orderId,
        'Are you sure you wish to cancel this pending order? This action will release the reserved stock.',
        function() {
            document.getElementById('cancel-form-' + orderId).submit();
        },
        'Yes, Cancel Order',
        true
    );
}
</script>

<jsp:include page="../common/footer.jsp" />
