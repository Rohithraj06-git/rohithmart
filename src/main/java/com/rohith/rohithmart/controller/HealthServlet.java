package com.rohith.rohithmart.controller;

import com.rohith.rohithmart.util.DatabaseUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check endpoint as specified in Capstone Section 18:
 * GET /api/v1/health returns { "status": "UP", "db": "UP" }, verifying database connectivity.
 */
@WebServlet(name = "HealthServlet", urlPatterns = {"/api/v1/health"})
public class HealthServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("status", "UP");

        boolean dbUp = false;
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            dbUp = stmt.execute("SELECT 1");
        } catch (Exception e) {
            logger.error("Health check DB probe failed", e);
        }

        statusMap.put("db", dbUp ? "UP" : "DOWN");

        int statusCode = dbUp ? HttpServletResponse.SC_OK : HttpServletResponse.SC_SERVICE_UNAVAILABLE;
        sendJsonResponse(res, statusCode, statusMap);
    }
}
