package com.krisped;

import java.awt.Color;

import net.runelite.client.config.*;

@ConfigGroup("pvpwarning")
public interface PvPWarningConfig extends Config
{
    // Enum-deklarasjoner flyttet til toppen slik at de er synlige for alle metoder
    enum InventoryOverlayPosition {
        TOP,
        BOTTOM
    }

    enum PriceSource {
        RUNELITE,
        OSRS_WIKI
    }

    enum InventoryFont {
        RUNESCAPE,
        ARIAL,
        VERDANA,
        TIMES_NEW_ROMAN,
        COURIER_NEW,
        COMIC_SANS,
        IMPACT,
        CALIBRI,
        GEORGIA,
        TAHOMA
    }

    // ------------------ Risk Onscreen Overlay ------------------
    @ConfigSection(
            name = "Risk Onscreen Overlay",
            description = "Settings for the onscreen risk overlay display.",
            position = 0,
            closedByDefault = true
    )
    String riskOnscreenOverlay = "riskOnscreenOverlay";

    @ConfigItem(
            keyName = "showRiskOverlayOnscreen",
            name = "Enable Onscreen Overlay",
            description = "Toggle the display of the onscreen risk overlay.",
            section = riskOnscreenOverlay,
            position = 1
    )
    default boolean showRiskOverlayOnscreen() {
        return true;
    }

    @ConfigItem(
            keyName = "onscreenMainColor",
            name = "Main Color",
            description = "Select the main color for onscreen risk text when no warning is active.",
            section = riskOnscreenOverlay,
            position = 2
    )
    default Color onscreenMainColor() {
        return Color.WHITE;
    }

    @ConfigItem(
            keyName = "onscreenProtectItem",
            name = "Protect Item Adjustment",
            description = "Adjust risk by excluding additional value when Protect Item Prayer is active (onscreen).",
            section = riskOnscreenOverlay,
            position = 3
    )
    default boolean onscreenProtectItem() {
        return false;
    }

    @ConfigItem(
            keyName = "onscreenOnlyEnableInPvP",
            name = "Only Enable in PvP",
            description = "Display the onscreen overlay only in unsafe PvP areas (e.g. Wilderness).",
            section = riskOnscreenOverlay,
            position = 4
    )
    default boolean onscreenOnlyEnableInPvP() {
        return true;
    }

    @ConfigItem(
            keyName = "onscreenRiskBasedOnPvPSkull",
            name = "Risk Based on PvP Skull",
            description = "Calculate risk based on PvP skull status (onscreen).",
            section = riskOnscreenOverlay,
            position = 5
    )
    default boolean onscreenRiskBasedOnPvPSkull() {
        return true;
    }

    @ConfigItem(
            keyName = "onscreenEnableRiskColor",
            name = "Enable Risk Color",
            description = "Enable custom color for risk text when the threshold is exceeded (onscreen).",
            section = riskOnscreenOverlay,
            position = 6
    )
    default boolean onscreenEnableRiskColor() {
        return false;
    }

    @ConfigItem(
            keyName = "onscreenWarningRiskOver",
            name = "Warning Threshold (GP)",
            description = "Set the risk threshold (in GP) above which a warning is displayed (onscreen).",
            section = riskOnscreenOverlay,
            position = 7
    )
    default int onscreenWarningRiskOver() {
        return 1000000;
    }

    @ConfigItem(
            keyName = "onscreenRiskColor",
            name = "Risk Color",
            description = "Select the color for risk text when the onscreen warning threshold is exceeded.",
            section = riskOnscreenOverlay,
            position = 8
    )
    default Color onscreenRiskColor() {
        return Color.RED;
    }

    @ConfigItem(
            keyName = "onscreenEnableBlink",
            name = "Enable Blink",
            description = "Toggle blinking of the onscreen risk text every 0.5 sec (active only when risk exceeds threshold and risk color is enabled).",
            section = riskOnscreenOverlay,
            position = 9
    )
    default boolean onscreenEnableBlink() {
        return false;
    }

    @ConfigItem(
            keyName = "onscreenPriceSource",
            name = "Price Source",
            description = "Select the source for item prices (onscreen overlay).",
            section = riskOnscreenOverlay,
            position = 10
    )
    default PriceSource onscreenPriceSource() {
        return PriceSource.RUNELITE;
    }

