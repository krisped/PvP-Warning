package com.krisped;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
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
import net.runelite.client.game.ItemManager;
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

    // Cache for OSRS Wiki-priser (gyldig i 5 minutter)
    private Map<Integer, Long> osrsWikiPrices;
    private long osrsWikiPricesLastFetch = 0;
    private static final long OSRS_WIKI_CACHE_DURATION = 300000;

    // Cache for OSRS GE Official priser (gyldig i 5 minutter)
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

    // Hjelpefunksjon: Returner manuell pris om tilgjengelig, ellers hent prisen basert på valgt kilde.
    private long getPriceForItem(int id) {
        if (MANUAL_PRICES.containsKey(id)) {
            return MANUAL_PRICES.get(id);
        }
        if (config.priceSource() == PriceSource.RUNELITE) {
            return itemManager.getItemPrice(id);
        } else if (config.priceSource() == PriceSource.OSRS_WIKI) {
            Map<Integer, Long> wiki = getOsrsWikiPrices();
            return wiki.getOrDefault(id, 0L);
        } else if (config.priceSource() == PriceSource.OSRS_GE_OFFICIAL) {
            return getGeOfficialPrice(id);
        }
        return 0L;
    }

    public void updateRisk() {
        int total = 0;
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (inventory != null) {
            for (Item item : inventory.getItems()) {
                if (item != null && item.getId() != -1) {
                    long price = getPriceForItem(item.getId());
                    total += price * item.getQuantity();
                }
            }
        }
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipment != null) {
            for (Item item : equipment.getItems()) {
                if (item != null && item.getId() != -1) {
                    long price = getPriceForItem(item.getId());
                    total += price * item.getQuantity();
                }
            }
        }

        if (config.riskBasedOnPvPSkull()) {
            // Sjekk skull-status via localPlayer.getSkullIcon()
            boolean skulled = client.getLocalPlayer().getSkullIcon() != -1;
            if (!skulled) {
                // Ikke skulled: Trekk fra de 3 dyreste items,
                // eller 4 hvis Protect Item (Protect Item Prayer) er aktiv.
                int numToSubtract = 3;
                if (config.protectItem() && isProtectItemPrayerActive()) {
                    numToSubtract = 4;
                }
                List<Long> priceList = new ArrayList<>();
                if (inventory != null) {
                    for (Item item : inventory.getItems()) {
                        if (item != null && item.getId() != -1) {
                            priceList.add(getPriceForItem(item.getId()));
                        }
                    }
                }
                if (equipment != null) {
                    for (Item item : equipment.getItems()) {
                        if (item != null && item.getId() != -1) {
                            priceList.add(getPriceForItem(item.getId()));
                        }
                    }
                }
                priceList.sort((a, b) -> Long.compare(b, a));
                long subtractSum = 0;
                for (int i = 0; i < numToSubtract && i < priceList.size(); i++) {
                    subtractSum += priceList.get(i);
                }
                total -= subtractSum;
                if (total < 0) {
                    total = 0;
                }
            } else {
                // Skulled: Full risk, men dersom Protect Item er aktiv (dvs. Protect Item Prayer er aktiv), trekk fra den dyreste itemen.
                if (config.protectItem() && isProtectItemPrayerActive()) {
                    long maxPrice = 0;
                    if (inventory != null) {
                        for (Item item : inventory.getItems()) {
                            if (item != null && item.getId() != -1) {
                                long price = getPriceForItem(item.getId());
                                if (price > maxPrice) {
                                    maxPrice = price;
                                }
                            }
                        }
                    }
                    if (equipment != null) {
                        for (Item item : equipment.getItems()) {
                            if (item != null && item.getId() != -1) {
                                long price = getPriceForItem(item.getId());
                                if (price > maxPrice) {
                                    maxPrice = price;
                                }
                            }
                        }
                    }
                    total -= maxPrice;
                    if (total < 0) {
                        total = 0;
                    }
                }
                // Ellers, full risk.
            }
        } else {
            // Standard modus (ikke skull-basert):
            if (config.protectItem() && isProtectItemPrayerActive()) {
                long maxPrice = 0;
                if (inventory != null) {
                    for (Item item : inventory.getItems()) {
                        if (item != null && item.getId() != -1) {
                            long price = getPriceForItem(item.getId());
                            if (price > maxPrice) {
                                maxPrice = price;
                            }
                        }
                    }
                }
                if (equipment != null) {
                    for (Item item : equipment.getItems()) {
                        if (item != null && item.getId() != -1) {
                            long price = getPriceForItem(item.getId());
                            if (price > maxPrice) {
                                maxPrice = price;
                            }
                        }
                    }
                }
                total -= maxPrice;
                if (total < 0) {
                    total = 0;
                }
            }
        }
        risk = total;
    }

    // Sjekk om Protect Item Prayer er aktiv for local player
    private boolean isProtectItemPrayerActive() {
        return client.getVar(Varbits.PRAYER_PROTECT_ITEM) == 1;
    }

    private Map<Integer, Long> getOsrsWikiPrices() {
        if (osrsWikiPrices == null || (System.currentTimeMillis() - osrsWikiPricesLastFetch) > OSRS_WIKI_CACHE_DURATION) {
            osrsWikiPrices = fetchOsrsWikiPrices();
            osrsWikiPricesLastFetch = System.currentTimeMillis();
        }
        return osrsWikiPrices;
    }

    private Map<Integer, Long> fetchOsrsWikiPrices() {
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

    // Metoder for OSRS GE Official priser
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
            URL url = new URL("https://services.runescape.com/m:itemdb_oldschool/api/catalogue/detail.json?item=" + itemId);
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

    // Tolker prisstrenger med suffikser, for eksempel "464.2k" -> 464200
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
}
