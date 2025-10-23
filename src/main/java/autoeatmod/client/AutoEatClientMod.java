package autoeatmod.client;

import autoeatmod.event.AutoEatHandler;
import autoeatmod.client.hud.AutoEatHUD;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import autoeatmod.client.gui.ModernConfigScreen;

public class AutoEatClientMod {

    public static void init(IEventBus modEventBus, ModContainer container) {
        // Register key bindings
        modEventBus.addListener(KeyBindings::registerKeyBindings);

        // Register event handlers
        NeoForge.EVENT_BUS.register(AutoEatHandler.class);
        NeoForge.EVENT_BUS.register(KeyBindings.class);
        NeoForge.EVENT_BUS.register(AutoEatHUD.class);

        // Register config screen
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (client, parent) -> new ModernConfigScreen(parent));
    }
}