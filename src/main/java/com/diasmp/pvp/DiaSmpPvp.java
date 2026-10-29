package com.diasmp.pvp;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class DiaSmpPvp implements ClientModInitializer {
    private static KeyBinding menuKey, zoomKey;
    private static boolean zooming = false;
    private static int savedFov = 70;

    @Override
    public void onInitializeClient() {
        Config.load();
        KeyBinding.Category cat = KeyBinding.Category.create(Identifier.of("diasmp", "main"));
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.diasmp.menu", GLFW.GLFW_KEY_RIGHT_SHIFT, cat));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.diasmp.zoom", GLFW.GLFW_KEY_C, cat));

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient()) Hud.onAttack(entity);
            return ActionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            Hud.tick(mc);
            while (menuKey.wasPressed()) mc.setScreen(new ConfigScreen(mc.currentScreen));
            handleZoom(mc);
        });
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> Hud.render(ctx));
    }

    private static void handleZoom(MinecraftClient mc) {
        boolean want = Config.INSTANCE.zoomEnabled && zoomKey.isPressed() && mc.currentScreen == null;
        if (want && !zooming) {
            savedFov = mc.options.getFov().getValue();
            zooming = true;
        }
        if (want) mc.options.getFov().setValue(Config.INSTANCE.zoomFov);
        else if (zooming) { mc.options.getFov().setValue(savedFov); zooming = false; }
    }
}
