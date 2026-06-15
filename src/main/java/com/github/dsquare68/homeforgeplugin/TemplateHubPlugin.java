package com.github.dsquare68.homeforgeplugin;

import com.github.dsquare68.homeforgeapi.dashboard.WidgetDescriptor;
import com.github.dsquare68.homeforgeapi.spi.HubApi;
import com.github.dsquare68.homeforgeapi.spi.HubPlugin;
import com.github.dsquare68.homeforgeapi.spi.PluginMetadata;
import com.github.dsquare68.homeforgeplugin.view.TemplateDashboardWidget;
import com.github.dsquare68.homeforgeplugin.view.TemplateMainView;
import com.vaadin.flow.router.RouteConfiguration;

import org.flywaydb.core.Flyway;
import org.pf4j.Extension;

/**
 * HUB lifecycle implementation for this plugin.
 *
 * <p>This is the class you spend most time in.  It:
 * <ul>
 *   <li>Declares plugin metadata (id, name, <b>path</b>, <b>schema</b>)</li>
 *   <li>Runs Flyway migrations against the plugin-scoped schema on install</li>
 *   <li>Registers Vaadin routes so the UI becomes reachable at {@code /my-plugin}</li>
 *   <li>Optionally contributes a dashboard widget</li>
 * </ul>
 *
 * <h2>How path and schema flow into HUB</h2>
 * <pre>
 * pom.xml
 *   &lt;plugin.path&gt;/my-plugin&lt;/plugin.path&gt;
 *   &lt;plugin.schema&gt;my_plugin_schema&lt;/plugin.schema&gt;
 *         │                       │
 *         ▼                       ▼
 *   MANIFEST.MF              MANIFEST.MF
 *   Hub-Path=/my-plugin      Hub-Schema=my_plugin_schema
 *         │                       │
 *         ▼                       ▼
 *   PluginMetadata#path      PluginMetadata#schema
 *         │                       │
 *         ▼                       ▼
 *   Vaadin route registered  Flyway migrates
 *   at /my-plugin            schema my_plugin_schema
 *   Sidebar entry added
 * </pre>
 *
 * <p><b>Plugin authors:</b> edit the constants at the top of this class to
 * match your {@code pom.xml} properties.
 */
@Extension
public class TemplateHubPlugin implements HubPlugin {

    // -----------------------------------------------------------------------
    // Plugin identity – keep in sync with pom.xml properties
    // -----------------------------------------------------------------------

    /** Must match {@code <plugin.path>} in pom.xml. */
    private static final String PLUGIN_PATH   = "/my-plugin";

    /** Must match {@code <plugin.schema>} in pom.xml. */
    private static final String PLUGIN_SCHEMA = "my_plugin_schema";

    /** Stable snake_case identifier – also used as the Flyway schema history table prefix. */
    private static final String PLUGIN_ID     = "my_plugin";

    private static final String PLUGIN_NAME    = "My Plugin";
    private static final String PLUGIN_VERSION = "1.0.0";
    private static final String PLUGIN_DESC    = "A blank HUB plugin – replace with your description.";

    /** Vaadin Lumo icon name shown in the sidebar. See https://vaadin.com/docs/latest/components/icons */
    private static final String PLUGIN_ICON   = "vaadin:puzzle-piece";

    // -----------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------

    private HubApi api;

    // -----------------------------------------------------------------------
    // HubPlugin SPI
    // -----------------------------------------------------------------------

    @Override
    public PluginMetadata getMetadata() {
        return new PluginMetadata(
                PLUGIN_ID,
                PLUGIN_NAME,
                PLUGIN_VERSION,
                PLUGIN_DESC,
                PLUGIN_PATH,    // ← tells HUB which URL path this plugin owns
                PLUGIN_SCHEMA,  // ← tells HUB which PostgreSQL schema to create
                PLUGIN_ICON
        );
    }

    /**
     * Called once on first install.
     *
     * <p>Run Flyway migrations here so the plugin's tables exist before
     * any user interacts with the plugin.
     */
    @Override
    public void onInstall(HubApi api) {
        runMigrations(api);
    }

    /**
     * Called every time the plugin is enabled (including after HUB restarts).
     *
     * <p>Re-register dashboard widgets, set up scheduled tasks, etc.
     */
    @Override
    public void onActivate(HubApi api) {
        this.api = api;

        // Register a dashboard widget (optional – delete if not needed)
        api.dashboard().registerWidget(
                WidgetDescriptor.builder()
                        .id(PLUGIN_ID + ".summary")
                        .title(PLUGIN_NAME)
                        .order(50)
                        .build()
        );
    }

    /**
     * Register Vaadin routes so the UI is reachable at {@value PLUGIN_PATH}.
     *
     * <p>HUB calls this after {@link #onActivate(HubApi)} and stores the
     * returned registrations so it can remove them on deactivation.
     */
    @Override
    public void registerRoutes(String routes) {
        // Primary view – accessible at PLUGIN_PATH, e.g. /my-plugin
        //routes.setRoute(
        //        stripLeadingSlash(PLUGIN_PATH),
        //        TemplateMainView.class
       // );

        // Add more sub-routes here:
        // routes.setRoute(stripLeadingSlash(PLUGIN_PATH) + "/settings", TemplateSettingsView.class);
    }

    /**
     * Called when the plugin is disabled.  Remove in-memory resources.
     * Do NOT drop database tables here – use {@link #onUninstall()} for that.
     */
    @Override
    public void onDeactivate() {
        api.dashboard().unregisterWidget(PLUGIN_ID + ".summary");
        this.api = null;
    }

    /**
     * Called when the plugin is permanently removed.
     * Optionally drop the plugin schema here.
     */
    @Override
    public void onUninstall() {
        // Uncomment to drop the schema on uninstall:
        // try (var conn = api.storage().dataSource().getConnection();
        //      var stmt = conn.createStatement()) {
        //     stmt.execute("DROP SCHEMA IF EXISTS " + PLUGIN_SCHEMA + " CASCADE");
        // } catch (Exception e) {
        //     throw new RuntimeException("Failed to drop plugin schema", e);
        // }
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    /**
     * Run Flyway migrations scoped to {@value PLUGIN_SCHEMA}.
     *
     * <p>Migration scripts live in
     * {@code src/main/resources/db/migration/} and must follow the naming
     * convention {@code V<version>__<description>.sql}, e.g.
     * {@code V1__create_my_plugin_tables.sql}.
     */
    private void runMigrations(HubApi api) {
        Flyway flyway = Flyway.configure()
                .dataSource(api.storage().dataSource())
                // Isolate history table inside the plugin schema
                .table(PLUGIN_SCHEMA + "_flyway_schema_history")
                // All migration scripts under db/migration/ in the plugin jar
                .locations("classpath:db/migration")
                // Create schema if it does not exist yet
                .schemas(PLUGIN_SCHEMA)
                .createSchemas(true)
                .load();

        flyway.migrate();
    }

    /** {@code "/my-plugin"} → {@code "my-plugin"} (Vaadin route format). */
    private static String stripLeadingSlash(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
