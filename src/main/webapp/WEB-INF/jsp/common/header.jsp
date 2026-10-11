<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="context-path" content="${pageContext.request.contextPath}">
    <meta name="csrf-token" content="${_csrf_token}">
    <title><c:out value="${pageTitle != null ? pageTitle : 'RohithMart | Multi-Seller Marketplace'}"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/main.css">
    <c:if test="${not empty extraCss}">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/${extraCss}.css">
    </c:if>
</head>
<body>
<div class="site-wrapper">
