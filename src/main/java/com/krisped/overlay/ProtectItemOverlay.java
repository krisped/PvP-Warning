package com.krisped.overlay;

import java.awt.Dimension;
import java.awt.Graphics2D;
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

    // Sprite-ID for når Protect Item Prayer er aktiv og inaktiv
    private final int spriteIdActive = 123;
    private final int spriteIdInactive = 143;

    private BufferedImage spriteImageActive;
    private BufferedImage spriteImageInactive;

    // For å sende chat-melding med et definert intervall (i game ticks)
    private int lastChatCycle = -1;

    // Vi antar at varbit for wilderness-nivå er 29
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
        // Sjekk om overlayet skal vises via config
        if (!config.protectItemWarning())
        {
            return null;
        }

        // Sjekk at spilleren er i en PvP-verden
        Set<WorldType> worldTypes = client.getWorldType();
        if (!worldTypes.contains(WorldType.PVP))
        {
            return null;
        }

        // Sjekk at spilleren er i et unsafe område:
        // - Wilderness-nivå (varbit 29) må være > 0
        // - Spilleren må ikke være i safe zone – her bruker vi varbit PVP_SPEC_ORB:
        //   Hvis client.getVar(Varbits.PVP_SPEC_ORB) == 0, tolkes det som safe.
        if (client.getVar(WILDERNESS_LEVEL_VARBIT) <= 0 || client.getVar(Varbits.PVP_SPEC_ORB) == 0)
        {
            return null;
        }

        loadSprites();
        // Sjekk om Protect Item Prayer er aktiv (1 betyr aktiv)
        boolean protectActive = client.getVar(Varbits.PRAYER_PROTECT_ITEM) == 1;
        BufferedImage spriteToShow = protectActive ? spriteImageActive : spriteImageInactive;

        // Dersom chat-varsel er aktivert og Protect Item Prayer er deaktivert,
        // send en melding med definert delay (i ticks)
        if (config.protectItemWarningChat() && !protectActive)
        {
            int currentCycle = client.getGameCycle();
            int delayTicks = config.protectItemWarningDelay(); // nå i ticks
            if (currentCycle - lastChatCycle >= delayTicks)
            {
                // Sender melding med mørkere rød farge (<col=990000>)
                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "System", "<col=990000>WARNING! Protect Item is DISABLED!!!", null);
                lastChatCycle = currentCycle;
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
