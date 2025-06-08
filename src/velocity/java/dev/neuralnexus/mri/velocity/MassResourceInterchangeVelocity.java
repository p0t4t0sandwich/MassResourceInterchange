/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;

import dev.neuralnexus.mri.Constants;
import dev.neuralnexus.mri.common.CommonClass;

import org.slf4j.Logger;

@Plugin(
        id = Constants.MOD_ID,
        name = Constants.MOD_NAME,
        version = Constants.VERSION,
        authors = Constants.AUTHOR,
        description = Constants.DESCRIPTION,
        url = Constants.URL)
public class MassResourceInterchangeVelocity {
    private final PluginContainer plugin;
    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public MassResourceInterchangeVelocity(
            PluginContainer plugin, ProxyServer server, Logger logger) {
        this.plugin = plugin;
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        Constants.logger().info("Hello Velocity world!");
        CommonClass.init();
    }
}
