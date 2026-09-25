package net.mrbt0907.weather2.config;

import net.mrbt0907.configex.api.ConfigAnnotations.*;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.WeatherAPI;

import java.io.File;


public class ConfigStorm implements IConfigEX {
    @Permission(0)
    @Comment("Whether or not to use the Enhanced Fujita Scale for tornados.")
    public static boolean enable_ef_scale = false;
    @Enforce
    @DoubleRange(min = 0.0D)
    @Comment("A multiplier for modifying the exponential growth of the sizes of storms. Use increments of 0.1 as that is considered a big increase.")
    public static double storm_size_curve_mult = 1.05D;
    @Enforce
    @IntegerRange(min = 1)
    @Comment("A deadly storm has a 10 in x chance to spawn a water spout in\nhigh wind conditions")
    public static int high_wind_waterspout_10_in_x = 150;
    @Enforce
    @IntegerRange(min = 0)
    @Comment("How many weather objects can develop in a dimension?")
    public static int max_weather_objects = 30;
    @Enforce
    @IntegerRange(min = 1)
    @Comment("A storm has a 1 in x chance to spawn a lightning bolt.\nHigher numbers means less lightning in storms")
    public static int lightning_bolt_1_in_x = 200;
    @Enforce
    @IntegerRange(min = 0)
    @Comment("How big can storms expand up to? Allows storms to grow larger depending on size.")
    public static int max_storm_size = 1500;
    @Enforce
    @IntegerRange(min = 0)
    @Comment("How small can storms expand up to? Allows storms to grow smaller depending on size.")
    public static int min_storm_size = 700;
    @Enforce
    @IntegerRange(min = 0)
    @Comment("How big can tornadoes expand up to? Allows funnels to grow larger depending on size.")
    public static int max_funnel_size = 900;
    @Enforce
    @IntegerRange(min = 0)
    @Comment("How small can tornadoes expand up to? Allows funnels to grow larger depending on size.")
    public static int min_funnel_size = 300;
    @Enforce
    @IntegerRange(min = 1)
    @Comment("Tick delay for storms. Higher values means storms have slower development. DO NOT PUT 0")
    public static int storm_tick_delay = 60;
    @Enforce
    @IntegerRange(min = 0)
    @Comment("How much water builds up in storms. Higher values = heavier rain faster")
    public static int humidity_buildup_rate = 3;
    @Enforce
    @IntegerRange(min = 0)
    @Comment("How much water is lost in a storm when it rains. Higher values = shorter rain time")
    public static int humidity_spend_rate = 2;
    @Enforce
    @DoubleRange(min = 0.0D)
    @Comment("Maybe the rate that a storm changes their temps to match another biome's temperature?")
    public static double temperature_adjust_rate = 0.1D;
    @Enforce
    @DoubleRange(min = 0.0D, max = 100.0D)
    @Comment("Percent chance for a storm to develop much stronger and larger than normal")
    public static double chance_for_violent_storm = 5.0D;
    @Enforce
    @IntegerRange(min = 0)
    @Comment("How much hail falls from the sky in a storm per tick?")
    public static int hail_stones_per_tick = 2;
    @Enforce
    @DoubleRange(min = 0.0D, max = 100.0D)
    @Comment("Percent chance for a storm to become a supercell")
    public static double chance_for_supercell = 35.0D;
    @Enforce
    @DoubleRange(min = 0.0D, max = 100.0D)
    @Comment("Percent chance for a storm to become a thunderstorm")
    public static double chance_for_thunderstorm = 32.5D;
    @Enforce
    @Comment("See config file for examples. A list of tornado stages with the chances for a storm to reach the stage. Use commas and/or spaces to separate each entry. Do not use quotation marks. Use = without spaces to indicate the chance. Chance goes from 0 (0% Chance) to 1 (100% Chance). Accepted formats - stage=chance")
    public static String chances_for_tornados = "F5=0.1, F4=1.05, F3=6.11, F2=13.61, F1=38.88, F0=37.22";
    @Enforce
    @Comment("See config file for examples. A list of hurricane stages with the chances for a storm to reach the stage. Use commas and/or spaces to separate each entry. Do not use quotation marks. Use = without spaces to indicate the chance. Chance goes from 0 (0% Chance) to 1 (100% Chance). Accepted formats - stage=chance")
    public static String chances_for_hurricanes = "C5=0.1, C4=2, C3=8.2, C2=10, C1=28, C0=30.5";

