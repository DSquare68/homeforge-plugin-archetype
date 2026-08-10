package ${package};

/**
 * Central plugin identity constants - keep in sync with the {@code plugin.*}
 * properties in {@code pom.xml}.
 *
 * <p>These are for your own code: route paths, widget ids, log lines. HUB never
 * reads this class - it goes through {@code HubPluginImpl#getMetadata()}, which
 * is where these values are handed over. The same values also reach HUB from the
 * jar manifest, so deleting this class and dropping the {@code getMetadata()}
 * override would still leave the plugin working; it exists so your identity is
 * visible in one place instead of only in the pom.
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

    /**
     * Classpath location of the sidebar/plugin-manager icon, relative to
     * {@code src/main/resources/}. The file ships empty - replace it with a real
     * PNG, or point this at another one and keep the name you prefer.
     */
    public static final String PLUGIN_ICON = "icon.png";

    private PluginInfo() {
    }
}
