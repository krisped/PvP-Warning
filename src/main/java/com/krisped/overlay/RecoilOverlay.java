package com.krisped.overlay;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.InventoryID;
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

    // Ring of recoil item ID (antatt 2550)
    private final int ringOfRecoilId = 2550;
    private BufferedImage ringImage;

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
        // Vis overlay kun dersom "No Recoil Overlay" er aktiv i config
        if (!config.noRecoilOverlay())
        {
            return null;
        }
        // Vis overlay bare hvis du har ringen i inventory, men ikke utstyrt
        if (isRingEquipped() || !isRingInInventory())
        {
            return null;
        }

        loadRingImage();
        if (ringImage == null)
        {
            return null;
        }

        boolean blinkOff = false;
        if (config.blinkRecoilOverlay())
        {
            long time = System.currentTimeMillis();
            // Blink: vis ikonet i 500 ms, skjul i de neste 500 ms
            blinkOff = ((time % 1000) >= 500);
        }

        if (!blinkOff)
        {
            graphics.drawImage(ringImage, 0, 0, null);
        }
        // Returner samme dimensjon uansett, slik at overlayets posisjon forblir fast
        return new Dimension(ringImage.getWidth(), ringImage.getHeight());
    }
}
