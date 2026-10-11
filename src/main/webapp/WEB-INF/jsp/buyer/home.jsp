<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Home | RohithMart Marketplace" scope="request"/>
<jsp:include page="../common/header.jsp" />
<jsp:include page="../common/navbar.jsp" />
<jsp:include page="../common/toast.jsp" />

<main class="main-content">
    <section class="hero-banner">
        <div class="hero-content">
            <span class="badge">Exclusive Multi-Seller Marketplace</span>
            <h1>Curated Quality from Trusted Sellers</h1>
            <p>Discover premium technology, lifestyle, and artisan goods with secure checkout and fast fulfillment.</p>
            <div class="hero-actions">
                <a href="${pageContext.request.contextPath}/products" class="btn btn-primary">Explore Products</a>
                <c:if test="${empty sessionScope.user}">
                    <a href="${pageContext.request.contextPath}/auth/register?role=SELLER" class="btn btn-outline">Become a Seller</a>
                </c:if>
            </div>
        </div>
    </section>

    <section class="section-container">
        <div class="section-header">
            <h2>Featured Categories</h2>
            <p>Browse products across our popular marketplace categories</p>
        </div>
        <div class="category-grid">
            <a href="${pageContext.request.contextPath}/products?category=Electronics" class="category-card">
                <div class="category-icon">💻</div>
                <h3>Electronics</h3>
            </a>
            <a href="${pageContext.request.contextPath}/products?category=Fashion" class="category-card">
                <div class="category-icon">👔</div>
                <h3>Fashion</h3>
            </a>
            <a href="${pageContext.request.contextPath}/products?category=Home" class="category-card">
                <div class="category-icon">🏡</div>
                <h3>Home & Living</h3>
            </a>
            <a href="${pageContext.request.contextPath}/products?category=Books" class="category-card">
                <div class="category-icon">📚</div>
                <h3>Books</h3>
            </a>
        </div>
    </section>

    <section class="section-container">
        <div class="section-header">
            <h2>Trending Products</h2>
            <a href="${pageContext.request.contextPath}/products" class="view-all-link">View all &rarr;</a>
        </div>
        <div class="product-grid">
            <c:choose>
                <c:when test="${not empty featuredProducts}">
                    <c:forEach var="p" items="${featuredProducts}">
                        <div class="product-card">
                            <div class="product-image-wrap">
                                <img src="<c:out value="${p.imageUrl != null && !p.imageUrl.isEmpty() ? p.imageUrl : 'https://via.placeholder.com/300x200?text=Product'}"/>" alt="<c:out value="${p.name}"/>" loading="lazy">
                            </div>
                            <div class="product-info">
                                <span class="product-category"><c:out value="${p.category}"/></span>
                                <h3 class="product-title">
                                    <a href="${pageContext.request.contextPath}/products/detail?id=${p.id}"><c:out value="${p.name}"/></a>
                                </h3>
                                
                                <div class="rating-stars" style="margin-bottom:0.5rem;">
                                    <span>★</span>
                                    <span class="rating-score"><fmt:formatNumber value="${p.averageRating}" minFractionDigits="1" maxFractionDigits="1"/></span>
                                    <span class="rating-count">(${p.reviewCount})</span>
                                </div>

                                <div style="margin-bottom:0.5rem;">
                                    <c:choose>
                                        <c:when test="${p.stockQty > 5}">
                                            <span class="badge in-stock">In Stock</span>
                                        </c:when>
                                        <c:when test="${p.stockQty > 0}">
                                            <span class="badge status-pending">Only ${p.stockQty} left</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge out-of-stock">Out of Stock</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <div class="product-bottom">
                                    <span class="product-price">$<fmt:formatNumber value="${p.price}" minFractionDigits="2" maxFractionDigits="2"/></span>
                                    <button type="button" class="btn btn-sm btn-primary add-to-cart-btn" data-product-id="${p.id}">Add to Cart</button>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <div class="empty-state">
                        <p>No featured products available at this moment. Check back soon!</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </section>
</main>

<jsp:include page="../common/footer.jsp" />
