package com.diasmp.pvp;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class Hud {
    private static final Deque<Long> clicks = new ArrayDeque<>();
    private static boolean wasDown = false;

    // combo / target / speed state
    public static int combo = 0;
    private static long lastHitMs = 0;
    private static int prevHurtTime = 0;
    private static LivingEntity lastTarget = null;
    private static long lastTargetMs = 0;
    private static double prevX, prevZ;
    private static double speed = 0;

    /** Called when the local player attacks an entity (via Fabric AttackEntityCallback). */
    public static void onAttack(Entity target) {
        if (!(target instanceof LivingEntity)) return;
        combo++;
        lastHitMs = System.currentTimeMillis();
    }

    public static void tick(MinecraftClient mc) {
        if (mc.getWindow() == null) return;

        // CPS (left click edge detection)
        boolean down = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (down && !wasDown && mc.currentScreen == null) clicks.addLast(System.currentTimeMillis());
        wasDown = down;
        long now = System.currentTimeMillis();
        while (!clicks.isEmpty() && clicks.peekFirst() < now - 1000) clicks.pollFirst();

        PlayerEntity p = mc.player;
        if (p == null) { combo = 0; return; }

        // combo: reset when we get hurt, or after 3s without hitting
        if (p.hurtTime > prevHurtTime) combo = 0;
        prevHurtTime = p.hurtTime;
        if (combo > 0 && now - lastHitMs > 3000) combo = 0;

        // target (crosshair entity), kept for 2s after you look away
        if (mc.targetedEntity instanceof LivingEntity le && le != p) { lastTarget = le; lastTargetMs = now; }
        if (lastTarget != null && (now - lastTargetMs > 2000 || !lastTarget.isAlive())) lastTarget = null;

        // speed in blocks/sec
        speed = Math.hypot(p.getX() - prevX, p.getZ() - prevZ) * 20.0;
        prevX = p.getX(); prevZ = p.getZ();
    }

    private static int count(PlayerEntity p, Item item) {
        int n = 0;
        for (int i = 0; i < p.getInventory().size(); i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (s.isOf(item)) n += s.getCount();
        }
        return n;
    }

    private static String fmtTime(int ticks) {
        int s = ticks / 20;
        return String.format("%d:%02d", s / 60, s % 60);
    }

    private static String roman(int n) {
        String[] r = { "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X" };
        return n >= 0 && n < r.length ? r[n] : String.valueOf(n + 1);
    }

    private record Line(String text, int color) {}

    public static void render(DrawContext ctx) {
        Config c = Config.INSTANCE;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!c.hudEnabled || mc.player == null || mc.options.hudHidden) return;
        if (mc.getDebugHud().shouldShowDebugHud()) return;

        TextRenderer tr = mc.textRenderer;
        PlayerEntity p = mc.player;
        int base = c.color();
        List<Line> lines = new ArrayList<>();

        if (c.showFps) lines.add(new Line("FPS: " + mc.getCurrentFps(), base));
        if (c.showPing) {
            PlayerListEntry e = mc.getNetworkHandler() == null ? null : mc.getNetworkHandler().getPlayerListEntry(p.getUuid());
            int ping = e == null ? 0 : e.getLatency();
            lines.add(new Line("Ping: " + ping + "ms", ping < 80 ? 0xFF55FF55 : ping < 160 ? 0xFFFFFF55 : 0xFFFF5555));
        }
        if (c.showCps) lines.add(new Line("CPS: " + clicks.size(), base));
        if (c.showSpeed) lines.add(new Line(String.format("Speed: %.1f b/s", speed), base));
        if (c.showCombo) lines.add(new Line("Combo: " + combo, combo >= 5 ? 0xFFFFAA00 : base));
        if (c.showTotems) {
            int n = count(p, Items.TOTEM_OF_UNDYING);
            lines.add(new Line("Totems: " + n, n == 0 ? 0xFFFF5555 : n < 3 ? 0xFFFFFF55 : 0xFF55FF55));
        }
        if (c.showCounts) {
            lines.add(new Line("Crystals: " + count(p, Items.END_CRYSTAL), base));
            lines.add(new Line("Gapples: " + (count(p, Items.GOLDEN_APPLE) + count(p, Items.ENCHANTED_GOLDEN_APPLE)), base));
            lines.add(new Line("Pearls: " + count(p, Items.ENDER_PEARL), base));
            lines.add(new Line("XP bottles: " + count(p, Items.EXPERIENCE_BOTTLE), base));
        }
        if (c.showCoords) lines.add(new Line(String.format("XYZ: %d %d %d", (int) p.getX(), (int) p.getY(), (int) p.getZ()), base));
        if (c.showDirection) lines.add(new Line("Facing: " + p.getHorizontalFacing().asString(), base));

        int x = c.hudX, y = c.hudY;

        // text panel
        if (!lines.isEmpty()) {
            int w = 0;
            for (Line l : lines) w = Math.max(w, tr.getWidth(l.text()));
            if (c.background) ctx.fill(x - 3, y - 3, x + w + 3, y + lines.size() * 10 + 1, 0x70000000);
            for (Line l : lines) { ctx.drawTextWithShadow(tr, l.text(), x, y, l.color()); y += 10; }
            y += 6;
        }

        // armor + held items
        boolean lowArmor = false;
        if (c.showArmor) {
            EquipmentSlot[] slots = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND };
            for (EquipmentSlot slot : slots) {
                ItemStack s = p.getEquippedStack(slot);
                if (s.isEmpty()) continue;
                ctx.drawItem(s, x, y);
                if (s.isDamageable()) {
                    int left = s.getMaxDamage() - s.getDamage();
                    int pct = left * 100 / s.getMaxDamage();
                    if (pct <= 15 && slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) lowArmor = true;
                    int col = pct > 50 ? 0xFF55FF55 : pct > 20 ? 0xFFFFFF55 : 0xFFFF5555;
                    ctx.drawTextWithShadow(tr, left + " (" + pct + "%)", x + 20, y + 4, col);
                } else if (s.getCount() > 1) {
                    ctx.drawTextWithShadow(tr, "x" + s.getCount(), x + 20, y + 4, base);
                }
                y += 18;
            }
        }

        // potion effects
        if (c.showEffects && !p.getStatusEffects().isEmpty()) {
            y += 4;
            for (StatusEffectInstance e : p.getStatusEffects()) {
                String name = e.getEffectType().value().getName().getString() + " " + roman(e.getAmplifier());
                String time = e.isInfinite() ? "∞" : fmtTime(e.getDuration());
                int col = e.getDuration() < 200 && !e.isInfinite() ? 0xFFFF5555 : 0xFFFFFFFF;
                ctx.drawTextWithShadow(tr, name + " " + time, x, y, col);
                y += 10;
            }
        }

        // target health bar (centered under crosshair)
        if (c.showTarget && lastTarget != null) {
            int cx = ctx.getScaledWindowWidth() / 2, cy = ctx.getScaledWindowHeight() / 2 + 28;
            float hp = lastTarget.getHealth(), max = Math.max(1f, lastTarget.getMaxHealth());
            String label = lastTarget.getName().getString() + "  " + String.format("%.1f", hp) + "/" + String.format("%.0f", max);
            int w = Math.max(80, tr.getWidth(label) + 8);
            ctx.fill(cx - w / 2, cy - 2, cx + w / 2, cy + 20, 0x80000000);
            ctx.drawCenteredTextWithShadow(tr, label, cx, cy, 0xFFFFFFFF);
            float frac = Math.min(1f, hp / max);
            int barW = w - 8;
            ctx.fill(cx - barW / 2, cy + 11, cx + barW / 2, cy + 16, 0xFF330000);
            int col = frac > 0.5f ? 0xFF55FF55 : frac > 0.25f ? 0xFFFFFF55 : 0xFFFF5555;
            ctx.fill(cx - barW / 2, cy + 11, cx - barW / 2 + (int) (barW * frac), cy + 16, col);
        }

        // low warnings (centered, above crosshair)
        if (c.lowWarnings) {
            int cx = ctx.getScaledWindowWidth() / 2, cy = ctx.getScaledWindowHeight() / 2 - 40;
            if (c.showTotems && count(p, Items.TOTEM_OF_UNDYING) == 0 && p.getOffHandStack().isEmpty()) {
                ctx.drawCenteredTextWithShadow(tr, "NO TOTEM", cx, cy, 0xFFFF5555); cy -= 10;
            }
            if (lowArmor) ctx.drawCenteredTextWithShadow(tr, "ARMOR LOW", cx, cy, 0xFFFFAA00);
        }
    }
}
