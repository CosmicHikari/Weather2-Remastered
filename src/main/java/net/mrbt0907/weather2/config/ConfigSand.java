package net.mrbt0907.weather2.config;

import net.mrbt0907.configex.api.ConfigAnnotations.*;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.weather2.Weather2;

import java.io.File;


public class ConfigSand implements IConfigEX {
    @Enforce
    @Comment("Takes the sand out of sandwiches")
    public static boolean disable_sandstorms = false;
    @Hidden
    @Enforce
    @Comment("Use Server Storm Deadly Time instead of Sandstorm Rates")
    public static boolean enable_global_rates_for_sandstorms = false;
    @Enforce
    @IntegerRange(min = 1)
    @Comment("A Sandstorm has a 1 in x chance to spawn")
    public static int sandstorm_spawn_1_in_x = 30;
    @Enforce
    @IntegerRange(min = 0)
    @Comment("Time between sandstorms for either each player or entire server depending on if global rate is on, default: 3 mc days")
    public static int sandstorm_spawn_delay = 20 * 60 * 20 * 3;
    @Enforce
    @IntegerRange(min = 0)
    @Comment("Amount of game ticks between sand buildup iterations, keep it high to prevent client side chunk update spam that destroys FPS")
    public static int buildup_tick_delay = 40;
    @Enforce
    @IntegerRange(min = 0)
    @Comment("Base amount of loops done per iteration, scaled by the sandstorms intensity (value given here is the max possible)")
    public static int max_buildup_loop_ammount = 800;
    @Enforce
    @Comment("Allow layered sand blocks to buildup outside deserty biomes where sandstorm is")
    public static boolean enable_buildup_outside_desert = true;
    @Permission(0)
    @Comment("Enables sirens near sandstorms to play \\\"Darude - Sandstorm\". Meme content")
    public static boolean disable_darude_sandstorm_plz = false;

    @Override
    public String getName() {
        return "Weather2 Remastered - Sand";
    }

    @Override
    public String getSaveLocation() {
        return Weather2.MODID + File.separator + "ConfigSand";
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
}