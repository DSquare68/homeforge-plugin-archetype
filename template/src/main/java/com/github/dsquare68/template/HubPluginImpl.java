package com.github.dsquare68.template;

import com.github.dsquare68.homeforgeapi.dashboard.WidgetDescriptor;
import com.github.dsquare68.homeforgeapi.spi.HubApi;
import com.github.dsquare68.homeforgeapi.spi.HubPlugin;
import com.github.dsquare68.homeforgeapi.spi.PluginMetadata;
import com.github.dsquare68.template.view.MainView;

import org.flywaydb.core.Flyway;
import org.pf4j.Extension;

/**
 * HUB lifecycle implementation for this plugin.
 *
 * <p>This is the class you spend most time in. It:
 * <ul>
 *   <li>Declares plugin metadata (id, name, <b>path</b>, <b>schema</b>)</li>
 *   <li>Runs Flyway migrations against the plugin-scoped schema on install</li>
 *   <li>Registers Vaadin routes so the UI becomes reachable at {@link PluginInfo#PLUGIN_PATH}</li>
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

    /** This plugin's own database, from the properties file HUB wrote into the jar. */
    private PluginDb db;

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
     * any user interacts with the plugin. They run against this plugin's own
     * PostgreSQL role - see {@link PluginDb}.
     */
    @Override
    public void onInstall(HubApi api) {
        runMigrations();
    }

    /**
     * Called every time the plugin is enabled (including after HUB restarts).
     *
     * <p>Re-register dashboard widgets, set up scheduled tasks, etc.
     */
    @Override
    public void onActivate(HubApi api) {
        this.api = api;
        this.db = PluginDb.load();

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
     * Register Vaadin routes so the UI is reachable at {@link PluginInfo#PLUGIN_PATH}.
     *
     * <p>HUB calls this after {@link #onActivate(HubApi)} and stores the
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
     * Do NOT drop database tables here - use {@link #onUninstall()} for that.
     */
    @Override
    public void onDeactivate() {
        api.dashboard().unregisterWidget(PluginInfo.PLUGIN_ID + ".summary");
        if (db != null) {
            db.close();
            this.db = null;
        }
        this.api = null;
    }

    /**
     * Called when the plugin is permanently removed.
     * Optionally drop the plugin schema here.
     */
    @Override
    public void onUninstall() {
        // Nothing to do: HUB drops this plugin's role and schema when it is
        // removed, along with the credentials file inside the jar.
        if (db != null) {
            db.close();
            this.db = null;
        }
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    /**
     * Run Flyway migrations scoped to {@link PluginInfo#PLUGIN_SCHEMA}.
     *
     * <p>Migration scripts live in
     * {@code src/main/resources/db/migration/} and must follow the naming
     * convention {@code V<version>__<description>.sql}.
     */
    private void runMigrations() {
        if (db == null) {
            db = PluginDb.load();
        }

        // Configure with THIS plugin's classloader: Flyway defaults to the
        // thread context classloader, which under PF4J belongs to the HUB
        // host and cannot see db/migration inside the plugin jar.
        Flyway flyway = Flyway.configure(HubPluginImpl.class.getClassLoader())
                // This plugin's own role - not HUB's - so migrations can only
                // touch this plugin's schema.
                .dataSource(db.dataSource())
                // Isolate history table inside the plugin schema
                .table(db.schema() + "_flyway_schema_history")
                // All migration scripts under db/migration/ in the plugin jar
                .locations("classpath:db/migration")
                // HUB already created the schema; this keeps local runs working
                .schemas(db.schema())
                .createSchemas(true)
                .load();

        flyway.migrate();
    }

    /** {@code "/my-plugin"} -&gt; {@code "my-plugin"} (Vaadin route format). */
    private static String stripLeadingSlash(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
