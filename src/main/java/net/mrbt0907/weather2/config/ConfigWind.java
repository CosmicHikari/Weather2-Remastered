package net.mrbt0907.weather2.config;

import net.mrbt0907.configex.api.ConfigAnnotations.Comment;
import net.mrbt0907.configex.api.ConfigAnnotations.DoubleRange;
import net.mrbt0907.configex.api.ConfigAnnotations.Enforce;
import net.mrbt0907.configex.api.ConfigAnnotations.IntegerRange;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.weather2.Weather2;

import java.io.File;

public class ConfigWind implements IConfigEX {
    @Enforce
    @Comment("Enables the entire wind systen")
    public static boolean enable = true;
    @Enforce
    @Comment("Enables the ability for wind to push entities.")
    public static boolean enableWindAffectsEntities = true;
    @Enforce
    @DoubleRange(min = 0.0D)
    @Comment("The multiplier for players weight while being pushed by wind. Lower values makes the player lighter.")
    public static double windPlayerWeightMult = 1.0F;
    @Enforce
    @DoubleRange(min = 0.0D)
    @Comment("The multiplier for entities other than players weight while being pushed by wind. Lower values makes the player lighter.")
    public static double windEntityWeightMult = 1.0F;
    @Enforce
    @DoubleRange(min = 0.0D)
    @Comment("The multiplier for all entities weight while being pushed by wind while swimming. Lower values makes all entities lighter while in liquid. Stacks ontop of the other two multipliers")
    public static double windSwimmingWeightMult = 1.0F;

    @Enforce
    @DoubleRange(min = 0.0D, max = 360.0D)
    @Comment("The maximum angle in degrees that wind angle can change with.")
    public static double windAngleChangeMax = 30.0F;
    @Enforce
    @DoubleRange(min = 0.0D)
    @Comment("The multiplier for how smooth wind can change. Lower values increases the smoothness.")
    public static double windChangeMult = 1.0F;

    @Enforce
    @DoubleRange(min = 0.0D)
    @Comment("Min wind speed to maintain for storms and clouds. Higher means faster storms.")
    public static double windSpeedMin = 0.0D;
    @Enforce
    @DoubleRange(min = 0.0D)
    @Comment("Max wind speed to maintain for storms and clouds. Higher means faster storms.")
    public static double windSpeedMax = 2.5D;
    @Enforce
    @IntegerRange(min = 0)
    @Comment("The minimum amount of time in ticks it can take for wind to change speed and direction.")
    public static int windRefreshMin = 100;
    @Enforce
    @IntegerRange(min = 0)
    @Comment("The maximum amount of time in ticks it can take for wind to change speed and direction.")
    public static int windRefreshMax = 400;

    @Override
    public String getName() {
        return "Weather2 Remastered - Wind";
    }

    @Override
    public String getSaveLocation() {
        return Weather2.MODID + File.separator + "ConfigWind";
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