package autoeatmod.client.hud;

import autoeatmod.config.AutoEatConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

public class AutoEatHUD {

    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGuiEvent.Post event) {
        if (!AutoEatConfig.CONFIG.showHUD.get()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Calculate position based on config
        AutoEatConfig.HUDPosition position = AutoEatConfig.CONFIG.hudPosition.get();
        int x = AutoEatConfig.CONFIG.hudOffsetX.get();
        int y = AutoEatConfig.CONFIG.hudOffsetY.get();

        switch (position) {
            case TOP_LEFT:
                // x and y are already correct
                break;
            case TOP_RIGHT:
                x = screenWidth - 120 + x;
                break;
            case BOTTOM_LEFT:
                y = screenHeight - 40 + y;
                break;
            case BOTTOM_RIGHT:
                x = screenWidth - 120 + x;
                y = screenHeight - 40 + y;
                break;
        }

        // Draw background
        int bgColor = 0x90000000; // Semi-transparent black
        graphics.fill(x, y, x + 110, y + 35, bgColor);

        // Draw border
        int borderColor = AutoEatConfig.CONFIG.enabled.get() ? 0xFF4A90E2 : 0xFF808080;
        graphics.fill(x, y, x + 110, y + 1, borderColor);
        graphics.fill(x, y + 34, x + 110, y + 35, borderColor);
        graphics.fill(x, y, x + 1, y + 35, borderColor);
        graphics.fill(x + 109, y, x + 110, y + 35, borderColor);

        int textY = y + 5;

        // Draw status
        if (AutoEatConfig.CONFIG.hudShowStatus.get()) {
            String statusText = "AutoEat: " + (AutoEatConfig.CONFIG.enabled.get() ? "§aON" : "§cOFF");
            graphics.drawString(mc.font, Component.literal(statusText), x + 5, textY, 0xFFFFFF);
            textY += 10;
        }

        // Draw hunger info
        if (AutoEatConfig.CONFIG.hudShowHunger.get()) {
            int hunger = mc.player.getFoodData().getFoodLevel();
            int maxHunger = 20;
            String hungerText = String.format("Hunger: §e%d§r/§e%d", hunger, maxHunger);
            graphics.drawString(mc.font, Component.literal(hungerText), x + 5, textY, 0xFFFFFF);
            textY += 10;

            // Draw hunger bar
            int barWidth = 100;
            int barHeight = 4;
            int barX = x + 5;
            int barY = textY;

            // Background bar
            graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);

            // Hunger bar
            int hungerWidth = (int) ((hunger / (float) maxHunger) * barWidth);
            int hungerColor = hunger > 14 ? 0xFF4CAF50 : hunger > 6 ? 0xFFFFA726 : 0xFFE53935;
            graphics.fill(barX, barY, barX + hungerWidth, barY + barHeight, hungerColor);
        }
    }
}
