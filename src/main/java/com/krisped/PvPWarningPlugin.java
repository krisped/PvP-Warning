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
import com.krisped.overlay.RecoilOverlay;

@PluginDescriptor(
        name = "[KP] PvP Warnings",
        description = "Displays risk based on items, PvP skull, and Protect Item settings.",
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
    private RecoilOverlay recoilOverlay;

    @Override
    protected void startUp() throws Exception {
        onscreenOverlay = new OnscreenOverlay(client, itemManager, config);
        overlayManager.add(onscreenOverlay);

        inventoryOverlay = new InventoryOverlay(client, itemManager, config);
        overlayManager.add(inventoryOverlay);

        protectItemOverlay = new ProtectItemOverlay(spriteManager, config, client);
        overlayManager.add(protectItemOverlay);

        recoilOverlay = new RecoilOverlay(itemManager, config, client);
        overlayManager.add(recoilOverlay);
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(onscreenOverlay);
        overlayManager.remove(inventoryOverlay);
        overlayManager.remove(protectItemOverlay);
        overlayManager.remove(recoilOverlay);
        onscreenOverlay = null;
        inventoryOverlay = null;
        protectItemOverlay = null;
        recoilOverlay = null;
    }

    @Subscribe
    public void onGameTick(net.runelite.api.events.GameTick event) {
        if (onscreenOverlay != null) {
            onscreenOverlay.updateRisk();
        }
        if (inventoryOverlay != null) {
            inventoryOverlay.updateRisk();
        }
        // Logic for Protect Item and Recoil overlays is handled in their respective classes.
    }

    @Provides
    PvPWarningConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PvPWarningConfig.class);
    }
}
