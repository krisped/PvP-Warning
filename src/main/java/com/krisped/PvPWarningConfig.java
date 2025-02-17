package com.krisped;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("pvpwarning")
public interface PvPWarningConfig extends Config
{
    @ConfigSection(
            name = "Risk Overlay Settings",
            description = "Innstillinger for risk overlay",
            position = 0
    )
    String riskOverlaySettings = "riskOverlaySettings";

    @ConfigItem(
            keyName = "showRiskOverlay",
            name = "Show Risk Overlay",
            description = "Vis onscreen overlay med total risk",
            section = riskOverlaySettings
    )
    default boolean showRiskOverlay()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showInventoryRiskOverlay",
            name = "Show Inventory Risk Overlay",
            description = "Vis risk i inventory hvis aktivert",
            section = riskOverlaySettings
    )
    default boolean showInventoryRiskOverlay()
    {
        return false;
    }

    @ConfigItem(
            keyName = "inventoryOverlayPosition",
            name = "Inventory Overlay Position",
            description = "Velg om risk-teksten skal vises øverst eller nederst i inventory (gjelder kun Inventory overlay)",
            section = riskOverlaySettings
    )
    default InventoryOverlayPosition inventoryOverlayPosition()
    {
        return InventoryOverlayPosition.TOP;
    }

    @ConfigItem(
            keyName = "priceSource",
            name = "Get prices from:",
            description = "Velg kilde for item priser",
            section = riskOverlaySettings
    )
    default PriceSource priceSource()
    {
        return PriceSource.RUNELITE;
    }

    @ConfigItem(
            keyName = "protectItem",
            name = "Protect Item",
            description = "Når aktiv, trekk fra ekstra verdi (i skull-modus: trekk fra 3 eller 4 dyreste items avhengig av Protect Item Prayer).",
            section = riskOverlaySettings
    )
    default boolean protectItem()
    {
        return false;
    }

    @ConfigItem(
            keyName = "riskBasedOnPvPSkull",
            name = "Risk based on PvP Skull",
            description = "Hvis aktiv: Hvis skulled, vis full risk (med unntak: trekk fra den dyreste itemen dersom Protect Item Prayer er aktiv). Hvis ikke skulled, trekk fra de 3 dyreste items (4 hvis Protect Item og Protect Item Prayer er aktiv).",
            section = riskOverlaySettings
    )
    default boolean riskBasedOnPvPSkull()
    {
        return true;
    }

    @ConfigSection(
            name = "PvP Overlay Settings",
            description = "Innstillinger for PvP overlay",
            position = 1
    )
    String pvpOverlaySettings = "pvpOverlaySettings";

    @ConfigItem(
            keyName = "protectItemWarning",
            name = "Protect Item Warning",
            description = "Skru av og på Protect Item Overlay",
            section = pvpOverlaySettings
    )
    default boolean protectItemWarning()
    {
        return true;
    }

    enum InventoryOverlayPosition
    {
        TOP,
        BOTTOM
    }

    enum PriceSource
    {
        RUNELITE,
        OSRS_WIKI
    }
}
