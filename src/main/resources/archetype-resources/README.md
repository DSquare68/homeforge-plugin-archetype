# ${pluginName}

A [HUB](https://github.com/homeforge/hub) plugin generated from
`homeforge-plugin-archetype`: PF4J entry point, HUB lifecycle class, a Vaadin
view, a dashboard widget, and a Flyway migration. It compiles and packages
as-is.

Build config — dependencies, pinned versions, jar/shade setup — is inherited
from `com.github.dsquare68:homeforge-plugin-starter-parent`. Install that once
before building:

```bash
mvn -f ../homeforge-plugin-starter-parent install
```

## Plugin identity

| Property | Value |
|----------|-------|
| Path     | `/${pluginPath}` |
| Schema   | `${pluginSchema}` |
| Id       | `${pluginId}` |

These live in `pom.xml` (`plugin.path` / `plugin.schema`) and
`${package}.PluginInfo` — keep both in sync if you change them.

## Layout

```
${artifactId}/
├── pom.xml                                 <- plugin.path & plugin.schema here
└── src/main/
    ├── java/...
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

`MainView` is the Vaadin view served at `/${pluginPath}`. Add sub-views and
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

Produces `target/${artifactId}-${version}.jar`. In HUB:
**Settings → Plugins → Upload Plugin → choose the jar → Install**.
