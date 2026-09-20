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
            <div class="alert alert-danger">
                <c:out value="${errorMessage}"/>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/auth/login" method="post" class="auth-form">
            <div class="form-group">
                <label for="email">Email Address</label>
                <input type="email" id="email" name="email" value="<c:out value="${param.email}"/>" required autocomplete="email" placeholder="name@example.com">
            </div>

            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" name="password" required autocomplete="current-password" placeholder="Enter your password">
            </div>

            <button type="submit" class="btn btn-primary btn-block">Sign In</button>
        </form>

        <div class="auth-footer">
            <p>Don't have an account yet? <a href="${pageContext.request.contextPath}/auth/register">Create one now</a></p>
        </div>
    </div>
</main>

<jsp:include page="../common/footer.jsp" />
