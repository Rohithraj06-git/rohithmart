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
        <h1 style="font-size: 2rem; color: var(--primary-color); margin-bottom: 2rem;">Secure Checkout</h1>
        
        <div class="checkout-layout">
            <!-- Left Column: Customer Details, Address, & Payment -->
            <div class="checkout-form-section">
                <form action="${pageContext.request.contextPath}/checkout/place" method="post" id="checkout-form">
                    <input type="hidden" name="_csrf" value="${_csrf_token}">

                    <h3>1. Customer Information</h3>
                    <div class="form-row">
                        <div class="form-group col-half">
                            <label for="fullName">Full Name</label>
                            <input type="text" id="fullName" name="fullName" required value="<c:out value="${sessionScope.user.name}"/>" placeholder="e.g. John Doe">
                        </div>
                        <div class="form-group col-half">
                            <label for="email">Email Address</label>
                            <input type="email" id="email" name="email" required value="<c:out value="${sessionScope.user.email}"/>" readonly style="background:#f1f5f9;">
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="phone">Phone Number</label>
                        <input type="tel" id="phone" name="phone" placeholder="+1 (555) 019-2834" required value="+1 (555) 019-2834">
                    </div>

                    <h3 style="margin-top:2rem;">2. Shipping Destination</h3>
                    <div class="form-group">
                        <label for="shippingAddress">Street Address</label>
                        <input type="text" id="shippingAddress" name="shippingAddress" required placeholder="e.g. 742 Evergreen Terrace, Apt 4B">
                    </div>
                    <div class="form-row">
                        <div class="form-group col-half">
                            <label for="city">City</label>
                            <input type="text" id="city" name="city" required placeholder="e.g. Springfield">
                        </div>
                        <div class="form-group col-half">
                            <label for="postalCode">Postal Code</label>
                            <input type="text" id="postalCode" name="postalCode" required placeholder="e.g. 97477">
                        </div>
                    </div>

                    <h3 style="margin-top:2rem;">3. Payment Method</h3>
                    <p style="color:var(--text-secondary);font-size:0.9rem;margin-bottom:1rem;">
                        Per capstone scope specification, transactions use mock authorization.
                    </p>

                    <!-- Mock Card Option -->
                    <div class="payment-card-box selected">
                        <label class="filter-checkbox-label" style="font-weight:600;cursor:pointer;">
                            <input type="radio" name="paymentMethod" value="MOCK_CARD" checked>
                            Credit or Debit Card (Instant Mock Settlement)
                        </label>
                        <div id="mock-card-details" class="mock-card-inputs">
                            <div class="form-group" style="margin-bottom:0.75rem;">
                                <label for="cardNum" style="font-size:0.8rem;">Card Number</label>
                                <input type="text" id="cardNum" placeholder="•••• •••• •••• 4242" value="4242 •••• •••• 4242" readonly style="background:#ffffff;letter-spacing:1px;">
                            </div>
                            <div class="form-row" style="margin-bottom:0;">
                                <div class="form-group col-half" style="margin-bottom:0;">
                                    <label for="cardExp" style="font-size:0.8rem;">Expiration</label>
                                    <input type="text" id="cardExp" placeholder="MM/YY" value="12/28" readonly style="background:#ffffff;">
                                </div>
                                <div class="form-group col-half" style="margin-bottom:0;">
                                    <label for="cardCvv" style="font-size:0.8rem;">Security Code (CVV)</label>
                                    <input type="text" id="cardCvv" placeholder="CVV" value="888" readonly style="background:#ffffff;">
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Mock Cash on Delivery Option -->
                    <div class="payment-card-box">
                        <label class="filter-checkbox-label" style="font-weight:600;cursor:pointer;">
                            <input type="radio" name="paymentMethod" value="MOCK_COD">
                            Cash on Delivery (Mock COD)
                        </label>
                    </div>

                    <button type="submit" class="btn btn-primary btn-block btn-lg" style="margin-top:2rem;">
                        Confirm & Place Order ($<fmt:formatNumber value="${cartTotal}" minFractionDigits="2" maxFractionDigits="2"/>)
                    </button>
                </form>
            </div>

            <!-- Right Column: Order Items Breakdown & Total -->
            <div class="checkout-summary-section">
                <h3>Order Summary (${cartItems.size()} ${cartItems.size() == 1 ? 'item' : 'items'})</h3>
                
                <ul class="checkout-item-list">
                    <c:forEach var="item" items="${cartItems}">
                        <li class="checkout-item">
                            <div>
                                <div style="font-weight:600;color:var(--text-primary);margin-bottom:0.2rem;">
                                    <c:out value="${item.productName}"/>
                                </div>
                                <div style="font-size:0.85rem;color:var(--text-secondary);">
                                    Quantity: <c:out value="${item.quantity}"/> &times; $<fmt:formatNumber value="${item.unitPrice}" minFractionDigits="2" maxFractionDigits="2"/>
                                </div>
                            </div>
                            <span class="item-price" style="font-weight:600;">
                                $<fmt:formatNumber value="${item.subtotal}" minFractionDigits="2" maxFractionDigits="2"/>
                            </span>
                        </li>
                    </c:forEach>
                </ul>

                <div class="summary-row">
                    <span>Subtotal</span>
                    <span>$<fmt:formatNumber value="${cartTotal}" minFractionDigits="2" maxFractionDigits="2"/></span>
                </div>
                <div class="summary-row">
                    <span>Estimated Shipping</span>
                    <span style="color:var(--success-color);font-weight:600;">FREE</span>
                </div>
                <div class="summary-row">
                    <span>Estimated Taxes</span>
                    <span>$0.00</span>
                </div>
                
                <div class="summary-row total">
                    <span>Total Due</span>
                    <span class="total-amount">$<fmt:formatNumber value="${cartTotal}" minFractionDigits="2" maxFractionDigits="2"/></span>
                </div>

                <div style="margin-top:1.5rem;padding:1rem;background:#f8fafc;border-radius:var(--radius-sm);font-size:0.85rem;color:var(--text-secondary);border:1px solid var(--border-color);">
                    🔒 <strong>Encrypted & Authoritative</strong><br>
                    Prices and inventory are validated on the server at checkout.
                </div>
            </div>
        </div>
    </div>
</main>

<jsp:include page="../common/footer.jsp" />
