<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Sign In | RohithMart" scope="request"/>
<c:set var="extraCss" value="auth" scope="request"/>
<jsp:include page="../common/header.jsp" />
<jsp:include page="../common/navbar.jsp" />
<jsp:include page="../common/toast.jsp" />

<main class="auth-container">
    <div class="auth-card">
        <h2>Sign In to RohithMart</h2>
        <p class="auth-subtitle">Access your account, track orders, or manage listings</p>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger" role="alert">
                <c:out value="${errorMessage}"/>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/auth/login" method="post" class="auth-form" id="login-form">
            <input type="hidden" name="_csrf" value="${_csrf_token}">

            <div class="form-group">
                <label for="email">Email Address</label>
                <input type="email" id="email" name="email" value="<c:out value="${param.email}"/>" required autocomplete="email" placeholder="name@example.com">
            </div>

            <div class="form-group">
                <label for="password">Password</label>
                <div class="password-wrapper">
                    <input type="password" id="password" name="password" required autocomplete="current-password" placeholder="Enter your password">
                    <button type="button" class="password-toggle-btn" data-target="password" aria-label="Toggle password visibility">👁️</button>
                </div>
            </div>

            <button type="submit" class="btn btn-primary btn-block">Sign In</button>
        </form>

        <!-- Quick Demo Credentials for Rapid Testing -->
        <div style="margin-top:1.5rem;padding:1rem;background:#f8fafc;border-radius:var(--radius-sm);border:1px solid var(--border-color);font-size:0.85rem;">
            <div style="font-weight:600;color:var(--text-primary);margin-bottom:0.5rem;">Demo Testing Accounts (Password@123):</div>
            <div style="display:flex;flex-wrap:wrap;gap:0.4rem;">
                <button type="button" class="btn btn-sm btn-outline" onclick="fillDemo('john.buyer@example.com')">Buyer Demo</button>
                <button type="button" class="btn btn-sm btn-outline" onclick="fillDemo('techseller@rohithmart.com')">Seller Demo</button>
                <button type="button" class="btn btn-sm btn-outline" onclick="fillDemo('admin@rohithmart.com')">Admin Demo</button>
            </div>
        </div>

        <div class="auth-footer">
            <p>Don't have an account yet? <a href="${pageContext.request.contextPath}/auth/register">Create one now</a></p>
        </div>
    </div>
</main>

<script>
function fillDemo(email) {
    document.getElementById('email').value = email;
    document.getElementById('password').value = 'Password@123';
}
</script>

<jsp:include page="../common/footer.jsp" />
