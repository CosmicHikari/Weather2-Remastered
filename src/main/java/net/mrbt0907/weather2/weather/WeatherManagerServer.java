package net.mrbt0907.weather2.weather;

import net.corosus.coroutillegacy.util.CoroUtilFile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.weather.IWeatherRain;
import net.mrbt0907.weather2.api.weather.IWeatherStaged;
import net.mrbt0907.weather2.config.*;
import net.mrbt0907.weather2.network.packets.*;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.util.WeatherUtilBlock;
import net.mrbt0907.weather2.util.WeatherUtilEntity;
import net.mrbt0907.weather2.weather.storm.FrontObject;
import net.mrbt0907.weather2.weather.storm.SandstormObject;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.WeatherObject;
import net.mrbt0907.weather2.weather.volcano.VolcanoObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WeatherManagerServer extends WeatherManager {
    public static int stormChanceToday = 10;
    private long ticksFrontFormed = 0L;
    private long ticksSandstormFormed = 0L;
    private long ticksStormFormed = 0L;

    public WeatherManagerServer(World world) {
        super(world);
    }

    @Override
    public World getWorld() {
        return ServerLifecycleHooks.getCurrentServer().getLevel(dim);
    }

    @Override
    public void tick() {
        super.tick();
        if (world != null) {
            tickWeatherCoverage(ticks);

            FrontObject front;
            WeatherObject system;

            if (world.getDayTime() % 24000 == 1)
                stormChanceToday = Maths.random(ConfigStorm.storm_spawn_chance_min, ConfigStorm.storm_spawn_chance_max);
            List<FrontObject> fronts = new ArrayList<FrontObject>(this.fronts.values());
            List<WeatherObject> systems = getWeatherObjects();
            WeatherObject spawn = null;
            boolean spawned = false, spawnInFront = Maths.chance(ConfigFront.chance_to_spawn_storm_in_front * 0.01D);

            for (int i = 0; i < fronts.size(); i++) {
                front = fronts.get(i);
                if (front.isDead) {
                    if (front.isGlobal()) {
                        front.reset();
                        systems.forEach(weather -> PacketWeatherObject.remove(dim, weather));
                        systems.clear();
                        front.isDead = false;
                    } else {
                        PacketFrontObject.remove(dim, front);
                        removeFront(front.getUUID());
                    }
                } else {
                    if (!front.equals(globalFront) && spawnInFront && canSpawnWeather(1)) {
                        spawn = front.createNaturalStorm();
                        if (spawn != null) {
                            spawned = true;
                            PacketWeatherObject.create(dim, spawn);
                        }
                    }
                    if (ticks % 40 == 0)
                        PacketFrontObject.update(dim, front);
                }
            }

            if (spawned)
                ticksStormFormed = world.getGameTime() + ConfigStorm.storm_spawn_delay;

            for (int i = 0; i < systems.size(); i++) {
                system = systems.get(i);

                if (ticks % 20 == 0) {
                    if (ConfigMisc.remove_storms_if_no_players && world.players().size() == 0 || WeatherUtilEntity.getClosestPlayer(world, system.posGround.posX, system.posGround.posY, system.posGround.posZ, ConfigSimulation.max_storm_distance) == null)
                        system.ticksSinceNoNearPlayer += 20;
                    else
                        system.ticksSinceNoNearPlayer = 0;
                }

                if (system.isDead || system.ticksSinceNoNearPlayer > 600 || ConfigMisc.aesthetic_mode) {
                    PacketWeatherObject.remove(dim, system);
                    system.front.removeWeatherObject(system.getUUID());
                } else if (ticks % system.getNetRate() == 0)
                    PacketWeatherObject.update(dim, system);
            }

            if (ticks % 40 == 0)
                volcanoObjects.forEach(vo -> PacketVolcanoObject.update(dim, vo));

            if (ticks % 60 == 0) {
                PacketWind.update(dim, windManager);
                nbtStormsForIMC();
            }

            if (!ConfigMisc.aesthetic_mode) {
                if (EZConfigParser.isWeatherEnabled(world.dimension().location().toString()) && world.getGameTime() % ConfigStorm.spawningTickRate == 0) {
                    List<PlayerEntity> players = new ArrayList<>(world.players());
                    int layer, frontCount = fronts.size() + 1;

                    for (PlayerEntity player : players) {
                        layer = Maths.random(2);
                        if (canSpawnWeather(0) && ConfigStorm.isLayerValid(layer)) {
                            ticksFrontFormed = world.getGameTime() + ConfigStorm.storm_spawn_delay;
                            PacketFrontObject.create(dim, createNaturalFront(layer, player));

                            if (!ConfigStorm.enable_spawn_per_player)
                                break;
                        }
                        for (int i = 0; i < frontCount; i++)
                            if (!spawnInFront && canSpawnWeather(1)) {
                                spawn = globalFront.createNaturalStorm(player);
                                if (spawn != null) {
                                    spawned = true;
                                    PacketWeatherObject.create(dim, spawn);
                                }
                            }
                    }

                    if (!spawnInFront && spawned)
                        ticksStormFormed = world.getGameTime() + ConfigStorm.storm_spawn_delay;

                    if (canSpawnWeather(2)) {
                        if (world.players().size() > 0) {
                            PlayerEntity player = world.players().get(Maths.random(world.players().size() - 1));
                            ticksSandstormFormed = world.getGameTime() + ConfigSand.sandstorm_spawn_delay;
                            spawnSandstorm(new Vec3(player.getX(), player.getY(), player.getZ()));
                        }
                    }
                }
            }
        }
    }

    public void tickWeatherCoverage(long ticks) {
        IServerWorldInfo worldInfo = (IServerWorldInfo) world.getLevelData();
        boolean isRaining = worldInfo.isRaining();
        boolean isThundering = worldInfo.isThundering();
        if (EZConfigParser.isWeatherEnabled(world.dimension().location().toString())) {
            if (!ConfigMisc.overcast_mode && ConfigMisc.server_weather_mode != -1) {
                worldInfo.setRaining(ConfigMisc.server_weather_mode == 1);
                worldInfo.setThundering(ConfigMisc.server_weather_mode == 1);
            }

            if (isThundering && ConfigStorm.prevent_vanilla_thunderstorms)
                worldInfo.setThundering(false);
        }

        if (ticks % 40 == 0)
            PacketVanillaWeather.send(dim, isRaining ? isThundering ? 2 : 1 : 0, worldInfo.getRainTime());

        if (ticks % 200 == 0)
            cloudIntensity = ConfigMisc.overcast_mode && isRaining ? 1.0F : cloudIntensity + (float) Maths.clamp(Maths.random(ConfigClient.cloud_coverage_change_amount) - Maths.random(ConfigClient.cloud_coverage_change_amount), ConfigClient.min_cloud_coverage_perc * 0.01D, ConfigClient.max_cloud_coverage_perc * 0.01D);
    }

    public void writeToFile() {
        Weather2.debug("Saving weather2 data...");
        CompoundNBT mainNBT = new CompoundNBT();
        CompoundNBT volcanoesNBT = new CompoundNBT();
        volcanoObjects.forEach(vo -> {
            CompoundNBT nbt = new CompoundNBT();
            vo.writeToNBT(nbt);
            volcanoesNBT.put("volcano_" + vo.ID, nbt);
        });
        mainNBT.put("volcanoData", volcanoesNBT);

        CompoundNBT frontNBT = new CompoundNBT();
        CompoundNBT weatherNBT = new CompoundNBT();
        frontNBT.put("front_global", globalFront.writeNBT());
        globalFront.getWeatherObjects().forEach(weatherObject ->
                {
                    weatherObject.nbt.setUpdateForced(true);
                    weatherObject.nbt.setUUID("frontUUID", globalFront.getUUID());
                    weatherNBT.put("storm_" + weatherObject.getUUID().toString(), weatherObject.writeToNBT().getNewNBT());
                    weatherObject.nbt.setUpdateForced(false);
                    Weather2.debug("Saved storm_" + weatherObject.getUUID().toString());
                }
        );
        Weather2.debug("Saved front_global");

        fronts.forEach((uuid, front) ->
                {
                    if (!front.equals(globalFront)) {
                        frontNBT.put("front_" + uuid.toString(), front.writeNBT());
                        front.getWeatherObjects().forEach(weatherObject ->
                                {
                                    weatherObject.nbt.setUpdateForced(true);
                                    weatherObject.nbt.setUUID("frontUUID", uuid);
                                    weatherNBT.put("storm_" + weatherObject.getUUID().toString(), weatherObject.writeToNBT().getNewNBT());
                                    weatherObject.nbt.setUpdateForced(false);
                                    Weather2.debug("Saved storm_" + weatherObject.getUUID().toString());
                                }
                        );
                        Weather2.debug("Saved front_" + uuid);
                    }
                }
        );

        mainNBT.put("frontData", frontNBT);
        mainNBT.put("stormData", weatherNBT);
        mainNBT.putFloat("cloudIntensity", cloudIntensity);
        mainNBT.putFloat("ticksFrontFormed", ticksFrontFormed);
        mainNBT.putFloat("ticksSandstormFormed", ticksSandstormFormed);
        mainNBT.putFloat("ticksStormFormed", ticksStormFormed);
        mainNBT.put("windMan", windManager.writeToNBT(new CompoundNBT()));
        mainNBT.putInt("stormChanceToday", stormChanceToday);

        String saveFolder = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + "weather2" + File.separator;

        try {
            if (!(new File(saveFolder).exists())) new File(saveFolder).mkdirs();
            FileOutputStream fos = new FileOutputStream(saveFolder + "WeatherData_" + dim.location().toString().replace(":", "_") + ".dat");
            CompressedStreamTools.writeCompressed(mainNBT, fos);
            fos.close();
            Weather2.debug("Save successful!");
        } catch (Exception ex) {
            Weather2.debug("Save failed.");
            ex.printStackTrace();
        }
    }

    public void readFromFile() {
        Weather2.debug("Loading weather2 data...");
        CompoundNBT mainNBT = new CompoundNBT();
        String saveFolder = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + "weather2" + File.separator;

        try {
            if (new File(saveFolder + "WeatherData_" + dim.location().toString().replace(":", "_") + ".dat").exists()) {
                mainNBT = CompressedStreamTools.readCompressed(new FileInputStream(saveFolder + "WeatherData_" + dim.location().toString().replace(":", "_") + ".dat"));
                File tmp = (new File(saveFolder + "WeatherData_" + dim.location().toString().replace(":", "_") + "_BACKUP0.dat"));
                if (tmp.exists())
                    FileUtils.copyFile(tmp, (new File(saveFolder + "WeatherData_" + dim.location().toString().replace(":", "_") + "_BACKUP1.dat")));
                if ((new File(saveFolder + "WeatherData_" + dim.location().toString().replace(":", "_") + ".dat").exists()))
                    FileUtils.copyFile((new File(saveFolder + "WeatherData_" + dim.location().toString().replace(":", "_") + ".dat")), (new File(saveFolder + "WeatherData_" + dim.location().toString().replace(":", "_") + "_BACKUP0.dat")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Weather2.warn("Weather2 File: WeatherData.dat failed to load, automatically restoring to backup from previous game run");
            try {
                if ((new File(saveFolder + "WeatherData_" + dim.location().toString().replace(":", "_") + "_BACKUP0.dat")).exists())
                    mainNBT = CompressedStreamTools.readCompressed(new FileInputStream(saveFolder + "WeatherData_" + dim.location().toString().replace(":", "_") + "_BACKUP0.dat"));
                else
                    Weather2.warn("Failed to find backup file WeatherData_BACKUP0.dat, nothing loaded");
            } catch (Exception e) {
                e.printStackTrace();
                Weather2.warn("Completely failed to find backup file WeatherData_BACKUP0.dat, nothing loaded");
            }
        }

        if (mainNBT.contains("cloudIntensity"))
            cloudIntensity = mainNBT.getFloat("cloudIntensity");
        if (mainNBT.contains("ticksFrontFormed"))
            ticksFrontFormed = mainNBT.getLong("ticksFrontFormed");
        if (mainNBT.contains("ticksSandstormFormed"))
            ticksSandstormFormed = mainNBT.getLong("ticksSandstormFormed");
        if (mainNBT.contains("ticksStormFormed"))
            ticksStormFormed = mainNBT.getLong("ticksStormFormed");
        if (mainNBT.contains("stormChanceToday"))
            stormChanceToday = mainNBT.getInt("stormChanceToday");

        windManager.readFromNBT(mainNBT.getCompound("windMan"));
        CompoundNBT volcanosNBT = mainNBT.getCompound("volcanoData");

        volcanosNBT.getAllKeys().forEach(name -> {
            CompoundNBT nbt = volcanosNBT.getCompound(name);
            VolcanoObject vo = new VolcanoObject(this);

            try {
                vo.readFromNBT(nbt);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            addVolcanoObject(vo);

            PacketVolcanoObject.create(dim, vo);
        });

        CompoundNBT frontNBT = mainNBT.getCompound("frontData");
        frontNBT.getAllKeys().forEach(name ->
        {
            CompoundNBT nbt = frontNBT.getCompound(name);
            Weather2.debug("Front name: " + name);
            if (name.equals("front_global")) {
                globalFront = new FrontObject(this, null, 0);
                globalFront.readNBT(nbt);
                fronts.put(globalFront.getUUID(), globalFront);

                Weather2.debug("Loaded global front " + globalFront.getUUID().toString());
                PacketFrontObject.create(world.dimension(), globalFront);
            } else {
                FrontObject front = new FrontObject(this, new Vec3(nbt.getInt("layer"), nbt.getDouble("posX"), nbt.getDouble("posZ")), nbt.getInt("layer"));
                front.readNBT(nbt);
                fronts.put(front.getUUID(), front);
                Weather2.debug("Loaded front " + front.getUUID().toString());
                PacketFrontObject.create(world.dimension(), front);
            }
        });

        if (globalFront == null) {
            globalFront = new FrontObject(this, null, 0);
            fronts.put(globalFront.getUUID(), globalFront);
            Weather2.debug("Created a new global front " + globalFront.getUUID().toString());
            PacketFrontObject.create(world.dimension(), globalFront);
        }

        CompoundNBT weatherNBT = mainNBT.getCompound("stormData");
        weatherNBT.getAllKeys().forEach(name ->
        {
            CompoundNBT nbt = weatherNBT.getCompound(name);
            FrontObject front = getFront(nbt.getUUID("frontUUID"));
            WeatherObject wo = null;

            if (front == null) front = globalFront;
            switch (nbt.getInt("weatherObjectType")) {
                case 0:
                    wo = new StormObject(front);
                    break;
                case 1:
                    wo = new SandstormObject(this);
                    break;
                default:
                    Weather2.warn("Non-existant type attempted to load into the weather system. Skipping...");
            }

            if (wo != null) {
                try {
                    wo.nbt.setNewNBT(nbt);
                    wo.nbt.updateCacheFromNew();
                    wo.readFromNBT();
                    front.addWeatherObject(wo);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                Weather2.debug("Loaded storm " + wo.getUUID().toString());
                PacketWeatherObject.create(world.dimension(), wo);
            }
        });
        Weather2.debug("Loading Weather2 data successful!");
    }

    public boolean spawnSandstorm(Vec3 posIn) {
        int searchRadius = 512;
        double angle = windManager.windAngle;

        double dirX = -Maths.fastSin(Math.toRadians(angle));
        double dirZ = Maths.fastCos(Math.toRadians(angle));
        double vecX = dirX * searchRadius / 2 * -1;
        double vecZ = dirZ * searchRadius / 2 * -1;

        Random rand = new Random();

        BlockPos foundPos = null;

        int findTriesMax = 30;
        for (int i = 0; i < findTriesMax; i++) {
            int x = MathHelper.floor(posIn.posX + vecX + rand.nextInt(searchRadius * 2) - searchRadius);
            int z = MathHelper.floor(posIn.posZ + vecZ + rand.nextInt(searchRadius * 2) - searchRadius);

            BlockPos pos = new BlockPos(x, 0, z);

            if (!world.isLoaded(pos)) continue;
            Biome biomeIn = world.getBiome(pos);

            if (SandstormObject.isDesert(biomeIn, true)) {
                foundPos = pos;

                double dirXLeft = -Maths.fastSin(Math.toRadians(angle - 90));
                double dirZLeft = Maths.fastCos(Math.toRadians(angle - 90));
                double dirXRight = -Maths.fastSin(Math.toRadians(angle + 90));
                double dirZRight = Maths.fastCos(Math.toRadians(angle + 90));

                double distLeftRight = 20;
                BlockPos posLeft = new BlockPos(foundPos.getX() + (dirXLeft * distLeftRight), 0, foundPos.getZ() + (dirZLeft * distLeftRight));
                if (!world.isLoaded(posLeft)) continue;
                if (!SandstormObject.isDesert(world.getBiome(posLeft))) continue;

                BlockPos posRight = new BlockPos(foundPos.getX() + (dirXRight * distLeftRight), 0, foundPos.getZ() + (dirZRight * distLeftRight));
                if (!world.isLoaded(posRight)) continue;
                if (!SandstormObject.isDesert(world.getBiome(posRight))) continue;

                BlockPos posFind = new BlockPos(foundPos);
                BlockPos posFindLastGoodUpwind = new BlockPos(foundPos);
                BlockPos posFindLastGoodDownwind = new BlockPos(foundPos);
                double tickDist = 10;

                while (world.isLoaded(posFind) && SandstormObject.isDesert(world.getBiome(posFind))) {
                    posFindLastGoodUpwind = new BlockPos(posFind);

                    int xx = MathHelper.floor(posFind.getX() + (dirX * -1D * tickDist));
                    int zz = MathHelper.floor(posFind.getZ() + (dirZ * -1D * tickDist));

                    posFind = new BlockPos(xx, 0, zz);
                }

                posFind = new BlockPos(foundPos);

                while (world.isLoaded(posFind) && SandstormObject.isDesert(world.getBiome(posFind))) {
                    posFindLastGoodDownwind = new BlockPos(posFind);

                    int xx = MathHelper.floor(posFind.getX() + (dirX * 1D * tickDist));
                    int zz = MathHelper.floor(posFind.getZ() + (dirZ * 1D * tickDist));

                    posFind = new BlockPos(xx, 0, zz);
                }

                int minDistanceOfDesertStretchNeeded = 200;
                double dist = Math.sqrt(posFindLastGoodUpwind.distSqr(posFindLastGoodDownwind.getX(), posFindLastGoodDownwind.getY(), posFindLastGoodDownwind.getZ(), false));

                if (dist >= minDistanceOfDesertStretchNeeded) {
                    SandstormObject sandstorm = new SandstormObject(this);

                    sandstorm.init();
                    BlockPos posSpawn = new BlockPos(WeatherUtilBlock.getPrecipitationHeightSafe(world, posFindLastGoodUpwind)).above(1);
                    sandstorm.initSandstormSpawn(new Vec3(posSpawn));
                    globalFront.addWeatherObject(sandstorm);
                    PacketWeatherObject.create(dim, sandstorm);

                    Weather2.debug("found decent spot and stretch for sandstorm, stretch: " + dist);
                    return true;
                }
            }
        }

        Weather2.debug("couldnt spawn sandstorm");
        return false;
    }

    public void playerJoinedWorldSyncFull(ServerPlayerEntity entP) {
        Weather2.debug((entP == null ? "An unknown player " : "Player " + entP.getDisplayName().getString() + "'s client requested for a full sync"));

        fronts.forEach((uuid, front) -> {
            PacketFrontObject.create(entP, front);
            front.getWeatherObjects().forEach(wo -> PacketWeatherObject.create(entP, wo));
        });

        volcanoObjects.forEach(vo -> PacketVolcanoObject.create(entP, vo));
    }

    public void nbtStormsForIMC() {
        CompoundNBT nbt = new CompoundNBT();
        List<WeatherObject> list = getWeatherObjects();
        for (WeatherObject wo : list)
            if (wo instanceof IWeatherRain && ((IWeatherRain) wo).hasDownfall() || wo instanceof IWeatherStaged && wo.getStage() > 0) {
                wo.writeToNBT();
                nbt.put("storm_" + wo.getUUID().toString(), wo.nbt.getNewNBT());
            }

        if (!nbt.isEmpty())
            InterModComms.sendTo(Weather2.MODID, "weather.storms", () -> nbt);
    }

    protected boolean canSpawnWeather(int type) {
        if (!EZConfigParser.isWeatherEnabled(world.dimension().location().toString()))
            return false;
        long ticks;

        switch (type) {
            case 0:
                ticks = ticksFrontFormed - world.getGameTime();
                if (ticks > ConfigStorm.storm_spawn_delay)
                    ticksFrontFormed = world.getGameTime() + ConfigStorm.storm_spawn_delay;
                return (!ConfigStorm.disable_tornados || !ConfigStorm.disable_cyclones) && ticksFrontFormed < world.getGameTime() && fronts.size() - 1 < ConfigFront.max_front_objects;
            case 1:
                ticks = ticksStormFormed - world.getGameTime();
                if (ticks > ConfigStorm.storm_spawn_delay)
                    ticksStormFormed = world.getGameTime() + ConfigStorm.storm_spawn_delay;
                return (!ConfigStorm.disable_tornados || !ConfigStorm.disable_cyclones) && ticksStormFormed < world.getGameTime() && systems.size() < ConfigStorm.max_weather_objects;
            case 2:
                return false;
            default:
                return false;
        }
    }
}