package autoeatmod.client;

import autoeatmod.client.gui.ModernConfigScreen;
import autoeatmod.event.AutoEatHandler;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;
import autoeatmod.config.AutoEatConfig;

import java.util.ArrayList;
import java.util.List;

public class KeyBindings {

    public static final String CATEGORY = "key.categories.autoeat";

    public static final KeyMapping TOGGLE_KEY = new KeyMapping(
            "key.autoeat.toggle",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            CATEGORY
    );

    public static final KeyMapping OPEN_CONFIG_KEY = new KeyMapping(
            "key.autoeat.config",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            CATEGORY
    );

    public static final KeyMapping ADD_TO_FILTER_KEY = new KeyMapping(
            "key.autoeat.addfilter",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            CATEGORY
    );

    // This is registered via modEventBus.addListener in AutoEatClientMod
    public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_KEY);
        event.register(OPEN_CONFIG_KEY);
        event.register(ADD_TO_FILTER_KEY);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        // Toggle auto-eat
        if (TOGGLE_KEY.consumeClick()) {
            boolean currentState = AutoEatConfig.CONFIG.enabled.get();
            AutoEatConfig.CONFIG.enabled.set(!currentState);

            // Show notification
            if (AutoEatConfig.CONFIG.showNotifications.get()) {
                String status = !currentState ? "enabled" : "disabled";
                minecraft.getToasts().addToast(
                        SystemToast.multiline(minecraft,
                                SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                                Component.translatable("autoeat.toast.toggle"),
                                Component.translatable("autoeat.toast." + status))
                );
            }

            // Play sound
            if (AutoEatConfig.CONFIG.playSounds.get()) {
                minecraft.player.playSound(
                        net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,
                        1.0F,
                        !currentState ? 1.2F : 0.8F
                );
            }
        }

        // Open config screen
        if (OPEN_CONFIG_KEY.consumeClick() && minecraft.screen == null) {
            minecraft.setScreen(new ModernConfigScreen(minecraft.screen));
        }

        // Add/Remove item from filter
        if (ADD_TO_FILTER_KEY.consumeClick() && minecraft.player != null) {
            ItemStack heldItem = minecraft.player.getMainHandItem();

            // Check if item is food
            if (heldItem.isEmpty() || heldItem.getFoodProperties(minecraft.player) == null) {
                // Show error message
                minecraft.getToasts().addToast(
                        SystemToast.multiline(minecraft,
                                SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                                Component.literal("§cAutoEat Filter"),
                                Component.literal("§7You must hold food!"))
                );
                return;
            }

            String itemId = BuiltInRegistries.ITEM.getKey(heldItem.getItem()).toString();
            AutoEatConfig.FilterMode mode = AutoEatConfig.CONFIG.filterMode.get();

            boolean added = false;
            String listName = "";

            if (mode == AutoEatConfig.FilterMode.BLACKLIST) {
                List<String> blacklist = new ArrayList<>((List<String>) AutoEatConfig.CONFIG.blacklist.get());
                if (blacklist.contains(itemId)) {
                    blacklist.remove(itemId);
                    listName = "Removed from Blacklist";
                } else {
                    blacklist.add(itemId);
                    added = true;
                    listName = "Added to Blacklist";
                }
                AutoEatConfig.CONFIG.blacklist.set(blacklist);
            } else {
                List<String> whitelist = new ArrayList<>((List<String>) AutoEatConfig.CONFIG.whitelist.get());
                if (whitelist.contains(itemId)) {
                    whitelist.remove(itemId);
                    listName = "Removed from Whitelist";
                } else {
                    whitelist.add(itemId);
                    added = true;
                    listName = "Added to Whitelist";
                }
                AutoEatConfig.CONFIG.whitelist.set(whitelist);
            }

            // Reset cache
            AutoEatHandler.resetCache();

            // Show notification
            String itemName = heldItem.getHoverName().getString();
            minecraft.getToasts().addToast(
                    SystemToast.multiline(minecraft,
                            SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                            Component.literal("§6AutoEat Filter"),
                            Component.literal((added ? "§a" : "§c") + listName + ": §f" + itemName))
            );

            // Play sound
            if (AutoEatConfig.CONFIG.playSounds.get()) {
                minecraft.player.playSound(
                        net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,
                        1.0F,
                        added ? 1.2F : 0.8F
                );
            }
        }
    }
}