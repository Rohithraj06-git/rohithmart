<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="isEdit" value="${not empty product && product.id > 0}"/>
<c:set var="pageTitle" value="${isEdit ? 'Edit Product' : 'Add New Product'} | RohithMart" scope="request"/>
<c:set var="extraCss" value="dashboard" scope="request"/>
<jsp:include page="../common/header.jsp" />
<jsp:include page="../common/navbar.jsp" />
<jsp:include page="../common/toast.jsp" />

<main class="main-content">
    <div class="dashboard-container">
        <div class="form-card">
            <h2><c:out value="${isEdit ? 'Edit Product Listing' : 'Create New Product Listing'}"/></h2>
            
            <form action="${pageContext.request.contextPath}${isEdit ? '/seller/products/edit' : '/seller/products/new'}" method="post">
                <input type="hidden" name="_csrf" value="${_csrf_token}">
                <c:if test="${isEdit}">
                    <input type="hidden" name="id" value="${product.id}">
                </c:if>

                <div class="form-group">
                    <label for="name">Product Title</label>
                    <input type="text" id="name" name="name" value="<c:out value="${product.name}"/>" required maxlength="200" placeholder="e.g. Wireless Ergonomic Mouse">
                </div>

                <div class="form-group">
                    <label for="category">Category</label>
                    <select id="category" name="category" required>
                        <option value="">Select Category</option>
                        <option value="Electronics" ${product.category == 'Electronics' ? 'selected' : ''}>Electronics</option>
                        <option value="Fashion" ${product.category == 'Fashion' ? 'selected' : ''}>Fashion</option>
                        <option value="Home" ${product.category == 'Home' ? 'selected' : ''}>Home & Living</option>
                        <option value="Books" ${product.category == 'Books' ? 'selected' : ''}>Books</option>
                    </select>
                </div>

                <div class="form-row">
                    <div class="form-group col-half">
                        <label for="price">Price ($)</label>
                        <input type="number" id="price" name="price" step="0.01" min="0.01" value="${product.price}" required placeholder="29.99">
                    </div>
                    <div class="form-group col-half">
                        <label for="stockQty">Available Stock Quantity</label>
                        <input type="number" id="stockQty" name="stockQty" min="0" value="${product.stockQty != null ? product.stockQty : 0}" required placeholder="50">
                    </div>
                </div>

                <div class="form-group">
                    <label for="imageUrl">Image URL</label>
                    <input type="url" id="imageUrl" name="imageUrl" value="<c:out value="${product.imageUrl}"/>" placeholder="https://example.com/image.jpg">
                </div>

                <div class="form-group">
                    <label for="description">Detailed Description</label>
                    <textarea id="description" name="description" rows="5" required placeholder="Describe item features, specifications, and warranty..."><c:out value="${product.description}"/></textarea>
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn btn-primary"><c:out value="${isEdit ? 'Save Changes' : 'Publish Listing'}"/></button>
                    <a href="${pageContext.request.contextPath}/seller/products" class="btn btn-outline">Cancel</a>
                </div>
            </form>
        </div>
    </div>
</main>

<jsp:include page="../common/footer.jsp" />
