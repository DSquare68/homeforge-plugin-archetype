#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import com.github.dsquare68.homeforgeapi.dashboard.WidgetDescriptor;
import com.github.dsquare68.homeforgeapi.spi.HubApi;
import com.github.dsquare68.homeforgeapi.spi.HubPlugin;
import com.github.dsquare68.homeforgeapi.spi.PluginMetadata;
import ${package}.view.DashboardWidget;
import ${package}.view.MainView;
import com.vaadin.flow.router.RouteConfiguration;

import org.flywaydb.core.Flyway;
import org.pf4j.Extension;

/**
 * HUB lifecycle implementation for this plugin.
 *
 * <p>This is the class you spend most time in. It:
 * <ul>
 *   <li>Declares plugin metadata (id, name, <b>path</b>, <b>schema</b>)</li>
 *   <li>Runs Flyway migrations against the plugin-scoped schema on install</li>
 *   <li>Registers Vaadin routes so the UI becomes reachable at {@link PluginInfo${symbol_pound}PLUGIN_PATH}</li>
 *   <li>Optionally contributes a dashboard widget</li>
 * </ul>
 *
 * <p>Plugin identity (id, name, version, description, path, schema) lives in
 * {@link PluginInfo} and must stay in sync with the {@code plugin.path} /
 * {@code plugin.schema} properties in {@code pom.xml}.
 */
@Extension
public class HubPluginImpl implements HubPlugin {

    // -----------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------

    private HubApi api;

    // -----------------------------------------------------------------------
    // HubPlugin SPI
    // -----------------------------------------------------------------------

    /**
     * Returns the plugin's immutable descriptor. HUB reads this once at install time to:
     * <ol>
     *   <li>Register the plugin in the {@code hub_schema.plugins} table</li>
     *   <li>Add a sidebar navigation entry (icon + name linking to {@code path})</li>
     *   <li>Create the PostgreSQL schema named {@code schema} (if not already present)</li>
     * </ol>
     */
    @Override
    public PluginMetadata getMetadata() {
        return new PluginMetadata(
                PluginInfo.PLUGIN_ID,     // stable snake_case key
                PluginInfo.PLUGIN_NAME,   // sidebar label
                PluginInfo.PLUGIN_VERSION,
                PluginInfo.PLUGIN_DESC,
                PluginInfo.PLUGIN_PATH,   // URL path this plugin owns
                PluginInfo.PLUGIN_SCHEMA  // dedicated PostgreSQL schema
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

        // Register a dashboard widget (optional - delete if not needed)
        api.dashboard().registerWidget(
                WidgetDescriptor.builder()
                        .id(PluginInfo.PLUGIN_ID + ".summary")
                        .title(PluginInfo.PLUGIN_NAME)
                        .order(50)
                        .build()
        );
    }

    /**
     * Register Vaadin routes so the UI is reachable at {@link PluginInfo${symbol_pound}PLUGIN_PATH}.
     *
     * <p>HUB calls this after {@link ${symbol_pound}onActivate(HubApi)} and stores the
     * returned registrations so it can remove them on deactivation.
     */
    @Override
    public void registerRoutes(String routes) {
        // Primary view - accessible at PluginInfo.PLUGIN_PATH
        //routes.setRoute(
        //        stripLeadingSlash(PluginInfo.PLUGIN_PATH),
        //        MainView.class
        //);

        // Add more sub-routes here:
        // routes.setRoute(stripLeadingSlash(PluginInfo.PLUGIN_PATH) + "/settings", SettingsView.class);
    }

    /**
     * Called when the plugin is disabled. Remove in-memory resources.
     * Do NOT drop database tables here - use {@link ${symbol_pound}onUninstall()} for that.
     */
    @Override
    public void onDeactivate() {
        api.dashboard().unregisterWidget(PluginInfo.PLUGIN_ID + ".summary");
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
        //     stmt.execute("DROP SCHEMA IF EXISTS " + PluginInfo.PLUGIN_SCHEMA + " CASCADE");
        // } catch (Exception e) {
        //     throw new RuntimeException("Failed to drop plugin schema", e);
        // }
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    /**
     * Run Flyway migrations scoped to {@link PluginInfo${symbol_pound}PLUGIN_SCHEMA}.
     *
     * <p>Migration scripts live in
     * {@code src/main/resources/db/migration/} and must follow the naming
     * convention {@code V<version>__<description>.sql}.
     */
    private void runMigrations(HubApi api) {
        Flyway flyway = Flyway.configure()
                .dataSource(api.storage().dataSource())
                // Isolate history table inside the plugin schema
                .table(PluginInfo.PLUGIN_SCHEMA + "_flyway_schema_history")
                // All migration scripts under db/migration/ in the plugin jar
                .locations("classpath:db/migration")
                // Create schema if it does not exist yet
                .schemas(PluginInfo.PLUGIN_SCHEMA)
                .createSchemas(true)
                .load();

        flyway.migrate();
    }

    /** {@code "/my-plugin"} -&gt; {@code "my-plugin"} (Vaadin route format). */
    private static String stripLeadingSlash(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
