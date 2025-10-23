package plugin.autoeatmod;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plugin.autoeatmod.config.AutoEatConfig;

@Mod(AutoEatMod.MOD_ID)
public class AutoEatMod {
    public static final String MOD_ID = "autoeat";
    public static final Logger LOGGER = LoggerFactory.getLogger(AutoEatMod.class);

    public AutoEatMod(IEventBus modEventBus, ModContainer container) {
        LOGGER.info("Initializing AutoEat Mod");

        // Register configuration
        container.registerConfig(ModConfig.Type.CLIENT, AutoEatConfig.SPEC);

        LOGGER.info("AutoEat Mod initialized successfully");
    }
}
