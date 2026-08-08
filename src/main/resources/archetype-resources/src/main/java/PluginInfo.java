package ${package};

/**
 * Central plugin identity constants - keep in sync with the
 * {@code plugin.path} / {@code plugin.schema} properties in {@code pom.xml}.
 */
public final class PluginInfo {

    /** Must match {@code <plugin.path>} in pom.xml. */
    public static final String PLUGIN_PATH = "/${pluginPath}";

    /** Must match {@code <plugin.schema>} in pom.xml. */
    public static final String PLUGIN_SCHEMA = "${pluginSchema}";

    /** Stable snake_case identifier - also used as the Flyway schema history table prefix. */
    public static final String PLUGIN_ID = "${pluginId}";

    public static final String PLUGIN_NAME = "${pluginName}";

    public static final String PLUGIN_VERSION = "${version}";

    public static final String PLUGIN_DESC = "${pluginDescription}";

    public static final String TITLE = "${pluginName}";

    private PluginInfo() {
    }
}
