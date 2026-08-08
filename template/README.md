# Plugin Template

A starter [HUB](https://github.com/homeforge/hub) plugin: PF4J entry point, HUB
lifecycle class, a Vaadin view, a dashboard widget, and a Flyway migration.
It compiles and packages as-is — copy this directory to begin a new plugin.

Build config comes from [`homeforge-plugin-starter-parent`](../pom.xml); see the
[root README](../README.md) for what it provides and how to adapt this copy.

## Plugin identity

| Property | Value |
|----------|-------|
| Path     | `/template` |
| Schema   | `template_schema` |
| Id       | `template` |

These live in `pom.xml` (`plugin.path` / `plugin.schema`) and
`com.github.dsquare68.template.PluginInfo` — keep both in sync if you change
them.

## Layout

```
template/
├── pom.xml                                 <- plugin.path & plugin.schema here
└── src/main/
    ├── java/com/github/dsquare68/template/
    │   ├── PluginBootstrap.java             <- PF4J entry point
    │   ├── HubPluginImpl.java               <- HUB lifecycle (path, schema, routes)
    │   ├── PluginInfo.java                  <- plugin identity constants
    │   ├── PluginDb.java                    <- this plugin's own DB pool
    │   └── view/
    │       ├── MainView.java                <- served at plugin.path
    │       └── DashboardWidget.java         <- dashboard card
    └── resources/
        ├── db/migration/
        │   └── V1__init.sql                 <- Flyway migration
        └── META-INF/
            └── extensions.idx               <- PF4J extension index
```

## Write your database migrations

Add SQL files to `src/main/resources/db/migration/`:

```
V1__init.sql          <- already provided, edit the example table
V2__add_column.sql
...
```

Flyway runs them in order on first install, scoped to your schema, using this
plugin's own PostgreSQL role — see `PluginDb`.

## Build your UI

`MainView` is the Vaadin view served at your `plugin.path`. Add sub-views and
register their routes in `HubPluginImpl#registerRoutes`.

## Use platform APIs

Inside any lifecycle callback or view you can call HUB APIs through the
`HubApi` instance stored during `onActivate`:

```java
// Who is logged in?
HubUser me = api.user().currentUser();

// Send a notification
api.notifications().notifyUser(me.id(), "Hello!", Severity.INFO);

// Persist an entity
api.storage().save(new ExampleItem(...));
```

## Build & install

```bash
mvn clean package
```

Produces `target/homeforge-plugin-template-0.0.1-SNAPSHOT.jar`. In HUB:
**Settings → Plugins → Upload Plugin → choose the jar → Install**.
