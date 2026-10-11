<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Seller Orders | RohithMart" scope="request"/>
<c:set var="extraCss" value="dashboard" scope="request"/>
<jsp:include page="../common/header.jsp" />
<jsp:include page="../common/navbar.jsp" />
<jsp:include page="../common/toast.jsp" />

<main class="main-content">
    <div class="dashboard-container">
        <div class="dashboard-header">
            <div>
                <h1>Incoming Buyer Orders</h1>
                <p>Manage order fulfillment lifecycle and dispatch statuses for your store</p>
            </div>
            <div>
                <a href="${pageContext.request.contextPath}/seller/dashboard" class="btn btn-outline btn-sm">&larr; Back to Dashboard</a>
            </div>
        </div>

        <div class="table-card">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Order ID</th>
                        <th>Order Placed</th>
                        <th>Items Ordered</th>
                        <th>Order Total</th>
                        <th>Current Status</th>
                        <th style="text-align:right;">Update Fulfillment</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty orders}">
                            <c:forEach var="o" items="${orders}">
                                <tr>
                                    <td><strong>#<c:out value="${o.id}"/></strong></td>
                                    <td><fmt:formatDate value="${o.createdAt}" pattern="MMM dd, yyyy HH:mm"/></td>
                                    <td>
                                        <div style="display:flex;flex-direction:column;gap:0.25rem;">
                                            <c:forEach var="item" items="${o.items}">
                                                <span style="font-size:0.9rem;">
                                                    <c:out value="${item.productName}"/> &times; <strong>${item.quantity}</strong>
                                                    <span style="color:var(--text-secondary);font-size:0.8rem;">($<fmt:formatNumber value="${item.unitPrice}" minFractionDigits="2" maxFractionDigits="2"/>)</span>
                                                </span>
                                            </c:forEach>
                                        </div>
                                    </td>
                                    <td style="font-weight:700;color:var(--primary-color);">
                                        $<fmt:formatNumber value="${o.totalAmount}" minFractionDigits="2" maxFractionDigits="2"/>
                                    </td>
                                    <td>
                                        <span class="badge status-<c:out value="${o.status.name().toLowerCase()}"/>">
                                            <c:out value="${o.status}"/>
                                        </span>
                                    </td>
                                    <td style="text-align:right;">
                                        <form action="${pageContext.request.contextPath}/seller/orders/status" method="post" style="display:inline-flex;align-items:center;gap:0.5rem;">
                                            <input type="hidden" name="_csrf" value="${_csrf_token}">
                                            <input type="hidden" name="orderId" value="${o.id}">
                                            <select name="status" class="sm-select" onchange="this.form.submit()" aria-label="Order Status">
                                                <option value="PENDING" ${o.status == 'PENDING' ? 'selected' : ''}>PENDING</option>
                                                <option value="CONFIRMED" ${o.status == 'CONFIRMED' ? 'selected' : ''}>CONFIRMED</option>
                                                <option value="SHIPPED" ${o.status == 'SHIPPED' ? 'selected' : ''}>SHIPPED</option>
                                                <option value="DELIVERED" ${o.status == 'DELIVERED' ? 'selected' : ''}>DELIVERED</option>
                                                <option value="CANCELLED" ${o.status == 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
                                            </select>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="6" class="empty-state">No buyer orders placed for your listings yet.</td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</main>

<jsp:include page="../common/footer.jsp" />
