package ${package};

import com.github.dsquare68.homeforgeapi.dashboard.WidgetDescriptor;
import com.github.dsquare68.homeforgeapi.db.PluginDbConnection;
import com.github.dsquare68.homeforgeapi.spi.HubApi;
import com.github.dsquare68.homeforgeapi.spi.HubPlugin;
import com.github.dsquare68.homeforgeapi.spi.PluginIcon;
import com.github.dsquare68.homeforgeapi.spi.PluginMetadata;
import ${package}.view.MainView;

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
 * <p>It is also HUB's handle on this plugin: HUB resolves it with
 * {@code getExtensions(HubPlugin.class)} and reads identity through
 * {@link #getMetadata()} and the database through {@link #db()}. Anything HUB
 * should be able to reach belongs on the SPI, not on a class of your own.
 *
 * <p>Plugin identity (id, name, version, description, path, schema) lives in
 * {@link PluginInfo} and must stay in sync with the {@code plugin.*} properties
 * in {@code pom.xml}.
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
     *
     * <p>Overriding is optional: the default reads the same values back out of the
     * jar manifest. It is spelled out here so your identity is visible in code -
     * edit {@link PluginInfo}, and keep it in sync with {@code pom.xml}.
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
     * The image HUB shows next to this plugin in the sidebar and the plugin
     * manager, read from {@link PluginInfo#PLUGIN_ICON} inside the jar.
     *
     * <p>{@code src/main/resources/icon.png} ships empty, so until you replace it
     * with a real PNG this returns {@code null} and HUB draws its placeholder -
     * an empty file counts as no icon.
     *
     * <p>Overriding is optional: the default does exactly this with the same
     * conventional path. It is spelled out here so the icon is visible in code -
     * delete the method, or point {@link PluginInfo#PLUGIN_ICON} elsewhere.
     */
    @Override
    public byte[] getIconBytes() {
        return PluginIcon.load(HubPluginImpl.class, PluginInfo.PLUGIN_ICON);
    }

    /**
     * Called once on first install.
     *
     * <p>Run Flyway migrations here so the plugin's tables exist before
     * any user interacts with the plugin. They run against this plugin's own
     * PostgreSQL role - see {@link #db()}.
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
     *
     * <p>The connection pool behind {@link #db()} is not yours to close: HUB owns
     * it and shuts it down after this method returns.
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
        // Nothing to do: HUB drops this plugin's role and schema when it is
        // removed, along with the credentials file inside the jar and the
        // connection pool behind db().
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    /**
     * Run Flyway migrations scoped to {@link PluginInfo#PLUGIN_SCHEMA}.
     *
     * <p>Migration scripts live in
     * {@code src/main/resources/db/migration/} and must follow the naming
     * convention {@code V<version>__<description>.sql}. The folder starts
     * empty - with no scripts this is a no-op.
     */
    private void runMigrations() {
        PluginDbConnection db = db();

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
