package net.mrbt0907.weather2.config;

import net.mrbt0907.configex.api.ConfigAnnotations.Comment;
import net.mrbt0907.configex.api.ConfigAnnotations.Enforce;
import net.mrbt0907.configex.api.ConfigAnnotations.IntegerRange;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.weather2.Weather2;

import java.io.File;


public class ConfigSimulation implements IConfigEX {
    @Enforce
    @IntegerRange(min = 0)
    @Comment("Distance storms can go to from players before they are deleted")
    public static int max_storm_distance = 2300;
    @Enforce
    @IntegerRange(min = 0)
    @Comment("Distance storms can spawn away from players")
    public static int max_storm_spawning_distance = 2150;

    @Override
    public String getName() {
        return "Simulation";
    }

    @Override
    public String getSaveLocation() {
        return Weather2.MODID + File.separator + getName();
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