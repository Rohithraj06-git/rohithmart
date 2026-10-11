<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="All Orders | Admin | RohithMart" scope="request"/>
<c:set var="extraCss" value="dashboard" scope="request"/>
<jsp:include page="../common/header.jsp" />
<jsp:include page="../common/navbar.jsp" />
<jsp:include page="../common/toast.jsp" />

<main class="main-content">
    <div class="dashboard-container">
        <div class="dashboard-header">
            <div>
                <h1>Platform Orders</h1>
                <p>Overview of all marketplace transactions across buyers and sellers</p>
            </div>
            <div>
                <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-outline">&larr; Back to Admin</a>
            </div>
        </div>

        <div class="table-card">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Order ID</th>
                        <th>Buyer ID</th>
                        <th>Date</th>
                        <th>Total Amount</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="o" items="${allOrders}">
                        <tr>
                            <td>#<c:out value="${o.id}"/></td>
                            <td>Buyer #<c:out value="${o.buyerId}"/></td>
                            <td><fmt:formatDate value="${o.createdAt}" pattern="MMM dd, yyyy HH:mm"/></td>
                            <td>$<fmt:formatNumber value="${o.totalAmount}" minFractionDigits="2" maxFractionDigits="2"/></td>
                            <td>
                                <span class="badge status-<c:out value="${o.status.name().toLowerCase()}"/>"><c:out value="${o.status}"/></span>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</main>

<jsp:include page="../common/footer.jsp" />