    // ------------------ Risk Inventory Overlay ------------------
    @ConfigSection(
            name = "Risk Inventory Overlay",
            description = "Settings for the inventory risk overlay display.",
            position = 1,
            closedByDefault = true
    )
    String riskInventoryOverlay = "riskInventoryOverlay";

    @ConfigItem(
            keyName = "showRiskOverlayInventory",
            name = "Enable Inventory Overlay",
            description = "Toggle the display of the inventory risk overlay.",
            section = riskInventoryOverlay,
            position = 1
    )
    default boolean showRiskOverlayInventory() {
        return true;
    }

    @ConfigItem(
            keyName = "inventoryMainColor",
            name = "Main Color",
            description = "Select the main color for inventory risk text when no warning is active.",
            section = riskInventoryOverlay,
            position = 2
    )
    default Color inventoryMainColor() {
        return Color.WHITE;
    }

    @ConfigItem(
            keyName = "inventoryTextPos",
            name = "Inventory Text Pos.",
            description = "Select whether the inventory risk text appears at the top or bottom.",
            section = riskInventoryOverlay,
            position = 3
    )
    default InventoryOverlayPosition inventoryTextPos() {
        return InventoryOverlayPosition.TOP;
    }

    @ConfigItem(
            keyName = "inventoryFont",
            name = "Text Font",
            description = "Select the font for the inventory risk overlay text.",
            section = riskInventoryOverlay,
            position = 4
    )
    default InventoryFont inventoryFont() {
        return InventoryFont.RUNESCAPE;
    }

    @ConfigItem(
            keyName = "inventoryFontSize",
            name = "Font Size",
            description = "Adjust the font size of the inventory risk overlay.",
            section = riskInventoryOverlay
    )
    @Range(
            min = 10, // Minimum font størrelse
            max = 15  // Maksimum font størrelse
    )
    default int inventoryFontSize()
    {
        return 15; // Standardverdi
    }


    @ConfigItem(
            keyName = "inventoryTextOffset",
            name = "Text Vertical Offset",
            description = "Adjust the vertical position of the inventory risk text (-5 to +5).",
            section = riskInventoryOverlay,
            position = 6
    )
    default int inventoryTextOffset() {
        return 0;
    }

    @ConfigItem(
            keyName = "inventoryProtectItem",
            name = "Protect Item Adjustment",
            description = "Adjust risk by excluding additional value when Protect Item Prayer is active (inventory overlay).",
            section = riskInventoryOverlay,
            position = 7
    )
    default boolean inventoryProtectItem() {
        return false;
    }

    @ConfigItem(
            keyName = "inventoryOnlyEnableInPvP",
            name = "Only Enable in PvP",
            description = "Display the inventory risk overlay only in unsafe PvP areas (e.g. Wilderness).",
            section = riskInventoryOverlay,
            position = 8
    )
    default boolean inventoryOnlyEnableInPvP() {
        return true;
    }

    @ConfigItem(
            keyName = "inventoryRiskBasedOnPvPSkull",
            name = "Risk Based on PvP Skull",
            description = "Calculate risk based on PvP skull status (inventory overlay).",
            section = riskInventoryOverlay,
            position = 9
    )
    default boolean inventoryRiskBasedOnPvPSkull() {
        return true;
    }

    @ConfigItem(
            keyName = "inventoryEnableRiskColor",
            name = "Enable Risk Color",
            description = "Enable custom color for inventory risk text when threshold is exceeded.",
            section = riskInventoryOverlay,
            position = 10
    )
    default boolean inventoryEnableRiskColor() {
        return false;
    }

    @ConfigItem(
            keyName = "inventoryRiskColor",
            name = "Risk Color",
            description = "Select the color for inventory risk text when warning threshold is exceeded.",
            section = riskInventoryOverlay,
            position = 11
    )
    default Color inventoryRiskColor() {
        return Color.RED;
    }

    @ConfigItem(
            keyName = "inventoryWarningRiskOver",
            name = "Warning Threshold (GP)",
            description = "Set the risk threshold (in GP) for inventory overlay warnings.",
            section = riskInventoryOverlay,
            position = 12
    )
    default int inventoryWarningRiskOver() {
        return 1000000;
    }

