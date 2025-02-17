package com.krisped;

import net.runelite.api.Client;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TitleComponent;

import java.awt.*;
import java.text.NumberFormat;

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
    }

    // Oppdaterer risk ved å bruke PriceManager
    public void updateRisk() {
        risk = PriceManager.computeRisk(client, itemManager, config);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.showRiskOverlay())
            return null;
        updateRisk();
        String text = "Risk: " + NumberFormat.getInstance().format(risk) + " GP";
        panelComponent.getChildren().clear();
        panelComponent.getChildren().add(
                TitleComponent.builder()
                        .text(text)
                        .color(Color.WHITE)
                        .build()
        );
        panelComponent.setPreferredSize(new Dimension(
                graphics.getFontMetrics().stringWidth(text) + 20,
                graphics.getFontMetrics().getHeight() + 10
        ));
        return super.render(graphics);
    }
}
