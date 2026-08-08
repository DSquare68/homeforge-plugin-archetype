package com.github.dsquare68.template;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

/**
 * PF4J {@link Plugin} bootstrap class.
 *
 * <p>This class is the entry point declared in {@code MANIFEST.MF} under
 * {@code Plugin-Class}. Keep it thin - delegate all real work to
 * {@link HubPluginImpl} which implements the HUB lifecycle SPI.
 */
public class PluginBootstrap extends Plugin {

    public PluginBootstrap(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        // PF4J calls this when the plugin JAR is loaded.
        // Spring wiring happens inside HubPluginImpl#onActivate.
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
