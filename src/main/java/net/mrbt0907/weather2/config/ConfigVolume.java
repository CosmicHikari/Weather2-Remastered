package net.mrbt0907.weather2.config;

import net.mrbt0907.configex.api.ConfigAnnotations.Comment;
import net.mrbt0907.configex.api.ConfigAnnotations.FloatRange;
import net.mrbt0907.configex.api.ConfigAnnotations.Permission;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.weather2.Weather2;

import java.io.File;

public class ConfigVolume implements IConfigEX {
    @Permission(0)
    @FloatRange(min = 0.0F, max = 1.0F)
    @Comment("How loud lightning sounds are ingame. 1.0 is full volume, higher is louder, and lower is softer")
    public static float lightning = 1.0F;
    @Permission(0)
    @FloatRange(min = 0.0F, max = 1.0F)
    @Comment("How loud waterfall sounds are ingame. 1.0 is full volume, higher is louder, and lower is softer")
    public static float waterfall = 0.25F;
    @Permission(0)
    @FloatRange(min = 0.0F, max = 1.0F)
    @Comment("How loud rain sounds are ingame. 1.0 is full volume, higher is louder, and lower is softer")
    public static float rain = 0.1F;
    @Permission(0)
    @FloatRange(min = 0.0F, max = 1.0F)
    @Comment("How loud wind sounds are ingame. 1.0 is full volume, higher is louder, and lower is softer")
    public static float wind = 0.24F;
    @Permission(0)
    @FloatRange(min = 0.0F, max = 1.0F)
    @Comment("How loud leaves are ingame. 1.0 is full volume, higher is louder, and lower is softer")
    public static float leaves = 0.3F;
    @Permission(0)
    @FloatRange(min = 0.0F, max = 1.0F)
    @Comment("How loud tornado/hurricane sounds are ingame. 1.0 is full volume, higher is louder, and lower is softer")
    public static float cyclone = 1.0F;
    @Permission(0)
    @FloatRange(min = 0.0F, max = 1.0F)
    @Comment("How loud debris sounds are ingame. 1.0 is full volume, higher is louder, and lower is softer")
    public static float debris = 1.0F;
    @Permission(0)
    @FloatRange(min = 0.0F, max = 1.0F)
    @Comment("How loud sirens are ingame. 1.0 is full volume, higher is louder, and lower is softer")
    public static float sirens = 1.0F;

    @Override
    public String getName() {
        return "Weather2 Remastered - Volume";
    }

    @Override
    public String getSaveLocation() {
        return Weather2.MODID + File.separator + "ConfigVolume";
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