<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Create Account | RohithMart" scope="request"/>
<c:set var="extraCss" value="auth" scope="request"/>
<jsp:include page="../common/header.jsp" />
<jsp:include page="../common/navbar.jsp" />
<jsp:include page="../common/toast.jsp" />

<main class="auth-container">
    <div class="auth-card">
        <h2>Create an Account</h2>
        <p class="auth-subtitle">Join RohithMart as a Buyer or Seller</p>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger" role="alert">
                <c:out value="${errorMessage}"/>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/auth/register" method="post" class="auth-form" id="register-form">
            <input type="hidden" name="_csrf" value="${_csrf_token}">

            <div class="form-group">
                <label for="name">Full Name</label>
                <input type="text" id="name" name="name" value="<c:out value="${param.name}"/>" required placeholder="e.g. Alice Smith">
            </div>

            <div class="form-group">
                <label for="email">Email Address</label>
                <input type="email" id="email" name="email" value="<c:out value="${param.email}"/>" required placeholder="name@example.com">
            </div>

            <div class="form-group">
                <label for="password">Password (minimum 8 characters)</label>
                <div class="password-wrapper">
                    <input type="password" id="password" name="password" required minlength="8" placeholder="Create a strong password">
                    <button type="button" class="password-toggle-btn" data-target="password" aria-label="Toggle password visibility">👁️</button>
                </div>
            </div>

            <div class="form-group">
                <label for="role">Account Type</label>
                <select id="role" name="role" required>
                    <option value="BUYER" ${param.role == 'BUYER' || empty param.role ? 'selected' : ''}>Buyer (I want to discover and purchase goods)</option>
                    <option value="SELLER" ${param.role == 'SELLER' ? 'selected' : ''}>Seller (I want to list and sell products)</option>
                </select>
            </div>

            <button type="submit" class="btn btn-primary btn-block">Complete Registration</button>
        </form>

        <div class="auth-footer">
            <p>Already have an account? <a href="${pageContext.request.contextPath}/auth/login">Sign In here</a></p>
        </div>
    </div>
</main>

<jsp:include page="../common/footer.jsp" />
