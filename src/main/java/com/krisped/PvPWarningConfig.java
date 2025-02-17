package com.krisped;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("pvpwarning")
public interface PvPWarningConfig extends Config
{
    @ConfigItem(
            keyName = "showRiskOverlay",
            name = "Show Risk Overlay",
            description = "Skru av/på overlay som viser total risiko ved død"
    )
    default boolean showRiskOverlay()
    {
        return true;
    }

    @ConfigItem(
            keyName = "riskOverlayType",
            name = "Risk Overlay Type",
            description = "Velg om risk vises som en Overlay Box (flyttbar) eller som Inventory Overlay (fast tekst)"
    )
    default RiskOverlayType riskOverlayType()
    {
        return RiskOverlayType.OVERLAY_BOX;
    }

    @ConfigItem(
            keyName = "inventoryOverlayPosition",
            name = "Inventory Overlay Position",
            description = "Velg om risk-teksten skal vises øverst eller nederst i inventory (gjelder kun Inventory Overlay)"
    )
    default InventoryOverlayPosition inventoryOverlayPosition()
    {
        return InventoryOverlayPosition.TOP;
    }

    @ConfigItem(
            keyName = "priceSource",
            name = "Get prices from:",
            description = "Velg kilde for item priser"
    )
    default PriceSource priceSource()
    {
        return PriceSource.RUNELITE;
    }

    @ConfigItem(
            keyName = "protectItem",
            name = "Protect Item",
            description = "Når aktiv, trekk fra ekstra verdi (i skull-modus: trekk fra 3 eller 4 dyreste items avhengig av Protect Item Prayer)."
    )
    default boolean protectItem()
    {
        return false;
    }

    @ConfigItem(
            keyName = "riskBasedOnPvPSkull",
            name = "Risk based on PvP Skull",
            description = "Hvis aktiv: Hvis skulled, vis full risk (med unntak: trekk fra den dyreste itemen dersom Protect Item Prayer er aktiv). Hvis ikke skulled, trekk fra de 3 dyreste items (4 hvis Protect Item og Protect Item Prayer er aktiv)."
    )
    default boolean riskBasedOnPvPSkull()
    {
        return true;
    }

    enum RiskOverlayType
    {
        OVERLAY_BOX,
        INVENTORY_OVERLAY
    }

    enum InventoryOverlayPosition
    {
        TOP,
        BOTTOM
    }
}
