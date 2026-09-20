<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Manage Products | RohithMart" scope="request"/>
<c:set var="extraCss" value="dashboard" scope="request"/>
<jsp:include page="../common/header.jsp" />
<jsp:include page="../common/navbar.jsp" />
<jsp:include page="../common/toast.jsp" />

<main class="main-content">
    <div class="dashboard-container">
        <div class="dashboard-header">
            <div>
                <h1>Manage Products</h1>
                <p>Add, edit, or adjust inventory for your marketplace listings</p>
            </div>
            <div>
                <a href="${pageContext.request.contextPath}/seller/products/new" class="btn btn-primary">+ Add New Product</a>
            </div>
        </div>

        <div class="table-card">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Image</th>
                        <th>Product Name</th>
                        <th>Category</th>
                        <th>Price</th>
                        <th>Stock</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty products}">
                            <c:forEach var="p" items="${products}">
                                <tr>
                                    <td>#<c:out value="${p.id}"/></td>
                                    <td>
                                        <img src="<c:out value="${p.imageUrl != null && !p.imageUrl.isEmpty() ? p.imageUrl : 'https://via.placeholder.com/50'}"/>" alt="thumb" class="table-thumbnail">
                                    </td>
                                    <td><strong><c:out value="${p.name}"/></strong></td>
                                    <td><c:out value="${p.category}"/></td>
                                    <td>$<fmt:formatNumber value="${p.price}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                    <td><c:out value="${p.stockQty}"/></td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/seller/products/edit?id=${p.id}" class="btn btn-sm btn-outline">Edit</a>
                                        <form action="${pageContext.request.contextPath}/seller/products/delete" method="post" style="display:inline;" onsubmit="return confirm('Are you sure you want to delete this listing?');">
                                            <input type="hidden" name="id" value="${p.id}">
                                            <button type="submit" class="btn btn-sm btn-danger">Delete</button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="7" class="text-center">No listings found. Click "+ Add New Product" to create your first listing!</td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</main>

<jsp:include page="../common/footer.jsp" />
