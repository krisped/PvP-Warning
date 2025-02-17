package com.krisped;

import com.google.gson.Gson;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.InventoryID;
import net.runelite.api.Varbits;
import net.runelite.client.game.ItemManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class PriceManager {
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
    private static final long OSRS_WIKI_CACHE_DURATION = 300000;
    private static Map<Integer, Long> osrsWikiPrices;
    private static long osrsWikiPricesLastFetch = 0;

    // Henter prisen for et item basert på konfigurasjonen
    public static long getPriceForItem(int itemId, ItemManager itemManager, PvPWarningConfig config) {
        if (MANUAL_PRICES.containsKey(itemId))
            return MANUAL_PRICES.get(itemId);
        if (config.priceSource() == PvPWarningConfig.PriceSource.RUNELITE)
            return itemManager.getItemPrice(itemId);
        else if (config.priceSource() == PvPWarningConfig.PriceSource.OSRS_WIKI)
            return getOsrsWikiPrice(itemId);
        return 0L;
    }

    private static long getOsrsWikiPrice(int itemId) {
        Map<Integer, Long> prices = getOsrsWikiPrices();
        return prices.getOrDefault(itemId, 0L);
    }

    private static Map<Integer, Long> getOsrsWikiPrices() {
        long now = System.currentTimeMillis();
        if (osrsWikiPrices != null && (now - osrsWikiPricesLastFetch) < OSRS_WIKI_CACHE_DURATION) {
            return osrsWikiPrices;
        }
        Map<Integer, Long> prices = new HashMap<>();
        try {
            URL url = new URL("https://prices.runescape.wiki/api/v1/osrs/latest");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream is = connection.getInputStream();
                Reader reader = new InputStreamReader(is);
                Gson gson = new Gson();
                OSRSWikiResponse response = gson.fromJson(reader, OSRSWikiResponse.class);
                if (response != null && response.data != null) {
                    for (Map.Entry<String, OSRSItemPrice> entry : response.data.entrySet()) {
                        int id = Integer.parseInt(entry.getKey());
                        OSRSItemPrice itemPrice = entry.getValue();
                        long price = itemPrice.high != 0 ? itemPrice.high :
                                (itemPrice.buy_average != 0 ? itemPrice.buy_average : itemPrice.sell_average);
                        prices.put(id, price);
                    }
                }
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        osrsWikiPrices = prices;
        osrsWikiPricesLastFetch = System.currentTimeMillis();
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

    // Beregner total risk basert på items, PvP-skull og Protect Item
    public static int computeRisk(Client client, ItemManager itemManager, PvPWarningConfig config) {
        int total = 0;
        List<Item> allItems = new ArrayList<>();
        if (client.getItemContainer(InventoryID.INVENTORY) != null) {
            allItems.addAll(Arrays.asList(client.getItemContainer(InventoryID.INVENTORY).getItems()));
        }
        if (client.getItemContainer(InventoryID.EQUIPMENT) != null) {
            allItems.addAll(Arrays.asList(client.getItemContainer(InventoryID.EQUIPMENT).getItems()));
        }
        for (Item item : allItems) {
            if (item != null && item.getId() != -1) {
                long price = getPriceForItem(item.getId(), itemManager, config);
                total += price * item.getQuantity();
            }
        }

        // Justering basert på PvP-skull og Protect Item
        if (config.riskBasedOnPvPSkull()) {
            boolean skulled = client.getLocalPlayer().getSkullIcon() != -1;
            if (!skulled) {
                int numToSubtract = 3;
                if (config.protectItem() && isProtectItemPrayerActive(client))
                    numToSubtract = 4;
                List<Long> pricesList = new ArrayList<>();
                for (Item item : allItems) {
                    if (item != null && item.getId() != -1)
                        pricesList.add(getPriceForItem(item.getId(), itemManager, config));
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
                if (config.protectItem() && isProtectItemPrayerActive(client)) {
                    long maxPrice = 0;
                    for (Item item : allItems) {
                        if (item != null && item.getId() != -1) {
                            long price = getPriceForItem(item.getId(), itemManager, config);
                            if (price > maxPrice)
                                maxPrice = price;
                        }
                    }
                    total -= maxPrice;
                    if (total < 0)
                        total = 0;
                }
            }
        } else {
            if (config.protectItem() && isProtectItemPrayerActive(client)) {
                long maxPrice = 0;
                for (Item item : allItems) {
                    if (item != null && item.getId() != -1) {
                        long price = getPriceForItem(item.getId(), itemManager, config);
                        if (price > maxPrice)
                            maxPrice = price;
                    }
                }
                total -= maxPrice;
                if (total < 0)
                    total = 0;
            }
        }
        return total;
    }

    private static boolean isProtectItemPrayerActive(Client client) {
        return client.getVar(Varbits.PRAYER_PROTECT_ITEM) == 1;
    }
}
