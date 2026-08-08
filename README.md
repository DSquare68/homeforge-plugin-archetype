# Homeforge Plugin Starter Parent

Parent POM and starter template for [HUB](https://github.com/homeforge/hub)
plugins.

| Module | Artifact | Packaging | What it is |
|--------|----------|-----------|------------|
| *(root)* | `homeforge-plugin-starter-parent` | `pom` | Dependencies, versions and jar/shade build config that every plugin inherits |
| [`template/`](template) | `homeforge-plugin-template` | `jar` | A compilable starter plugin — copy it to begin a new one |

## Install the parent

```bash
mvn install
```

This installs `com.github.dsquare68:homeforge-plugin-starter-parent:0.0.1-SNAPSHOT`
into your local repository, so plugins in other checkouts can inherit from it.

## Use it in a plugin

A plugin POM only has to say who it is and where it lives — the parent supplies
the rest:

```xml
<parent>
    <groupId>com.github.dsquare68</groupId>
    <artifactId>homeforge-plugin-starter-parent</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</parent>

<artifactId>gym</artifactId>
<name>Gym Tracker</name>
<description>Track workouts, personal records and progress charts.</description>

<properties>
    <plugin.path>/gym</plugin.path>
    <plugin.schema>gym_schema</plugin.schema>
    <plugin.bootstrap.class>com.github.dsquare68.gym.PluginBootstrap</plugin.bootstrap.class>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
        </plugin>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-jar-plugin</artifactId>
        </plugin>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

Version and groupId are inherited, so a child normally declares neither.

### Properties a child sets

| Property | Description | Parent default |
|----------|-------------|----------------|
| `plugin.path` | URL path served by HUB, e.g. `/gym` | `/my-plugin` |
| `plugin.schema` | PostgreSQL schema for this plugin's tables | `my_plugin_schema` |
| `plugin.bootstrap.class` | Class extending `org.pf4j.Plugin`, written as `Plugin-Class` | `${project.groupId}.${project.artifactId}.PluginBootstrap` |

### What the parent already provides

**Dependencies** — inherited directly, nothing to declare:

| Dependency | Scope | Why |
|------------|-------|-----|
| `homeforge-api` | provided | The SPI your plugin implements |
| `pf4j` | provided | Plugin container, supplied by the host |
| `vaadin-core` | provided | UI, supplied by the host (version via `vaadin-bom`) |
| `postgresql`, `HikariCP` | provided | For the plugin's own pool (see `PluginDb`) |
| `flyway-core`, `flyway-database-postgresql` | compile | Migrations; **shaded into the jar** — the host does not provide these |
| `junit-jupiter` | test | |

**Build** — `maven-compiler-plugin`, `maven-jar-plugin` and
`maven-shade-plugin` are fully configured in `<pluginManagement>`; a child
declares them by coordinates only. Between them they set the Java release,
write the PF4J/HUB manifest entries, and shade Flyway in while keeping
`provided` libraries out.

**Pinned versions** — Java 21, Vaadin 25.1.7, PF4J 3.12.0, Flyway 13.2.0,
HikariCP 7.0.2, PostgreSQL 42.7.11. Keep these in step with the HUB host.

## How path and schema reach HUB

Path and schema are written into `MANIFEST.MF` at build time so HUB reads them
before the plugin class is even loaded:

```
pom.xml                             maven-jar-plugin
<plugin.path>/gym</plugin.path>     ──────────────────►  Hub-Path: /gym  (MANIFEST.MF)
<plugin.schema>gym_schema</...>     ──────────────────►  Hub-Schema: gym_schema

                ▼ HUB reads MANIFEST at install time ▼

PluginMetadata.path = "/gym"          → sidebar entry + Vaadin route
PluginMetadata.schema = "gym_schema"  → Flyway creates schema, scopes DataSource
```

## Starting a new plugin

Copy [`template/`](template) out of this repo, then:

1. Rename the directory and set `<artifactId>`, `<name>`, `<description>`.
2. Rename the Java package from `com.github.dsquare68.template` to your own.
3. Update `plugin.path`, `plugin.schema` and `plugin.bootstrap.class` in
   `pom.xml`, and the matching constants in `PluginInfo.java`.
4. Update `src/main/resources/META-INF/extensions.idx` to your
   `HubPluginImpl`'s fully qualified name.
5. Rewrite `db/migration/V1__init.sql` for your own tables.
6. Drop the `<relativePath>` from `<parent>` if the plugin lives outside this
   repo.

See [`template/README.md`](template/README.md) for what each generated file
does.

## Verifying a change to the parent

```bash
mvn -o clean install
```

Builds the parent and the template module, and confirms the template still
compiles, shades and produces the right manifest.
