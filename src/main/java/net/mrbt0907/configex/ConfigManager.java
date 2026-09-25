package net.mrbt0907.configex;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.configex.manager.ConfigInstance;
import net.mrbt0907.configex.manager.FieldInstance;
import net.mrbt0907.configex.network.NetworkHandler;
import net.mrbt0907.weather2.mixins.accessor.ClientPlayerEntityAccessor;
import net.mrbt0907.weather2.util.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.util.*;

public class ConfigManager {
    public static final boolean isRemote;
    private static final Map<String, ConfigInstance> configs = new LinkedHashMap<String, ConfigInstance>();

    static {
        isRemote = FMLLoader.getDist().equals(Dist.CLIENT);
    }

    public static void readNBT(CompoundNBT nbt) {
        ConfigModEX.debug("Reading provided config nbt data...");
        CompoundNBT nbtManager = nbt.getCompound("manager");
        nbtManager.getAllKeys().forEach(key ->
        {
            ConfigInstance instance = configs.get(key);
            if (instance == null)
                ConfigModEX.error(new NullPointerException("Received a non-existent nbt entry for configs. Skipping..."));
            else {
                CompoundNBT nbtConfig = nbtManager.getCompound(key);
                if (!isRemote) {
                    if (nbt.hasUUID("player"))
                        nbtConfig.putUUID("player", nbt.getUUID("player"));
                } else
                    nbtConfig.putBoolean("setClient", nbt.getBoolean("setClient"));
                instance.readNBT(nbtConfig);
            }
        });
        ConfigModEX.debug("Finished reading config nbt data");
        if (!ConfigManager.isRemote) {
            ConfigModEX.debug("Sending nbt data back to all clients");
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null && server.isRunning()) {
                NetworkHandler.sendClientPacket(0, ConfigManager.writeNBT(new CompoundNBT()));
            }
        }
    }

    public static CompoundNBT writeNBT(CompoundNBT nbt) {
        ConfigModEX.debug("Writing config nbt data for processing...");
        CompoundNBT nbtManager = new CompoundNBT();
        configs.forEach((name, config) ->
        {
            CompoundNBT nbtConfig = new CompoundNBT();
            config.writeNBT(nbtConfig);
            nbtManager.put(name, nbtConfig);
        });
        ConfigModEX.debug("Finished writing config nbt data");
        nbt.put("manager", nbtManager);
        return nbt;
    }

    public static FieldInstance getFieldInstance(String configName, String fieldName) {
        configName = StringUtils.parseID(configName);
        fieldName = StringUtils.parseID(fieldName);

        ConfigInstance instance = configs.get(configName);
        if (instance == null) {
            ConfigModEX.error(new NullPointerException("Config instance was null. Skipping..."));
            return null;
        }
        return instance.getField(fieldName);
    }

    public static List<FieldInstance> getFieldInstances(String configName) {
        configName = StringUtils.parseID(configName);
        ConfigInstance instance = configs.get(configName);

        if (instance == null) {
            ConfigModEX.error(new NullPointerException("Config instance was null. Skipping..."));
            return null;
        }

        return instance.getFields();
    }

    public static ConfigInstance getInstance(IConfigEX config) {
        return getInstance(config.getName());
    }

    public static ConfigInstance getInstance(String configName) {
        return configs.get(StringUtils.parseID(configName));
    }

    public static List<ConfigInstance> getInstances() {
        return new ArrayList<ConfigInstance>(configs.values());
    }

    public static void save() {
        ConfigModEX.debug("Requested save of all configurations");
        configs.forEach((name, instance) -> save(name, null));
    }

    public static void save(String configID) {
        save(configID, null);
    }

    public static void save(String configID, String variableName) {
        configID = StringUtils.parseID(configID);
        ConfigModEX.debug("Requested save of configuration " + configID);
        ConfigInstance instance = configs.get(configID);

        if (instance == null) {
            ConfigModEX.error(new NullPointerException("Config instance was null. Skipping..."));
            return;
        }

        if (variableName == null)
            instance.updateAllFields(true);
        else {
            FieldInstance field = instance.getField(variableName);

            if (field == null) {
                ConfigModEX.error(new NullPointerException("Field instance was null. Skipping..."));
                return;
            }

            instance.updateField(field, isRemote);
        }

        instance.writeConfigFile(false);
        if (!isRemote) {
            ConfigModEX.debug("Sending information to all clients...");
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null && server.isRunning()) {
                NetworkHandler.sendClientPacket(0, ConfigManager.writeNBT(new CompoundNBT()));
            }
        }
    }

    public static void load() {
        ConfigModEX.debug("Requested load of all configurations");
        configs.forEach((name, instance) -> instance.readConfigFile());
        if (!isRemote) {
            ConfigModEX.debug("Sending information to all clients...");
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null && server.isRunning()) {
                NetworkHandler.sendClientPacket(0, ConfigManager.writeNBT(new CompoundNBT()));
            }
        }
    }

    public static void load(String configID) {
        configID = StringUtils.parseID(configID);
        ConfigModEX.debug("Requested load of configuration " + configID);
        ConfigInstance instance = configs.get(configID);

        if (instance == null) {
            ConfigModEX.error(new NullPointerException("Config instance was null. Skipping..."));
            return;
        }

        instance.readConfigFile();
        if (!isRemote) {
            ConfigModEX.debug("Sending information to all clients...");
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null && server.isRunning()) {
                NetworkHandler.sendClientPacket(0, ConfigManager.writeNBT(new CompoundNBT()));
            }
        }
    }

    public static IConfigEX register(IConfigEX config) {
        if (config == null)
            ConfigModEX.fatal(new NullPointerException("Config instance was null"));
        else if (config.getName() == null)
            ConfigModEX.fatal(new NullPointerException("Config instance has a null name"));
        else if (config.getSaveLocation() == null)
            ConfigModEX.fatal(new NullPointerException("Config instance has a null save location"));
        else if (configs.containsKey(StringUtils.parseID(config.getName()))) {
            ConfigModEX.error(new IllegalArgumentException("Config instance was already registered. Skipping..."));
            return null;
        }
        ConfigInstance instance = new ConfigInstance(config);
        configs.put(instance.registryName, instance);
        ConfigModEX.info("Registered config " + config.getName());

        return config;
    }

    public static int size() {
        return configs.size();
    }

    public static int sizeFields() {
        int size = 0;
        for (ConfigInstance config : configs.values())
            size += config.size();
        return size;
    }

    public static void reset(boolean fullReset) {
        if (isRemote) {
            ConfigModEX.debug("Resetting all config values to original values...");
            configs.forEach((name, config) -> config.reset(fullReset));
            return;
        }
        ConfigModEX.error("Cannot reset variables server side. Skipping call...");
    }

    public static int getPermissionLevel() {
        return getPermissionLevel((UUID) null);
    }

    public static int getPermissionLevel(UUID uuid) {
        if (isRemote) {
            Minecraft mc = Minecraft.getInstance();
            return mc.player == null ? 4 : ((ClientPlayerEntityAccessor) mc.player).invokeGetPermissionLevel();
        } else {
            if (uuid == null) return 4;
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) return 0;
            PlayerList players = server.getPlayerList();
            ServerPlayerEntity player = players.getPlayer(uuid);
            return player == null ? 0 : players.getOps().get(player.getGameProfile()) != null ?
                    players.getOps().get(player.getGameProfile()).getLevel() : 0;
        }
    }

    public static int getPermissionLevel(ServerPlayerEntity player) {
        if (player == null) return 0;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return 0;
        return server.getProfilePermissions(player.getGameProfile());
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isSinglePlayer() {
        Minecraft mc = Minecraft.getInstance();
        return mc.hasSingleplayerServer() || mc.level == null;
    }

    public static String[] getFieldIDs() {
        List<String> ids = new ArrayList<String>();
        String[] output;
        for (ConfigInstance config : configs.values())
            for (FieldInstance field : config.getFields())
                ids.add(field.registryName);
        output = new String[ids.size()];
        output = ids.toArray(output);
        return output;
    }

    public static String formatComment(String comment) {
        return formatComment(new String[]{comment});
    }

    public static String formatComment(String[] comment) {
        String output = "";

        for (String line : comment) {
            if (output.isEmpty())
                output = line;
            else
                output += System.lineSeparator() + line;
        }

        return output.isEmpty() ? null : output;
    }

    public static String formatCommentForCFG(FieldInstance field) {
        if (field.comment == null) return null;
        return (field.comment.isEmpty() ? "" : field.comment + System.lineSeparator()) + (field.type < 6 ? "Minimum: \"" + (field.type < 4 ? String.format("%d", (long) field.min) : field.min) + "\",  Maximum: \"" + (field.type < 4 ? String.format("%d", (long) field.max) : field.max) + "\"" + System.lineSeparator() : "") + "Default: \"" + field.defaultValue + "\"";
    }

    public static String formatCommentForGui(final String comment, String defaultValue, int type, boolean showMin, boolean showMax, double min, double max) {
        if (comment == null) return null;
        String input;

        try {
            input = WordUtils.wrap(comment, 40);
        } catch (Exception e) {
            input = comment;
        }

        return (comment.isEmpty() ? "" : input + System.lineSeparator()) + (type < 6 ? (showMin ? TextFormatting.YELLOW + "Minimum: " + (type < 4 ? String.format("%d", (long) min) : min) + (showMax ? ",  " : System.lineSeparator()) : "") + (showMax ? TextFormatting.YELLOW + "Maximum: " + (type < 4 ? String.format("%d", (long) max) : max) + System.lineSeparator() : "") : "") + TextFormatting.GOLD + "Default: " + defaultValue;
    }
}