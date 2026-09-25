package net.mrbt0907.weather2.config;

import net.mrbt0907.configex.api.ConfigAnnotations.Comment;
import net.mrbt0907.configex.api.ConfigAnnotations.Enforce;
import net.mrbt0907.configex.api.ConfigAnnotations.IntegerRange;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.weather2.Weather2;

import java.io.File;


public class ConfigSnow implements IConfigEX {
    //snow
    @Enforce
    @Comment("Should snow build up in a snowstorm?")
    public static boolean Snow_PerformSnowfall = false;
    @Enforce
    @IntegerRange(min = 1)
    @Comment("Snow layers have a 1 in x chance to spawn durring a snowstorm")
    public static int Snow_RarityOfBuildup = 64;

    @Override
    public String getName() {
        return "Weather2 Remastered - Snow";
    }

    @Override
    public String getSaveLocation() {
        return Weather2.MODID + File.separator + "ConfigSnow";
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