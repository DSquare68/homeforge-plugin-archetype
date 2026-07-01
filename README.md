# HUB Plugin Archetype

A Maven archetype that scaffolds a new [HUB](https://github.com/homeforge/hub)
plugin project (PF4J entry point, HUB lifecycle class, a Vaadin view, a
dashboard widget, and a Flyway migration) - ready to build with `mvn package`.

## Install the archetype locally

```bash
mvn install
```

This installs `com.github.dsquare68:homeforge-plugin-archetype:0.0.1-SNAPSHOT`
into your local repository.

## Generate a new plugin project

```bash
mvn archetype:generate \
    -DarchetypeGroupId=com.github.dsquare68 \
    -DarchetypeArtifactId=homeforge-plugin-archetype \
    -DarchetypeVersion=0.0.1-SNAPSHOT \
    -DgroupId=com.github.dsquare68 \
    -DartifactId=gym-plugin \
    -Dversion=1.0.0-SNAPSHOT \
    -Dpackage=com.github.dsquare68.gymplugin \
    -DpluginPath=/gym \
    -DpluginSchema=gym_schema \
    -DpluginId=gym_tracker \
    -DpluginName="Gym Tracker" \
    -DpluginVersion=1.0.0 \
    -DpluginDescription="Track workouts, personal records and progress charts."
```

Omit any of the `plugin*` properties to be prompted for them interactively
(or fall back to their defaults) when running in interactive mode:

```bash
mvn archetype:generate \
    -DarchetypeGroupId=com.github.dsquare68 \
    -DarchetypeArtifactId=homeforge-plugin-archetype \
    -DarchetypeVersion=0.0.1-SNAPSHOT
```

### Archetype properties

| Property | Description | Default |
|----------|--------------|---------|
| `groupId` | Maven group id of the generated plugin | - |
| `artifactId` | Maven artifact id of the generated plugin | - |
| `version` | Maven version of the generated plugin | - |
| `package` | Java package for the generated sources | - |
| `pluginPath` | URL path served by HUB, e.g. `/gym` | `/my-plugin` |
| `pluginSchema` | PostgreSQL schema for this plugin's tables | `my_plugin_schema` |
| `pluginId` | Stable snake_case identifier | `my_plugin` |
| `pluginName` | Human-readable name shown in the sidebar | `My Plugin` |
| `pluginVersion` | Semantic version reported to HUB | `1.0.0` |
| `pluginDescription` | One-sentence description for the plugin manager | `A HUB plugin generated from the homeforge-plugin-archetype.` |

## What gets generated

```
<artifactId>/
├── pom.xml                                 <- plugin.path & plugin.schema here
└── src/main/
    ├── java/<package>/
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

Path and schema are written into `MANIFEST.MF` at build time so HUB reads
them before the plugin class is even loaded:

```
pom.xml                             maven-jar-plugin
<plugin.path>/gym</plugin.path>     ──────────────────►  Hub-Path: /gym  (MANIFEST.MF)
<plugin.schema>gym_schema</...>     ──────────────────►  Hub-Schema: gym_schema

                ▼ HUB reads MANIFEST at install time ▼

PluginMetadata.path = "/gym"          → sidebar entry + Vaadin route
PluginMetadata.schema = "gym_schema"  → Flyway creates schema, scopes DataSource
```

## Developing this archetype

The template lives under
[`src/main/resources/archetype-resources`](src/main/resources/archetype-resources),
and the custom properties it exposes are declared in
[`src/main/resources/META-INF/maven/archetype-metadata.xml`](src/main/resources/META-INF/maven/archetype-metadata.xml).

After changing the template, verify it end-to-end:

```bash
mvn install
mvn archetype:generate -DarchetypeGroupId=com.github.dsquare68 \
    -DarchetypeArtifactId=homeforge-plugin-archetype -DarchetypeVersion=0.0.1-SNAPSHOT \
    -DgroupId=com.example -DartifactId=demo-plugin -Dversion=1.0.0-SNAPSHOT \
    -Dpackage=com.example.demoplugin -DinteractiveMode=false
cd demo-plugin && mvn -o compile
```
