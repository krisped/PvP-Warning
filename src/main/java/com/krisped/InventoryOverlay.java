package com.krisped;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.text.NumberFormat;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.WorldType;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

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
        // Bruker inventoryPriceSource for riskberegning
        risk = PriceManager.computeRisk(client, itemManager, config.inventoryPriceSource(), config);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        // Sjekk om inventory-overlay skal vises
        if (!config.showRiskOverlayInventory()) {
            return null;
        }
        // Hvis "Only Enable in PvP" er aktiv, sjekk at vi er i en PvP-verden med wilderness > 0
        if (config.inventoryOnlyEnableInPvP()) {
            Set<WorldType> worldTypes = client.getWorldType();
            if (!worldTypes.contains(WorldType.PVP) || client.getVar(29) <= 0) {
                return null;
            }
        }
        var inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget == null || inventoryWidget.isHidden()) {
            return null;
        }
        updateRisk();
        Rectangle bounds = inventoryWidget.getBounds();
        String fullText = "Risk: " + NumberFormat.getInstance().format(risk) + " GP";

        // Blink logikk: hvis enableBlink er aktiv, risiko overstiger terskel og enableRiskColor er aktiv, fjern teksten 0.5 sek.
        boolean blink = config.inventoryEnableBlink() && config.inventoryEnableRiskColor() && (risk >= config.inventoryWarningRiskOver());
        long time = System.currentTimeMillis();
        String displayText = (blink && (time % 1000 >= 500)) ? "" : fullText;

        // Sett farge: bruk inventoryMainColor dersom ingen advarsel, ellers inventoryRiskColor.
        Color textColor = config.inventoryMainColor();
        if (config.inventoryEnableRiskColor() && risk >= config.inventoryWarningRiskOver()) {
            textColor = config.inventoryRiskColor();
        }

        // Velg font basert på inventoryFont dropdown
        Font baseFont;
        switch (config.inventoryFont()) {
            case ARIAL:
                baseFont = new Font("Arial", Font.PLAIN, 12);
                break;
            case VERDANA:
                baseFont = new Font("Verdana", Font.PLAIN, 12);
                break;
            case TIMES_NEW_ROMAN:
                baseFont = new Font("Times New Roman", Font.PLAIN, 12);
                break;
            case COURIER_NEW:
                baseFont = new Font("Courier New", Font.PLAIN, 12);
                break;
            case COMIC_SANS:
                baseFont = new Font("Comic Sans MS", Font.PLAIN, 12);
                break;
            case IMPACT:
                baseFont = new Font("Impact", Font.PLAIN, 12);
                break;
            case CALIBRI:
                baseFont = new Font("Calibri", Font.PLAIN, 12);
                break;
            case GEORGIA:
                baseFont = new Font("Georgia", Font.PLAIN, 12);
                break;
            case TAHOMA:
                baseFont = new Font("Tahoma", Font.PLAIN, 12);
                break;
            case RUNESCAPE:
            default:
                baseFont = FontManager.getRunescapeFont();
                break;
        }

        // Konverter inventoryFontSize robust
        float fontSize;
        try {
            fontSize = config.inventoryFontSize();
        } catch (ClassCastException e) {
            try {
                fontSize = Float.parseFloat(String.valueOf(config.inventoryFontSize()));
            } catch (Exception ex) {
                fontSize = 15f;
            }
        }
        Font font = baseFont.deriveFont(fontSize);
        graphics.setFont(font);
        graphics.setColor(textColor);
        int textWidth = graphics.getFontMetrics().stringWidth(fullText);
        int x = bounds.x + (bounds.width - textWidth) / 2;
        int yTop = bounds.y + graphics.getFontMetrics().getAscent();
        int yBottom = bounds.y + bounds.height;
        int offset = config.inventoryTextOffset();  // justert med slider -5 til +5
        int y = config.inventoryTextPos() == PvPWarningConfig.InventoryOverlayPosition.TOP ? yTop - offset : yBottom + offset;
        graphics.drawString(displayText, x, y);
        return new Dimension(textWidth, graphics.getFontMetrics().getHeight());
    }
}
