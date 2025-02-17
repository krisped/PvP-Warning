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
            description = "Når aktiv, trekk fra ekstra verdi (totalt 4 dyreste items) dersom Protect Item prayer er aktiv. Hvis ikke aktiv, trekk fra 3 dyreste items i skull-modus."
    )
    default boolean protectItem()
    {
        return false;
    }

    @ConfigItem(
            keyName = "riskBasedOnPvPSkull",
            name = "Risk based on PvP Skull",
            description = "Hvis aktiv: Hvis skulled, vis full risk. Hvis ikke skulled, trekk fra de 3 dyreste items (4 hvis Protect Item er aktiv og Protect Item Prayer er aktiv)."
    )
    default boolean riskBasedOnPvPSkull()
    {
        return true;
    }
}
