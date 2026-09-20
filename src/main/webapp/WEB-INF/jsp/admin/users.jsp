<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="User Management | Admin | RohithMart" scope="request"/>
<c:set var="extraCss" value="dashboard" scope="request"/>
<jsp:include page="../common/header.jsp" />
<jsp:include page="../common/navbar.jsp" />
<jsp:include page="../common/toast.jsp" />

<main class="main-content">
    <div class="dashboard-container">
        <div class="dashboard-header">
            <div>
                <h1>User Accounts</h1>
                <p>View all registered buyers, sellers, and platform administrators</p>
            </div>
            <div>
                <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-outline">&larr; Back to Admin</a>
            </div>
        </div>

        <div class="table-card">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Role</th>
                        <th>Created At</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="u" items="${users}">
                        <tr>
                            <td>#<c:out value="${u.id}"/></td>
                            <td><strong><c:out value="${u.name}"/></strong></td>
                            <td><c:out value="${u.email}"/></td>
                            <td><span class="badge role-<c:out value="${u.role.toLowerCase()}"/>"><c:out value="${u.role}"/></span></td>
                            <td><fmt:formatDate value="${u.createdAt}" pattern="MMM dd, yyyy HH:mm"/></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</main>

<jsp:include page="../common/footer.jsp" />
