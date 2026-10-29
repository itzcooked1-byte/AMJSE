package com.diasmp.pvp;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ConfigScreen extends Screen {
    private final Screen parent;

    public ConfigScreen(Screen parent) {
        super(Text.literal("Diamond SMP PvP"));
        this.parent = parent;
    }

    private static Text label(String name, boolean on) {
        return Text.literal(name + ": " + (on ? "§aON" : "§cOFF"));
    }

    private void toggle(int col, int row, String name, BooleanSupplier get, Consumer<Boolean> set) {
        int w = 150;
        int x = this.width / 2 - w - 4 + col * (w + 8);
        int y = 30 + row * 22;
        addDrawableChild(ButtonWidget.builder(label(name, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            b.setMessage(label(name, get.getAsBoolean()));
            Config.save();
        }).dimensions(x, y, w, 20).build());
    }

    @Override
    protected void init() {
        Config c = Config.INSTANCE;
        toggle(0, 0, "HUD", () -> c.hudEnabled, v -> c.hudEnabled = v);
        toggle(1, 0, "Background", () -> c.background, v -> c.background = v);
        toggle(0, 1, "FPS", () -> c.showFps, v -> c.showFps = v);
        toggle(1, 1, "Ping", () -> c.showPing, v -> c.showPing = v);
        toggle(0, 2, "CPS", () -> c.showCps, v -> c.showCps = v);
        toggle(1, 2, "Speed", () -> c.showSpeed, v -> c.showSpeed = v);
        toggle(0, 3, "Combo counter", () -> c.showCombo, v -> c.showCombo = v);
        toggle(1, 3, "Target health", () -> c.showTarget, v -> c.showTarget = v);
        toggle(0, 4, "Totem count", () -> c.showTotems, v -> c.showTotems = v);
        toggle(1, 4, "Item counts", () -> c.showCounts, v -> c.showCounts = v);
        toggle(0, 5, "Armor durability", () -> c.showArmor, v -> c.showArmor = v);
        toggle(1, 5, "Potion timers", () -> c.showEffects, v -> c.showEffects = v);
        toggle(0, 6, "Low warnings", () -> c.lowWarnings, v -> c.lowWarnings = v);
        toggle(1, 6, "Zoom (hold key)", () -> c.zoomEnabled, v -> c.zoomEnabled = v);
        toggle(0, 7, "Coordinates", () -> c.showCoords, v -> c.showCoords = v);
        toggle(1, 7, "Facing", () -> c.showDirection, v -> c.showDirection = v);

        int y = 30 + 8 * 22 + 4;
        int cx = this.width / 2;
        addDrawableChild(ButtonWidget.builder(Text.literal("Color: " + Config.COLOR_NAMES[c.colorIndex % Config.COLORS.length]), b -> {
            c.colorIndex = (c.colorIndex + 1) % Config.COLORS.length;
            b.setMessage(Text.literal("Color: " + Config.COLOR_NAMES[c.colorIndex]));
            Config.save();
        }).dimensions(cx - 154, y, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Zoom FOV: " + c.zoomFov), b -> {
            c.zoomFov = c.zoomFov <= 10 ? 60 : c.zoomFov - 10;
            b.setMessage(Text.literal("Zoom FOV: " + c.zoomFov));
            Config.save();
        }).dimensions(cx - 50, y, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), b -> {
            Config.INSTANCE = new Config(); Config.save(); this.clearAndInit();
        }).dimensions(cx + 54, y, 100, 20).build());

        y += 24;
        addDrawableChild(ButtonWidget.builder(Text.literal("←"), b -> { c.hudX = Math.max(0, c.hudX - 10); Config.save(); })
                .dimensions(cx - 154, y, 36, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("→"), b -> { c.hudX += 10; Config.save(); })
                .dimensions(cx - 114, y, 36, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("↑"), b -> { c.hudY = Math.max(0, c.hudY - 10); Config.save(); })
                .dimensions(cx - 74, y, 36, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("↓"), b -> { c.hudY += 10; Config.save(); })
                .dimensions(cx - 34, y, 36, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(cx + 6, y, 148, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 12, 0xFFFFFFFF);
        Hud.render(ctx);
    }

    @Override
    public void close() {
        Config.save();
        this.client.setScreen(parent);
    }
}
