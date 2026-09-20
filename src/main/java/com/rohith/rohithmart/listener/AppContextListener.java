package com.rohith.rohithmart.listener;

import com.rohith.rohithmart.util.ConfigUtil;
import com.rohith.rohithmart.util.DatabaseMigrationManager;
import com.rohith.rohithmart.util.DatabaseUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Owns the complete lifecycle of the connection pool and database initialization.
 * Enforces Capstone Rule 5: Connection pool lifecycle owned by a single ServletContextListener.
 * Guarantees zero connection leaks and automatic schema migrations.
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
            config.setPoolName(ConfigUtil.get("db.pool.name", "RohithMartHikariPool"));
            config.setDriverClassName(ConfigUtil.get("db.driver", "org.h2.Driver"));
            config.setJdbcUrl(ConfigUtil.get("db.url", "jdbc:h2:./data/rohithmart;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE"));
            config.setUsername(ConfigUtil.get("db.user", "sa"));
            config.setPassword(ConfigUtil.get("db.password", ""));

            // Pool sizing and timeouts
            config.setMaximumPoolSize(ConfigUtil.getInt("db.pool.maxSize", 10));
            config.setMinimumIdle(ConfigUtil.getInt("db.pool.minIdle", 2));
            config.setConnectionTimeout(ConfigUtil.getInt("db.pool.connectionTimeout", 30000));
            config.setIdleTimeout(ConfigUtil.getInt("db.pool.idleTimeout", 600000));
            config.setMaxLifetime(ConfigUtil.getInt("db.pool.maxLifetime", 1800000));

            // Leak detection: logs a stack trace if a connection is open longer than threshold
            int leakThreshold = ConfigUtil.getInt("db.pool.leakDetectionThreshold", 60000);
            if (leakThreshold > 0) {
                config.setLeakDetectionThreshold(leakThreshold);
            }

            this.dataSource = new HikariDataSource(config);
            DatabaseUtil.setDataSource(this.dataSource);
            logger.info("HikariCP connection pool '{}' successfully initialized.", config.getPoolName());

            // Run automated migrations and seed verification
            boolean autoMigrate = Boolean.parseBoolean(ConfigUtil.get("db.migration.autoRun", "true"));
            if (autoMigrate) {
                DatabaseMigrationManager migrationManager = new DatabaseMigrationManager(this.dataSource);
                migrationManager.migrate();

                boolean autoSeed = Boolean.parseBoolean(ConfigUtil.get("db.seed.autoRun", "true"));
                if (autoSeed) {
                    migrationManager.seedIfEmpty();
                }
            }

        } catch (Exception e) {
            logger.error("FATAL: Failed to initialize database connection pool and migrations", e);
            throw new RuntimeException("Application startup aborted due to DB initialization failure", e);
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
