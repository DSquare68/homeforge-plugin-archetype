package com.github.dsquare68.template;

/**
 * Central plugin identity constants - keep in sync with the
 * {@code plugin.path} / {@code plugin.schema} properties in {@code pom.xml}.
 */
public final class PluginInfo {

    /** Must match {@code <plugin.path>} in pom.xml. */
    public static final String PLUGIN_PATH = "/template";

    /** Must match {@code <plugin.schema>} in pom.xml. */
    public static final String PLUGIN_SCHEMA = "template_schema";

    /** Stable snake_case identifier - also used as the Flyway schema history table prefix. */
    public static final String PLUGIN_ID = "template";

    public static final String PLUGIN_NAME = "Plugin Template";

    public static final String PLUGIN_VERSION = "0.0.1-SNAPSHOT";

    public static final String PLUGIN_DESC = "A starter HUB plugin - copy this module and make it your own.";

    public static final String TITLE = "Plugin Template";

    private PluginInfo() {
    }
}
