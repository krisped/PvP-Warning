package com.krisped.overlay;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import com.krisped.PvPWarningConfig;

public class ProtectItemOverlay extends Overlay
{
    private final SpriteManager spriteManager;
    private final PvPWarningConfig config;
    private final int spriteId = 123;
    private BufferedImage spriteImage;

    public ProtectItemOverlay(SpriteManager spriteManager, PvPWarningConfig config)
    {
        this.spriteManager = spriteManager;
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    private void loadSprite()
    {
        if (spriteImage == null)
        {
            try
            {
                // Henter sprite med ekstra parameter (0)
                spriteImage = spriteManager.getSprite(spriteId, 0);
            }
            catch (AssertionError ae)
            {
                System.err.println("Failed to load sprite with id: " + spriteId);
            }
        }
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        // Dersom checkboxen er skrudd av, ikke render overlayet
        if (!config.protectItemWarning())
        {
            return null;
        }
        loadSprite();
        if (spriteImage != null)
        {
            graphics.drawImage(spriteImage, 0, 0, null);
            return new Dimension(spriteImage.getWidth(), spriteImage.getHeight());
        }
        return null;
    }
}
