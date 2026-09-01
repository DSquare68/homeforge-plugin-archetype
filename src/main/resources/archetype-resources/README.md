# ${pluginName}

A [HUB](https://github.com/homeforge/hub) plugin generated from
`homeforge-plugin-archetype`: PF4J entry point, HUB lifecycle class, a Vaadin
view, a dashboard widget, and Flyway wired up for your own migrations. It
compiles and packages as-is.

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

These live in `pom.xml` (the `plugin.*` properties) and in
`${package}.PluginInfo` — keep both in sync if you change them.

The pom values are written into `MANIFEST.MF` at build time, and that is where
HUB reads them from; `PluginInfo` is what *your* code uses, and what
`HubPluginImpl#getMetadata()` hands back. That override is optional — delete it
and the manifest values are used instead.

## Layout

```
${artifactId}/
├── pom.xml                                 <- plugin.path & plugin.schema here
└── src/main/
    ├── java/...
    │   ├── PluginBootstrap.java             <- PF4J entry point
    │   ├── HubPluginImpl.java               <- HUB lifecycle (path, schema, routes)
    │   ├── ExampleController.java           <- REST API example
    │   ├── PluginInfo.java                  <- plugin identity constants
    │   └── view/
    │       ├── MainView.java                <- served at plugin.path
    │       └── DashboardWidget.java         <- dashboard card
    └── resources/
        ├── icon.png                          <- plugin icon (empty placeholder)
        ├── db/migration/                    <- put Flyway migrations here (empty)
        └── META-INF/
            └── extensions.idx               <- PF4J extension index
```

## Set your plugin icon

`src/main/resources/icon.png` is the image HUB shows in the sidebar and the
plugin manager. It ships as an empty file — replace it with a real PNG and it is
picked up on the next build, no wiring needed. While it is empty HUB draws its
own placeholder instead.

The path is `${package}.PluginInfo.PLUGIN_ICON`, read by
`HubPluginImpl#getIconBytes()`. Keep another name or another folder if you
prefer — point the constant at it:

```java
public static final String PLUGIN_ICON = "icons/my-plugin.png";
```

## Write your database migrations

The folder ships empty — no schema is assumed. Users and their accounts belong
to HUB, so reference them by id instead of creating your own user tables.

Add SQL files to `src/main/resources/db/migration/`:

```
V1__init.sql
V2__add_column.sql
...
```

Flyway runs them in order on first install, scoped to your schema, on this
plugin's own PostgreSQL role — `db()`, inherited from `HubPlugin`. With no
scripts present it is a no-op, so you can ignore it until you need a table.

## Talk to your database

`db()` is available anywhere in `HubPluginImpl`, no setup and nothing to close:

```java
DataSource ds = db().dataSource();
```

It is a connection pool on a role that owns `${pluginSchema}` and has no grants
anywhere else, so it can only ever touch your own data. HUB reaches the same
pool through the same method, which is how it closes it when your plugin stops.

## Build your UI

`MainView` is the Vaadin view served at `/${pluginPath}`. Add sub-views and
contribute their routes from `HubPluginImpl#routes()` — HUB reads that list
and registers/removes the routes itself; you never call Vaadin's
`RouteConfiguration` directly.

## Build your REST API

`ExampleController` shows the pattern: a plain class with normal Spring MVC
annotations (`@GetMapping`, `@PostMapping`, ...), contributed from
`HubPluginImpl#restControllers()`. Whatever path you declare, HUB always
serves it at `/api/plugins/${pluginId}/...` and requires an authenticated
HUB session — you never configure routing prefixes or security yourself.
The one thing that's different from an ordinary Spring Boot controller:
nothing is `@Autowired` here, since your plugin has no Spring context of its
own — pass in `HubApi` (and anything else the controller needs) through the
constructor, the same way `HubPluginImpl` does.

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