    @ConfigItem(
            keyName = "inventoryEnableBlink",
            name = "Enable Blink",
            description = "Toggle blinking of the inventory risk text every 0.5 sec (active only when risk exceeds threshold and risk color is enabled).",
            section = riskInventoryOverlay,
            position = 13
    )
    default boolean inventoryEnableBlink() {
        return false;
    }

    @ConfigItem(
            keyName = "inventoryPriceSource",
            name = "Price Source",
            description = "Select the source for item prices (inventory overlay).",
            section = riskInventoryOverlay,
            position = 14
    )
    default PriceSource inventoryPriceSource() {
        return PriceSource.RUNELITE;
    }

    // ------------------ Protect Item Settings ------------------
    @ConfigSection(
            name = "Protect Item Settings",
            description = "Settings for the Protect Item overlay and alerts.",
            position = 2,
            closedByDefault = true
    )
    String protectItemSettings = "protectItemSettings";

    @ConfigItem(
            keyName = "protectItemWarning",
            name = "Protect Item Warning",
            description = "Toggle the display of the Protect Item overlay.",
            section = protectItemSettings,
            position = 1
    )
    default boolean protectItemWarning() {
        return true;
    }

    @ConfigItem(
            keyName = "protectItemWarningChat",
            name = "Protect Item Warning Chat",
            description = "Send a chat message when Protect Item Prayer is disabled.",
            section = protectItemSettings,
            position = 2
    )
    default boolean protectItemWarningChat() {
        return false;
    }

    @ConfigItem(
            keyName = "protectItemWarningDelay",
            name = "Warning Delay (ticks)",
            description = "Set the interval (in game ticks) between protect item warning messages.",
            section = protectItemSettings,
            position = 3
    )
    default int protectItemWarningDelay() {
        return 20;
    }

    @ConfigItem(
            keyName = "protectItemWarningChatColor",
            name = "Protect Item Warning Chat Color",
            description = "Select the chat message color for protect item warnings.",
            section = protectItemSettings,
            position = 4
    )
    default Color protectItemWarningChatColor() {
        return Color.RED;
    }

    // ------------------ Recoil Settings ------------------
    @ConfigSection(
            name = "Recoil Settings",
            description = "Settings for the Recoil overlay display.",
            position = 3,
            closedByDefault = true
    )
    String recoilSettings = "recoilSettings";

    @ConfigItem(
            keyName = "noRecoilOverlay",
            name = "No Recoil Overlay",
            description = "Display the overlay when a Ring of Recoil is present in the inventory but not equipped.",
            section = recoilSettings,
            position = 1
    )
    default boolean noRecoilOverlay() {
        return true;
    }

    @ConfigItem(
            keyName = "blinkRecoilOverlay",
            name = "Blink Recoil Overlay",
            description = "Enable blinking of the Recoil overlay icon (icon toggles every 0.5 sec).",
            section = recoilSettings,
            position = 2
    )
    default boolean blinkRecoilOverlay() {
        return false;
    }

    @ConfigItem(
            keyName = "onlyActiveInPvP",
            name = "Only Enable in PvP",
            description = "Display the Recoil overlay only in unsafe PvP areas (Wilderness with PVP_SPEC_ORB not 0).",
            section = recoilSettings,
            position = 3
    )
    default boolean onlyActiveInPvP() {
        return true;
    }

    @ConfigItem(
            keyName = "warningNoRecoil",
            name = "Warning No Recoil",
            description = "Send a chat message when no Ring of Recoil is equipped.",
            section = recoilSettings,
            position = 4
    )
    default boolean warningNoRecoil() {
        return false;
    }

    @ConfigItem(
            keyName = "warningNoRecoilDelay",
            name = "Warning Delay (ticks)",
            description = "Set the interval (in game ticks) between no recoil warning messages.",
            section = recoilSettings,
            position = 5
    )
    default int warningNoRecoilDelay() {
        return 20;
    }

    @ConfigItem(
            keyName = "recoilWarningChatColor",
            name = "Recoil Warning Chat Color",
            description = "Select the chat message color for no recoil warnings.",
            section = recoilSettings,
            position = 6
    )
    default Color recoilWarningChatColor() {
        return Color.BLUE;
    }
}
