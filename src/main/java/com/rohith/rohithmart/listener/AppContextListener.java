package com.rohith.rohithmart.listener;

import com.rohith.rohithmart.util.ConfigUtil;
import com.rohith.rohithmart.util.DatabaseUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Owns the complete lifecycle of the connection pool and database initialization.
 * Enforces Capstone Rule 5: Connection pool lifecycle owned by a single ServletContextListener.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(AppContextListener.class);
    private HikariDataSource dataSource;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Initializing RohithMart ServletContext and HikariCP connection pool...");

        try {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName(ConfigUtil.get("db.driver", "org.h2.Driver"));
            config.setJdbcUrl(ConfigUtil.get("db.url", "jdbc:h2:./data/rohithmart;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE"));
            config.setUsername(ConfigUtil.get("db.user", "sa"));
            config.setPassword(ConfigUtil.get("db.password", ""));

            config.setMaximumPoolSize(ConfigUtil.getInt("db.pool.maxSize", 10));
            config.setMinimumIdle(ConfigUtil.getInt("db.pool.minIdle", 2));
            config.setConnectionTimeout(ConfigUtil.getInt("db.pool.connectionTimeout", 30000));
            config.setPoolName("RohithMartHikariPool");

            this.dataSource = new HikariDataSource(config);
            DatabaseUtil.setDataSource(this.dataSource);
            logger.info("HikariCP connection pool initialized successfully.");

            // Run initial schema initialization if in local/development mode
            initDatabaseSchema();

        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool", e);
            throw new RuntimeException("Application startup aborted due to DB connection failure", e);
        }
    }

    private void initDatabaseSchema() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             InputStream is = getClass().getClassLoader().getResourceAsStream("db/schema.sql")) {

            if (is != null) {
                logger.info("Running automatic schema verification/initialization from classpath...");
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
                logger.info("Database schema verified/initialized successfully.");
            }
        } catch (Exception e) {
            logger.warn("Schema auto-run skipped or encountered non-fatal notice: {}", e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Closing HikariCP connection pool and releasing resources...");
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("HikariCP connection pool closed cleanly.");
        }
    }
}
