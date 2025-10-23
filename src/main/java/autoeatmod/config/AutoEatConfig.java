package plugin.autoeatmod.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class AutoEatConfig {
    public static final AutoEatConfig CONFIG;
    public static final ModConfigSpec SPEC;

    static {
        Pair<AutoEatConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(AutoEatConfig::new);
        CONFIG = pair.getLeft();
        SPEC = pair.getRight();
    }

    // Configuration values
    public final ModConfigSpec.BooleanValue enabled;
    public final ModConfigSpec.IntValue hungerThreshold;
    public final ModConfigSpec.BooleanValue eatUntilFull;
    public final ModConfigSpec.BooleanValue prioritizeBetterFood;
    public final ModConfigSpec.BooleanValue useOffhand;
    public final ModConfigSpec.BooleanValue pauseOnDamage;
    public final ModConfigSpec.IntValue pauseCooldown;
    public final ModConfigSpec.BooleanValue showNotifications;

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

        builder.pop();
    }
}