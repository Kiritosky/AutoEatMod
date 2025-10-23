package autoeatmod;

import autoeatmod.config.AutoEatConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(AutoEatMod.MOD_ID)
public final class AutoEatMod {
    public static final String MOD_ID = "autoeat";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public AutoEatMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register config
        modContainer.registerConfig(ModConfig.Type.CLIENT, AutoEatConfig.SPEC);

        // Initialize client-side only if on client
        if (FMLEnvironment.dist == Dist.CLIENT) {
            autoeatmod.client.AutoEatClientMod.init(modEventBus, modContainer);
        }

        LOGGER.info("AutoEat Mod initialized");
    }
}
