package com.rohith.rohithmart.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests HikariCP connection pool acquisition, release, lifecycle, and zero-leak behavior.
 */
class HikariCPConnectionPoolTest {

    private HikariDataSource dataSource;

    @BeforeEach
    void setUp() {
        HikariConfig config = new HikariConfig();
        config.setPoolName("TestHikariPool");
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:pool_test;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(5000);
        config.setLeakDetectionThreshold(2000); // 2s leak threshold for test

        dataSource = new HikariDataSource(config);
    }

    @AfterEach
    void tearDown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Test
    @DisplayName("Connection can be acquired, validated, and returned using try-with-resources")
    void testConnectionAcquisitionAndRelease() throws SQLException {
        assertFalse(dataSource.isClosed());
        assertEquals("TestHikariPool", dataSource.getPoolName());

        try (Connection conn = dataSource.getConnection()) {
            assertNotNull(conn);
            assertFalse(conn.isClosed());
            assertTrue(conn.isValid(1));

            try (PreparedStatement ps = conn.prepareStatement("SELECT 1");
                 ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
            }
        }

        // After block, connection must be returned to pool (active connections should be 0)
        assertEquals(0, dataSource.getHikariPoolMXBean().getActiveConnections());
    }

    @Test
    @DisplayName("Concurrent connections up to pool capacity can be acquired and released without leaks")
    void testConcurrentConnectionBorrow() throws Exception {
        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Callable<Boolean>> tasks = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            tasks.add(() -> {
                try (Connection conn = dataSource.getConnection();
                     PreparedStatement ps = conn.prepareStatement("SELECT 42");
                     ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(42, rs.getInt(1));
                    Thread.sleep(50); // simulate brief work
                    return true;
                }
            });
        }

        List<Future<Boolean>> results = executor.invokeAll(tasks);
        for (Future<Boolean> future : results) {
            assertTrue(future.get(), "Thread task should succeed");
        }

        executor.shutdown();

        // Verify all connections returned
        assertEquals(0, dataSource.getHikariPoolMXBean().getActiveConnections(), "All connections must be returned to pool");
    }

    @Test
    @DisplayName("Connection pool properly shuts down and rejects new requests")
    void testPoolShutdown() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            assertTrue(conn.isValid(1));
        }

        dataSource.close();
        assertTrue(dataSource.isClosed());

        assertThrows(SQLException.class, () -> dataSource.getConnection(), "Should reject connection borrow when pool is closed");
    }
}
