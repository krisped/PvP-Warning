package com.krisped.overlay;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Varbits;
import net.runelite.api.WorldType;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import com.krisped.PvPWarningConfig;

public class ProtectItemOverlay extends Overlay
{
    private final SpriteManager spriteManager;
    private final PvPWarningConfig config;
    private final Client client;

    // Sprite-ID for Protect Item overlay (active/inactive)
    private final int spriteIdActive = 123;
    private final int spriteIdInactive = 143;

    private BufferedImage spriteImageActive;
    private BufferedImage spriteImageInactive;

    // For chat warning timing
    private long lastChatTime = 0;

    // Assume wilderness level is stored in varbit 29
    private static final int WILDERNESS_LEVEL_VARBIT = 29;

    public ProtectItemOverlay(SpriteManager spriteManager, PvPWarningConfig config, Client client)
    {
        this.spriteManager = spriteManager;
        this.config = config;
        this.client = client;
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    private void loadSprites()
    {
        if (spriteImageActive == null)
        {
            try
            {
                spriteImageActive = spriteManager.getSprite(spriteIdActive, 0);
            }
            catch (AssertionError ae)
            {
                System.err.println("Failed to load sprite with id: " + spriteIdActive);
            }
        }
        if (spriteImageInactive == null)
        {
            try
            {
                spriteImageInactive = spriteManager.getSprite(spriteIdInactive, 0);
            }
            catch (AssertionError ae)
            {
                System.err.println("Failed to load sprite with id: " + spriteIdInactive);
            }
        }
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.protectItemWarning())
        {
            return null;
        }
        Set<WorldType> worldTypes = client.getWorldType();
        if (!worldTypes.contains(WorldType.PVP) ||
                client.getVar(WILDERNESS_LEVEL_VARBIT) <= 0 ||
                client.getVar(Varbits.PVP_SPEC_ORB) == 0)
        {
            return null;
        }
        if (worldTypes.contains(WorldType.HIGH_RISK))
        {
            return null;
        }
        loadSprites();
        boolean protectActive = client.getVar(Varbits.PRAYER_PROTECT_ITEM) == 1;
        BufferedImage spriteToShow = protectActive ? spriteImageActive : spriteImageInactive;

        if (config.protectItemWarningChat() && !protectActive)
        {
            long currentTime = System.currentTimeMillis();
            int delayTicks = config.protectItemWarningDelay();
            long delayMillis = delayTicks * 600L;
            if (currentTime - lastChatTime >= delayMillis)
            {
                Color chatColor = config.protectItemWarningChatColor();
                String hexColor = String.format("%06X", (0xFFFFFF & chatColor.getRGB()));
                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "System", "<col=" + hexColor + "><u>WARNING!</u> Protect Item prayer is DISABLED!", null);
                lastChatTime = currentTime;
            }
        }
        if (spriteToShow != null)
        {
            graphics.drawImage(spriteToShow, 0, 0, null);
            return new Dimension(spriteToShow.getWidth(), spriteToShow.getHeight());
        }
        return null;
    }
}
