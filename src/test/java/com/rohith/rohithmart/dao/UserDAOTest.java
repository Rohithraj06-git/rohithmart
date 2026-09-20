package com.rohith.rohithmart.dao;

import com.rohith.rohithmart.model.Role;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.util.DatabaseUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOTest {

    private static HikariDataSource dataSource;
    private UserDAO userDAO;

    @BeforeAll
    static void initDataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(5);

        dataSource = new HikariDataSource(config);
        DatabaseUtil.setDataSource(dataSource);
    }

    @AfterAll
    static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @BeforeEach
    void setupSchema() throws Exception {
        userDAO = new UserDAOImpl();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");

            try (InputStream is = getClass().getClassLoader().getResourceAsStream("db/schema.sql")) {
                assertNotNull(is, "db/schema.sql should be present on test classpath");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                StringBuilder sql = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().startsWith("--") || line.trim().isEmpty()) {
                        continue;
                    }
                    sql.append(line).append("\n");
                    if (line.trim().endsWith(";")) {
                        stmt.execute(sql.toString());
                        sql.setLength(0);
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("UserDAO creates and retrieves user by email and ID")
    void testCreateAndFindUser() throws SQLException {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@rohithmart.com");
        user.setPasswordHash("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
        user.setRole(Role.BUYER);

        User created = userDAO.create(user);
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);

        Optional<User> byEmail = userDAO.findByEmail("test@rohithmart.com");
        assertTrue(byEmail.isPresent());
        assertEquals("Test User", byEmail.get().getName());
        assertEquals(Role.BUYER, byEmail.get().getRole());

        Optional<User> byId = userDAO.findById(created.getId());
        assertTrue(byId.isPresent());
        assertEquals("test@rohithmart.com", byId.get().getEmail());
    }

    @Test
    @DisplayName("UserDAO returns empty when user is not found")
    void testFindNonExistentUser() throws SQLException {
        Optional<User> nonExistent = userDAO.findByEmail("notfound@example.com");
        assertTrue(nonExistent.isEmpty());
    }
}
