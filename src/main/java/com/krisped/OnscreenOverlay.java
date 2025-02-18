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
        risk = PriceManager.computeRisk(client, itemManager, config);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        // Global toggle and onscreen risk overlay toggle
        if (!config.enableRiskOverlay() || !config.showRiskOverlay()) {
            return null;
        }
        // Only enable in PvP: check if we're in a PvP world and in wilderness (varbit 29 > 0)
        if (config.onlyEnableInPvP()) {
            Set<WorldType> worldTypes = client.getWorldType();
            if (!worldTypes.contains(WorldType.PVP) || client.getVar(29) <= 0) {
                return null;
            }
        }
        updateRisk();
        String fullText = "Risk: " + NumberFormat.getInstance().format(risk) + " GP";

        // Blink only if enableBlink is active, risk exceeds threshold, and enableRiskColor is active.
        boolean blink = config.enableBlink() && config.enableRiskColor() && (risk >= config.warningRiskOver());
        long time = System.currentTimeMillis();
        String displayText = (blink && (time % 1000 >= 500)) ? "" : fullText;

        // Set text color: if enableRiskColor is active and risk >= threshold, use riskColor; otherwise, use mainColor.
        Color textColor = config.mainColor();
        if (config.enableRiskColor() && risk >= config.warningRiskOver()) {
            textColor = config.riskColor();
        }

        panelComponent.getChildren().clear();
        panelComponent.getChildren().add(
                TitleComponent.builder()
                        .text(displayText)
                        .color(textColor)
                        .build()
        );
        // Preserve overlay dimensions using fullText width.
        panelComponent.setPreferredSize(new Dimension(
                graphics.getFontMetrics().stringWidth(fullText) + 20,
                graphics.getFontMetrics().getHeight() + 10
        ));
        return super.render(graphics);
    }
}
