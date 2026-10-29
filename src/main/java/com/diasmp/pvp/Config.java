package com.diasmp.pvp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("diasmp-pvp.json");
    public static Config INSTANCE = new Config();

    public static final int[] COLORS = { 0xFFFFFF, 0x55FFFF, 0xFF55FF, 0xFFAA00, 0x55FF55, 0xFF5555 };
    public static final String[] COLOR_NAMES = { "White", "Aqua", "Pink", "Gold", "Green", "Red" };

    // main panel
    public boolean hudEnabled = true;
    public boolean showFps = true;
    public boolean showPing = true;
    public boolean showCps = true;
    public boolean showSpeed = false;
    public boolean showCoords = false;
    public boolean showDirection = false;
    public boolean background = true;
    public int colorIndex = 0;
    public int hudX = 6;
    public int hudY = 6;

    // pvp modules
    public boolean showTotems = true;
    public boolean showCounts = true;      // crystals, gapples, pearls, xp bottles
    public boolean showArmor = true;
    public boolean showCombo = true;
    public boolean showTarget = true;
    public boolean showEffects = true;
    public boolean lowWarnings = true;     // low totem / low armor warning

    // zoom
    public boolean zoomEnabled = true;
    public int zoomFov = 30;

    public static void load() {
        try {
            if (Files.exists(FILE)) INSTANCE = GSON.fromJson(Files.readString(FILE), Config.class);
        } catch (Exception e) { INSTANCE = new Config(); }
        if (INSTANCE == null) INSTANCE = new Config();
    }

    public static void save() {
        try { Files.writeString(FILE, GSON.toJson(INSTANCE)); } catch (Exception ignored) {}
    }

    public int color() { return COLORS[Math.floorMod(colorIndex, COLORS.length)] | 0xFF000000; }
}
