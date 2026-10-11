<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<header class="main-header">
    <div class="header-container">
        <a href="${pageContext.request.contextPath}/home" class="brand-logo">
            <span class="logo-accent">Rohith</span>Mart
        </a>

        <form action="${pageContext.request.contextPath}/products" method="get" class="search-form">
            <input type="text" name="q" placeholder="Search products, brands, and categories..." value="<c:out value="${param.q}"/>" aria-label="Search">
            <button type="submit" class="btn btn-search">Search</button>
        </form>

        <nav class="nav-links">
            <a href="${pageContext.request.contextPath}/products">Browse</a>
            <c:choose>
                <c:when test="${not empty sessionScope.user}">
                    <c:if test="${sessionScope.user.role == 'BUYER'}">
                        <a href="${pageContext.request.contextPath}/cart" class="cart-link">
                            Cart <span id="nav-cart-badge" class="badge badge-navy" style="display:none;font-size:0.75rem;padding:0.15rem 0.45rem;border-radius:10px;">0</span>
                        </a>
                        <a href="${pageContext.request.contextPath}/buyer/orders">My Orders</a>
                    </c:if>
                    <c:if test="${sessionScope.user.role == 'SELLER'}">
                        <a href="${pageContext.request.contextPath}/seller/dashboard">Seller Portal</a>
                    </c:if>
                    <c:if test="${sessionScope.user.role == 'ADMIN'}">
                        <a href="${pageContext.request.contextPath}/admin/dashboard">Admin Panel</a>
                    </c:if>
                    <span class="user-greeting">Hi, <c:out value="${sessionScope.user.name}"/></span>
                    <a href="${pageContext.request.contextPath}/auth/logout" class="btn btn-outline btn-sm">Logout</a>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/auth/login" class="btn btn-outline btn-sm">Sign In</a>
                    <a href="${pageContext.request.contextPath}/auth/register" class="btn btn-primary btn-sm">Register</a>
                </c:otherwise>
            </c:choose>
        </nav>
    </div>
</header>
