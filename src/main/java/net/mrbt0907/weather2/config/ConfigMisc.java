package net.mrbt0907.weather2.config;

import net.mrbt0907.configex.api.ConfigAnnotations.*;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.weather2.Weather2;

import java.io.File;


public class ConfigMisc implements IConfigEX {

    @Permission(0)
    @Comment("Disabling this will fix particles not rendering over water")
    public static boolean proxy_render_override = true;
    @Permission(0)
    @Comment("Enables the mod to turn vanilla clouds off")
    public static boolean enable_forced_clouds_off = true;
    @Hidden
    @Enforce
    @IntegerRange(min = 1)
    @Comment("How often does the mod save storms in ticks?")
    public static int auto_save_interval = 20 * 60 * 30;
    @Hidden
    @Comment("Enables the mod to output data into the console for debugging")
    public static boolean debug_mode = true;
    @Hidden
    @Comment("Enables the mod to output data into all radars about storms")
    public static boolean debug_mode_radar = false;
    @Enforce
    @DoubleRange(min = 0.0D)
    @Comment("How far do Radars sense storms?")
    public static double radar_range = 512.0D;
    @Enforce
    @DoubleRange(min = 0.0D)
    @Comment("How far do Doppler Radars sense storms?")
    public static double doppler_radar_range = 1024.0D;
    @Enforce
    @DoubleRange(min = 0.0D)
    @Comment("How far do Pulse Doppler Radars sense storms?")
    public static double pulse_doppler_radar_range = 2048.0D;
    @Enforce
    @DoubleRange(min = 0.0D)
    @Comment("How far do Computerized Radars sense storms?")
    public static double computer_radar_range = 2048.0D;
    @Hidden
    @Enforce
    @IntegerRange(min = 1)
    @Comment("How often does the mod sync storms in ticks? Fixes desync when ran")
    public static int sync_interval = 2400;
    @Enforce
    @Comment("If true, storms are removed when no players are in the dimension")
    public static boolean remove_storms_if_no_players = false;
    @Enforce
    @Comment("If true, lets server side do vanilla weather rules, weather2 will only make storms when server side says 'rain' is on")
    public static boolean overcast_mode = false;
    @Hidden
    @Enforce
    @IntegerRange(min = -1, max = 1)
    @Comment("Used if overcastMode is off, 1 = lock weather on, 0 = lock weather off, -1 = dont lock anything, let server do whatever")
    public static int server_weather_mode = 0;
    @Enforce
    @DoubleRange(min = 0.0D)
    @Comment("How far can sirens detect a storm")
    public static double siren_scan_range = 256.0D;
    @Enforce
    @DoubleRange(min = 0.0D)
    @Comment("How far can sensors detect a storm")
    public static double sensor_scan_range = 512.0D;
    @Enforce
    @Comment("Disables the Weather Machine's ability to create tornados or hurricanes")
    public static boolean disable_weather_machine_cyclones = false;
    @Enforce
    @Comment("Disables the Weather Machine's recipe")
    public static boolean disable_weather_machine = false;
    @Enforce
    @Comment("Disables the Tornado Sensor's recipe")
    public static boolean disable_sensor = false;
    @Enforce
    @Comment("Disables the Tornado Siren's recipe")
    public static boolean disable_siren = false;
    @Enforce
    @Comment("Disables the Manual Tornado Siren's recipe")
    public static boolean disable_manual_siren = false;
    @Enforce
    @Comment("Disables the Wind Vane's recipe")
    public static boolean disable_wind_vane = false;
    @Enforce
    @Comment("Disables the Anemometer's recipe")
    public static boolean disable_anemometer = false;
    @Enforce
    @Comment("Disables the Weather Forcast's recipe")
    public static boolean disable_weather_radar = false;
    @Enforce
    @Comment("Disables the Weather Deflector's recipe")
    public static boolean disable_weather_deflector = false;
    @Enforce
    @Comment("Disables the Sand Layer's recipe")
    public static boolean disable_sand_layer = false;
    @Enforce
    @Comment("Disables Sand's recipe")
    public static boolean disable_sand = false;
    @Enforce
    @Comment("Disables the Pocket Sand item's recipe")
    public static boolean disable_pocket_sand = false;
    @Enforce
    @Comment("Disabling this recipe will keep them from using other recipes since it depends on this item")
    public static boolean disable_weather_item = false;
    @Enforce
    @RequiresWorldReload
    @Permission(4)
    @Comment("The name of the command used to spawn storms")
    public static String command_weather2 = "weather2";


    @Hidden
    @Enforce
    @Comment("List of dimensions the mod can use for weather (use resource location format, e.g., 'minecraft:overworld, minecraft:the_nether')")
    public static String dimensions_weather = "minecraft:overworld";

    @Hidden
    @Enforce
    @Comment("List of dimensions the mod can use for effects (use resource location format, e.g., 'minecraft:overworld, minecraft:the_nether')")
    public static String dimensions_effects = "minecraft:overworld";
    @Enforce
    @Comment("With repair mode on, enabling this will allow blocks to instantly repair when a tornado is not damaging it")
    public static boolean block_instant_repair = false;
    @Enforce
    @Comment("If true, will cancel vanilla behavior of setting clear weather when the player sleeps, for global overcast mode")
    public static boolean disable_rain_reset_upon_sleep = true;
    @Permission(0)
    @Comment("Use if you are on a server with weather but want it ALL off client side for performance reasons, overrides basically every client based setting")
    public static boolean toaster_pc_mode = false;
    @Permission(0)
    @Comment("Server and client side, Locks down the mod to only do wind, leaves, foliage shader if on, etc. No weather systems, turns overcast mode on")
    public static boolean aesthetic_mode = false;

    @Override
    public String getName() {
        return "Weather2 Remastered - Misc";
    }

    @Override
    public String getSaveLocation() {
        return Weather2.MODID + File.separator + "ConfigMisc";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void onConfigChanged(Phase phase, int variables) {


    }

    @Override
    public void onValueChanged(String variable, Object oldValue, Object newValue) {
    }

    public static boolean isDisabled(String configKey) {
        try {
            java.lang.reflect.Field field = ConfigMisc.class.getField(configKey);
            return field.getBoolean(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Weather2.warn("ConfigMisc : no such config field '" + configKey + "' — check the key used in the recipe condition JSON.");
            return false;
        }
    }
}
