# HUB Plugin Template

Blank starter for a [HUB](https://github.com/homeforge/hub) plugin.

## Quick start

### 1. Configure your plugin identity

Edit **`pom.xml`** — two properties drive everything:

```xml
<!-- URL path this plugin owns, e.g. /gym → https://hub.local/gym -->
<plugin.path>/my-plugin</plugin.path>

<!-- PostgreSQL schema for this plugin's tables -->
<plugin.schema>my_plugin_schema</plugin.schema>
```

Both values are written into `MANIFEST.MF` at build time so HUB reads them
before the plugin class is even loaded.

### 2. Rename the classes

| File | Rename to |
|------|-----------|
| `TemplatePlugin.java` | `GymPlugin.java` |
| `TemplateHubPlugin.java` | `GymHubPlugin.java` |
| `TemplateMainView.java` | `GymMainView.java` |
| `TemplateDashboardWidget.java` | `GymDashboardWidget.java` |

Update the constants at the top of `TemplateHubPlugin`:

```java
private static final String PLUGIN_PATH   = "/gym";          // matches pom.xml
private static final String PLUGIN_SCHEMA = "gym_schema";    // matches pom.xml
private static final String PLUGIN_ID     = "gym_tracker";
private static final String PLUGIN_NAME   = "Gym Tracker";
private static final String PLUGIN_VERSION = "1.0.0";
private static final String PLUGIN_ICON   = "vaadin:trophy"; // Lumo icon name
```

### 3. Write your database migrations

Add SQL files to `src/main/resources/db/migration/`:

```
V1__init.sql          ← already provided, edit the example table
V2__add_column.sql
…
```

Flyway runs them in order on first install, scoped to your schema.

### 4. Build your UI

`TemplateMainView` is the Vaadin view served at your `plugin.path`.  
Add sub-views and register their routes in `TemplateHubPlugin#registerRoutes`:

```java
routes.setRoute("gym",          GymMainView.class);
routes.setRoute("gym/history",  GymHistoryView.class);
routes.setRoute("gym/settings", GymSettingsView.class);
```

### 5. Use platform APIs

Inside any lifecycle callback or view you can call HUB APIs through the
`HubApi` instance stored during `onActivate`:

```java
// Who is logged in?
HubUser me = api.user().currentUser();

// Send a notification
api.notifications().notifyUser(me.id(), "PR set! 💪", Severity.INFO);

// Persist an entity
api.storage().save(new ExerciseSet(...));
```

### 6. Build & install

```bash
mvn clean package
# → target/my-plugin-1.0.0-SNAPSHOT.jar

# In HUB: Settings → Plugins → Upload Plugin → choose the jar → Install
```

---

## File layout

```
hub-plugin-template/
├── pom.xml                                         ← plugin.path & plugin.schema here
└── src/main/
    ├── java/dev/homeforge/plugin/template/
    │   ├── TemplatePlugin.java                     ← PF4J entry point
    │   ├── TemplateHubPlugin.java                  ← HUB lifecycle (path, schema, routes)
    │   ├── view/
    │   │   ├── TemplateMainView.java               ← served at plugin.path
    │   │   └── TemplateDashboardWidget.java        ← dashboard card
    │   ├── service/                                ← your business logic
    │   └── entity/                                 ← your JPA entities / records
    └── resources/
        ├── db/migration/
        │   └── V1__init.sql                        ← Flyway migration
        └── META-INF/
            └── extensions.idx                      ← PF4J extension index
```

## How path & schema are wired

```
pom.xml                         maven-jar-plugin
<plugin.path>/gym</plugin.path> ──────────────────► Hub-Path: /gym  (MANIFEST.MF)
<plugin.schema>gym_schema</…>  ──────────────────► Hub-Schema: gym_schema

                ▼ HUB reads MANIFEST at install time ▼

PluginMetadata.path = "/gym"          → sidebar entry + Vaadin route
PluginMetadata.schema = "gym_schema"  → Flyway creates schema, scopes DataSource
```
