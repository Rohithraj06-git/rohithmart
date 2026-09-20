<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div id="toast-container" class="toast-container" aria-live="polite" aria-atomic="true">
    <c:if test="${not empty sessionScope.flashSuccess}">
        <div class="toast toast-success show" role="alert">
            <span class="toast-message"><c:out value="${sessionScope.flashSuccess}"/></span>
            <button type="button" class="toast-close" onclick="this.parentElement.remove();">&times;</button>
        </div>
        <c:remove var="flashSuccess" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.flashError}">
        <div class="toast toast-error show" role="alert">
            <span class="toast-message"><c:out value="${sessionScope.flashError}"/></span>
            <button type="button" class="toast-close" onclick="this.parentElement.remove();">&times;</button>
        </div>
        <c:remove var="flashError" scope="session"/>
    </c:if>
</div>
