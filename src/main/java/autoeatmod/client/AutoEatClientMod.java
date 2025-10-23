package plugin.autoeatmod.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import plugin.autoeatmod.AutoEatMod;
import plugin.autoeatmod.client.gui.AutoEatConfigScreen;

@Mod(value = AutoEatMod.MOD_ID, dist = Dist.CLIENT)
public class AutoEatClientMod {

    public AutoEatClientMod(IEventBus modEventBus, ModContainer container) {
        // Register key bindings
        modEventBus.addListener(KeyBindings::registerKeyBindings);

        // Register event handlers
        NeoForge.EVENT_BUS.register(AutoEatHandler.class);
        NeoForge.EVENT_BUS.register(KeyBindings.class);

        // Register config screen
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (client, parent) -> new AutoEatConfigScreen(parent));
    }
}