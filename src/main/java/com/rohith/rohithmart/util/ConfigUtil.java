package com.rohith.rohithmart.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility for reading configuration parameters from application.properties
 * with environment variable override capabilities.
 */
public final class ConfigUtil {

    private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);
    private static final Properties properties = new Properties();

    static {
        try (InputStream is = ConfigUtil.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                properties.load(is);
                logger.info("Loaded application.properties successfully");
            } else {
                logger.warn("application.properties not found on classpath");
            }
        } catch (IOException e) {
            logger.error("Failed to load application.properties", e);
        }
    }

    private ConfigUtil() {
    }

    public static String get(String key) {
        String envKey = key.replace('.', '_').toUpperCase();
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.trim().isEmpty()) {
            return envVal;
        }
        String sysVal = System.getProperty(key);
        if (sysVal != null && !sysVal.trim().isEmpty()) {
            return sysVal;
        }
        return properties.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        String val = get(key);
        return val != null ? val : defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        String val = get(key);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
