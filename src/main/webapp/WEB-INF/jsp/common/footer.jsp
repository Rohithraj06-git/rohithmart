<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<footer class="main-footer">
    <div class="footer-container">
        <div class="footer-col">
            <h4>RohithMart</h4>
            <p>A trusted multi-seller e-commerce marketplace built with Java Servlets, JSP, JDBC, and Apache Tomcat.</p>
        </div>
        <div class="footer-col">
            <h4>Quick Links</h4>
            <ul>
                <li><a href="${pageContext.request.contextPath}/home">Home</a></li>
                <li><a href="${pageContext.request.contextPath}/products">All Products</a></li>
                <li><a href="${pageContext.request.contextPath}/auth/login">Seller Login</a></li>
            </ul>
        </div>
        <div class="footer-col">
            <h4>Support</h4>
            <p>Need assistance? Use our AI assistant or contact support@rohithmart.com.</p>
        </div>
    </div>
    <div class="footer-bottom">
        <p>&copy; 2026 RohithMart. Built for Anna University R2025 Semester 3 Capstone.</p>
    </div>
</footer>

<!-- AI Chat Floating Trigger & Drawer -->
<div id="ai-chat-root">
    <button id="ai-chat-btn" class="ai-chat-fab" aria-label="Open AI Assistant" title="Ask RohithMart AI">
        💬
    </button>
    <div id="ai-chat-panel" class="ai-chat-panel hidden">
        <div class="ai-chat-header">
            <h3>RohithMart Assistant</h3>
            <button id="ai-chat-close" class="close-btn">&times;</button>
        </div>
        <div id="ai-chat-messages" class="ai-chat-messages">
            <div class="chat-bubble ai">Hello! I'm your RohithMart assistant. Ask me anything about our products, categories, or marketplace features!</div>
        </div>
        <form id="ai-chat-form" class="ai-chat-input-area">
            <input type="text" id="ai-chat-input" placeholder="Type your question..." autocomplete="off" maxlength="300" required>
            <button type="submit" class="btn btn-primary btn-sm">Send</button>
        </form>
    </div>
</div>

</div><!-- /.site-wrapper -->

<script src="${pageContext.request.contextPath}/static/js/main.js"></script>
<script src="${pageContext.request.contextPath}/static/js/toast.js"></script>
<script src="${pageContext.request.contextPath}/static/js/chat.js"></script>
<c:if test="${not empty extraJs}">
    <script src="${pageContext.request.contextPath}/static/js/${extraJs}.js"></script>
</c:if>
</body>
</html>
