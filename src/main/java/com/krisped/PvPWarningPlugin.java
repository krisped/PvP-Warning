package com.krisped;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import com.krisped.overlay.ProtectItemOverlay;

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
    private SpriteManager spriteManager;
    @Inject
    private PvPWarningConfig config;
    @Inject
    private OverlayManager overlayManager;

    private OnscreenOverlay onscreenOverlay;
    private InventoryOverlay inventoryOverlay;
    private ProtectItemOverlay protectItemOverlay;

    @Override
    protected void startUp() throws Exception {
        // Legg til risk overlays
        onscreenOverlay = new OnscreenOverlay(client, itemManager, config);
        overlayManager.add(onscreenOverlay);

        inventoryOverlay = new InventoryOverlay(client, itemManager, config);
        overlayManager.add(inventoryOverlay);

        // Legg til Protect Item Overlay (overlayet sjekker selv config for om det skal vises)
        protectItemOverlay = new ProtectItemOverlay(spriteManager, config);
        overlayManager.add(protectItemOverlay);
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(onscreenOverlay);
        overlayManager.remove(inventoryOverlay);
        overlayManager.remove(protectItemOverlay);
        onscreenOverlay = null;
        inventoryOverlay = null;
        protectItemOverlay = null;
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

    @Provides
    PvPWarningConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PvPWarningConfig.class);
    }
}
