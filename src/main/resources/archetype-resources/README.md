${symbol_pound} ${pluginName}

${pluginDescription}

A [HUB](https://github.com/homeforge/hub) plugin generated from the
`homeforge-plugin-archetype`.

${symbol_pound}${symbol_pound} Plugin identity

| Property | Value |
|----------|-------|
| Path     | `${pluginPath}` |
| Schema   | `${pluginSchema}` |
| Id       | `${pluginId}` |

These live in `pom.xml` (`plugin.path` / `plugin.schema`) and
`${package}.PluginInfo` - keep both in sync if you change them later.

${symbol_pound}${symbol_pound} Project layout

```
${artifactId}/
├── pom.xml                                 <- plugin.path & plugin.schema here
└── src/main/
    ├── java/.../${package}/
    │   ├── PluginBootstrap.java             <- PF4J entry point
    │   ├── HubPluginImpl.java               <- HUB lifecycle (path, schema, routes)
    │   ├── PluginInfo.java                  <- plugin identity constants
    │   ├── view/
    │   │   ├── MainView.java                <- served at plugin.path
    │   │   └── DashboardWidget.java         <- dashboard card
    │   ├── service/                         <- your business logic
    │   └── entity/                          <- your JPA entities / records
    └── resources/
        ├── db/migration/
        │   └── V1__init.sql                 <- Flyway migration
        └── META-INF/
            └── extensions.idx                <- PF4J extension index
```

${symbol_pound}${symbol_pound} Write your database migrations

Add SQL files to `src/main/resources/db/migration/`:

```
V1__init.sql          <- already provided, edit the example table
V2__add_column.sql
...
```

Flyway runs them in order on first install, scoped to your schema.

${symbol_pound}${symbol_pound} Build your UI

`MainView` is the Vaadin view served at your `plugin.path`. Add sub-views and
register their routes in `HubPluginImpl${symbol_pound}registerRoutes`.

${symbol_pound}${symbol_pound} Use platform APIs

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

${symbol_pound}${symbol_pound} Build & install

```bash
mvn clean package
${symbol_pound} -> target/${artifactId}-${version}.jar

${symbol_pound} In HUB: Settings -> Plugins -> Upload Plugin -> choose the jar -> Install
```
