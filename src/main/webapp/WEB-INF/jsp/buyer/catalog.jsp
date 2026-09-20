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
            <form action="${pageContext.request.contextPath}/products" method="get" class="filter-form">
                <input type="hidden" name="q" value="<c:out value="${param.q}"/>">

                <div class="filter-group">
                    <label for="category">Category</label>
                    <select id="category" name="category" onchange="this.form.submit()">
                        <option value="">All Categories</option>
                        <option value="Electronics" ${param.category == 'Electronics' ? 'selected' : ''}>Electronics</option>
                        <option value="Fashion" ${param.category == 'Fashion' ? 'selected' : ''}>Fashion</option>
                        <option value="Home" ${param.category == 'Home' ? 'selected' : ''}>Home & Living</option>
                        <option value="Books" ${param.category == 'Books' ? 'selected' : ''}>Books</option>
                    </select>
                </div>

                <div class="filter-group">
                    <label for="sortBy">Sort By</label>
                    <select id="sortBy" name="sortBy" onchange="this.form.submit()">
                        <option value="newest" ${param.sortBy == 'newest' ? 'selected' : ''}>Newest First</option>
                        <option value="price_asc" ${param.sortBy == 'price_asc' ? 'selected' : ''}>Price: Low to High</option>
                        <option value="price_desc" ${param.sortBy == 'price_desc' ? 'selected' : ''}>Price: High to Low</option>
                        <option value="name_asc" ${param.sortBy == 'name_asc' ? 'selected' : ''}>Name: A to Z</option>
                    </select>
                </div>

                <a href="${pageContext.request.contextPath}/products" class="btn btn-outline btn-sm btn-block">Clear Filters</a>
            </form>
        </aside>

        <!-- Product Listings -->
        <section class="catalog-main">
            <div class="catalog-header">
                <h2>Products</h2>
                <c:if test="${not empty param.q}">
                    <p class="search-indicator">Showing results for: <strong>"<c:out value="${param.q}"/>"</strong></p>
                </c:if>
            </div>

            <div class="product-grid">
                <c:choose>
                    <c:when test="${not empty products}">
                        <c:forEach var="p" items="${products}">
                            <div class="product-card">
                                <div class="product-image-wrap">
                                    <img src="<c:out value="${p.imageUrl != null && !p.imageUrl.isEmpty() ? p.imageUrl : 'https://via.placeholder.com/300x200?text=Product'}"/>" alt="<c:out value="${p.name}"/>">
                                </div>
                                <div class="product-info">
                                    <span class="product-category"><c:out value="${p.category}"/></span>
                                    <h3 class="product-title">
                                        <a href="${pageContext.request.contextPath}/products/detail?id=${p.id}"><c:out value="${p.name}"/></a>
                                    </h3>
                                    <div class="product-bottom">
                                        <span class="product-price">$<fmt:formatNumber value="${p.price}" minFractionDigits="2" maxFractionDigits="2"/></span>
                                        <button class="btn btn-sm btn-primary add-to-cart-btn" data-product-id="${p.id}">Add to Cart</button>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-state">
                            <p>No products found matching your search or filter criteria.</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>
    </div>
</main>

<jsp:include page="../common/footer.jsp" />
