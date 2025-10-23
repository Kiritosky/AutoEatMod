package autoeatmod.client.gui;

import autoeatmod.client.KeyBindings;
import autoeatmod.config.AutoEatConfig;
import autoeatmod.event.AutoEatHandler;
import autoeatmod.statistics.FoodStatistics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModernConfigScreen extends Screen {
    private final Screen parent;
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SPACING = 25;
    private static final int TAB_WIDTH = 100;
    private static final int TAB_HEIGHT = 25;
    private static final int CONTENT_TOP = 75;
    private static final int CONTENT_BOTTOM_OFFSET = 45;

    private static final int SMALL_BUTTON_WIDTH = 50;
    private static final int LIST_ITEM_HEIGHT = 22;

    private ConfigTab currentTab = ConfigTab.GENERAL;
    private double scrollOffset = 0;
    private double maxScroll = 0;

    private enum ConfigTab {
        GENERAL("General"),
        ADVANCED("Advanced"),
        FILTER("Filter"),
        STATISTICS("Statistics");

        private final String name;

        ConfigTab(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public ModernConfigScreen(Screen parent) {
        super(Component.literal("AutoEat Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();
        this.scrollOffset = 0;

        // Draw tabs at the top
        int tabX = this.width / 2 - (ConfigTab.values().length * TAB_WIDTH / 2);
        int tabY = 40;

        for (ConfigTab tab : ConfigTab.values()) {
            int finalTabX = tabX;
            this.addRenderableWidget(Button.builder(
                Component.literal(tab.getName()),
                button -> {
                    currentTab = tab;
                    this.clearWidgets();
                    this.init();
                }
            ).bounds(finalTabX, tabY, TAB_WIDTH, TAB_HEIGHT).build());
            tabX += TAB_WIDTH;
        }

        // Calculate max scroll BEFORE rendering content
        calculateMaxScroll();

        // Render content based on current tab
        switch (currentTab) {
            case GENERAL:
                initGeneralTab();
                break;
            case ADVANCED:
                initAdvancedTab();
                break;
            case FILTER:
                initFilterTab();
                break;
            case STATISTICS:
                initStatisticsTab();
                break;
        }

        // Done button at the bottom (FIXED POSITION - not scrollable)
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.done"),
            button -> this.minecraft.setScreen(parent)
        ).bounds(this.width / 2 - BUTTON_WIDTH / 2, this.height - 35, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }

    private void calculateMaxScroll() {
        int contentHeight = 0;
        switch (currentTab) {
            case GENERAL -> contentHeight = 6 * SPACING + CONTENT_TOP;
            case ADVANCED -> contentHeight = 9 * SPACING + CONTENT_TOP;
            case FILTER -> {
                // Calculate based on list size
                List<String> currentList = getCurrentFilterList();
                int listHeight = currentList.size() * LIST_ITEM_HEIGHT;
                contentHeight = CONTENT_TOP + 120 + listHeight; // 120 for header info
            }
            case STATISTICS -> contentHeight = 200;
        }

        int availableHeight = this.height - CONTENT_TOP - CONTENT_BOTTOM_OFFSET;
        maxScroll = Math.max(0, contentHeight - availableHeight);
    }

    private void initGeneralTab() {
        int yStart = CONTENT_TOP - (int)scrollOffset;
        int xCenter = this.width / 2 - BUTTON_WIDTH / 2;

        // Calculate the max Y position to avoid Done button
        int maxYPosition = this.height - CONTENT_BOTTOM_OFFSET - BUTTON_HEIGHT;

        // Enabled toggle
        if (yStart >= CONTENT_TOP - BUTTON_HEIGHT && yStart <= maxYPosition) {
            this.addRenderableWidget(CycleButton.onOffBuilder(AutoEatConfig.CONFIG.enabled.get())
                .create(xCenter, yStart, BUTTON_WIDTH, BUTTON_HEIGHT,
                    Component.literal("Enabled"),
                    (button, value) -> AutoEatConfig.CONFIG.enabled.set(value))
            );
        }

        yStart += SPACING;

        // Hunger threshold
        if (yStart >= CONTENT_TOP - BUTTON_HEIGHT && yStart <= maxYPosition) {
            this.addRenderableWidget(Button.builder(
                Component.literal("Hunger Threshold: " + AutoEatConfig.CONFIG.hungerThreshold.get()),
                button -> {
                    int current = AutoEatConfig.CONFIG.hungerThreshold.get();
                    int next = (current + 2) % 21;
                    AutoEatConfig.CONFIG.hungerThreshold.set(next);
                    button.setMessage(Component.literal("Hunger Threshold: " + next));
                }
            ).bounds(xCenter, yStart, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        }

        yStart += SPACING;

        // Check interval
        if (yStart >= CONTENT_TOP - BUTTON_HEIGHT && yStart <= maxYPosition) {
            this.addRenderableWidget(Button.builder(
                Component.literal("Check Interval: " + AutoEatConfig.CONFIG.checkInterval.get() + " ticks"),
                button -> {
                    int current = AutoEatConfig.CONFIG.checkInterval.get();
                    int next = current == 10 ? 20 : current == 20 ? 40 : 10;
                    AutoEatConfig.CONFIG.checkInterval.set(next);
                    button.setMessage(Component.literal("Check Interval: " + next + " ticks"));
                }
            ).bounds(xCenter, yStart, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        }

        yStart += SPACING;

        // Eat until full
        if (yStart >= CONTENT_TOP - BUTTON_HEIGHT && yStart <= maxYPosition) {
            this.addRenderableWidget(CycleButton.onOffBuilder(AutoEatConfig.CONFIG.eatUntilFull.get())
                .create(xCenter, yStart, BUTTON_WIDTH, BUTTON_HEIGHT,
                    Component.literal("Eat Until Full"),
                    (button, value) -> AutoEatConfig.CONFIG.eatUntilFull.set(value))
            );
        }

        yStart += SPACING;

        // Prioritize better food
        if (yStart >= CONTENT_TOP - BUTTON_HEIGHT && yStart <= maxYPosition) {
            this.addRenderableWidget(CycleButton.onOffBuilder(AutoEatConfig.CONFIG.prioritizeBetterFood.get())
                .create(xCenter, yStart, BUTTON_WIDTH, BUTTON_HEIGHT,
                    Component.literal("Prioritize Better Food"),
                    (button, value) -> AutoEatConfig.CONFIG.prioritizeBetterFood.set(value))
            );
        }

        yStart += SPACING;

        // Use offhand
        if (yStart >= CONTENT_TOP - BUTTON_HEIGHT && yStart <= maxYPosition) {
            this.addRenderableWidget(CycleButton.onOffBuilder(AutoEatConfig.CONFIG.useOffhand.get())
                .create(xCenter, yStart, BUTTON_WIDTH, BUTTON_HEIGHT,
                    Component.literal("Use Offhand"),
                    (button, value) -> AutoEatConfig.CONFIG.useOffhand.set(value))
            );
        }
    }

    private void initAdvancedTab() {
        int yStart = CONTENT_TOP - (int)scrollOffset;
        int xCenter = this.width / 2 - BUTTON_WIDTH / 2;

        // Calculate the max Y position to avoid Done button
        int maxYPosition = this.height - CONTENT_BOTTOM_OFFSET - BUTTON_HEIGHT;

        // Pause on damage
        if (yStart >= CONTENT_TOP - BUTTON_HEIGHT && yStart <= maxYPosition) {
            this.addRenderableWidget(CycleButton.onOffBuilder(AutoEatConfig.CONFIG.pauseOnDamage.get())
                .create(xCenter, yStart, BUTTON_WIDTH, BUTTON_HEIGHT,
                    Component.literal("Pause On Damage"),
                    (button, value) -> AutoEatConfig.CONFIG.pauseOnDamage.set(value))
            );
        }

        yStart += SPACING;

        // Show notifications
        if (yStart >= CONTENT_TOP - BUTTON_HEIGHT && yStart <= maxYPosition) {
            this.addRenderableWidget(CycleButton.onOffBuilder(AutoEatConfig.CONFIG.showNotifications.get())
                .create(xCenter, yStart, BUTTON_WIDTH, BUTTON_HEIGHT,
                    Component.literal("Show Notifications"),
                    (button, value) -> AutoEatConfig.CONFIG.showNotifications.set(value))
            );
        }

        yStart += SPACING;

        // Play sounds
        if (yStart >= CONTENT_TOP - BUTTON_HEIGHT && yStart <= maxYPosition) {
            this.addRenderableWidget(CycleButton.onOffBuilder(AutoEatConfig.CONFIG.playSounds.get())
                .create(xCenter, yStart, BUTTON_WIDTH, BUTTON_HEIGHT,
                    Component.literal("Play Sounds"),
                    (button, value) -> AutoEatConfig.CONFIG.playSounds.set(value))
            );
        }

        yStart += SPACING;

        // Avoid negative effects
        if (yStart >= CONTENT_TOP - BUTTON_HEIGHT && yStart <= maxYPosition) {
            this.addRenderableWidget(CycleButton.onOffBuilder(AutoEatConfig.CONFIG.avoidNegativeEffects.get())
                .create(xCenter, yStart, BUTTON_WIDTH, BUTTON_HEIGHT,
                    Component.literal("Avoid Negative Effects"),
                    (button, value) -> AutoEatConfig.CONFIG.avoidNegativeEffects.set(value))
            );
        }

        yStart += SPACING;

        // Enable panic mode
        if (yStart >= CONTENT_TOP - BUTTON_HEIGHT && yStart <= maxYPosition) {
            this.addRenderableWidget(CycleButton.onOffBuilder(AutoEatConfig.CONFIG.enablePanicMode.get())
                .create(xCenter, yStart, BUTTON_WIDTH, BUTTON_HEIGHT,
                    Component.literal("Enable Panic Mode"),
                    (button, value) -> AutoEatConfig.CONFIG.enablePanicMode.set(value))
            );
        }

        yStart += SPACING;

        // Panic health threshold
        if (yStart >= CONTENT_TOP - BUTTON_HEIGHT && yStart <= maxYPosition) {
            this.addRenderableWidget(Button.builder(
                Component.literal("Panic Health: " + AutoEatConfig.CONFIG.panicHealthThreshold.get() + "❤"),
                button -> {
                    int current = AutoEatConfig.CONFIG.panicHealthThreshold.get();
                    int next = (current + 2) % 21;
                    if (next < 1) next = 1;
                    AutoEatConfig.CONFIG.panicHealthThreshold.set(next);
                    button.setMessage(Component.literal("Panic Health: " + next + "❤"));
                }
            ).bounds(xCenter, yStart, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        }

        yStart += SPACING;

        // Show HUD
        if (yStart >= CONTENT_TOP - BUTTON_HEIGHT && yStart <= maxYPosition) {
            this.addRenderableWidget(CycleButton.onOffBuilder(AutoEatConfig.CONFIG.showHUD.get())
                .create(xCenter, yStart, BUTTON_WIDTH, BUTTON_HEIGHT,
                    Component.literal("Show HUD"),
                    (button, value) -> AutoEatConfig.CONFIG.showHUD.set(value))
            );
        }

        yStart += SPACING;

        // HUD Position
        if (yStart >= CONTENT_TOP - BUTTON_HEIGHT && yStart <= maxYPosition) {
            this.addRenderableWidget(CycleButton.builder((AutoEatConfig.HUDPosition pos) -> Component.literal("HUD: " + pos.name()))
                .withValues(AutoEatConfig.HUDPosition.values())
                .withInitialValue(AutoEatConfig.CONFIG.hudPosition.get())
                .create(xCenter, yStart, BUTTON_WIDTH, BUTTON_HEIGHT,
                    Component.literal("HUD Position"),
                    (button, value) -> AutoEatConfig.CONFIG.hudPosition.set(value))
            );
        }
    }

    private void initFilterTab() {
        int yStart = CONTENT_TOP - (int)scrollOffset;
        int xCenter = this.width / 2 - BUTTON_WIDTH / 2;

        // Calculate the max Y position to avoid Done button
        int maxYPosition = this.height - CONTENT_BOTTOM_OFFSET - BUTTON_HEIGHT;

        // Filter mode button
        if (yStart >= CONTENT_TOP - BUTTON_HEIGHT && yStart <= maxYPosition) {
            this.addRenderableWidget(CycleButton.builder((AutoEatConfig.FilterMode mode) -> Component.literal("Mode: " + mode.name()))
                .withValues(AutoEatConfig.FilterMode.values())
                .withInitialValue(AutoEatConfig.CONFIG.filterMode.get())
                .create(xCenter, yStart, BUTTON_WIDTH, BUTTON_HEIGHT,
                    Component.literal("Filter Mode"),
                    (button, value) -> {
                        AutoEatConfig.CONFIG.filterMode.set(value);
                        AutoEatHandler.resetCache();
                        this.clearWidgets();
                        this.init();
                    })
            );
        }

        yStart += SPACING + 10;

        // Info text is rendered separately in render() - skip space for it (3 lines of text)
        yStart += 60;

        // Clear all button
        if (yStart >= CONTENT_TOP - BUTTON_HEIGHT && yStart <= maxYPosition) {
            AutoEatConfig.FilterMode mode = AutoEatConfig.CONFIG.filterMode.get();
            String listName = mode == AutoEatConfig.FilterMode.BLACKLIST ? "Blacklist" : "Whitelist";

            this.addRenderableWidget(Button.builder(
                Component.literal("Clear " + listName),
                button -> {
                    if (mode == AutoEatConfig.FilterMode.BLACKLIST) {
                        AutoEatConfig.CONFIG.blacklist.set(new ArrayList<>());
                    } else {
                        AutoEatConfig.CONFIG.whitelist.set(new ArrayList<>());
                    }
                    AutoEatHandler.resetCache();
                    this.clearWidgets();
                    this.init();
                }
            ).bounds(xCenter, yStart, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        }

        yStart += SPACING + 15;

        // List of items
        List<String> currentList = getCurrentFilterList();

        if (currentList.isEmpty()) {
            // Show "empty" message - will be rendered in render method
        } else {
            for (int i = 0; i < currentList.size(); i++) {
                String itemId = currentList.get(i);
                final int index = i;

                if (yStart >= CONTENT_TOP - LIST_ITEM_HEIGHT && yStart <= maxYPosition) {
                    // Item name (shortened for display)
                    String displayName = itemId.replace("minecraft:", "")
                                              .replace("farmersdelight:", "fd:")
                                              .replace("croptopia:", "crop:")
                                              .replace("_", " ");

                    if (displayName.length() > 22) {
                        displayName = displayName.substring(0, 19) + "...";
                    }

                    // Remove button
                    this.addRenderableWidget(Button.builder(
                        Component.literal("✕"),
                        button -> {
                            removeFromFilter(index);
                            this.clearWidgets();
                            this.init();
                        }
                    ).bounds(xCenter - 30, yStart, 20, 18).build());

                    // Item display (clickable for full name)
                    this.addRenderableWidget(Button.builder(
                        Component.literal(displayName),
                        button -> {
                            // Show full item ID in chat
                            if (minecraft.player != null) {
                                minecraft.player.sendSystemMessage(
                                    Component.literal("§6Item ID: §f" + itemId)
                                );
                            }
                        }
                    ).bounds(xCenter - 5, yStart, BUTTON_WIDTH - 40, 18).build());
                }

                yStart += LIST_ITEM_HEIGHT;
            }
        }
    }

    private List<String> getCurrentFilterList() {
        AutoEatConfig.FilterMode mode = AutoEatConfig.CONFIG.filterMode.get();
        if (mode == AutoEatConfig.FilterMode.BLACKLIST) {
            return new ArrayList<>((List<String>) AutoEatConfig.CONFIG.blacklist.get());
        } else {
            return new ArrayList<>((List<String>) AutoEatConfig.CONFIG.whitelist.get());
        }
    }

    private void removeFromFilter(int index) {
        AutoEatConfig.FilterMode mode = AutoEatConfig.CONFIG.filterMode.get();

        if (mode == AutoEatConfig.FilterMode.BLACKLIST) {
            List<String> blacklist = new ArrayList<>((List<String>) AutoEatConfig.CONFIG.blacklist.get());
            if (index >= 0 && index < blacklist.size()) {
                String removed = blacklist.remove(index);
                AutoEatConfig.CONFIG.blacklist.set(blacklist);

                // Show notification
                if (minecraft.player != null) {
                    minecraft.player.sendSystemMessage(
                        Component.literal("§cRemoved from Blacklist: §f" + removed)
                    );
                }
            }
        } else {
            List<String> whitelist = new ArrayList<>((List<String>) AutoEatConfig.CONFIG.whitelist.get());
            if (index >= 0 && index < whitelist.size()) {
                String removed = whitelist.remove(index);
                AutoEatConfig.CONFIG.whitelist.set(whitelist);

                // Show notification
                if (minecraft.player != null) {
                    minecraft.player.sendSystemMessage(
                        Component.literal("§cRemoved from Whitelist: §f" + removed)
                    );
                }
            }
        }

        AutoEatHandler.resetCache();
    }

    private void initStatisticsTab() {
        // Statistics are rendered in the render method
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScroll > 0) {
            scrollOffset = Mth.clamp(scrollOffset - scrollY * 10, 0, maxScroll);
            // Rebuild UI with new scroll offset
            this.clearWidgets();
            this.init();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        // Draw title
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);

        // Draw tab highlights
        int tabX = this.width / 2 - (ConfigTab.values().length * TAB_WIDTH / 2);
        int tabY = 40;

        for (ConfigTab tab : ConfigTab.values()) {
            if (tab == currentTab) {
                graphics.fill(tabX, tabY - 2, tabX + TAB_WIDTH, tabY, 0xFF4A90E2);
            }
            tabX += TAB_WIDTH;
        }

        // Render filter info text
        if (currentTab == ConfigTab.FILTER) {
            AutoEatConfig.FilterMode mode = AutoEatConfig.CONFIG.filterMode.get();
            String infoText = mode == AutoEatConfig.FilterMode.BLACKLIST
                ? "§7Blacklist: Blocks dangerous foods"
                : "§7Whitelist: Only allows specific foods";
            graphics.drawCenteredString(this.font, infoText, this.width / 2, CONTENT_TOP + 35, 0xAAAAAA);

            graphics.drawCenteredString(this.font, "§eSupports ALL mods (Farmer's Delight, etc.)",
                this.width / 2, CONTENT_TOP + 50, 0xFFAA00);

            // Show list info
            List<String> currentList = getCurrentFilterList();
            String listName = mode == AutoEatConfig.FilterMode.BLACKLIST ? "Blacklist" : "Whitelist";

            if (currentList.isEmpty()) {
                // Get dynamic keybind name
                String keyName = KeyBindings.ADD_TO_FILTER_KEY.getTranslatedKeyMessage().getString();

                graphics.drawCenteredString(this.font, "§7" + listName + " is empty",
                    this.width / 2, CONTENT_TOP + 145, 0x888888);
                graphics.drawCenteredString(this.font, "§6Hold food and press " + keyName + " to add",
                    this.width / 2, CONTENT_TOP + 160, 0xFFAA00);
            } else {
                // Get dynamic keybind name
                String keyName = KeyBindings.ADD_TO_FILTER_KEY.getTranslatedKeyMessage().getString();

                graphics.drawCenteredString(this.font, "§e" + listName + " (" + currentList.size() + " items)",
                    this.width / 2, CONTENT_TOP + 70, 0xFFAA00);
                graphics.drawCenteredString(this.font, "§7Hold food + " + keyName + " to add/remove",
                    this.width / 2, CONTENT_TOP + 83, 0x888888);
            }
        }

        // Render statistics if on that tab
        if (currentTab == ConfigTab.STATISTICS) {
            renderStatistics(graphics);
        }

        // Draw scroll indicator
        if (maxScroll > 0) {
            int scrollbarHeight = 60;
            int scrollbarY = CONTENT_TOP + (int)((this.height - CONTENT_TOP - CONTENT_BOTTOM_OFFSET - scrollbarHeight) * (scrollOffset / maxScroll));
            graphics.fill(this.width - 8, scrollbarY, this.width - 5, scrollbarY + scrollbarHeight, 0xFF8B8B8B);
        }
    }

    private void renderStatistics(GuiGraphics graphics) {
        FoodStatistics stats = FoodStatistics.getInstance();
        int y = 80;
        int x = this.width / 2 - 100;

        graphics.drawString(this.font, "Total Food Eaten: " + stats.getTotalFoodEaten(), x, y, 0xFFFFFF);
        y += 15;

        graphics.drawString(this.font, "Most Eaten: " + stats.getMostEatenFood(), x, y, 0xFFFFFF);
        y += 15;

        long duration = stats.getSessionDuration();
        long minutes = duration / 60;
        long seconds = duration % 60;
        graphics.drawString(this.font, String.format("Session: %dm %ds", minutes, seconds), x, y, 0xFFFFFF);
        y += 25;

        graphics.drawString(this.font, "§7Top Foods:", x, y, 0xFFFFFF);
        y += 15;

        final int startY = y;
        final int startX = x;

        List<Map.Entry<String, Integer>> topFoods = stats.getFoodCount().entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .limit(5)
            .toList();

        for (int i = 0; i < topFoods.size(); i++) {
            Map.Entry<String, Integer> entry = topFoods.get(i);
            String itemName = entry.getKey().replace("minecraft:", "").replace("_", " ");
            graphics.drawString(this.font, "  " + itemName + ": " + entry.getValue(), startX, startY + (i * 12), 0xAAAAAA);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
