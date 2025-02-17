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
public class PvPWarningPlugin extends Plugin {
    @Inject
    private Client client;
    @Inject
    private ItemManager itemManager;
    @Inject
    private PvPWarningConfig config;
    @Inject
    private OverlayManager overlayManager;

    private OnscreenOverlay onscreenOverlay;
    private InventoryOverlay inventoryOverlay;

    @Override
    protected void startUp() throws Exception {
        if (config.showRiskOverlay()) {
            onscreenOverlay = new OnscreenOverlay(client, itemManager, config);
            overlayManager.add(onscreenOverlay);
        }
        if (config.showInventoryRiskOverlay()) {
            inventoryOverlay = new InventoryOverlay(client, itemManager, config);
            overlayManager.add(inventoryOverlay);
        }
    }

    @Override
    protected void shutDown() throws Exception {
        if (onscreenOverlay != null) {
            overlayManager.remove(onscreenOverlay);
            onscreenOverlay = null;
        }
        if (inventoryOverlay != null) {
            overlayManager.remove(inventoryOverlay);
            inventoryOverlay = null;
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (onscreenOverlay != null) {
            onscreenOverlay.updateRisk();
        }
        if (inventoryOverlay != null) {
            inventoryOverlay.updateRisk();
        }
    }

    @Subscribe
    public void onConfigChanged(net.runelite.client.events.ConfigChanged event) {
        if (!event.getGroup().equals("pvpwarning"))
            return;
        // Oppdater onscreen overlay
        if (!config.showRiskOverlay() && onscreenOverlay != null) {
            overlayManager.remove(onscreenOverlay);
            onscreenOverlay = null;
        } else if (config.showRiskOverlay() && onscreenOverlay == null) {
            onscreenOverlay = new OnscreenOverlay(client, itemManager, config);
            overlayManager.add(onscreenOverlay);
        }
        // Oppdater inventory overlay
        if (!config.showInventoryRiskOverlay() && inventoryOverlay != null) {
            overlayManager.remove(inventoryOverlay);
            inventoryOverlay = null;
        } else if (config.showInventoryRiskOverlay() && inventoryOverlay == null) {
            inventoryOverlay = new InventoryOverlay(client, itemManager, config);
            overlayManager.add(inventoryOverlay);
        }
    }

    @Provides
    PvPWarningConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PvPWarningConfig.class);
    }
}
