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
        Font font = FontManager.getRunescapeFont();
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);
        int textWidth = graphics.getFontMetrics().stringWidth(text);
        int x = bounds.x + (bounds.width - textWidth) / 2;

        // Beregn y-posisjon med justering: TOP opp og BOTTOM ned
        int yTop = bounds.y + graphics.getFontMetrics().getAscent() + 2;
        int yBottom = bounds.y + bounds.height - 2;
        int offset = 3; // Juster offseten her etter ønske (3 piksler ca. et par mm)
        int y = config.inventoryOverlayPosition() == PvPWarningConfig.InventoryOverlayPosition.TOP
                ? yTop - offset
                : yBottom + offset;

        graphics.drawString(text, x, y);
        return new Dimension(textWidth, graphics.getFontMetrics().getHeight());
    }
}
