package com.github.dsquare68.homeforgeplugin;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

/**
 * PF4J {@link Plugin} bootstrap class.
 *
 * <p>This class is the entry point declared in {@code MANIFEST.MF} under
 * {@code Plugin-Class}. Keep it thin – delegate all real work to
 * {@link TemplateHubPlugin} which implements the HUB lifecycle SPI.
 *
 * <p><b>Plugin authors:</b> rename this class and update {@code pom.xml}:
 * <pre>
 *   &lt;Plugin-Class&gt;dev.homeforge.plugin.myplugin.MyPlugin&lt;/Plugin-Class&gt;
 * </pre>
 */
public class TemplatePlugin extends Plugin {

    public TemplatePlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        // PF4J calls this when the plugin JAR is loaded.
        // Spring wiring happens inside TemplateHubPlugin#onActivate.
    }

    @Override
    public void stop() {
        // PF4J calls this when the plugin is stopped.
    }

    @Override
    public void delete() {
        // PF4J calls this when the plugin is permanently removed.
    }
}
