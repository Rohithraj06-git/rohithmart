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
        <h1 style="font-size: 2rem; color: var(--primary-color); margin-bottom: 2rem;">Shopping Cart</h1>

        <c:choose>
            <c:when test="${not empty cartItems}">
                <div class="cart-layout">
                    <!-- Left: Cart Items Table -->
                    <div class="cart-table-wrap">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Item</th>
                                    <th>Price</th>
                                    <th style="width: 130px;">Quantity</th>
                                    <th>Subtotal</th>
                                    <th style="width: 80px; text-align: center;">Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${cartItems}">
                                    <tr>
                                        <td>
                                            <div style="display:flex;align-items:center;gap:1rem;">
                                                <a href="${pageContext.request.contextPath}/products/detail?id=${item.productId}" style="font-weight:600;color:var(--text-primary);text-decoration:none;">
                                                    <c:out value="${item.productName}"/>
                                                </a>
                                            </div>
                                        </td>
                                        <td>$<fmt:formatNumber value="${item.unitPrice}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                        <td>
                                            <div class="qty-stepper">
                                                <button type="button" class="qty-minus" aria-label="Decrease quantity">&minus;</button>
                                                <input type="number" class="cart-qty-input" value="${item.quantity}" min="1" max="99" data-cart-item-id="${item.id}" aria-label="Quantity">
                                                <button type="button" class="qty-plus" aria-label="Increase quantity">&plus;</button>
                                            </div>
                                        </td>
                                        <td class="item-subtotal" style="font-weight:600;color:var(--primary-color);">
                                            $<fmt:formatNumber value="${item.subtotal}" minFractionDigits="2" maxFractionDigits="2"/>
                                        </td>
                                        <td style="text-align:center;">
                                            <button type="button" class="btn btn-sm btn-outline btn-remove-cart-item" data-cart-item-id="${item.id}" data-product-name="<c:out value="${item.productName}"/>" title="Remove item" aria-label="Remove item">
                                                ✕
                                            </button>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>

                    <!-- Right: Order Summary -->
                    <div class="cart-summary-card">
                        <h3>Cart Summary</h3>
                        <div class="summary-row">
                            <span>Total Items</span>
                            <span>${cartItems.size()}</span>
                        </div>
                        <div class="summary-row">
                            <span>Standard Shipping</span>
                            <span style="color:var(--success-color);font-weight:600;">FREE</span>
                        </div>
                        <div class="summary-row total">
                            <span>Estimated Total</span>
                            <span class="total-amount">$<fmt:formatNumber value="${cartTotal}" minFractionDigits="2" maxFractionDigits="2"/></span>
                        </div>
                        
                        <div style="margin-top: 1.5rem; display: flex; flex-direction: column; gap: 0.75rem;">
                            <a href="${pageContext.request.contextPath}/checkout" class="btn btn-primary btn-block btn-lg">
                                Proceed to Checkout
                            </a>
                            <a href="${pageContext.request.contextPath}/products" class="btn btn-outline btn-block btn-sm">
                                &larr; Continue Shopping
                            </a>
                        </div>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="empty-state">
                    <div class="empty-state-icon">🛒</div>
                    <h3>Your cart is currently empty</h3>
                    <p>Discover handpicked items and artisan collections from verified marketplace sellers.</p>
                    <a href="${pageContext.request.contextPath}/products" class="btn btn-primary">
                        Browse Products
                    </a>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</main>

<jsp:include page="../common/footer.jsp" />
