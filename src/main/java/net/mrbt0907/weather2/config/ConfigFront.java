package net.mrbt0907.weather2.config;

import net.mrbt0907.configex.api.ConfigAnnotations.*;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.weather2.Weather2;

import java.io.File;


public class ConfigFront implements IConfigEX {
    @Permission(0)
    @Comment("Show fronts on Radar")
    public static boolean ShowFrontsOnRadar = true;
    @Enforce
    @IntegerRange(min = 0)
    @Name("Max Fronts")
    @Comment("How many front objects can develop in a dimension?")
    public static int max_front_objects = 3;
    @Enforce
    @IntegerRange(min = 0)
    @Name("Max Front Size")
    @Comment("How large can fronts become? Higher values will spread storms out more.")
    public static int max_front_size = 1500;
    @Hidden
    @Enforce
    @IntegerRange(min = 1)
    @Name("Front Tickrate")
    @Comment("How many ticks it takes for fronts to tick once.\nLower numbers will increase lag.")
    public static int tick_rate = 20;
    @Enforce
    @DoubleRange(min = 0.0D)
    @Name("Speed Change Multiplier")
    @Comment("How fast fronts can change speeds. Higher numbers allow for faster acceleration and decceleration of fronts.")
    public static double speed_change_mult = 1.0D;
    @Enforce
    @DoubleRange(min = 0.0D)
    @Name("Angle Change Multiplier")
    @Comment("How fast fronts can change direction. Higher numbers allow for faster direction changes.")
    public static double angle_change_mult = 1.0D;
    @Enforce
    @DoubleRange(min = 0.0D)
    @Name("Environment Change Mult")
    @Comment("How fast fronts update their internal varaiables like temperature. Higher numbers make fronts change faster.")
    public static double environment_change_mult = 1.0D;
    @Enforce
    @DoubleRange(min = 0.0D, max = 100.0D)
    @Name("Chance To Spawn In Front")
    @Comment("Percent chance for a storm to spawn in a front")
    public static double chance_to_spawn_storm_in_front = 20.0D;

    @Override
    public String getName() {
        return "Weather2 Remastered - Front";
    }

    @Override
    public String getSaveLocation() {
        return Weather2.MODID + File.separator + "ConfigFront";
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