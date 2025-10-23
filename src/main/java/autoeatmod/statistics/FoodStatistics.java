package autoeatmod.statistics;

import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FoodStatistics {
    private static final FoodStatistics INSTANCE = new FoodStatistics();

    private final Map<String, Integer> foodCount = new ConcurrentHashMap<>();
    private int totalFoodEaten = 0;
    private long sessionStartTime = System.currentTimeMillis();

    public static FoodStatistics getInstance() {
        return INSTANCE;
    }

    public void recordFoodEaten(Item item) {
        String itemId = BuiltInRegistries.ITEM.getKey(item).toString();
        foodCount.merge(itemId, 1, Integer::sum);
        totalFoodEaten++;
    }

    public int getTotalFoodEaten() {
        return totalFoodEaten;
    }

    public Map<String, Integer> getFoodCount() {
        return new HashMap<>(foodCount);
    }

    public String getMostEatenFood() {
        return foodCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");
    }

    public int getFoodEatenCount(String itemId) {
        return foodCount.getOrDefault(itemId, 0);
    }

    public long getSessionDuration() {
        return (System.currentTimeMillis() - sessionStartTime) / 1000; // seconds
    }

    public void reset() {
        foodCount.clear();
        totalFoodEaten = 0;
        sessionStartTime = System.currentTimeMillis();
    }
}

