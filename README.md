# Diamond SMP PvP v3 (Fabric 1.21.11)

Legit client-side PvP HUD. No cheats (no reach, aimbot, killaura, x-ray, etc).

Features: FPS, ping, CPS, speed, combo counter, target health bar, totem count, crystal/gapple/pearl/XP counts,
armor + held-item durability, potion timers, NO TOTEM / ARMOR LOW warnings, hold-to-zoom (C),
coords, facing, color picker, HUD position, full ON/OFF GUI (Right Shift or Mod Menu), reset button.

## Build the JAR
A) No install: push folder to GitHub -> Actions -> "build" -> download artifact `diasmp-pvp-jar`.
B) Local: JDK 21 + Gradle 8.14+, run `gradle build` -> `build/libs/diasmp-pvp-3.0.0.jar` (not the -sources one).

## Install
JAR into `.minecraft/mods` with Fabric Loader 0.18.1+, Fabric API, optional Mod Menu 17.0.0-beta.2+.
