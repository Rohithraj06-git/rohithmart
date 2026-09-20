package com.rohith.rohithmart.util;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Provides access to the application DataSource managed exclusively by AppContextListener.
 * Direct DriverManager.getConnection() calls outside AppContextListener are prohibited.
 */
public final class DatabaseUtil {

    private static volatile DataSource dataSource;

    private DatabaseUtil() {
    }

    public static void setDataSource(DataSource ds) {
        dataSource = ds;
    }

    public static DataSource getDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource has not been initialized yet.");
        }
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }
}
