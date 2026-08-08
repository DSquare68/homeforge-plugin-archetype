package com.github.dsquare68.template;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * This plugin's own database connection, read from the
 * {@code /<plugin_id>.properties} file HUB writes into the plugin jar at install time.
 *
 * <p>HUB provisions a dedicated PostgreSQL role and schema per plugin and drops
 * the credentials into the jar root, so they arrive as an ordinary classpath
 * resource:
 *
 * <pre>{@code
 * db.url=jdbc:postgresql://host:5432/hub?currentSchema=template_schema
 * db.username=plugin_template
 * db.password=<generated>
 * db.schema=template_schema
 * db.driver-class-name=org.postgresql.Driver
 * }</pre>
 *
 * <p>The role owns {@code template_schema} and has no grants on {@code hub_schema}
 * or any other plugin's schema, so this connection can only ever touch this
 * plugin's own data. Use it instead of {@code HubApi#storage()#dataSource()},
 * which runs with HUB's credentials.
 *
 * <p>The file is generated per install and contains a password — never commit
 * it, and never log the values.
 */
public final class PluginDb {

    /** Written by HUB at install time; absent until the plugin has been installed. */
    private static final String CREDENTIALS_FILE = "/" + PluginInfo.PLUGIN_ID + ".properties";

    private static final String KEY_URL = "db.url";
    private static final String KEY_USERNAME = "db.username";
    private static final String KEY_PASSWORD = "db.password";
    private static final String KEY_SCHEMA = "db.schema";
    private static final String KEY_DRIVER = "db.driver-class-name";

    private static final int MAX_POOL_SIZE = 3;

    private final String url;
    private final String username;
    private final String password;
    private final String schema;
    private final String driverClassName;

    private volatile HikariDataSource dataSource;

    private PluginDb(String url, String username, String password, String schema, String driverClassName) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.schema = schema;
        this.driverClassName = driverClassName;
    }

    /**
     * Loads the credentials HUB generated for this plugin.
     *
     * @throws IllegalStateException if the file is missing or incomplete, which
     *         means the plugin jar was loaded without HUB provisioning it first
     */
    public static PluginDb load() {
        Properties properties = new Properties();
        try (InputStream in = PluginDb.class.getResourceAsStream(CREDENTIALS_FILE)) {
            if (in == null) {
                throw new IllegalStateException(CREDENTIALS_FILE
                        + " is missing from the plugin jar — HUB writes it when the plugin is installed.");
            }
            properties.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + CREDENTIALS_FILE, e);
        }

        String url = require(properties, KEY_URL);
        String username = require(properties, KEY_USERNAME);
        String password = require(properties, KEY_PASSWORD);
        String schema = properties.getProperty(KEY_SCHEMA, PluginInfo.PLUGIN_SCHEMA);
        String driverClassName = properties.getProperty(KEY_DRIVER, "org.postgresql.Driver");

        return new PluginDb(url, username, password, schema, driverClassName);
    }

    /**
     * A connection pool scoped to this plugin's schema, created on first use.
     * Kept small on purpose: every plugin gets its own pool against the same
     * PostgreSQL instance.
     */
    public DataSource dataSource() {
        HikariDataSource existing = dataSource;
        if (existing != null) {
            return existing;
        }
        synchronized (this) {
            if (dataSource == null) {
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(url);
                config.setUsername(username);
                config.setPassword(password);
                config.setDriverClassName(driverClassName);
                config.setSchema(schema);
                config.setPoolName(PluginInfo.PLUGIN_ID + "-pool");
                config.setMaximumPoolSize(MAX_POOL_SIZE);
            dataSource = new HikariDataSource(config);
            }
            return dataSource;
        }
    }

    /** The schema this plugin owns, as decided by HUB. */
    public String schema() {
        return schema;
    }

    /** Closes the pool. Call from {@code onDeactivate()}. */
    public void close() {
        HikariDataSource open = dataSource;
        if (open != null) {
            open.close();
            dataSource = null;
        }
    }

    private static String require(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(CREDENTIALS_FILE + " has no '" + key + "' entry.");
        }
        return value;
    }
}
