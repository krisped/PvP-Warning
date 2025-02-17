package com.krisped;

import net.runelite.api.Client;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.api.widgets.WidgetInfo;

import java.awt.*;
import java.text.NumberFormat;

public class InventoryOverlay extends Overlay {
    private final Client client;
    private final ItemManager itemManager;
    private final PvPWarningConfig config;
    private int risk;

    // Juster skriftstørrelsen her:
    private static final float INVENTORY_FONT_SIZE = 15f;

    public InventoryOverlay(Client client, ItemManager itemManager, PvPWarningConfig config) {
        this.client = client;
        this.itemManager = itemManager;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    public void updateRisk() {
        risk = PriceManager.computeRisk(client, itemManager, config);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.showInventoryRiskOverlay())
            return null;
        var inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget == null || inventoryWidget.isHidden())
            return null;

        updateRisk();

        Rectangle bounds = inventoryWidget.getBounds();
        String text = "Risk: " + NumberFormat.getInstance().format(risk) + " GP";

        // Hent Runescape-fonten med en lokal definert størrelse
        Font baseFont = FontManager.getRunescapeFont();
        Font font = baseFont.deriveFont(INVENTORY_FONT_SIZE);
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);

        int textWidth = graphics.getFontMetrics().stringWidth(text);
        int x = bounds.x + (bounds.width - textWidth) / 2;

        // Beregn y-posisjon med justering: TOP flyttes opp og BOTTOM flyttes ned med offset
        int yTop = bounds.y + graphics.getFontMetrics().getAscent();
        int yBottom = bounds.y + bounds.height;
        int offset = 3; // juster offset etter ønske
        int y = config.inventoryOverlayPosition() == PvPWarningConfig.InventoryOverlayPosition.TOP
                ? yTop - offset
                : yBottom + offset;

        graphics.drawString(text, x, y);
        return new Dimension(textWidth, graphics.getFontMetrics().getHeight());
    }
}
