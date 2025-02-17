package com.krisped;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
        name = "[KP] PvP Warnings",
        description = "Viser et overlay med total risiko basert på local players inventory og equipment",
        tags = {"pvp", "warnings", "overlay"}
)
public class PvPWarningPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private ItemManager itemManager;

    @Inject
    private PvPWarningConfig config;

    @Inject
    private OverlayManager overlayManager;

    private PvPWarningOverlay overlay;

    @Override
    protected void startUp() throws Exception
    {
        overlay = new PvPWarningOverlay(client, itemManager, config);
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception
    {
        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (overlay != null)
        {
            overlay.updateRisk();
        }
    }

    @Provides
    PvPWarningConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(PvPWarningConfig.class);
    }
}
