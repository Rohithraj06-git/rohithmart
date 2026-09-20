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
                <h1>Incoming Orders</h1>
                <p>Manage and fulfill orders placed for your products</p>
            </div>
        </div>

        <div class="table-card">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Order ID</th>
                        <th>Date</th>
                        <th>Product</th>
                        <th>Quantity</th>
                        <th>Unit Price</th>
                        <th>Subtotal</th>
                        <th>Status</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty sellerOrders}">
                            <c:forEach var="so" items="${sellerOrders}">
                                <tr>
                                    <td>#<c:out value="${so.orderId}"/></td>
                                    <td><fmt:formatDate value="${so.createdAt}" pattern="MMM dd, yyyy"/></td>
                                    <td><c:out value="${so.productName}"/></td>
                                    <td><c:out value="${so.quantity}"/></td>
                                    <td>$<fmt:formatNumber value="${so.unitPrice}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                    <td>$<fmt:formatNumber value="${so.subtotal}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                    <td>
                                        <span class="badge status-<c:out value="${so.status.toLowerCase()}"/>"><c:out value="${so.status}"/></span>
                                    </td>
                                    <td>
                                        <form action="${pageContext.request.contextPath}/seller/orders/status" method="post" style="display:inline;">
                                            <input type="hidden" name="orderId" value="${so.orderId}">
                                            <select name="status" onchange="this.form.submit()" class="sm-select">
                                                <option value="PENDING" ${so.status == 'PENDING' ? 'selected' : ''}>Pending</option>
                                                <option value="CONFIRMED" ${so.status == 'CONFIRMED' ? 'selected' : ''}>Confirmed</option>
                                                <option value="SHIPPED" ${so.status == 'SHIPPED' ? 'selected' : ''}>Shipped</option>
                                                <option value="DELIVERED" ${so.status == 'DELIVERED' ? 'selected' : ''}>Delivered</option>
                                                <option value="CANCELLED" ${so.status == 'CANCELLED' ? 'selected' : ''}>Cancelled</option>
                                            </select>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="8" class="text-center">No incoming orders for your listings yet.</td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</main>

<jsp:include page="../common/footer.jsp" />
