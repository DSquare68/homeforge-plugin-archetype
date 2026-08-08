# Homeforge Plugin Archetype

Maven archetype that scaffolds a new [HUB](https://github.com/homeforge/hub)
plugin. It is the companion of
[`homeforge-plugin-starter-parent`](../homeforge-plugin-starter-parent), which
lives in its own repository:

| Repository | Artifact | Packaging | What it is |
|------------|----------|-----------|------------|
| `homeforge-plugin-starter-parent` | `homeforge-plugin-starter-parent` | `pom` | Dependencies, pinned versions and jar/shade build config that every plugin inherits |
| *this one* | `homeforge-plugin-archetype` | `maven-archetype` | Generates a compilable starter plugin whose POM inherits that parent |

## Install

```bash
mvn install
```

Installs `com.github.dsquare68:homeforge-plugin-archetype:0.0.1-SNAPSHOT` into
your local repository. The starter parent must be installed too, or generated
projects will not resolve their parent:

```bash
mvn -f ../homeforge-plugin-starter-parent install
```

## Generate a plugin

Interactive — Maven prompts for every property:

```bash
mvn archetype:generate -DarchetypeGroupId=com.github.dsquare68 -DarchetypeArtifactId=homeforge-plugin-archetype -DarchetypeVersion=0.0.1-SNAPSHOT
```

Non-interactive:

```bash
mvn archetype:generate -B -DarchetypeGroupId=com.github.dsquare68 -DarchetypeArtifactId=homeforge-plugin-archetype -DarchetypeVersion=0.0.1-SNAPSHOT -DgroupId=com.github.dsquare68 -DartifactId=gym -Dversion=0.0.1-SNAPSHOT -Dpackage=com.github.dsquare68.gym -DpluginPath=gym -DpluginSchema=gym_schema -DpluginId=gym -DpluginName="Gym Tracker" -DpluginDescription="Track workouts, personal records and progress charts."
```

Then:

```bash
cd gym && mvn clean package
```

### Properties

| Property | Description | Default |
|----------|-------------|---------|
| `groupId`, `artifactId`, `version`, `package` | Standard Maven coordinates | — |
| `pluginPath` | URL path segment served by HUB, **without** a leading slash — `gym` becomes `/gym` | `${artifactId}` |
| `pluginSchema` | PostgreSQL schema for this plugin's tables | `${artifactId}_schema` |
| `pluginId` | Stable snake_case id — `Plugin-Id` in the manifest and the name of the credentials file HUB writes into the jar | `${artifactId}` |
| `pluginName` | Sidebar label and dashboard card title | `${artifactId}` |
| `pluginDescription` | Shown in HUB's plugin list | `A HUB plugin.` |
| `starterParentVersion` | Version of the starter parent to inherit | `0.0.1-SNAPSHOT` |

`pluginPath`, `pluginSchema` and `pluginId` are written into both `pom.xml`
(`plugin.path` / `plugin.schema`) and `PluginInfo.java`, so a generated project
starts out consistent — keep them in step if you change them later.

## What gets generated

```
<artifactId>/
├── pom.xml                        <- inherits homeforge-plugin-starter-parent
├── README.md
├── .gitignore
└── src/main/
    ├── java/<package>/
    │   ├── PluginBootstrap.java    <- PF4J entry point (Plugin-Class)
    │   ├── HubPluginImpl.java      <- HUB lifecycle: metadata, migrations, routes
    │   ├── PluginInfo.java         <- identity constants
    │   ├── PluginDb.java           <- the plugin's own PostgreSQL pool
    │   └── view/
    │       ├── MainView.java       <- Vaadin view served at plugin.path
    │       └── DashboardWidget.java
    └── resources/
        ├── db/migration/V1__init.sql
        └── META-INF/extensions.idx <- PF4J extension index
```

The generated POM declares nothing but identity and plugin coordinates — see
the starter parent's README for the dependencies, pinned versions and manifest
entries it supplies.

## How path and schema reach HUB

The parent's `maven-jar-plugin` config writes them into `MANIFEST.MF` at build
time, so HUB reads them before the plugin class is even loaded:

```
pom.xml                             maven-jar-plugin
<plugin.path>/gym</plugin.path>     ──────────────────►  Hub-Path: /gym  (MANIFEST.MF)
<plugin.schema>gym_schema</...>     ──────────────────►  Hub-Schema: gym_schema

                ▼ HUB reads MANIFEST at install time ▼

PluginMetadata.path = "/gym"          → sidebar entry + Vaadin route
PluginMetadata.schema = "gym_schema"  → Flyway creates schema, scopes DataSource
```

## Working on the archetype

Everything under `src/main/resources/archetype-resources/` is a Velocity
template: `${package}`, `${artifactId}`, `${version}` and the properties above
are substituted at generation time. `src/main/resources/META-INF/maven/archetype-metadata.xml`
declares which properties are prompted for and which files are copied.

Verify a change end to end:

```bash
mvn clean install && mvn archetype:generate -B -DarchetypeGroupId=com.github.dsquare68 -DarchetypeArtifactId=homeforge-plugin-archetype -DarchetypeVersion=0.0.1-SNAPSHOT -DgroupId=com.example -DartifactId=smoke -Dversion=0.0.1-SNAPSHOT -Dpackage=com.example.smoke -DoutputDirectory=target/it && (cd target/it/smoke && mvn -o clean package)
```
