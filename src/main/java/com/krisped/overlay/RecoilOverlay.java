package com.krisped.overlay;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.InventoryID;
import net.runelite.api.Varbits;
import net.runelite.api.WorldType;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import com.krisped.PvPWarningConfig;

public class RecoilOverlay extends Overlay
{
    private final ItemManager itemManager;
    private final PvPWarningConfig config;
    private final Client client;

    // Ring of recoil item ID (assumed 2550)
    private final int ringOfRecoilId = 2550;
    private BufferedImage ringImage;

    // For chat warning timing for no recoil
    private long lastRecoilWarningTime = 0;

    public RecoilOverlay(ItemManager itemManager, PvPWarningConfig config, Client client)
    {
        this.itemManager = itemManager;
        this.config = config;
        this.client = client;
        setPosition(OverlayPosition.TOP_RIGHT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    private void loadRingImage()
    {
        if (ringImage == null)
        {
            ringImage = itemManager.getImage(ringOfRecoilId);
        }
    }

    private boolean isRingEquipped()
    {
        if (client.getItemContainer(InventoryID.EQUIPMENT) == null)
        {
            return false;
        }
        for (Item item : client.getItemContainer(InventoryID.EQUIPMENT).getItems())
        {
            if (item != null && item.getId() == ringOfRecoilId)
            {
                return true;
            }
        }
        return false;
    }

    private boolean isRingInInventory()
    {
        if (client.getItemContainer(InventoryID.INVENTORY) == null)
        {
            return false;
        }
        for (Item item : client.getItemContainer(InventoryID.INVENTORY).getItems())
        {
            if (item != null && item.getId() == ringOfRecoilId)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        // Display overlay only if "No Recoil Overlay" is enabled.
        if (!config.noRecoilOverlay())
        {
            return null;
        }
        // Only show overlay if the ring is present in inventory but not equipped.
        if (isRingEquipped() || !isRingInInventory())
        {
            return null;
        }
        // If "Only Enable in PvP" is enabled, show only in unsafe PvP areas.
        if (config.onlyActiveInPvP()) {
            Set<WorldType> worldTypes = client.getWorldType();
            if (!worldTypes.contains(WorldType.PVP) || client.getVar(29) <= 0 || client.getVar(Varbits.PVP_SPEC_ORB) == 0) {
                return null;
            }
        }
        // New: Warning No Recoil chat message if enabled.
        if (config.warningNoRecoil()) {
            long currentTime = System.currentTimeMillis();
            int delayTicks = config.warningNoRecoilDelay();
            long delayMillis = delayTicks * 600L;
            if (currentTime - lastRecoilWarningTime >= delayMillis) {
                // Use recoil warning chat color from config.
                Color chatColor = config.recoilWarningChatColor();
                String hexColor = String.format("%06X", (0xFFFFFF & chatColor.getRGB()));
                client.addChatMessage(net.runelite.api.ChatMessageType.GAMEMESSAGE, "System", "<col=" + hexColor + "><u>WARNING!</u> No Ring of Recoil equipped!", null);
                lastRecoilWarningTime = currentTime;
            }
        }
        // Blink logic: if Blink Recoil Overlay is enabled, use system time to toggle display.
        boolean blinkOff = false;
        if (config.blinkRecoilOverlay()) {
            long time = System.currentTimeMillis();
            blinkOff = ((time % 1000) >= 500);
        }
        loadRingImage();
        if (ringImage == null) {
            return null;
        }
        if (!blinkOff) {
            graphics.drawImage(ringImage, 0, 0, null);
        }
        // Always return the same dimension to keep overlay position constant.
        return new Dimension(ringImage.getWidth(), ringImage.getHeight());
    }
}
