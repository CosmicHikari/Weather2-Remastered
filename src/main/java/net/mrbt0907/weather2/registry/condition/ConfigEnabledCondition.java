package net.mrbt0907.weather2.registry.condition;

import com.google.gson.JsonObject;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.config.ConfigMisc;

public class ConfigEnabledCondition implements ICondition {
    public static final ResourceLocation NAME = new ResourceLocation(Weather2.OLD_MODID, "config_enabled");

    private final String configKey;

    public ConfigEnabledCondition(String configKey) {
        this.configKey = configKey;
    }

    @Override
    public ResourceLocation getID() {
        return NAME;
    }

    @Override
    public boolean test() {
        return !ConfigMisc.isDisabled(configKey);
    }

    @Override
    public String toString() {
        return "config_enabled(\"" + configKey + "\")";
    }

    public static class Serializer implements IConditionSerializer<ConfigEnabledCondition> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, ConfigEnabledCondition value) {
            json.addProperty("key", value.configKey);
        }

        @Override
        public ConfigEnabledCondition read(JsonObject json) {
            return new ConfigEnabledCondition(JSONUtils.getAsString(json, "key"));
        }

        @Override
        public ResourceLocation getID() {
            return NAME;
        }
    }
}