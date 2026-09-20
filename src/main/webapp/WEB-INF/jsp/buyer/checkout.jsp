<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Checkout | RohithMart" scope="request"/>
<c:set var="extraJs" value="checkout" scope="request"/>
<jsp:include page="../common/header.jsp" />
<jsp:include page="../common/navbar.jsp" />
<jsp:include page="../common/toast.jsp" />

<main class="main-content">
    <div class="checkout-container">
        <h1>Complete Your Order</h1>
        <div class="checkout-layout">
            <div class="checkout-form-section">
                <form action="${pageContext.request.contextPath}/checkout/place" method="post" id="checkout-form">
                    <h3>Shipping Address</h3>
                    <div class="form-group">
                        <label for="shippingAddress">Street Address</label>
                        <input type="text" id="shippingAddress" name="shippingAddress" required placeholder="123 Main Street, Apt 4B">
                    </div>
                    <div class="form-row">
                        <div class="form-group col-half">
                            <label for="city">City</label>
                            <input type="text" id="city" name="city" required placeholder="City">
                        </div>
                        <div class="form-group col-half">
                            <label for="postalCode">Postal Code</label>
                            <input type="text" id="postalCode" name="postalCode" required placeholder="10001">
                        </div>
                    </div>

                    <h3>Mock Payment Confirmation</h3>
                    <p class="text-muted">Per capstone scope specification, payments are mocked. Choose your payment method:</p>
                    <div class="payment-options">
                        <label class="radio-label">
                            <input type="radio" name="paymentMethod" value="MOCK_CARD" checked>
                            Mock Credit / Debit Card (Instant Approval)
                        </label>
                        <label class="radio-label">
                            <input type="radio" name="paymentMethod" value="MOCK_COD">
                            Cash on Delivery (Mock)
                        </label>
                    </div>

                    <button type="submit" class="btn btn-primary btn-block btn-lg" style="margin-top:1.5rem;">
                        Confirm & Place Order ($<fmt:formatNumber value="${cartTotal}" minFractionDigits="2" maxFractionDigits="2"/>)
                    </button>
                </form>
            </div>

            <div class="checkout-summary-section">
                <h3>Order Items</h3>
                <ul class="checkout-item-list">
                    <c:forEach var="item" items="${cartItems}">
                        <li class="checkout-item">
                            <span class="item-name"><c:out value="${item.productName}"/> &times; ${item.quantity}</span>
                            <span class="item-price">$<fmt:formatNumber value="${item.subtotal}" minFractionDigits="2" maxFractionDigits="2"/></span>
                        </li>
                    </c:forEach>
                </ul>
                <div class="summary-row total">
                    <span>Total</span>
                    <span class="total-amount">$<fmt:formatNumber value="${cartTotal}" minFractionDigits="2" maxFractionDigits="2"/></span>
                </div>
            </div>
        </div>
    </div>
</main>

<jsp:include page="../common/footer.jsp" />
