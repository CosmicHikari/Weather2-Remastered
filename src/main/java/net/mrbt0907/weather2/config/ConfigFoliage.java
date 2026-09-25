package net.mrbt0907.weather2.config;

import net.mrbt0907.configex.api.ConfigAnnotations.Comment;
import net.mrbt0907.configex.api.ConfigAnnotations.IntegerRange;
import net.mrbt0907.configex.api.ConfigAnnotations.Name;
import net.mrbt0907.configex.api.ConfigAnnotations.Permission;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.weather2.Weather2;

import java.io.File;

public class ConfigFoliage implements IConfigEX {
    @Permission(0)
    @Name("Foliage Render Distance")
    @Comment("How far in blocks can foliage shaders render up to?")
    public static int shader_range = 40;
    @Permission(0)
    @IntegerRange(min = 0)
    @Name("Foliage Process Delay")
    @Comment("How many ticks it takes for foliage shaders to process new plants.\nLower values increases lag.")
    public static int shader_process_delay = 1000;
    @Permission(0)
    @Name("Enable Extra Grass")
    @Comment("When foliage shaders are enabled in the EZ Gui, should extra grass render?")
    public static boolean enable_extra_grass = false;

    @Override
    public String getName() {
        return "Weather2 Remastered - Foliage";
    }

    @Override
    public String getSaveLocation() {
        return Weather2.MODID + File.separator + "ConfigFoliage";
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