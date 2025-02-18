package com.krisped;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("pvpwarning")
public interface PvPWarningConfig extends Config
{
    @ConfigSection(
            name = "Risk Overlay Settings",
            description = "Settings for the risk overlay display, including risk calculations and visual alerts.",
            position = 0,
            closedByDefault = true
    )
    String riskOverlaySettings = "riskOverlaySettings";

    // 1. Enable Risk Overlay (global toggle)
    @ConfigItem(
            keyName = "enableRiskOverlay",
            name = "Enable Risk Overlay",
            description = "Toggle the entire risk overlay functionality on or off.",
            section = riskOverlaySettings,
            position = 1
    )
    default boolean enableRiskOverlay() {
        return true;
    }

    // 2. Main Color for normal text
    @ConfigItem(
            keyName = "mainColor",
            name = "Main Color",
            description = "Select the main color for risk text when no warning is active.",
            section = riskOverlaySettings,
            position = 2
    )
    default Color mainColor() {
        return Color.WHITE;
    }

    // 3. Show Risk Overlay (onscreen)
    @ConfigItem(
            keyName = "showRiskOverlay",
            name = "Show Risk Overlay",
            description = "Display the onscreen risk overlay with the total risk value.",
            section = riskOverlaySettings,
            position = 3
    )
    default boolean showRiskOverlay() {
        return true;
    }

    // 4. Show Inventory Risk Overlay
    @ConfigItem(
            keyName = "showInventoryRiskOverlay",
            name = "Show Inventory Risk Overlay",
            description = "Display the risk overlay inside the inventory if enabled.",
            section = riskOverlaySettings,
            position = 4
    )
    default boolean showInventoryRiskOverlay() {
        return false;
    }

    // 5. Inventory Overlay Position
    @ConfigItem(
            keyName = "inventoryOverlayPosition",
            name = "Inventory Overlay Position",
            description = "Select whether the risk text in the inventory appears at the top or bottom.",
            section = riskOverlaySettings,
            position = 5
    )
    default InventoryOverlayPosition inventoryOverlayPosition() {
        return InventoryOverlayPosition.TOP;
    }

    // 6. Inventory Overlay Font
    @ConfigItem(
            keyName = "inventoryOverlayFont",
            name = "Inventory Overlay Font",
            description = "Select the font to be used for the inventory risk overlay text.",
            section = riskOverlaySettings,
            position = 6
    )
    default InventoryFont inventoryOverlayFont() {
        return InventoryFont.RUNESCAPE;
    }

    // 7. Inventory Font Size
    @ConfigItem(
            keyName = "inventoryOverlayFontSize",
            name = "Inventory Font Size",
            description = "Set the font size for the inventory risk overlay text.",
            section = riskOverlaySettings,
            position = 7
    )
    default float inventoryOverlayFontSize() {
        return 15f;
    }

    // 8. Protect Item Adjustment
    @ConfigItem(
            keyName = "protectItem",
            name = "Protect Item Adjustment",
            description = "Adjust risk by excluding additional value when Protect Item Prayer is active.",
            section = riskOverlaySettings,
            position = 8
    )
    default boolean protectItem() {
        return false;
    }

    // 9. Only Enable in PvP
    @ConfigItem(
            keyName = "onlyEnableInPvP",
            name = "Only Enable in PvP",
            description = "Enable risk overlay functionality only in PvP worlds or unsafe areas (e.g. Wilderness).",
            section = riskOverlaySettings,
            position = 9
    )
    default boolean onlyEnableInPvP() {
        return true;
    }

    // 10. Risk Based on PvP Skull
    @ConfigItem(
            keyName = "riskBasedOnPvPSkull",
            name = "Risk Based on PvP Skull",
            description = "Calculate risk based on PvP skull status and adjust for Protect Item Prayer if applicable.",
            section = riskOverlaySettings,
            position = 10
    )
    default boolean riskBasedOnPvPSkull() {
        return true;
    }

    // 11. Enable Risk Color
    @ConfigItem(
            keyName = "enableRiskColor",
            name = "Enable Risk Color",
            description = "Enable custom color for risk text when the threshold is exceeded.",
            section = riskOverlaySettings,
            position = 11
    )
    default boolean enableRiskColor() {
        return false;
    }

    // 12. Risk Color
    @ConfigItem(
            keyName = "riskColor",
            name = "Risk Color",
            description = "Select the color to be used for risk text when warning threshold is exceeded.",
            section = riskOverlaySettings,
            position = 12
    )
    default Color riskColor() {
        return Color.RED;
    }

    // 13. Warning Threshold (GP)
    @ConfigItem(
            keyName = "warningRiskOver",
            name = "Warning Threshold (GP)",
            description = "Set the risk threshold (in GP) above which a warning is displayed.",
            section = riskOverlaySettings,
            position = 13
    )
    default int warningRiskOver() {
        return 1000000;
    }

    // 14. Enable Blink (active only when risk exceeds threshold and risk color is enabled)
    @ConfigItem(
            keyName = "enableBlink",
            name = "Enable Blink",
            description = "Toggle blinking of the risk text every 0.5 seconds (active only when risk exceeds threshold and risk color is enabled).",
            section = riskOverlaySettings,
            position = 14
    )
    default boolean enableBlink() {
        return false;
    }

    // 15. Price Source
    @ConfigItem(
            keyName = "priceSource",
            name = "Price Source",
            description = "Select the source for item prices.",
            section = riskOverlaySettings,
            position = 15
    )
    default PriceSource priceSource() {
        return PriceSource.RUNELITE;
    }

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

    // ------------------ Protect Item Settings ------------------
    @ConfigSection(
            name = "Protect Item Settings",
            description = "Settings for the Protect Item overlay and alerts.",
            position = 1,
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
            position = 2,
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
