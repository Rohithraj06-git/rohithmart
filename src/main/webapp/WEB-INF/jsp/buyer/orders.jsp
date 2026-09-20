<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="My Orders | RohithMart" scope="request"/>
<jsp:include page="../common/header.jsp" />
<jsp:include page="../common/navbar.jsp" />
<jsp:include page="../common/toast.jsp" />

<main class="main-content">
    <div class="orders-container">
        <h1>My Order History</h1>
        <c:choose>
            <c:when test="${not empty orders}">
                <div class="orders-list">
                    <c:forEach var="o" items="${orders}">
                        <div class="order-card">
                            <div class="order-header">
                                <div>
                                    <span class="order-id">Order #<c:out value="${o.id}"/></span>
                                    <span class="order-date"><fmt:formatDate value="${o.createdAt}" pattern="MMM dd, yyyy HH:mm"/></span>
                                </div>
                                <div>
                                    <span class="badge status-<c:out value="${o.status.toLowerCase()}"/>"><c:out value="${o.status}"/></span>
                                    <span class="order-total">$<fmt:formatNumber value="${o.totalAmount}" minFractionDigits="2" maxFractionDigits="2"/></span>
                                </div>
                            </div>
                            <div class="order-items-preview">
                                <c:forEach var="item" items="${o.items}">
                                    <div class="order-item-row">
                                        <span><c:out value="${item.productName}"/> &times; ${item.quantity}</span>
                                        <span>$<fmt:formatNumber value="${item.unitPrice}" minFractionDigits="2" maxFractionDigits="2"/></span>
                                    </div>
                                </c:forEach>
                            </div>
                            <div class="order-actions">
                                <c:if test="${o.status == 'PENDING'}">
                                    <form action="${pageContext.request.contextPath}/orders/cancel" method="post" style="display:inline;">
                                        <input type="hidden" name="orderId" value="${o.id}">
                                        <button type="submit" class="btn btn-sm btn-outline">Cancel Order</button>
                                    </form>
                                </c:if>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="empty-state">
                    <h3>No past orders</h3>
                    <p>You haven't placed any orders yet.</p>
                    <a href="${pageContext.request.contextPath}/products" class="btn btn-primary">Start Shopping</a>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</main>

<jsp:include page="../common/footer.jsp" />
