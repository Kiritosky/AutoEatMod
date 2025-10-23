package autoeatmod.client.gui;

import autoeatmod.config.AutoEatConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AutoEatConfigScreen extends Screen {

    private final Screen parent;
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SPACING = 25;

    public AutoEatConfigScreen(Screen parent) {
        super(Component.translatable("autoeat.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int yStart = this.height / 2 - 120;
        int xCenter = this.width / 2 - BUTTON_WIDTH / 2;

        // Enabled toggle
        this.addRenderableWidget(CycleButton.onOffBuilder(AutoEatConfig.CONFIG.enabled.get())
                .withTooltip((value) -> net.minecraft.client.gui.components.Tooltip.create(
                        Component.translatable("autoeat.config.enabled.tooltip")))
                .create(xCenter, yStart, BUTTON_WIDTH, BUTTON_HEIGHT,
                        Component.translatable("autoeat.config.enabled"),
                        (button, value) -> AutoEatConfig.CONFIG.enabled.set(value))
        );

        // Hunger threshold slider
        yStart += SPACING;
        this.addRenderableWidget(Button.builder(
                Component.translatable("autoeat.config.hungerThreshold",
                        AutoEatConfig.CONFIG.hungerThreshold.get()),
                button -> {}
        ).bounds(xCenter, yStart, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        // Eat until full toggle
        yStart += SPACING;
        this.addRenderableWidget(CycleButton.onOffBuilder(AutoEatConfig.CONFIG.eatUntilFull.get())
                .withTooltip((value) -> net.minecraft.client.gui.components.Tooltip.create(
                        Component.translatable("autoeat.config.eatUntilFull.tooltip")))
                .create(xCenter, yStart, BUTTON_WIDTH, BUTTON_HEIGHT,
                        Component.translatable("autoeat.config.eatUntilFull"),
                        (button, value) -> AutoEatConfig.CONFIG.eatUntilFull.set(value))
        );

        // Prioritize better food toggle
        yStart += SPACING;
        this.addRenderableWidget(CycleButton.onOffBuilder(AutoEatConfig.CONFIG.prioritizeBetterFood.get())
                .withTooltip((value) -> net.minecraft.client.gui.components.Tooltip.create(
                        Component.translatable("autoeat.config.prioritizeBetterFood.tooltip")))
                .create(xCenter, yStart, BUTTON_WIDTH, BUTTON_HEIGHT,
                        Component.translatable("autoeat.config.prioritizeBetterFood"),
                        (button, value) -> AutoEatConfig.CONFIG.prioritizeBetterFood.set(value))
        );

        // Use offhand toggle
        yStart += SPACING;
        this.addRenderableWidget(CycleButton.onOffBuilder(AutoEatConfig.CONFIG.useOffhand.get())
                .withTooltip((value) -> net.minecraft.client.gui.components.Tooltip.create(
                        Component.translatable("autoeat.config.useOffhand.tooltip")))
                .create(xCenter, yStart, BUTTON_WIDTH, BUTTON_HEIGHT,
                        Component.translatable("autoeat.config.useOffhand"),
                        (button, value) -> AutoEatConfig.CONFIG.useOffhand.set(value))
        );

        // Pause on damage toggle
        yStart += SPACING;
        this.addRenderableWidget(CycleButton.onOffBuilder(AutoEatConfig.CONFIG.pauseOnDamage.get())
                .withTooltip((value) -> net.minecraft.client.gui.components.Tooltip.create(
                        Component.translatable("autoeat.config.pauseOnDamage.tooltip")))
                .create(xCenter, yStart, BUTTON_WIDTH, BUTTON_HEIGHT,
                        Component.translatable("autoeat.config.pauseOnDamage"),
                        (button, value) -> AutoEatConfig.CONFIG.pauseOnDamage.set(value))
        );

        // Show notifications toggle
        yStart += SPACING;
        this.addRenderableWidget(CycleButton.onOffBuilder(AutoEatConfig.CONFIG.showNotifications.get())
                .withTooltip((value) -> net.minecraft.client.gui.components.Tooltip.create(
                        Component.translatable("autoeat.config.showNotifications.tooltip")))
                .create(xCenter, yStart, BUTTON_WIDTH, BUTTON_HEIGHT,
                        Component.translatable("autoeat.config.showNotifications"),
                        (button, value) -> AutoEatConfig.CONFIG.showNotifications.set(value))
        );

        // Done button
        yStart += SPACING + 10;
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                button -> this.minecraft.setScreen(parent)
        ).bounds(xCenter, yStart, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        // Draw title
        graphics.drawCenteredString(this.font, this.title,
                this.width / 2, 20, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