    //per server storm settings
    @Hidden
    @Enforce
    @Comment("Use global storm instead of per player rates to spawn storms.\nEnable if you want storms to stay at the same rarity no matter how many players are in the server.")
    public static boolean enable_spawn_per_player = false;
    @Enforce
    @IntegerRange(min = 0, max = 100)
    @Comment("Minimum chance of any cloud to become a storm. Range is 0 to 100")
    public static int storm_spawn_chance_min = 20;
    @Enforce
    @IntegerRange(min = 0, max = 100)
    @Comment("Maximum chance of any cloud to become a storm. Range is 0 to 100.")
    public static int storm_spawn_chance_max = 40;
    @Enforce
    @IntegerRange(min = 0)
    @Comment("The time in ticks it takes for all weather to spawn")
    public static int storm_spawn_delay = 750;
    @Hidden
    @Enforce
    @Comment("Should Weather2 cancel vanilla rainstorms at all times?")
    public static boolean prevent_vanilla_thunderstorms = true;
    //lightning
    @Enforce
    @IntegerRange(min = 1)
    @Comment("A lightning bolt has a 10 in x chance to catch something on fire")
    public static int lightning_bolt_sets_fire_10_in_x = 20;
    @Hidden
    @Enforce
    @IntegerRange(min = 0)
    @Comment("How long in seconds fire can last")
    public static int lightning_bolt_fire_lifetime = 3;
    @Hidden
    @Enforce
    @IntegerRange(min = 0)
    @Comment("How close does a storm need to be to a player to experience lightning")
    public static int max_lightning_bolt_distance = 256;
    @Enforce
    @Comment("Should lightning start fires?")
    public static boolean enable_lightning_bolt_fires = false;
    @Enforce
    @IntegerRange(min = 0)
    @Comment("How far a Storm Deflector can remove storms")
    public static int storm_deflector_range = 150;
    @Hidden
    @Enforce
    @IntegerRange(min = 0)
    @Comment("The minimum stage a storm has to be at to be removed,\nstages are: 0 = anything, 1 = thunder, 2 = high wind, 3 = hail, 4 = F0/C0, 5 = F1/C1, 6 = F2/C2, 7 = F3/C3, 8 = F4/C4, 9 = F5/C5")
    public static int storm_deflector_minimum_stage = 1;
    @Enforce
    @Comment("Should any Storm Deflector prevent rainstorms?")
    public static boolean storm_deflector_removes_rain = false;
    @Enforce
    @Comment("Should any Storm Deflector prevent sandstorms?")
    public static boolean storm_deflector_removes_sandstorms = true;
    @Hidden
    @Enforce
    @FloatRange(min = 0.0F)
    @Comment("Minimum amount of visual rain shown when its raining globally during overcast mode")
    public static float min_overcast_rain = 0.01F;
    @Enforce
    @IntegerRange(min = 1)
    @Comment("A storm in overcast mode has a 1 in x chance to start raining")
    public static int overcast_1_in_x = 50;
    @Hidden
    @Enforce
    @IntegerRange(min = 0)
    @Comment("How often in ticks, a rainstorm updates its list of\nentities under the rainstorm to extinguish. Extinguishes entities under rainclouds when globalOvercast is off. Set to 0 or less to disable")
    public static int storm_rain_extinguish_delay = 200;
    @Enforce
    @IntegerRange(min = 0)
    @Comment("How far can Villagers see storms")
    public static int villager_detection_range = 256;
    @Enforce
    @Comment("Enables Villagers to detect storms and run inside")
    public static boolean enable_villagers_take_cover = true;
    @Enforce
    @IntegerRange(min = 0)
    @Comment("How high should storms and clouds in the upper layer spawn at?")
    public static int cloud_layer_2_height = 300;
    @Enforce
    @IntegerRange(min = 0)
    @Comment("How high should storms and clouds in the middle layer spawn at?")
    public static int cloud_layer_1_height = 250;
    @Enforce
    @IntegerRange(min = 0)
    @Comment("How high should storms and clouds in the lower layer spawn at?")
    public static int cloud_layer_0_height = 200;
    @Enforce
    @Comment("For a third layer of passive non storm progressing clouds")
    public static boolean enable_cloud_layer_2 = false;
    @Enforce
    @Comment("For a second layer of passive non storm progressing clouds")
    public static boolean enable_cloud_layer_1 = false;
    @Enforce
    @DoubleRange(min = 0.0D)
    @Comment("How large can a storm's damage path be? Higher values will impact preformance.")
    public static double max_storm_damage_size = 350.0D;
    @Enforce
    @IntegerRange(min = 0, max = 359)
    @Comment("Accuracy of tornado aimed at player in degrees.\nHigher values means less accuracy up to 360 degrees")
    public static int storm_aim_accuracy_in_angle = 35;
    @Hidden
    @Enforce
    @IntegerRange(min = 1)
    @Comment("Tick rate for the storm spawning system")
    public static int spawningTickRate = 20;
    @Enforce
    @Comment("Make tornados initial heading aimed towards closest player")
    public static boolean storms_aim_at_player = true;
    @Enforce
    @Comment("Should tornados spawn?")
    public static boolean disable_tornados = false;
    @Enforce
    @Comment("Should cyclones spawn?")
    public static boolean disable_cyclones = false;
    @Enforce
    @DoubleRange(min = 0.0D)
    @Comment("How big can a storm grow up to? 1.0 means 100% normal size, 0.0 means 0.0% of normal size")
    public static double max_size_growth = 1.35D;
    @Enforce
    @DoubleRange(min = 0.0D)
    @Comment("How small can a storm grow up to? 1.0 means 100% normal size, 0.0 means 0.0% of normal size")
    public static double min_size_growth = 0.75D;
    @Enforce
    @DoubleRange(min = 0.0D)
    @Comment("How much does a violent storm add onto a storm's size at a maximum?")
    public static double max_violent_size_growth = 1.65D;
    @Enforce
    @DoubleRange(min = 0.0D)
    @Comment("How much does a violent storm add onto a storm's size at a minimum?")
    public static double min_violent_size_growth = 0.25D;
    @Enforce
    @IntegerRange(min = 0)
    @Comment("How many times can any storm revive?")
    public static int max_storm_revives = 2;
    @Enforce
    @DoubleRange(min = 0.0D, max = 100.0D)
    @Comment("Percent chance for a storm to revive after it is nearly dead")
    public static double chance_for_storm_revival = 38.0D;
    @Enforce
    @DoubleRange(min = 0.0D, max = 100.0D)
    @Comment("Percent chance for a storm to begin to hail")
    public static double chance_for_hail = 42.0D;
    @Enforce
    @DoubleRange(min = 0.0D)
    @Comment("How much hail builds up in storms. Higher values = more hail faster")
    public static double hail_max_buildup_rate = 1.0D;
    @Enforce
    @DoubleRange(min = 0.0D)
    @Comment("How fast storms may progress at a minimum. Higher values = faster development")
    public static double storm_lifespan_min = 0.003D;
    @Enforce
    @DoubleRange(min = 0.0D)
    @Comment("How fast storms may progress at a maximum. Higher values = faster development")
    public static double storm_lifespan_max = 0.04D;
    @Enforce
    @FloatRange(min = 0.0F)
    @Comment("How much rain can storms have at maximum. Higher values = longer rain")
    public static float max_rain_buildup = 1500.0F;

    public static boolean isLayerValid(int layer) {
        switch (layer) {
            case 0:
                return true;
            case 1:
                return enable_cloud_layer_1;
            case 2:
                return enable_cloud_layer_2;
            default:
                return false;
        }
    }

    @Override
    public String getName() {
        return "Weather2 Remastered - Storm";
    }

    @Override
    public String getSaveLocation() {
        return Weather2.MODID + File.separator + "ConfigStorm";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void onConfigChanged(Phase phase, int variables) {
        if (phase.equals(Phase.END))
            WeatherAPI.refreshStages();
    }

    @Override
    public void onValueChanged(String variable, Object oldValue, Object newValue) {
    }
}