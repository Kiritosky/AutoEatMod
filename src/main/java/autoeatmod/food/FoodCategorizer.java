package autoeatmod.food;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public class FoodCategorizer {
    private static final Map<Item, FoodCategory> FOOD_CATEGORIES = new HashMap<>();

    static {
        // Super Premium Foods
        registerFood(Items.ENCHANTED_GOLDEN_APPLE, FoodCategory.SUPER_PREMIUM);
        registerFood(Items.GOLDEN_APPLE, FoodCategory.SUPER_PREMIUM);

        // Premium Foods
        registerFood(Items.GOLDEN_CARROT, FoodCategory.PREMIUM);
        registerFood(Items.COOKED_BEEF, FoodCategory.PREMIUM);
        registerFood(Items.COOKED_PORKCHOP, FoodCategory.PREMIUM);
        registerFood(Items.COOKED_MUTTON, FoodCategory.PREMIUM);
        registerFood(Items.COOKED_SALMON, FoodCategory.PREMIUM);

        // Normal Foods
        registerFood(Items.BREAD, FoodCategory.NORMAL);
        registerFood(Items.COOKED_CHICKEN, FoodCategory.NORMAL);
        registerFood(Items.COOKED_COD, FoodCategory.NORMAL);
        registerFood(Items.COOKED_RABBIT, FoodCategory.NORMAL);
        registerFood(Items.BAKED_POTATO, FoodCategory.NORMAL);
        registerFood(Items.MUSHROOM_STEW, FoodCategory.NORMAL);
        registerFood(Items.RABBIT_STEW, FoodCategory.NORMAL);
        registerFood(Items.BEETROOT_SOUP, FoodCategory.NORMAL);

        // Low Quality Foods
        registerFood(Items.APPLE, FoodCategory.LOW_QUALITY);
        registerFood(Items.CARROT, FoodCategory.LOW_QUALITY);
        registerFood(Items.POTATO, FoodCategory.LOW_QUALITY);
        registerFood(Items.BEETROOT, FoodCategory.LOW_QUALITY);
        registerFood(Items.MELON_SLICE, FoodCategory.LOW_QUALITY);
        registerFood(Items.SWEET_BERRIES, FoodCategory.LOW_QUALITY);
        registerFood(Items.GLOW_BERRIES, FoodCategory.LOW_QUALITY);
        registerFood(Items.COOKIE, FoodCategory.LOW_QUALITY);

        // Dangerous Foods
        registerFood(Items.ROTTEN_FLESH, FoodCategory.DANGEROUS);
        registerFood(Items.SPIDER_EYE, FoodCategory.DANGEROUS);
        registerFood(Items.POISONOUS_POTATO, FoodCategory.DANGEROUS);
        registerFood(Items.PUFFERFISH, FoodCategory.DANGEROUS);
        registerFood(Items.CHORUS_FRUIT, FoodCategory.DANGEROUS);
    }

    private static void registerFood(Item item, FoodCategory category) {
        FOOD_CATEGORIES.put(item, category);
    }

    public static FoodCategory getCategory(Item item) {
        return FOOD_CATEGORIES.getOrDefault(item, FoodCategory.UNKNOWN);
    }

    public static boolean isDangerous(Item item) {
        return getCategory(item).isDangerous();
    }

    public static boolean isPremium(Item item) {
        return getCategory(item).isPremium();
    }

    public static int getPriority(Item item) {
        return getCategory(item).getPriority();
    }
}

