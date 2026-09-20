<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="${product.name} | RohithMart" scope="request"/>
<jsp:include page="../common/header.jsp" />
<jsp:include page="../common/navbar.jsp" />
<jsp:include page="../common/toast.jsp" />

<main class="main-content">
    <div class="product-detail-container">
        <div class="product-detail-image">
            <img src="<c:out value="${product.imageUrl != null && !product.imageUrl.isEmpty() ? product.imageUrl : 'https://via.placeholder.com/600x400?text=No+Image'}"/>" alt="<c:out value="${product.name}"/>">
        </div>
        <div class="product-detail-info">
            <span class="product-category"><c:out value="${product.category}"/></span>
            <h1 class="product-detail-title"><c:out value="${product.name}"/></h1>
            <p class="product-seller">Sold by Seller #<c:out value="${product.sellerId}"/></p>
            <div class="product-detail-price">$<fmt:formatNumber value="${product.price}" minFractionDigits="2" maxFractionDigits="2"/></div>
            
            <div class="product-stock-status">
                <c:choose>
                    <c:when test="${product.stockQty > 0}">
                        <span class="badge in-stock">In Stock (<c:out value="${product.stockQty}"/> available)</span>
                    </c:when>
                    <c:otherwise>
                        <span class="badge out-of-stock">Out of Stock</span>
                    </c:otherwise>
                </c:choose>
            </div>

            <p class="product-description"><c:out value="${product.description}"/></p>

            <c:if test="${product.stockQty > 0}">
                <form action="${pageContext.request.contextPath}/cart/add" method="post" class="add-to-cart-form">
                    <input type="hidden" name="productId" value="${product.id}">
                    <div class="quantity-input-group">
                        <label for="qty">Quantity</label>
                        <input type="number" id="qty" name="quantity" value="1" min="1" max="${product.stockQty}">
                    </div>
                    <button type="submit" class="btn btn-primary btn-lg">Add to Cart</button>
                </form>
            </c:if>
        </div>
    </div>

    <!-- Product Reviews & Ratings (F8) -->
    <section class="section-container reviews-section">
        <h2>Customer Reviews</h2>
        <c:choose>
            <c:when test="${not empty reviews}">
                <div class="reviews-list">
                    <c:forEach var="rev" items="${reviews}">
                        <div class="review-item">
                            <div class="review-header">
                                <span class="review-user"><c:out value="${rev.userName != null ? rev.userName : 'Verified Buyer'}"/></span>
                                <span class="review-rating">&#9733; <c:out value="${rev.rating}"/>/5</span>
                            </div>
                            <p class="review-comment"><c:out value="${rev.comment}"/></p>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <p class="empty-state">No reviews yet for this product.</p>
            </c:otherwise>
        </c:choose>
    </section>
</main>

<jsp:include page="../common/footer.jsp" />
