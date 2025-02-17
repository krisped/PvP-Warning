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
        description = "Viser risk basert på items, PvP skull og Protect Item",
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

    private PvPWarningOverlay riskOverlay;
    private PvPWarningOverlay.InventoryTextOverlay inventoryOverlay;

    @Override
    protected void startUp() throws Exception
    {
        if (config.riskOverlayType() == PvPWarningConfig.RiskOverlayType.OVERLAY_BOX) {
            riskOverlay = new PvPWarningOverlay(client, itemManager, config);
            overlayManager.add(riskOverlay);
        } else {
            PvPWarningOverlay temp = new PvPWarningOverlay(client, itemManager, config);
            inventoryOverlay = temp.new InventoryTextOverlay();
            overlayManager.add(inventoryOverlay);
        }
    }

    @Override
    protected void shutDown() throws Exception
    {
        if (riskOverlay != null)
            overlayManager.remove(riskOverlay);
        if (inventoryOverlay != null)
            overlayManager.remove(inventoryOverlay);
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        int computedRisk = PvPWarningOverlay.computeRisk(client, itemManager, config);
        if (config.riskOverlayType() == PvPWarningConfig.RiskOverlayType.OVERLAY_BOX) {
            if (riskOverlay != null) {
                riskOverlay.updateRisk();
            }
        } else {
            if (inventoryOverlay != null) {
                inventoryOverlay.updateRisk(computedRisk);
            }
        }
    }

    @Provides
    PvPWarningConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(PvPWarningConfig.class);
    }
}
