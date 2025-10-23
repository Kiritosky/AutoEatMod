package autoeatmod.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

public class AutoEatConfig {
    public static final AutoEatConfig CONFIG;
    public static final ModConfigSpec SPEC;

    static {
        Pair<AutoEatConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(AutoEatConfig::new);
        CONFIG = pair.getLeft();
        SPEC = pair.getRight();
    }

    // General
    public final ModConfigSpec.BooleanValue enabled;
    public final ModConfigSpec.IntValue hungerThreshold;
    public final ModConfigSpec.BooleanValue eatUntilFull;
    public final ModConfigSpec.BooleanValue prioritizeBetterFood;
    public final ModConfigSpec.BooleanValue useOffhand;
    public final ModConfigSpec.IntValue checkInterval;

    // Advanced
    public final ModConfigSpec.BooleanValue pauseOnDamage;
    public final ModConfigSpec.IntValue pauseCooldown;
    public final ModConfigSpec.BooleanValue showNotifications;
    public final ModConfigSpec.BooleanValue playSounds;
    public final ModConfigSpec.BooleanValue avoidNegativeEffects;

    // Panic Mode
    public final ModConfigSpec.BooleanValue enablePanicMode;
    public final ModConfigSpec.IntValue panicHealthThreshold;
    public final ModConfigSpec.BooleanValue panicUsePremiumFood;

    // Filter System
    public final ModConfigSpec.EnumValue<FilterMode> filterMode;
    public final ModConfigSpec.ConfigValue<List<? extends String>> blacklist;
    public final ModConfigSpec.ConfigValue<List<? extends String>> whitelist;

    // HUD
    public final ModConfigSpec.BooleanValue showHUD;
    public final ModConfigSpec.EnumValue<HUDPosition> hudPosition;
    public final ModConfigSpec.IntValue hudOffsetX;
    public final ModConfigSpec.IntValue hudOffsetY;
    public final ModConfigSpec.BooleanValue hudShowHunger;
    public final ModConfigSpec.BooleanValue hudShowStatus;

    // Statistics
    public final ModConfigSpec.BooleanValue trackStatistics;

    private AutoEatConfig(ModConfigSpec.Builder builder) {
        builder.push("general");

        enabled = builder
                .comment("Enable or disable auto-eating")
                .translation("autoeat.config.enabled")
                .define("enabled", true);

        hungerThreshold = builder
                .comment("Hunger level threshold to start eating (0-20, where 20 is full)")
                .translation("autoeat.config.hungerThreshold")
                .defineInRange("hungerThreshold", 14, 0, 20);

        eatUntilFull = builder
                .comment("Continue eating until hunger bar is completely full")
                .translation("autoeat.config.eatUntilFull")
                .define("eatUntilFull", false);

        prioritizeBetterFood = builder
                .comment("Prioritize food with better saturation values")
                .translation("autoeat.config.prioritizeBetterFood")
                .define("prioritizeBetterFood", true);

        useOffhand = builder
                .comment("Check offhand for food first")
                .translation("autoeat.config.useOffhand")
                .define("useOffhand", true);

        checkInterval = builder
                .comment("How often to check for eating in ticks (20 ticks = 1 second). Higher = better performance")
                .translation("autoeat.config.checkInterval")
                .defineInRange("checkInterval", 10, 1, 100);

        builder.pop();

        builder.push("advanced");

        pauseOnDamage = builder
                .comment("Stop eating when taking damage")
                .translation("autoeat.config.pauseOnDamage")
                .define("pauseOnDamage", true);

        pauseCooldown = builder
                .comment("Cooldown in ticks after taking damage before eating again (20 ticks = 1 second)")
                .translation("autoeat.config.pauseCooldown")
                .defineInRange("pauseCooldown", 40, 0, 200);

        showNotifications = builder
                .comment("Show toast notifications when auto-eat is toggled")
                .translation("autoeat.config.showNotifications")
                .define("showNotifications", true);

        playSounds = builder
                .comment("Play sounds for auto-eat events")
                .translation("autoeat.config.playSounds")
                .define("playSounds", true);

        avoidNegativeEffects = builder
                .comment("Avoid eating food that gives negative status effects")
                .translation("autoeat.config.avoidNegativeEffects")
                .define("avoidNegativeEffects", true);

        builder.pop();

        builder.push("panicMode");

        enablePanicMode = builder
                .comment("Enable panic mode when health is critically low")
                .translation("autoeat.config.enablePanicMode")
                .define("enablePanicMode", true);

        panicHealthThreshold = builder
                .comment("Health threshold to trigger panic mode (in half hearts, 20 = full health)")
                .translation("autoeat.config.panicHealthThreshold")
                .defineInRange("panicHealthThreshold", 6, 1, 20);

        panicUsePremiumFood = builder
                .comment("Use premium food (golden apples, etc.) in panic mode")
                .translation("autoeat.config.panicUsePremiumFood")
                .define("panicUsePremiumFood", true);

        builder.pop();

        builder.push("filter");

        filterMode = builder
                .comment("Filter mode: BLACKLIST (block specific items) or WHITELIST (only allow specific items)")
                .defineEnum("filterMode", FilterMode.BLACKLIST);

        blacklist = builder
                .comment("Items that should never be eaten automatically",
                        "Format: minecraft:item_id or modid:item_id")
                .defineList("blacklist", Arrays.asList(
                        "minecraft:rotten_flesh",
                        "minecraft:spider_eye",
                        "minecraft:poisonous_potato",
                        "minecraft:pufferfish",
                        "minecraft:chorus_fruit"
                ), obj -> obj instanceof String);

        whitelist = builder
                .comment("Items that are allowed to be eaten (only used in WHITELIST mode)",
                        "Format: minecraft:item_id or modid:item_id")
                .defineList("whitelist", Arrays.asList(
                        "minecraft:cooked_beef",
                        "minecraft:cooked_porkchop",
                        "minecraft:golden_carrot",
                        "minecraft:bread"
                ), obj -> obj instanceof String);

        builder.pop();

        builder.push("hud");

        showHUD = builder
                .comment("Show HUD overlay with auto-eat status")
                .define("showHUD", true);

        hudPosition = builder
                .comment("Position of the HUD overlay")
                .defineEnum("hudPosition", HUDPosition.TOP_LEFT);

        hudOffsetX = builder
                .comment("Horizontal offset from the HUD position")
                .defineInRange("hudOffsetX", 5, -1000, 1000);

        hudOffsetY = builder
                .comment("Vertical offset from the HUD position")
                .defineInRange("hudOffsetY", 5, -1000, 1000);

        hudShowHunger = builder
                .comment("Show hunger level in HUD")
                .define("hudShowHunger", true);

        hudShowStatus = builder
                .comment("Show enabled/disabled status in HUD")
                .define("hudShowStatus", true);

        builder.pop();

        builder.push("statistics");

        trackStatistics = builder
                .comment("Track statistics about auto-eating")
                .define("trackStatistics", true);

        builder.pop();
    }

    public enum FilterMode {
        BLACKLIST,
        WHITELIST
    }

    public enum HUDPosition {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }
}