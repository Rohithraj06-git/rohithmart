<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="${product.name} | RohithMart" scope="request"/>
<jsp:include page="../common/header.jsp" />
<jsp:include page="../common/navbar.jsp" />
<jsp:include page="../common/toast.jsp" />

<main class="main-content">
    <div style="margin-bottom: 1.5rem;">
        <a href="${pageContext.request.contextPath}/products" style="color:var(--text-secondary);text-decoration:none;font-size:0.9rem;">
            &larr; Back to Products
        </a>
    </div>

    <!-- Product Showcase -->
    <div class="product-detail-container">
        <!-- Left: Image & Interactive Gallery -->
        <div class="product-detail-gallery">
            <div class="product-detail-image">
                <img id="main-product-img" src="<c:out value="${product.imageUrl != null && !product.imageUrl.isEmpty() ? product.imageUrl : 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600'}"/>" alt="<c:out value="${product.name}"/>">
            </div>
            <!-- Gallery Thumbnails -->
            <div class="gallery-thumbs">
                <div class="gallery-thumb active" data-full-src="<c:out value="${product.imageUrl != null && !product.imageUrl.isEmpty() ? product.imageUrl : 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600'}"/>">
                    <img src="<c:out value="${product.imageUrl != null && !product.imageUrl.isEmpty() ? product.imageUrl : 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600'}"/>" alt="View 1">
                </div>
                <!-- Contextual alternate perspective thumbnails -->
                <div class="gallery-thumb" data-full-src="https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600">
                    <img src="https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600" alt="View 2">
                </div>
                <div class="gallery-thumb" data-full-src="https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=600">
                    <img src="https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=600" alt="View 3">
                </div>
            </div>
        </div>

        <!-- Right: Information, Stock, and Purchase Actions -->
        <div class="product-detail-info">
            <span class="product-category"><c:out value="${product.category}"/></span>
            <h1 class="product-detail-title"><c:out value="${product.name}"/></h1>
            
            <div class="product-detail-meta">
                <!-- Star Rating -->
                <div class="rating-stars">
                    <span>★</span>
                    <span class="rating-score"><fmt:formatNumber value="${product.averageRating}" minFractionDigits="1" maxFractionDigits="1"/></span>
                    <span class="rating-count">(${product.reviewCount} ${product.reviewCount == 1 ? 'review' : 'reviews'})</span>
                </div>
                <span style="color:var(--text-light);">|</span>
                <span style="color:var(--text-secondary);font-size:0.9rem;">Sold by Verified Seller #<c:out value="${product.sellerId}"/></span>
            </div>

            <div class="product-detail-price">
                $<fmt:formatNumber value="${product.price}" minFractionDigits="2" maxFractionDigits="2"/>
            </div>

            <div>
                <c:choose>
                    <c:when test="${product.stockQty > 5}">
                        <span class="product-stock-tag in-stock">
                            ✓ In Stock (<c:out value="${product.stockQty}"/> available)
                        </span>
                    </c:when>
                    <c:when test="${product.stockQty > 0}">
                        <span class="product-stock-tag low-stock">
                            ⚠ Only <c:out value="${product.stockQty}"/> left in stock
                        </span>
                    </c:when>
                    <c:otherwise>
                        <span class="product-stock-tag out-of-stock">
                            ✕ Currently Out of Stock
                        </span>
                    </c:otherwise>
                </c:choose>
            </div>

            <p class="product-description"><c:out value="${product.description}"/></p>

            <c:choose>
                <c:when test="${product.stockQty > 0}">
                    <form action="${pageContext.request.contextPath}/cart/add" method="post" class="purchase-actions-form">
                        <input type="hidden" name="_csrf" value="${_csrf_token}">
                        <input type="hidden" name="productId" value="${product.id}">

                        <div class="qty-stepper-wrap">
                            <label for="qty" style="font-weight:600;font-size:0.9rem;">Quantity:</label>
                            <div class="qty-stepper">
                                <button type="button" class="qty-minus" aria-label="Decrease quantity">&minus;</button>
                                <input type="number" id="qty" name="quantity" value="1" min="1" max="${product.stockQty}" aria-label="Quantity">
                                <button type="button" class="qty-plus" aria-label="Increase quantity">&plus;</button>
                            </div>
                            <span style="font-size:0.85rem;color:var(--text-secondary);">Max: ${product.stockQty}</span>
                        </div>

                        <div class="button-action-row">
                            <!-- Quick Add to Cart Button -->
                            <button type="button" class="btn btn-outline btn-lg add-to-cart-btn" data-product-id="${product.id}">
                                Add to Cart
                            </button>

                            <!-- Buy Now Form Submit -->
                            <button type="submit" name="buyNow" value="true" class="btn btn-primary btn-lg">
                                Buy Now
                            </button>
                        </div>
                    </form>
                </c:when>
                <c:otherwise>
                    <div style="padding:1rem;background:#fef2f2;border-radius:var(--radius-sm);color:#991b1b;font-size:0.9rem;border:1px solid #fecaca;">
                        This item is temporarily out of inventory. Please check back soon or browse alternate categories.
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <!-- Customer Reviews Section -->
    <section class="section-container" style="margin-top: 3rem;">
        <div class="section-header">
            <div>
                <h2>Customer Reviews</h2>
                <p style="color:var(--text-secondary);font-size:0.9rem;margin-top:0.25rem;">
                    Average rating: <strong><fmt:formatNumber value="${product.averageRating}" minFractionDigits="1" maxFractionDigits="1"/> / 5.0</strong> across ${reviews.size()} verified buyers
                </p>
            </div>
        </div>

        <c:choose>
            <c:when test="${not empty reviews}">
                <div class="reviews-list">
                    <c:forEach var="rev" items="${reviews}">
                        <div class="review-item">
                            <div class="review-header">
                                <div>
                                    <span class="review-user"><c:out value="${rev.userName != null ? rev.userName : 'Verified Buyer'}"/></span>
                                    <span style="color:var(--success-color);font-size:0.8rem;margin-left:0.5rem;">✓ Verified Purchase</span>
                                </div>
                                <div style="display:flex;align-items:center;gap:0.75rem;">
                                    <div class="rating-stars">
                                        <c:forEach begin="1" end="5" var="s">
                                            <span class="${s <= rev.rating ? '' : 'star-empty'}">★</span>
                                        </c:forEach>
                                    </div>
                                    <span class="review-date">
                                        <fmt:formatDate value="${rev.createdAt}" pattern="MMM dd, yyyy"/>
                                    </span>
                                </div>
                            </div>
                            <p class="review-comment"><c:out value="${rev.comment}"/></p>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="empty-state">
                    <p>No buyer reviews posted yet. Be the first to review this product!</p>
                </div>
            </c:otherwise>
        </c:choose>

        <!-- Write a Review Form -->
        <div class="review-form-card">
            <h3>Write a Review</h3>
            <c:choose>
                <c:when test="${not empty sessionScope.user}">
                    <form action="${pageContext.request.contextPath}/products/review" method="post">
                        <input type="hidden" name="_csrf" value="${_csrf_token}">
                        <input type="hidden" name="productId" value="${product.id}">

                        <div class="form-group">
                            <label style="display:block;margin-bottom:0.5rem;font-weight:600;">Overall Rating</label>
                            <div class="star-rating-picker" role="radiogroup" aria-label="Rating">
                                <input type="radio" id="star5" name="rating" value="5" required><label for="star5" title="5 stars">★</label>
                                <input type="radio" id="star4" name="rating" value="4"><label for="star4" title="4 stars">★</label>
                                <input type="radio" id="star3" name="rating" value="3"><label for="star3" title="3 stars">★</label>
                                <input type="radio" id="star2" name="rating" value="2"><label for="star2" title="2 stars">★</label>
                                <input type="radio" id="star1" name="rating" value="1"><label for="star1" title="1 star">★</label>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="reviewComment" style="font-weight:600;">Your Experience & Review</label>
                            <textarea id="reviewComment" name="comment" rows="4" required placeholder="Describe your experience with the item quality, performance, and craftsmanship..." style="width:100%;padding:0.75rem;border:1px solid var(--border-color);border-radius:var(--radius-sm);font-family:inherit;"></textarea>
                        </div>

                        <button type="submit" class="btn btn-primary btn-sm">
                            Submit Review
                        </button>
                    </form>
                </c:when>
                <c:otherwise>
                    <p style="color:var(--text-secondary);font-size:0.95rem;">
                        Please <a href="${pageContext.request.contextPath}/auth/login" style="color:var(--primary-color);font-weight:600;">sign in</a> to share your feedback and rate this product.
                    </p>
                </c:otherwise>
            </c:choose>
        </div>
    </section>
</main>

<jsp:include page="../common/footer.jsp" />
