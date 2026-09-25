package net.mrbt0907.weather2.event;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.EZConfigParser;
import net.mrbt0907.weather2.entity.EntityMovingBlock;
import net.mrbt0907.weather2.network.packets.PacketEZGUI;
import net.mrbt0907.weather2.weather.WeatherManagerServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerTickHandler {
    public static Map<RegistryKey<World>, WeatherManagerServer> dimensionSystems = new HashMap<>();
    public static World lastWorld;
    public static CompoundNBT worldNBT = new CompoundNBT();

    public static void onTickInGame() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null)
            return;

        ServerWorld world = server.getLevel(World.OVERWORLD);

        if (world != null && ServerTickHandler.lastWorld != world)
            ServerTickHandler.lastWorld = world;

        if (world != null)
            if (world.getGameTime() % ConfigMisc.auto_save_interval == 0)
                Weather2.writeOutData(false);

        Iterable<ServerWorld> worlds = server.getAllLevels();
        List<RegistryKey<World>> removedManagers = new ArrayList<>();

        for (ServerWorld dim : worlds) {
            RegistryKey<World> dimension = dim.dimension();
            ResourceLocation dimLocation = dimension.location();
            String dimFullPath = dimLocation.toString();

            EntityMovingBlock.updateEntities(dim);

            if (!ServerTickHandler.dimensionSystems.containsKey(dimension)) {
                if (EZConfigParser.isWeatherEnabled(dimFullPath)) {
                    ServerTickHandler.addWeatherSystem(dim);
                }

                if (!EZConfigParser.dimNames.containsKey(dimFullPath)) {
                    String dimName = dimLocation.toString();
                    EZConfigParser.dimNames.put(dimFullPath, dimFullPath + ":>  " + dimName);

                    CompoundNBT dimData = EZConfigParser.nbtServerData.getCompound("dimData");
                    dimData.putString("dima_" + dimFullPath, dimFullPath + ":>  " + dimName);
                    EZConfigParser.nbtSaveDataServer();
                }
            }

            if (ServerTickHandler.dimensionSystems.containsKey(dimension)) {
                if (EZConfigParser.isWeatherEnabled(dimFullPath))
                    ServerTickHandler.dimensionSystems.get(dimension).tick();
                else
                    removedManagers.add(dimension);
            }
        }

        for (RegistryKey<World> key : removedManagers)
            ServerTickHandler.removeWeatherSystem(key);

        if (ConfigMisc.aesthetic_mode) {
            if (!ConfigMisc.overcast_mode) {
                ConfigMisc.overcast_mode = true;
                Weather2.debug("detected Aesthetic_Only_Mode on, setting overcast mode on");
                EZConfigParser.setOvercastModeServerSide(ConfigMisc.overcast_mode);
                ConfigManager.save();
                ServerTickHandler.syncServerConfigToClient();
            }
        }

        if (world != null && world.getGameTime() % 200 == 0)
            ServerTickHandler.syncServerConfigToClient();
    }

    public static void addWeatherSystem(ServerWorld world) {
        RegistryKey<World> dim = world.dimension();
        Weather2.debug("Registering Weather2 manager for dim: " + dim.location());
        WeatherManagerServer wm = new WeatherManagerServer(world);
        ServerTickHandler.dimensionSystems.put(dim, wm);
        wm.readFromFile();
    }

    public static void removeWeatherSystem(RegistryKey<World> dim) {
        Weather2.debug("Weather2: Unregistering manager for dim: " + dim.location());
        WeatherManagerServer wm = ServerTickHandler.dimensionSystems.get(dim);

        try {
            if (wm != null) {
                ServerTickHandler.dimensionSystems.remove(dim);
                wm.writeToFile();
                wm.reset(true);
            }
        } catch (Exception e) {
            Weather2.error(e);
        }
    }

    public static void playerClientRequestsFullSync(ServerPlayerEntity entP) {
        WeatherManagerServer wm = ServerTickHandler.dimensionSystems.get(entP.level.dimension());
        if (wm != null) {
            wm.playerJoinedWorldSyncFull(entP);
        }
    }

    public static void reset() {
        Weather2.debug("Weather2: ServerTickHandler resetting");
        int size = ServerTickHandler.dimensionSystems.size();
        Object[] set = ServerTickHandler.dimensionSystems.keySet().toArray();

        for (int i = 0; i < size; i++)
            ServerTickHandler.removeWeatherSystem((RegistryKey<World>) set[i]);

        EntityMovingBlock.resetEntities();

        if (!ServerTickHandler.dimensionSystems.isEmpty()) {
            Weather2.debug("Weather2: reset state failed to manually clear lists, dimensionSystems.size(): " + ServerTickHandler.dimensionSystems.size() + " - forcing a full clear of lists");
            ServerTickHandler.dimensionSystems.clear();
        }
    }

    public static WeatherManagerServer getWeatherSystemForDim(RegistryKey<World> dimKey) {
        return ServerTickHandler.dimensionSystems.get(dimKey);
    }

    public static WeatherManagerServer getWeatherSystemForDim(ResourceLocation dimLocation) {
        for (Map.Entry<RegistryKey<World>, WeatherManagerServer> entry : dimensionSystems.entrySet()) {
            if (entry.getKey().location().equals(dimLocation)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static void syncServerConfigToClient() {
        CompoundNBT data = new CompoundNBT();
        PacketEZGUI.apply(data);
    }

    public static void syncServerConfigToClientPlayer(ServerPlayerEntity player) {
        CompoundNBT data = new CompoundNBT();
        PacketEZGUI.apply(data, player);
    }
}