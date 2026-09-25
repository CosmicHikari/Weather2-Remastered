package net.mrbt0907.configex.config;

import net.mrbt0907.configex.ConfigModEX;
import net.mrbt0907.configex.api.ConfigAnnotations.Comment;
import net.mrbt0907.configex.api.ConfigAnnotations.Hidden;
import net.mrbt0907.configex.api.ConfigAnnotations.Name;
import net.mrbt0907.configex.api.IConfigEX;

public class ConfigMaster implements IConfigEX {
    @Name("Enable Debug Mode")
    @Comment("Enables the displaying of various debugging information in the console")
    @Hidden
    public static boolean debug_mode = false;

    @Override
    public String getName() {
        return "ConfigEX - Master";
    }

    @Override
    public String getDescription() {
        return "This is the master config file for the config mod.";
    }

    @Override
    public String getSaveLocation() {
        return ConfigModEX.MODID + "/" + getName();
    }

    @Override
    public void onConfigChanged(Phase phase, int variables) {
    }

    @Override
    public void onValueChanged(String variable, Object oldValue, Object newValue) {
    }
}
