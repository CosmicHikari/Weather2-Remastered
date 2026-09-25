package net.mrbt0907.configex.event;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.ConfigModEX;
import net.mrbt0907.configex.gui.GuiConfigEditor;
import net.mrbt0907.weather2.mixins.accessor.ClientPlayerEntityAccessor;

@OnlyIn(Dist.CLIENT)
public class ClientHandler {
    private static final Minecraft MC = Minecraft.getInstance();
    private static int permissionLevel;
    private static boolean inGame;

    public static void onTick() {
        if (MC.level == null && inGame) {
            ConfigModEX.debug("World was closed. Resetting client handler...");
            inGame = false;
            permissionLevel = ConfigManager.getPermissionLevel();
            ConfigManager.reset(true);
        } else if (MC.level != null && !inGame) {
            ConfigModEX.debug("Detected new world. Preparing to client handler...");
            inGame = true;
            permissionLevel = ConfigManager.getPermissionLevel();
        }

        if (inGame)
            onGameTick();
    }

    public static void onGameTick() {
        int permission;
        if (MC.player != null && permissionLevel != (permission = getPlayerPermissionLevel())) {
            permissionLevel = permission;
            ConfigManager.reset(false);
            if (MC.screen instanceof GuiConfigEditor)
                ((GuiConfigEditor) MC.screen).scrollPane.populateData();
        }
    }


    private static int getPlayerPermissionLevel() {
        if (MC.player == null)
            return 0;


        return ((ClientPlayerEntityAccessor) MC.player).invokeGetPermissionLevel();
    }

    public static boolean inGame() {
        return inGame;
    }
}