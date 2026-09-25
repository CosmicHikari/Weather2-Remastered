package net.corosus.coroutillegacy.util;

import net.corosus.coroutillegacy.forge.CULog;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.lang.reflect.Method;

public class CoroUtilCompatibility {

    private static boolean tanInstalled = false;
    private static boolean checkTAN = true;

    private static boolean sereneSeasonsInstalled = false;
    private static boolean checksereneSeasons = true;

    private static Class class_TAN_ASMHelper = null;
    private static Method method_TAN_getFloatTemperature = null;

    private static Class class_SereneSeasons_ASMHelper = null;
    private static Method method_sereneSeasons_getFloatTemperature = null;


    public static float getAdjustedTemperature(World world, Biome biome, BlockPos pos) {


        if (isTANInstalled()) {
            try {
                if (method_TAN_getFloatTemperature == null) {
                    method_TAN_getFloatTemperature = class_TAN_ASMHelper.getDeclaredMethod("getFloatTemperature", Biome.class, BlockPos.class);
                }
                return (float) method_TAN_getFloatTemperature.invoke(null, biome, pos);
            } catch (Exception ex) {
                ex.printStackTrace();

                tanInstalled = false;
                return biome.getTemperature(pos);
            }
        } else if (isSereneSeasonsInstalled()) {
            try {
                if (method_sereneSeasons_getFloatTemperature == null) {
                    method_sereneSeasons_getFloatTemperature = class_SereneSeasons_ASMHelper.getDeclaredMethod("getFloatTemperature", World.class, Biome.class, BlockPos.class);
                }
                return (float) method_sereneSeasons_getFloatTemperature.invoke(null, world, biome, pos);
            } catch (Exception ex) {
                ex.printStackTrace();

                sereneSeasonsInstalled = false;
                return biome.getTemperature(pos);
            }
        } else {
            return biome.getTemperature(pos);
        }
    }


    public static boolean isTANInstalled() {
        if (checkTAN) {
            try {
                checkTAN = false;
                class_TAN_ASMHelper = Class.forName("toughasnails.season.SeasonASMHelper");
                if (class_TAN_ASMHelper != null) {
                    tanInstalled = true;
                }
            } catch (Exception ex) {


            }

            CULog.log("CoroUtilLegacy detected Tough As Nails Seasons " + (tanInstalled ? "Installed" : "Not Installed") + " for use");
        }

        return tanInstalled;
    }


    public static boolean isSereneSeasonsInstalled() {
        if (checksereneSeasons) {
            try {
                checksereneSeasons = false;
                class_SereneSeasons_ASMHelper = Class.forName("sereneseasons.api.season.BiomeHooks");
                if (class_SereneSeasons_ASMHelper != null) {
                    sereneSeasonsInstalled = true;
                }
            } catch (Exception ex) {


            }

            CULog.log("CoroUtilLegacy detected Serene Seasons " + (sereneSeasonsInstalled ? "Installed" : "Not Installed") + " for use");
        }

        return sereneSeasonsInstalled;
    }


}