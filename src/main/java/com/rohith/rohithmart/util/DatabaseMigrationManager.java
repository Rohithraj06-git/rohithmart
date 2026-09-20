package com.rohith.rohithmart.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Migration and seed manager adhering to Capstone Section 14 specifications.
 * Tracks applied migrations in a schema_version table and applies migrations in sequential order.
 */
public class DatabaseMigrationManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigrationManager.class);

    private static final String CREATE_SCHEMA_VERSION_TABLE =
            "CREATE TABLE IF NOT EXISTS schema_version ("
                    + "installed_rank INT PRIMARY KEY, "
                    + "version VARCHAR(50) NOT NULL, "
                    + "description VARCHAR(200) NOT NULL, "
                    + "script VARCHAR(100) NOT NULL, "
                    + "installed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, "
                    + "execution_time_ms BIGINT NOT NULL, "
                    + "success BOOLEAN NOT NULL"
                    + ")";

    /**
     * Known versioned migration files in sequential execution order.
     */
    private static final List<MigrationFile> MIGRATIONS = List.of(
            new MigrationFile(1, "1", "init_schema", "db/migrations/V1__init_schema.sql")
    );

    private final DataSource dataSource;

    public DatabaseMigrationManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Executes pending database migrations and optionally applies seed data.
     */
    public void migrate() {
        logger.info("Starting database migration process...");
        try (Connection conn = dataSource.getConnection()) {
            initSchemaVersionTable(conn);

            for (MigrationFile migration : MIGRATIONS) {
                if (!isMigrationApplied(conn, migration.version)) {
                    applyMigration(conn, migration);
                } else {
                    logger.debug("Migration {} ({}) is already applied. Skipping.", migration.version, migration.description);
                }
            }
            logger.info("All database migrations verified successfully.");

        } catch (SQLException e) {
            logger.error("Database migration failed", e);
            throw new RuntimeException("Migration failed", e);
        }
    }

    /**
     * Populates database with seed data if the users table is currently empty.
     */
    public void seedIfEmpty() {
        try (Connection conn = dataSource.getConnection()) {
            if (!hasUsers(conn)) {
                logger.info("Database users table is empty. Applying development seed data from db/seed.sql...");
                executeSqlScript(conn, "db/seed.sql");
                logger.info("Seed data successfully applied.");
            } else {
                logger.info("Database already contains user records. Skipping seed data execution.");
            }
        } catch (SQLException e) {
            logger.error("Failed to check or apply seed data", e);
            throw new RuntimeException("Seed data application error", e);
        }
    }

    private void initSchemaVersionTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_SCHEMA_VERSION_TABLE);
        }
    }

    private boolean isMigrationApplied(Connection conn, String version) throws SQLException {
        String sql = "SELECT COUNT(*) FROM schema_version WHERE version = ? AND success = TRUE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, version);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private void applyMigration(Connection conn, MigrationFile migration) throws SQLException {
        logger.info("Applying migration V{}__{} from {}", migration.version, migration.description, migration.scriptPath);
        long start = System.currentTimeMillis();

        boolean autoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            executeSqlScript(conn, migration.scriptPath);

            long duration = System.currentTimeMillis() - start;
            String recordSql = "INSERT INTO schema_version (installed_rank, version, description, script, execution_time_ms, success) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(recordSql)) {
                ps.setInt(1, migration.rank);
                ps.setString(2, migration.version);
                ps.setString(3, migration.description);
                ps.setString(4, migration.scriptPath);
                ps.setLong(5, duration);
                ps.setBoolean(6, true);
                ps.executeUpdate();
            }

            conn.commit();
            logger.info("Migration V{} applied successfully in {}ms.", migration.version, duration);
        } catch (Exception e) {
            conn.rollback();
            logger.error("Failed to apply migration V{}", migration.version, e);
            throw new SQLException("Migration V" + migration.version + " failed: " + e.getMessage(), e);
        } finally {
            conn.setAutoCommit(autoCommit);
        }
    }

    private void executeSqlScript(Connection conn, String resourcePath) throws SQLException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            throw new SQLException("SQL script resource not found on classpath: " + resourcePath);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             Statement stmt = conn.createStatement()) {

            StringBuilder sql = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.startsWith("--") || trimmed.isEmpty()) {
                    continue;
                }
                sql.append(line).append("\n");
                if (trimmed.endsWith(";")) {
                    stmt.execute(sql.toString());
                    sql.setLength(0);
                }
            }

            // Flush any trailing statement without semicolon
            if (sql.length() > 0 && !sql.toString().trim().isEmpty()) {
                stmt.execute(sql.toString());
            }

        } catch (IOException e) {
            throw new SQLException("I/O error reading SQL script: " + resourcePath, e);
        }
    }

    private boolean hasUsers(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            // Table might not exist yet
            return false;
        }
        return false;
    }

    private static class MigrationFile {
        final int rank;
        final String version;
        final String description;
        final String scriptPath;

        MigrationFile(int rank, String version, String description, String scriptPath) {
            this.rank = rank;
            this.version = version;
            this.description = description;
            this.scriptPath = scriptPath;
        }
    }
}
