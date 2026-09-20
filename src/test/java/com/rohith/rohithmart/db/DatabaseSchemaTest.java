package com.rohith.rohithmart.db;

import com.rohith.rohithmart.util.DatabaseMigrationManager;
import com.rohith.rohithmart.util.DatabaseUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates table definitions, columns, primary keys, foreign keys, and indexes.
 */
class DatabaseSchemaTest {

    private static HikariDataSource dataSource;

    @BeforeAll
    static void setUp() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:schema_test;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(5);

        dataSource = new HikariDataSource(config);
        DatabaseUtil.setDataSource(dataSource);

        // Run migrations
        DatabaseMigrationManager migrationManager = new DatabaseMigrationManager(dataSource);
        migrationManager.migrate();
    }

    @AfterAll
    static void tearDown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Test
    @DisplayName("Verify all 6 required business tables and migration table exist")
    void testRequiredTablesExist() throws SQLException {
        Set<String> requiredTables = Set.of("USERS", "PRODUCTS", "ORDERS", "ORDER_ITEMS", "CART_ITEMS", "REVIEWS", "SCHEMA_VERSION");
        Set<String> actualTables = new HashSet<>();

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    actualTables.add(rs.getString("TABLE_NAME").toUpperCase());
                }
            }
        }

        for (String table : requiredTables) {
            assertTrue(actualTables.contains(table), "Expected table '" + table + "' to exist in schema");
        }
    }

    @Test
    @DisplayName("Verify created_at timestamp column is present on all business tables")
    void testCreatedAtColumnOnAllTables() throws SQLException {
        String[] tables = {"USERS", "PRODUCTS", "ORDERS", "ORDER_ITEMS", "CART_ITEMS", "REVIEWS"};

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            for (String table : tables) {
                boolean hasCreatedAt = false;
                try (ResultSet rs = metaData.getColumns(null, null, table, "CREATED_AT")) {
                    if (rs.next()) {
                        hasCreatedAt = true;
                        String typeName = rs.getString("TYPE_NAME");
                        assertTrue(typeName.contains("TIMESTAMP"), "CREATED_AT in " + table + " should be TIMESTAMP");
                    }
                }
                assertTrue(hasCreatedAt, "Table " + table + " must contain a CREATED_AT column");
            }
        }
    }

    @Test
    @DisplayName("Verify monetary columns are DECIMAL(10, 2) and not FLOAT")
    void testMonetaryColumnsAreDecimal() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            // products.price
            assertColumnIsDecimal(metaData, "PRODUCTS", "PRICE", 10, 2);
            // orders.total_amount
            assertColumnIsDecimal(metaData, "ORDERS", "TOTAL_AMOUNT", 10, 2);
            // order_items.unit_price
            assertColumnIsDecimal(metaData, "ORDER_ITEMS", "UNIT_PRICE", 10, 2);
        }
    }

    private void assertColumnIsDecimal(DatabaseMetaData metaData, String table, String column, int expectedPrecision, int expectedScale) throws SQLException {
        try (ResultSet rs = metaData.getColumns(null, null, table, column)) {
            assertTrue(rs.next(), "Column " + column + " in " + table + " must exist");
            String typeName = rs.getString("TYPE_NAME");
            int columnSize = rs.getInt("COLUMN_SIZE");
            int decimalDigits = rs.getInt("DECIMAL_DIGITS");

            assertTrue(typeName.toUpperCase().contains("DECIMAL") || typeName.toUpperCase().contains("NUMERIC"),
                    table + "." + column + " type should be DECIMAL, found: " + typeName);
            assertEquals(expectedPrecision, columnSize, "Precision should be " + expectedPrecision);
            assertEquals(expectedScale, decimalDigits, "Scale should be " + expectedScale);
        }
    }

    @Test
    @DisplayName("Verify primary keys exist for all tables")
    void testPrimaryKeysExist() throws SQLException {
        String[] tables = {"USERS", "PRODUCTS", "ORDERS", "ORDER_ITEMS", "CART_ITEMS", "REVIEWS"};

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            for (String table : tables) {
                try (ResultSet rs = metaData.getPrimaryKeys(null, null, table)) {
                    assertTrue(rs.next(), "Table " + table + " must have a primary key");
                    assertEquals("ID", rs.getString("COLUMN_NAME").toUpperCase());
                }
            }
        }
    }

    @Test
    @DisplayName("Verify foreign key constraints exist across related entities")
    void testForeignKeyConstraints() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            assertForeignKeyExists(metaData, "PRODUCTS", "SELLER_ID", "USERS", "ID");
            assertForeignKeyExists(metaData, "ORDERS", "BUYER_ID", "USERS", "ID");
            assertForeignKeyExists(metaData, "ORDER_ITEMS", "ORDER_ID", "ORDERS", "ID");
            assertForeignKeyExists(metaData, "ORDER_ITEMS", "PRODUCT_ID", "PRODUCTS", "ID");
            assertForeignKeyExists(metaData, "CART_ITEMS", "USER_ID", "USERS", "ID");
            assertForeignKeyExists(metaData, "CART_ITEMS", "PRODUCT_ID", "PRODUCTS", "ID");
            assertForeignKeyExists(metaData, "REVIEWS", "PRODUCT_ID", "PRODUCTS", "ID");
            assertForeignKeyExists(metaData, "REVIEWS", "USER_ID", "USERS", "ID");
        }
    }

    private void assertForeignKeyExists(DatabaseMetaData metaData, String fkTable, String fkColumn, String pkTable, String pkColumn) throws SQLException {
        boolean found = false;
        try (ResultSet rs = metaData.getImportedKeys(null, null, fkTable)) {
            while (rs.next()) {
                String fetchedPkTable = rs.getString("PKTABLE_NAME");
                String fetchedPkColumn = rs.getString("PKCOLUMN_NAME");
                String fetchedFkColumn = rs.getString("FKCOLUMN_NAME");

                if (pkTable.equalsIgnoreCase(fetchedPkTable)
                        && pkColumn.equalsIgnoreCase(fetchedPkColumn)
                        && fkColumn.equalsIgnoreCase(fetchedFkColumn)) {
                    found = true;
                    break;
                }
            }
        }
        assertTrue(found, "Expected foreign key in " + fkTable + "(" + fkColumn + ") -> " + pkTable + "(" + pkColumn + ")");
    }

    @Test
    @DisplayName("Verify important indexes exist for search, filter, and foreign keys")
    void testIndexesExist() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            assertIndexExists(metaData, "USERS", "EMAIL");
            assertIndexExists(metaData, "PRODUCTS", "SELLER_ID");
            assertIndexExists(metaData, "PRODUCTS", "CATEGORY");
            assertIndexExists(metaData, "ORDERS", "BUYER_ID");
            assertIndexExists(metaData, "ORDERS", "STATUS");
            assertIndexExists(metaData, "ORDER_ITEMS", "ORDER_ID");
            assertIndexExists(metaData, "CART_ITEMS", "USER_ID");
            assertIndexExists(metaData, "REVIEWS", "PRODUCT_ID");
        }
    }

    private void assertIndexExists(DatabaseMetaData metaData, String tableName, String columnName) throws SQLException {
        boolean found = false;
        try (ResultSet rs = metaData.getIndexInfo(null, null, tableName, false, false)) {
            while (rs.next()) {
                String indexedCol = rs.getString("COLUMN_NAME");
                if (columnName.equalsIgnoreCase(indexedCol)) {
                    found = true;
                    break;
                }
            }
        }
        assertTrue(found, "Expected index covering column " + columnName + " on table " + tableName);
    }
}
