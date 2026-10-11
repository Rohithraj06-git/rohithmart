<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Product Catalog | RohithMart" scope="request"/>
<jsp:include page="../common/header.jsp" />
<jsp:include page="../common/navbar.jsp" />
<jsp:include page="../common/toast.jsp" />

<main class="main-content">
    <div class="catalog-layout">
        <!-- Sidebar Filters -->
        <aside class="catalog-sidebar">
            <h3>Filter Products</h3>
            <form action="${pageContext.request.contextPath}/products" method="get" class="filter-form" id="filter-form">
                <input type="hidden" name="q" value="<c:out value="${filter.keyword}"/>">

                <!-- Category Filter -->
                <div class="filter-group">
                    <label for="category">Category</label>
                    <select id="category" name="category" onchange="this.form.submit()">
                        <option value="">All Categories</option>
                        <c:forEach var="cat" items="${categories}">
                            <option value="${cat}" ${filter.category == cat ? 'selected' : ''}><c:out value="${cat}"/></option>
                        </c:forEach>
                    </select>
                </div>

                <!-- Price Range Filter -->
                <div class="filter-group">
                    <label>Price Range ($)</label>
                    <div class="price-range-inputs">
                        <input type="number" name="minPrice" placeholder="Min" min="0" step="1" value="${filter.minPrice}" aria-label="Minimum Price">
                        <span>&ndash;</span>
                        <input type="number" name="maxPrice" placeholder="Max" min="0" step="1" value="${filter.maxPrice}" aria-label="Maximum Price">
                    </div>
                </div>

                <!-- Rating Filter -->
                <div class="filter-group">
                    <label for="rating">Minimum Rating</label>
                    <select id="rating" name="rating" onchange="this.form.submit()">
                        <option value="">Any Rating</option>
                        <option value="4.5" ${filter.minRating != null && filter.minRating >= 4.5 ? 'selected' : ''}>4.5★ and above</option>
                        <option value="4.0" ${filter.minRating != null && filter.minRating >= 4.0 && filter.minRating < 4.5 ? 'selected' : ''}>4.0★ and above</option>
                        <option value="3.0" ${filter.minRating != null && filter.minRating >= 3.0 && filter.minRating < 4.0 ? 'selected' : ''}>3.0★ and above</option>
                    </select>
                </div>

                <!-- Stock Status Filter -->
                <div class="filter-group">
                    <label class="filter-checkbox-label">
                        <input type="checkbox" name="inStock" value="true" ${filter.inStockOnly ? 'checked' : ''} onchange="this.form.submit()">
                        In Stock Items Only
                    </label>
                </div>

                <!-- Sort By -->
                <div class="filter-group">
                    <label for="sortBy">Sort By</label>
                    <select id="sortBy" name="sortBy" onchange="this.form.submit()">
                        <option value="newest" ${filter.sortBy == 'newest' ? 'selected' : ''}>Newest First</option>
                        <option value="price_asc" ${filter.sortBy == 'price_asc' ? 'selected' : ''}>Price: Low to High</option>
                        <option value="price_desc" ${filter.sortBy == 'price_desc' ? 'selected' : ''}>Price: High to Low</option>
                        <option value="name_asc" ${filter.sortBy == 'name_asc' ? 'selected' : ''}>Name: A to Z</option>
                    </select>
                </div>

                <div style="display:flex;flex-direction:column;gap:0.5rem;margin-top:1.5rem;">
                    <button type="submit" class="btn btn-primary btn-sm btn-block">Apply Filters</button>
                    <a href="${pageContext.request.contextPath}/products" class="btn btn-outline btn-sm btn-block">Clear All Filters</a>
                </div>
            </form>
        </aside>

        <!-- Product Listings -->
        <section class="catalog-main">
            <div class="catalog-header">
                <div>
                    <h2 style="font-size: 1.5rem; color: var(--primary-color);">
                        <c:choose>
                            <c:when test="${not empty filter.category}"><c:out value="${filter.category}"/></c:when>
                            <c:otherwise>All Marketplace Products</c:otherwise>
                        </c:choose>
                    </h2>
                    <c:if test="${not empty filter.keyword}">
                        <p style="font-size: 0.9rem; color: var(--text-secondary); margin-top: 0.2rem;">
                            Search query: <strong>"<c:out value="${filter.keyword}"/>"</strong>
                        </p>
                    </c:if>
                </div>
                <div class="results-count">
                    Showing <c:out value="${products.size()}"/> of <c:out value="${totalResults}"/> products
                </div>
            </div>

            <!-- Product Cards Grid -->
            <div class="product-grid">
                <c:choose>
                    <c:when test="${not empty products}">
                        <c:forEach var="p" items="${products}">
                            <div class="product-card">
                                <div class="product-image-wrap">
                                    <a href="${pageContext.request.contextPath}/products/detail?id=${p.id}" style="display:block;width:100%;height:100%;">
                                        <img src="<c:out value="${p.imageUrl != null && !p.imageUrl.isEmpty() ? p.imageUrl : 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600'}"/>" alt="<c:out value="${p.name}"/>" loading="lazy">
                                    </a>
                                </div>
                                <div class="product-info">
                                    <span class="product-category"><c:out value="${p.category}"/></span>
                                    <h3 class="product-title">
                                        <a href="${pageContext.request.contextPath}/products/detail?id=${p.id}">
                                            <c:out value="${p.name}"/>
                                        </a>
                                    </h3>

                                    <!-- Rating -->
                                    <div class="rating-stars" style="margin-bottom: 0.5rem;">
                                        <span>★</span>
                                        <span class="rating-score"><fmt:formatNumber value="${p.averageRating}" minFractionDigits="1" maxFractionDigits="1"/></span>
                                        <span class="rating-count">(${p.reviewCount})</span>
                                    </div>

                                    <!-- Stock Status -->
                                    <div style="margin-bottom: 0.75rem;">
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

                                    <!-- Bottom: Price & Quick Add -->
                                    <div class="product-bottom">
                                        <span class="product-price">$<fmt:formatNumber value="${p.price}" minFractionDigits="2" maxFractionDigits="2"/></span>
                                        <c:choose>
                                            <c:when test="${p.stockQty > 0}">
                                                <button type="button" class="btn btn-sm btn-primary add-to-cart-btn" data-product-id="${p.id}">
                                                    Add to Cart
                                                </button>
                                            </c:when>
                                            <c:otherwise>
                                                <button type="button" class="btn btn-sm btn-outline" disabled style="opacity:0.6;cursor:not-allowed;">
                                                    Unavailable
                                                </button>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-state" style="grid-column: 1 / -1;">
                            <div class="empty-state-icon">🔍</div>
                            <h3>No matching products found</h3>
                            <p>Try widening your search terms, clearing active category filters, or resetting price thresholds.</p>
                            <a href="${pageContext.request.contextPath}/products" class="btn btn-primary">
                                View All Products
                            </a>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- Pagination Bar -->
            <c:if test="${totalPages > 1}">
                <ul class="pagination">
                    <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                        <a class="page-link" href="${pageContext.request.contextPath}/products?page=${currentPage - 1}&q=<c:out value="${filter.keyword}"/>&category=<c:out value="${filter.category}"/>&sortBy=<c:out value="${filter.sortBy}"/>&minPrice=${filter.minPrice}&maxPrice=${filter.maxPrice}&rating=${filter.minRating}&inStock=${filter.inStockOnly}">&laquo; Prev</a>
                    </li>
                    <c:forEach begin="1" end="${totalPages}" var="i">
                        <li class="page-item ${currentPage == i ? 'active' : ''}">
                            <a class="page-link" href="${pageContext.request.contextPath}/products?page=${i}&q=<c:out value="${filter.keyword}"/>&category=<c:out value="${filter.category}"/>&sortBy=<c:out value="${filter.sortBy}"/>&minPrice=${filter.minPrice}&maxPrice=${filter.maxPrice}&rating=${filter.minRating}&inStock=${filter.inStockOnly}">${i}</a>
                        </li>
                    </c:forEach>
                    <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                        <a class="page-link" href="${pageContext.request.contextPath}/products?page=${currentPage + 1}&q=<c:out value="${filter.keyword}"/>&category=<c:out value="${filter.category}"/>&sortBy=<c:out value="${filter.sortBy}"/>&minPrice=${filter.minPrice}&maxPrice=${filter.maxPrice}&rating=${filter.minRating}&inStock=${filter.inStockOnly}">Next &raquo;</a>
                    </li>
                </ul>
            </c:if>
        </section>
    </div>
</main>

<jsp:include page="../common/footer.jsp" />
