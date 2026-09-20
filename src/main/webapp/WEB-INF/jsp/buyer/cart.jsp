<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Shopping Cart | RohithMart" scope="request"/>
<c:set var="extraJs" value="cart" scope="request"/>
<jsp:include page="../common/header.jsp" />
<jsp:include page="../common/navbar.jsp" />
<jsp:include page="../common/toast.jsp" />

<main class="main-content">
    <div class="cart-container">
        <h1>Shopping Cart</h1>
        <c:choose>
            <c:when test="${not empty cartItems}">
                <div class="cart-layout">
                    <div class="cart-table-wrap">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Product</th>
                                    <th>Unit Price</th>
                                    <th>Quantity</th>
                                    <th>Subtotal</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${cartItems}">
                                    <tr>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/products/detail?id=${item.productId}">
                                                <c:out value="${item.productName}"/>
                                            </a>
                                        </td>
                                        <td>$<fmt:formatNumber value="${item.unitPrice}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                        <td>
                                            <input type="number" class="cart-qty-input" value="${item.quantity}" min="1" max="99" data-cart-item-id="${item.id}">
                                        </td>
                                        <td>$<fmt:formatNumber value="${item.subtotal}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                        <td>
                                            <form action="${pageContext.request.contextPath}/cart/remove" method="post" style="display:inline;">
                                                <input type="hidden" name="cartItemId" value="${item.id}">
                                                <button type="submit" class="btn btn-sm btn-danger">Remove</button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                    <div class="cart-summary-card">
                        <h3>Order Summary</h3>
                        <div class="summary-row total">
                            <span>Total</span>
                            <span class="total-amount">$<fmt:formatNumber value="${cartTotal}" minFractionDigits="2" maxFractionDigits="2"/></span>
                        </div>
                        <a href="${pageContext.request.contextPath}/checkout" class="btn btn-primary btn-block">Proceed to Checkout</a>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="empty-state">
                    <h3>Your cart is empty</h3>
                    <p>Discover products to add to your cart.</p>
                    <a href="${pageContext.request.contextPath}/products" class="btn btn-primary">Start Shopping</a>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</main>

<jsp:include page="../common/footer.jsp" />
