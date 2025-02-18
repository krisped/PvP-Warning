package com.krisped;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.text.NumberFormat;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.WorldType;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class OnscreenOverlay extends OverlayPanel {
    private final Client client;
    private final ItemManager itemManager;
    private final PvPWarningConfig config;
    private int risk;

    public OnscreenOverlay(Client client, ItemManager itemManager, PvPWarningConfig config) {
        this.client = client;
        this.itemManager = itemManager;
        this.config = config;
        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    public void updateRisk() {
        risk = PriceManager.computeRisk(client, itemManager, config.onscreenPriceSource(), config);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        // Bruk kun onscreen-toggle
        if (!config.showRiskOverlayOnscreen()) {
            return null;
        }
        if (config.onscreenOnlyEnableInPvP()) {
            Set<WorldType> worldTypes = client.getWorldType();
            if (!worldTypes.contains(WorldType.PVP) || client.getVar(29) <= 0) {
                return null;
            }
        }
        updateRisk();
        String fullText = "Risk: " + NumberFormat.getInstance().format(risk) + " GP";
        boolean blink = config.onscreenEnableBlink() && config.onscreenEnableRiskColor() && (risk >= config.onscreenWarningRiskOver());
        long time = System.currentTimeMillis();
        String displayText = (blink && (time % 1000 >= 500)) ? "" : fullText;
        Color textColor = config.onscreenMainColor();
        if (config.onscreenEnableRiskColor() && risk >= config.onscreenWarningRiskOver()) {
            textColor = config.onscreenRiskColor();
        }
        panelComponent.getChildren().clear();
        panelComponent.getChildren().add(
                TitleComponent.builder()
                        .text(displayText)
                        .color(textColor)
                        .build()
        );
        panelComponent.setPreferredSize(new Dimension(
                graphics.getFontMetrics().stringWidth(fullText) + 20,
                graphics.getFontMetrics().getHeight() + 10
        ));
        return super.render(graphics);
    }
}
