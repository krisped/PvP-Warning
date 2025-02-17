package com.krisped;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.InventoryID;
import net.runelite.api.Varbits;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class PvPWarningOverlay extends OverlayPanel
{
    private final Client client;
    private final ItemManager itemManager;
    private final PvPWarningConfig config;
    private int risk;

    // Manuelle priser for utvalgte items (itemId -> pris)
    private static final Map<Integer, Long> MANUAL_PRICES = new HashMap<>();
    static {
        MANUAL_PRICES.put(6570, 150000L);   // Fire Cape
        MANUAL_PRICES.put(21275, 225000L);  // Infernal Cape
        MANUAL_PRICES.put(20072, 600000L);  // Avernic Defender
        MANUAL_PRICES.put(1540, 240000L);   // Dragon Defender
        MANUAL_PRICES.put(11664, 160000L);  // Void Knight Helmet
        MANUAL_PRICES.put(11665, 180000L);  // Void Knight Top
        MANUAL_PRICES.put(11666, 180000L);  // Void Knight Robe
        MANUAL_PRICES.put(8840, 120000L);   // Void Knight Gloves
        MANUAL_PRICES.put(11668, 250000L);  // Elite Void Top
        MANUAL_PRICES.put(11669, 250000L);  // Elite Void Bottom
        MANUAL_PRICES.put(7462, 150000L);   // Fighter Torso
        MANUAL_PRICES.put(24225, 1579420L); // Granite Maul (Ornate Handle)
        MANUAL_PRICES.put(19709, 150000L);  // Rune Pouch
        MANUAL_PRICES.put(21260, 240000L);  // Ava's Assembler
    }

    // Cache for OSRS Wiki-priser (gyldig i 5 min)
    private Map<Integer, Long> osrsWikiPrices;
    private long osrsWikiPricesLastFetch = 0;
    private static final long OSRS_WIKI_CACHE_DURATION = 300000;

    // Cache for OSRS GE Official priser (gyldig i 5 min)
    private Map<Integer, PriceCacheEntry> geOfficialPrices = new HashMap<>();
    private static final long GE_OFFICIAL_CACHE_DURATION = 300000;

    // Executor for asynkrone GE Official kall
    private final ExecutorService gePriceExecutor = Executors.newCachedThreadPool();

    public PvPWarningOverlay(Client client, ItemManager itemManager, PvPWarningConfig config)
    {
        this.client = client;
        this.itemManager = itemManager;
        this.config = config;
        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
    }

    // Instansmetode for å oppdatere risk for Overlay Box
    public void updateRisk() {
        risk = computeRisk(client, itemManager, config);
    }

    // Statisk metode for å beregne total risk
    public static int computeRisk(Client client, ItemManager itemManager, PvPWarningConfig config) {
        int total = 0;
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (inventory != null) {
            for (Item item : inventory.getItems()) {
                if (item != null && item.getId() != -1) {
                    long price = getPriceForItemStatic(item.getId(), itemManager, config);
                    total += price * item.getQuantity();
                }
            }
        }
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipment != null) {
            for (Item item : equipment.getItems()) {
                if (item != null && item.getId() != -1) {
                    long price = getPriceForItemStatic(item.getId(), itemManager, config);
                    total += price * item.getQuantity();
                }
            }
        }
        if (config.riskBasedOnPvPSkull()) {
            boolean skulled = client.getLocalPlayer().getSkullIcon() != -1;
            if (!skulled) {
                int numToSubtract = 3;
                if (config.protectItem() && isProtectItemPrayerActiveStatic(client))
                    numToSubtract = 4;
                List<Long> pricesList = new ArrayList<>();
                if (inventory != null) {
                    for (Item item : inventory.getItems()) {
                        if (item != null && item.getId() != -1)
                            pricesList.add(getPriceForItemStatic(item.getId(), itemManager, config));
                    }
                }
                if (equipment != null) {
                    for (Item item : equipment.getItems()) {
                        if (item != null && item.getId() != -1)
                            pricesList.add(getPriceForItemStatic(item.getId(), itemManager, config));
                    }
                }
                pricesList.sort((a, b) -> Long.compare(b, a));
                long subtractSum = 0;
                for (int i = 0; i < numToSubtract && i < pricesList.size(); i++) {
                    subtractSum += pricesList.get(i);
                }
                total -= subtractSum;
                if (total < 0)
                    total = 0;
            } else {
                if (config.protectItem() && isProtectItemPrayerActiveStatic(client)) {
                    long maxPrice = 0;
                    if (inventory != null) {
                        for (Item item : inventory.getItems()) {
                            if (item != null && item.getId() != -1) {
                                long price = getPriceForItemStatic(item.getId(), itemManager, config);
                                if (price > maxPrice)
                                    maxPrice = price;
                            }
                        }
                    }
                    if (equipment != null) {
                        for (Item item : equipment.getItems()) {
                            if (item != null && item.getId() != -1) {
                                long price = getPriceForItemStatic(item.getId(), itemManager, config);
                                if (price > maxPrice)
                                    maxPrice = price;
                            }
                        }
                    }
                    total -= maxPrice;
                    if (total < 0)
                        total = 0;
                }
            }
        } else {
            if (config.protectItem() && isProtectItemPrayerActiveStatic(client)) {
                long maxPrice = 0;
                if (inventory != null) {
                    for (Item item : inventory.getItems()) {
                        if (item != null && item.getId() != -1) {
                            long price = getPriceForItemStatic(item.getId(), itemManager, config);
                            if (price > maxPrice)
                                maxPrice = price;
                        }
                    }
                }
                if (equipment != null) {
                    for (Item item : equipment.getItems()) {
                        if (item != null && item.getId() != -1) {
                            long price = getPriceForItemStatic(item.getId(), itemManager, config);
                            if (price > maxPrice)
                                maxPrice = price;
                        }
                    }
                }
                total -= maxPrice;
                if (total < 0)
                    total = 0;
            }
        }
        return total;
    }

    private static long getPriceForItemStatic(int id, ItemManager itemManager, PvPWarningConfig config) {
        if (MANUAL_PRICES.containsKey(id))
            return MANUAL_PRICES.get(id);
        if (config.priceSource() == PriceSource.RUNELITE)
            return itemManager.getItemPrice(id);
        else if (config.priceSource() == PriceSource.OSRS_WIKI) {
            Map<Integer, Long> wiki = getOsrsWikiPricesStatic(itemManager, config);
            return wiki.getOrDefault(id, 0L);
        } else if (config.priceSource() == PriceSource.OSRS_GE_OFFICIAL)
            return getGeOfficialPriceStatic(id, itemManager, config);
        return 0L;
    }

    private static Map<Integer, Long> getOsrsWikiPricesStatic(ItemManager itemManager, PvPWarningConfig config) {
        Map<Integer, Long> prices = new HashMap<>();
        try {
            URL url = new URL("https://prices.runescape.wiki/api/v1/osrs/latest");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream is = connection.getInputStream();
                Reader reader = new InputStreamReader(is);
                Gson gson = new Gson();
                OSRSWikiResponse response = gson.fromJson(reader, OSRSWikiResponse.class);
                if (response != null && response.data != null) {
                    for (Map.Entry<String, OSRSItemPrice> entry : response.data.entrySet()) {
                        int itemId = Integer.parseInt(entry.getKey());
                        OSRSItemPrice itemPrice = entry.getValue();
                        long price = itemPrice.high != 0 ? itemPrice.high :
                                (itemPrice.buy_average != 0 ? itemPrice.buy_average : itemPrice.sell_average);
                        prices.put(itemId, price);
                    }
                }
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prices;
    }

    private static long getGeOfficialPriceStatic(int itemId, ItemManager itemManager, PvPWarningConfig config) {
        // For enkelhets skyld returneres 0 her
        return 0L;
    }

    private static boolean isProtectItemPrayerActiveStatic(Client client) {
        return client.getVar(Varbits.PRAYER_PROTECT_ITEM) == 1;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.showRiskOverlay()) {
            return null;
        }
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

    private long getGeOfficialPrice(final int itemId) {
        PriceCacheEntry entry = geOfficialPrices.get(itemId);
        long now = System.currentTimeMillis();
        if (entry != null && (now - entry.timestamp) < GE_OFFICIAL_CACHE_DURATION) {
            return entry.price;
        } else {
            gePriceExecutor.submit(() -> {
                long fetchedPrice = fetchGeOfficialPrice(itemId);
                geOfficialPrices.put(itemId, new PriceCacheEntry(fetchedPrice, System.currentTimeMillis()));
            });
            return (entry != null) ? entry.price : 0L;
        }
    }

    private long fetchGeOfficialPrice(int itemId) {
        try {
            URL url = new URL("https://services.runelite.com/m:itemdb_oldschool/api/catalogue/detail.json?item=" + itemId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream is = connection.getInputStream();
                Reader reader = new InputStreamReader(is);
                Gson gson = new Gson();
                GeOfficialResponse response = gson.fromJson(reader, GeOfficialResponse.class);
                reader.close();
                if (response != null && response.item != null && response.item.current != null) {
                    String priceStr = response.item.current.price.replace(",", "").trim();
                    if (!priceStr.isEmpty()) {
                        return parsePrice(priceStr);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    private static long parsePrice(String priceStr) throws NumberFormatException {
        priceStr = priceStr.toLowerCase();
        if (priceStr.endsWith("k")) {
            double value = Double.parseDouble(priceStr.substring(0, priceStr.length() - 1));
            return (long)(value * 1000);
        } else if (priceStr.endsWith("m")) {
            double value = Double.parseDouble(priceStr.substring(0, priceStr.length() - 1));
            return (long)(value * 1000000);
        } else if (priceStr.endsWith("b")) {
            double value = Double.parseDouble(priceStr.substring(0, priceStr.length() - 1));
            return (long)(value * 1000000000);
        } else {
            return Long.parseLong(priceStr);
        }
    }

    private static class OSRSWikiResponse {
        Map<String, OSRSItemPrice> data;
    }

    private static class OSRSItemPrice {
        String name;
        int limit;
        long buy_average;
        long sell_average;
        long high;
        long low;
    }

    private static class GeOfficialResponse {
        GeOfficialItem item;
    }
    private static class GeOfficialItem {
        int id;
        String name;
        GeOfficialCurrent current;
    }
    private static class GeOfficialCurrent {
        String trend;
        String price;
    }
    private static class PriceCacheEntry {
        long price;
        long timestamp;
        PriceCacheEntry(long price, long timestamp) {
            this.price = price;
            this.timestamp = timestamp;
        }
    }

    // Indre klasse for Inventory Overlay – vises fast i inventory-widgeten
    public class InventoryTextOverlay extends Overlay {
        public InventoryTextOverlay() {
            setPosition(OverlayPosition.DYNAMIC);
            setLayer(OverlayLayer.ABOVE_WIDGETS);
        }

        public void updateRisk(int risk) {
            PvPWarningOverlay.this.risk = risk;
        }

        @Override
        public Dimension render(Graphics2D graphics) {
            Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
            if (inventoryWidget == null || inventoryWidget.isHidden()) {
                return null;
            }
            Rectangle bounds = inventoryWidget.getBounds();
            String text = "Risk: " + NumberFormat.getInstance().format(risk) + " GP";
            Font font = new Font("Arial", Font.BOLD, 14);
            graphics.setFont(font);
            graphics.setColor(Color.WHITE);
            int textWidth = graphics.getFontMetrics().stringWidth(text);
            int x = bounds.x + (bounds.width - textWidth) / 2;
            int yTop = bounds.y + graphics.getFontMetrics().getAscent() + 2;
            int yBottom = bounds.y + bounds.height - 2;
            int y = config.inventoryOverlayPosition() == PvPWarningConfig.InventoryOverlayPosition.TOP ? yTop : yBottom;
            graphics.drawString(text, x, y);
            return null;
        }
    }

    // Nested enum slik at InventoryTextOverlay kan referere til den
    public enum InventoryOverlayPosition {
        TOP,
        BOTTOM
    }
}
