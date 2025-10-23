package autoeatmod.event;

import autoeatmod.AutoEatMod;
import autoeatmod.config.AutoEatConfig;
import autoeatmod.food.FoodCategorizer;
import autoeatmod.statistics.FoodStatistics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AutoEatHandler {

    private static int eatingTicks = 0;
    private static int selectedSlot = -1;
    private static InteractionHand eatingHand = InteractionHand.MAIN_HAND;
    private static long lastDamageTime = 0;
    private static int tickCounter = 0;
    private static boolean panicMode = false;
    private static boolean isCurrentlyEating = false;
    private static int startEatingDelay = 0; // Delay to allow item use to start

    // Cache for blacklist/whitelist
    private static Set<String> cachedBlacklist = new HashSet<>();
    private static Set<String> cachedWhitelist = new HashSet<>();
    private static boolean cacheInitialized = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        // Check if player exists and config is enabled
        if (player == null || !AutoEatConfig.CONFIG.enabled.get()) {
            if (player != null && isCurrentlyEating) {
                resetEating(player);
            }
            return;
        }

        // If currently eating, wait until finished
        if (isCurrentlyEating) {
            eatingTicks++;

            // Give the game a few ticks to start using the item
            if (startEatingDelay > 0) {
                startEatingDelay--;
                return;
            }

            // Check if still eating
            if (player.isUsingItem()) {
                return; // Keep eating, don't interrupt
            } else if (eatingTicks > 5) {
                // Only reset if we've been "eating" for at least 5 ticks
                // This ensures the item use actually started
                resetEating(player);
                tickCounter = AutoEatConfig.CONFIG.checkInterval.get(); // Don't check immediately
            }
            return;
        }

        // Performance optimization: Only check every N ticks (when not eating)
        tickCounter++;
        if (tickCounter < AutoEatConfig.CONFIG.checkInterval.get()) {
            return;
        }
        tickCounter = 0;

        // Initialize cache if needed
        if (!cacheInitialized) {
            initializeCache();
        }

        // Check if player is in a GUI
        if (minecraft.screen != null) {
            return;
        }

        // Check panic mode
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        boolean shouldEnterPanicMode = AutoEatConfig.CONFIG.enablePanicMode.get()
            && health <= AutoEatConfig.CONFIG.panicHealthThreshold.get();

        if (shouldEnterPanicMode && !panicMode) {
            panicMode = true;

            // Play panic sound
            if (AutoEatConfig.CONFIG.playSounds.get()) {
                player.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 0.5F);
            }

            // Show notification
            if (AutoEatConfig.CONFIG.showNotifications.get()) {
                minecraft.getToasts().addToast(
                    net.minecraft.client.gui.components.toasts.SystemToast.multiline(minecraft,
                        net.minecraft.client.gui.components.toasts.SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                        net.minecraft.network.chat.Component.literal("§c⚠ Panic Mode!"),
                        net.minecraft.network.chat.Component.literal("Health critically low!"))
                );
            }

            AutoEatMod.LOGGER.info("Panic mode activated! Health: " + health);
        } else if (!shouldEnterPanicMode && panicMode) {
            panicMode = false;
            AutoEatMod.LOGGER.info("Panic mode deactivated.");
        }

        // Check damage cooldown
        if (AutoEatConfig.CONFIG.pauseOnDamage.get()) {
            long currentTime = System.currentTimeMillis();
            long cooldownMs = AutoEatConfig.CONFIG.pauseCooldown.get() * 50L; // Convert ticks to ms
            if (currentTime - lastDamageTime < cooldownMs) {
                return;
            }
        }

        FoodData foodData = player.getFoodData();
        int hungerLevel = foodData.getFoodLevel();
        int threshold = AutoEatConfig.CONFIG.hungerThreshold.get();

        // In panic mode, eat even if hunger is higher
        if (panicMode) {
            threshold = Math.max(threshold, 18);
        }

        // Check if we need to eat
        boolean shouldEat = hungerLevel < threshold;
        if (AutoEatConfig.CONFIG.eatUntilFull.get()) {
            shouldEat = hungerLevel < 20;
        }

        if (!shouldEat) {
            return;
        }

        // Find best food item
        FoodSlot bestFood = findBestFood(player, panicMode);

        if (bestFood == null) {
            return;
        }

        // Start eating
        startEating(player, bestFood);
    }

    @SubscribeEvent
    public static void onPlayerDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof LocalPlayer && AutoEatConfig.CONFIG.pauseOnDamage.get()) {
            lastDamageTime = System.currentTimeMillis();
            LocalPlayer player = (LocalPlayer) event.getEntity();
            if (player.isUsingItem()) {
                player.stopUsingItem();
                resetEating(player);
            }
        }
    }

    private static void initializeCache() {
        cachedBlacklist.clear();
        cachedWhitelist.clear();

        cachedBlacklist.addAll((List<String>) AutoEatConfig.CONFIG.blacklist.get());
        cachedWhitelist.addAll((List<String>) AutoEatConfig.CONFIG.whitelist.get());

        cacheInitialized = true;
        AutoEatMod.LOGGER.info("Filter cache initialized. Blacklist: " + cachedBlacklist.size() + ", Whitelist: " + cachedWhitelist.size());
    }

    private static void startEating(LocalPlayer player, FoodSlot foodSlot) {
        Minecraft minecraft = Minecraft.getInstance();
        selectedSlot = player.getInventory().selected;
        eatingHand = foodSlot.hand;
        eatingTicks = 0;
        isCurrentlyEating = true;
        startEatingDelay = 3; // Give 3 ticks for item use to start

        if (foodSlot.hand == InteractionHand.MAIN_HAND && foodSlot.slot != player.getInventory().selected) {
            player.getInventory().selected = foodSlot.slot;
        }

        // Use Minecraft.getInstance().gameMode to interact with the item correctly
        ItemStack foodStack = player.getItemInHand(foodSlot.hand);

        // Start using the item using the game controller
        minecraft.gameMode.useItem(player, foodSlot.hand);

        // Track statistics
        if (AutoEatConfig.CONFIG.trackStatistics.get()) {
            FoodStatistics.getInstance().recordFoodEaten(foodStack.getItem());
        }

        // Play eating sound
        if (AutoEatConfig.CONFIG.playSounds.get()) {
            player.playSound(net.minecraft.sounds.SoundEvents.GENERIC_EAT, 0.5F, 1.0F);
        }

        AutoEatMod.LOGGER.info("Started eating: " + foodStack.getDisplayName().getString() +
            (panicMode ? " [PANIC MODE]" : ""));
    }

    private static void resetEating(LocalPlayer player) {
        if (player != null && selectedSlot != -1 && selectedSlot != player.getInventory().selected) {
            player.getInventory().selected = selectedSlot;
        }
        eatingTicks = 0;
        selectedSlot = -1;
        isCurrentlyEating = false;
        startEatingDelay = 0;
        AutoEatMod.LOGGER.info("Finished eating");
    }

    private static FoodSlot findBestFood(LocalPlayer player, boolean isPanicMode) {
        FoodSlot bestFood = null;
        double bestScore = -1;

        // Check offhand first if enabled
        if (AutoEatConfig.CONFIG.useOffhand.get()) {
            ItemStack offhandStack = player.getOffhandItem();
            if (isValidFood(offhandStack, player, isPanicMode)) {
                FoodProperties foodProps = offhandStack.getFoodProperties(player);
                if (foodProps != null) {
                    double score = calculateFoodScore(offhandStack, foodProps, isPanicMode);
                    bestFood = new FoodSlot(-1, InteractionHand.OFF_HAND, score);
                    if (!AutoEatConfig.CONFIG.prioritizeBetterFood.get() && !isPanicMode) {
                        return bestFood; // Use first food found if not prioritizing
                    }
                    bestScore = score;
                }
            }
        }

        // Check inventory
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isValidFood(stack, player, isPanicMode)) {
                FoodProperties foodProps = stack.getFoodProperties(player);
                if (foodProps != null) {
                    double score = calculateFoodScore(stack, foodProps, isPanicMode);
                    if (score > bestScore) {
                        bestFood = new FoodSlot(i, InteractionHand.MAIN_HAND, score);
                        bestScore = score;
                        if (!AutoEatConfig.CONFIG.prioritizeBetterFood.get() && !isPanicMode) {
                            return bestFood; // Use first food found if not prioritizing
                        }
                    }
                }
            }
        }

        return bestFood;
    }

    private static boolean isValidFood(ItemStack stack, LocalPlayer player, boolean isPanicMode) {
        if (stack.isEmpty() || !isFoodItem(stack)) {
            return false;
        }

        // Get item ID
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

        // Check filter mode
        AutoEatConfig.FilterMode filterMode = AutoEatConfig.CONFIG.filterMode.get();
        if (filterMode == AutoEatConfig.FilterMode.BLACKLIST) {
            if (cachedBlacklist.contains(itemId)) {
                return false;
            }
        } else if (filterMode == AutoEatConfig.FilterMode.WHITELIST) {
            if (!cachedWhitelist.contains(itemId)) {
                return false;
            }
        }

        // Check if dangerous
        if (FoodCategorizer.isDangerous(stack.getItem())) {
            return false;
        }

        // Check for negative effects
        if (AutoEatConfig.CONFIG.avoidNegativeEffects.get() && !isPanicMode) {
            FoodProperties foodProps = stack.getFoodProperties(player);
            if (foodProps != null && hasNegativeEffects(foodProps)) {
                return false;
            }
        }

        return true;
    }

    private static boolean hasNegativeEffects(FoodProperties foodProps) {
        if (foodProps.effects().isEmpty()) {
            return false;
        }

        for (var effect : foodProps.effects()) {
            MobEffect mobEffect = effect.effect().getEffect().value();

            // Check if effect is harmful
            if (mobEffect == MobEffects.POISON ||
                mobEffect == MobEffects.HUNGER ||
                mobEffect == MobEffects.WEAKNESS ||
                mobEffect == MobEffects.WITHER ||
                mobEffect == MobEffects.CONFUSION ||
                mobEffect == MobEffects.BLINDNESS ||
                mobEffect == MobEffects.MOVEMENT_SLOWDOWN) {
                return true;
            }
        }

        return false;
    }

    private static boolean isFoodItem(ItemStack stack) {
        return stack.getFoodProperties(Minecraft.getInstance().player) != null;
    }

    private static double calculateFoodScore(ItemStack stack, FoodProperties foodProps, boolean isPanicMode) {
        double score = foodProps.nutrition() + (foodProps.saturation() * 2);

        // In panic mode, heavily prioritize premium foods
        if (isPanicMode && AutoEatConfig.CONFIG.panicUsePremiumFood.get()) {
            if (FoodCategorizer.isPremium(stack.getItem())) {
                score *= 10.0; // Huge boost for premium foods
            }
        } else {
            // Normal priority boosting
            int categoryPriority = FoodCategorizer.getPriority(stack.getItem());
            score *= (1.0 + categoryPriority * 0.2);
        }

        return score;
    }

    private static class FoodSlot {
        final int slot;
        final InteractionHand hand;
        final double score;

        FoodSlot(int slot, InteractionHand hand, double score) {
            this.slot = slot;
            this.hand = hand;
            this.score = score;
        }
    }

    // Reset cache when config changes
    public static void resetCache() {
        cacheInitialized = false;
    }
}
