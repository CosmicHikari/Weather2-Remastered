package net.mrbt0907.configex.manager;

import net.minecraft.nbt.CompoundNBT;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.ConfigModEX;
import net.mrbt0907.configex.api.ConfigAnnotations.Ignore;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.configex.api.IConfigEX.Phase;
import net.mrbt0907.weather2.util.StringUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigInstance {
    public final IConfigEX config;
    public final String name;
    public final String registryName;
    public final String description;
    public final String saveLocation;
    public final com.electronwill.nightconfig.core.file.CommentedFileConfig configuration;
    public final File fileLocation;
    private final Map<String, FieldInstance> fields = new LinkedHashMap<String, FieldInstance>();
    private int variablesChanged;

    public ConfigInstance(IConfigEX config) {
        this.config = config;
        name = config.getName();
        registryName = StringUtils.parseID(name);
        description = config.getDescription();
        saveLocation = config.getSaveLocation();

        fileLocation = new File(ConfigModEX.getGameFolder() + "config/" + saveLocation + ".toml");


        if (!fileLocation.getParentFile().exists()) {
            fileLocation.getParentFile().mkdirs();
        }

        configuration = com.electronwill.nightconfig.core.file.CommentedFileConfig.builder(fileLocation)
                .sync()
                .autosave()
                .writingMode(com.electronwill.nightconfig.core.io.WritingMode.REPLACE)
                .build();
        initConfig();
    }

    public void initConfig() {
        ConfigModEX.debug("Initializing config " + registryName + "...");
        FieldInstance instance;

        fields.clear();

        for (Field field : config.getClass().getFields()) {
            if (field.isAnnotationPresent(Ignore.class)) {
                ConfigModEX.debug("Ignoring field " + field.getName());
                continue;
            }

            instance = new FieldInstance(config, field);
            fields.put(instance.registryName, instance);
        }


        readConfigFile();

        writeConfigFile(false);

        ConfigModEX.debug("Config " + registryName + " has initialized successfully!");
    }

    public void writeNBT(CompoundNBT nbt) {
        ConfigModEX.debug("Writing fields from " + name + " to nbt...");
        CompoundNBT nbtField;

        for (FieldInstance field : fields.values()) {
            nbtField = new CompoundNBT();
            nbtField.putString("value", String.valueOf(field.getServerValue()));
            nbt.put(field.registryName, nbtField);
        }

        ConfigModEX.debug("Write complete!");
    }

    public void readNBT(CompoundNBT nbt) {
        ConfigModEX.debug("Reading fields from " + (ConfigManager.isRemote ? "the server" : "a client") + " to config " + name + "...");
        CompoundNBT nbtField;
        String value;
        boolean checkPerm = !ConfigManager.isRemote && nbt.hasUUID("player");
        boolean setServer = !ConfigManager.isRemote || !nbt.getBoolean("setClient");
        int permission = checkPerm ? ConfigManager.getPermissionLevel(nbt.getUUID("player")) : ConfigManager.getPermissionLevel();
        config.onConfigChanged(Phase.START, fields.size());
        variablesChanged = 0;
        for (FieldInstance field : fields.values())
            if (nbt.contains(field.registryName)) {
                if (checkPerm && !field.hasPermission(permission)) {
                    ConfigModEX.warn("Rejecting value for " + field.registryName + " as the player does not have permission to change the value");
                    continue;
                }
                nbtField = nbt.getCompound(field.registryName);
                value = nbtField.getString("value");
                try {
                    switch (field.type) {
                        case 1:
                            setField(field, Integer.valueOf(value), setServer);
                            break;
                        case 2:
                            setField(field, Short.valueOf(value), setServer);
                            break;
                        case 3:
                            setField(field, Long.valueOf(value), setServer);
                            break;
                        case 4:
                            setField(field, Float.valueOf(value), setServer);
                            break;
                        case 5:
                            setField(field, Double.valueOf(value), setServer);
                            break;
                        case 6:
                            setField(field, value, setServer);
                            break;
                        case 7:
                            setField(field, Boolean.valueOf(value), setServer);
                            break;
                    }

                    if (updateField(field, false))
                        variablesChanged++;
                } catch (Exception e) {
                    ConfigModEX.warn("Rejecting value for " + field.registryName + " as the value was unable to be set");
                    continue;
                }
            }
        ConfigModEX.debug("Read complete!");
        config.onConfigChanged(Phase.END, variablesChanged);
        variablesChanged = -1;
    }

    public FieldInstance getField(String registryName) {
        return fields.get(registryName);
    }

    public List<FieldInstance> getFields() {
        return new ArrayList<FieldInstance>(fields.values());
    }

    public boolean setField(FieldInstance field, Object value, boolean changeServerVariable) {
        if (field == null)
            ConfigModEX.fatal(new NullPointerException("Field was null"));

        if (ConfigManager.isRemote) {
            if (changeServerVariable)
                return field.setServerValue(value);
            else
                return field.setClientValue(value);
        } else
            return field.setServerValue(value);
    }

    public void defaultField(FieldInstance field) {
        if (field == null)
            ConfigModEX.fatal(new NullPointerException("Field was null"));

        field.setToDefault();
    }

    public boolean updateField(FieldInstance field, boolean fullSync) {
        if (field == null)
            ConfigModEX.fatal(new NullPointerException("Field was null"));

        if (fullSync)
            if (ConfigManager.isRemote)
                field.setClientValue(field.getRealValue());
            else
                field.setServerValue(field.getRealValue());

        Object value = field.getRealCachedValue();

        if (field.hasChanged) {
            field.setRealValue();
            if (variablesChanged < 0) {
                config.onConfigChanged(Phase.START, 1);
                config.onValueChanged(field.name, value, ConfigManager.isRemote ? field.getClientValue() : field.getServerValue());
                config.onConfigChanged(Phase.END, 1);
            } else
                config.onValueChanged(field.name, value, ConfigManager.isRemote ? field.getClientValue() : field.getServerValue());
            return true;
        }
        return false;
    }

    public void updateAllFields(boolean fullSync) {
        if (fullSync) {
            config.onConfigChanged(Phase.START, fields.size());
            variablesChanged = 0;
        }

        for (FieldInstance field : fields.values()) {
            if (updateField(field, fullSync) && fullSync)
                variablesChanged++;
        }

        if (fullSync) {
            config.onConfigChanged(Phase.END, variablesChanged);
            variablesChanged = -1;
        }
    }

    public void writeConfigFile(boolean wipeFile) {
        if (wipeFile && fileLocation.exists()) fileLocation.delete();
        ConfigModEX.debug("Config " + registryName + " is writing to file " + ConfigModEX.getGameFolder() + "config/" + saveLocation + ".toml" + "...");
        fields.forEach((name, field) ->
        {
            if (field.hasConfigValue())
                field.updateConfigValue();
            else
                ConfigModEX.warn("Field " + name + " had not initialized in the config. Skipping write...");
        });
        configuration.save();
        ConfigModEX.debug("Saved config " + registryName + " successfully!");
    }

    public void readConfigFile() {
        ConfigModEX.debug("Config " + registryName + " is reading from file " + ConfigModEX.getGameFolder() + "config/" + saveLocation + ".toml" + "...");
        configuration.load();
        config.onConfigChanged(Phase.START, fields.size());
        variablesChanged = 0;

        fields.forEach((name, field) ->
        {

            String configPath = registryName + "." + field.displayName;

            switch (field.type) {
                case 1:
                    if (!field.hasConfigValue())
                        field.setConfigValue(configuration, field.defaultValue, ConfigManager.formatCommentForCFG(field));

                    setField(field, configuration.getIntOrElse(configPath, (int) field.defaultValue), false);
                    if (updateField(field, false))
                        variablesChanged++;
                    break;
                case 2:
                    if (!field.hasConfigValue())
                        field.setConfigValue(configuration, field.defaultValue, ConfigManager.formatCommentForCFG(field));

                    setField(field, (short) configuration.getIntOrElse(configPath, (short) field.defaultValue), false);
                    if (updateField(field, false))
                        variablesChanged++;
                    break;
                case 3:
                    if (!field.hasConfigValue())
                        field.setConfigValue(configuration, field.defaultValue, ConfigManager.formatCommentForCFG(field));

                    setField(field, configuration.getLongOrElse(configPath, (long) field.defaultValue), false);
                    if (updateField(field, false))
                        variablesChanged++;
                    break;
                case 4:
                    if (!field.hasConfigValue())
                        field.setConfigValue(configuration, field.defaultValue, ConfigManager.formatCommentForCFG(field));

                    setField(field, configuration.get(configPath), false);
                    if (updateField(field, false))
                        variablesChanged++;
                    break;
                case 5:
                    if (!field.hasConfigValue())
                        field.setConfigValue(configuration, field.defaultValue, ConfigManager.formatCommentForCFG(field));

                    setField(field, configuration.get(configPath), false);
                    if (updateField(field, false))
                        variablesChanged++;
                    break;
                case 6:
                    if (!field.hasConfigValue())
                        field.setConfigValue(configuration, field.defaultValue, ConfigManager.formatCommentForCFG(field));

                    setField(field, configuration.getOrElse(configPath, (String) field.defaultValue), false);
                    if (updateField(field, false))
                        variablesChanged++;
                    break;
                case 7:
                    if (!field.hasConfigValue())
                        field.setConfigValue(configuration, field.defaultValue, ConfigManager.formatCommentForCFG(field));

                    setField(field, configuration.getOrElse(configPath, (boolean) field.defaultValue), false);
                    if (updateField(field, false))
                        variablesChanged++;
                    break;
                default: {
                    ConfigModEX.warn("Field " + field.registryName + " is using an unsupported type. Skipping...");
                }
            }
        });
        configuration.save();
        ConfigModEX.debug("Loaded config " + registryName + " successfully!");
        config.onConfigChanged(Phase.END, variablesChanged);
        variablesChanged = -1;
    }

    public int size() {
        return fields.size();
    }

    public void setToDefault() {
        fields.forEach((registryName, field) -> field.setToDefault());
    }

    public void reset(boolean fullReset) {
        config.onConfigChanged(Phase.START, fields.size());
        variablesChanged = 0;
        fields.forEach((name, field) ->
        {
            Object value = field.getRealCachedValue();
            if (fullReset)
                field.reset();
            else
                field.setRealValue();
            if (value != field.getRealCachedValue() || !fullReset) {
                config.onValueChanged(field.name, value, field.getRealCachedValue());
                variablesChanged++;
            }
        });
        config.onConfigChanged(Phase.END, variablesChanged);
        variablesChanged = -1;
        ConfigModEX.debug("Reset all fields for config " + name);
    }
}