package net.mrbt0907.weather2.weather.storm;

import net.corosus.coroutillegacy.util.CoroUtilEntity;
import net.corosus.coroutillegacy.util.CoroUtilMisc;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.weather.IWeatherDetectable;
import net.mrbt0907.weather2.api.weather.WeatherEnum;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Type;
import net.mrbt0907.weather2.config.ConfigFront;
import net.mrbt0907.weather2.config.ConfigSimulation;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.util.WeatherUtil;
import net.mrbt0907.weather2.weather.WeatherManager;
import net.mrbt0907.weather2.weather.WeatherManagerServer;

import java.util.*;
import java.util.Map.Entry;

public class FrontObject implements IWeatherDetectable {
    protected final Map<UUID, WeatherObject> systems = new HashMap<UUID, WeatherObject>();
    private final WeatherManager manager;
    public CompoundNBT nbt;
    public Vec3 pos;
    public Vec3 motion;
    public boolean overrideAngle;
    public float angle;
    public float size;
    public boolean isDying;
    public boolean isDead;
    public float temperature;
    public float humidity;
    public float pressure;
    public float frontMultiplier;
    public int type;
    public int layer;
    public int maxStorms;
    public int storms;
    public int activeStorms;
    public int deathTicks;
    protected World world;
    private UUID uuid = UUID.randomUUID();
    private boolean isGlobal;

    public FrontObject(WeatherManager manager, Vec3 pos, int layer) {
        this.manager = manager;
        world = manager.getWorld();
        this.pos = pos;
        this.layer = layer;
        size = Maths.random(ConfigStorm.min_storm_size, ConfigFront.max_front_size);
        angle = manager.windManager.windAngle;
        float vecX = (float) -Maths.fastSin(Math.toRadians(angle));
        float vecZ = (float) Maths.fastCos(Math.toRadians(angle));
        float speed = (manager.windManager.windSpeed * 0.1F) + 0.02F;
        motion = new Vec3(vecX * speed, 0.0D, vecZ * speed);

        if (pos == null) {
            maxStorms = -1;
            isGlobal = true;
        } else {
            maxStorms = Maths.random(1, 35);
            temperature = WeatherUtil.getTemperature(world, pos.toBlockPos());
            humidity = WeatherUtil.getTemperature(world, pos.toBlockPos());
            pressure = WeatherUtil.getPressure(world, pos.toBlockPos());
            if (temperature > 0.5 || Maths.chance(25))
                type = 1;
            else
                type = 2;
        }

        frontMultiplier = Maths.random(0.5F, 2.0F);
        nbt = new CompoundNBT();
    }

    public void tick() {
        if (maxStorms > -1 && storms >= maxStorms) {
            if (!isDying)
                isDying = true;

            deathTicks++;

            if ((systems.size() == 0 || activeStorms == 0) && deathTicks > 2000) {
                isDead = true;
                return;
            }
        }

        tickMovement();

        if (!manager.getWorld().isClientSide)
            tickProgressionNormal();

        systems.forEach((uuid, system) -> {
            if (!system.isDead) {
                system.tick();
            }
        });
    }

    public void tickMovement() {
        if (pos != null)
            if (world.isClientSide) {
                pos.posX += motion.posX;
                pos.posZ += motion.posZ;
            } else {
                float mult = (type == 0 ? 0.25F : type == 1 ? 1.25F : 1.0F) * frontMultiplier;
                angle = CoroUtilMisc.adjVal(angle, manager.windManager.windAngle, 0.001F * (float) ConfigFront.angle_change_mult * mult);

                float vecX = (float) -Maths.fastSin(Math.toRadians(angle));
                float vecZ = (float) Maths.fastCos(Math.toRadians(angle));
                float cloudSpeed = 0.2F;
                float speed = ((manager.windManager.windSpeed * cloudSpeed) + (type == 1 ? 0.2F : 0.02F)) * (type == 0 ? 0.1F : 1.0F);
                motion.posX = CoroUtilMisc.adjVal((float) motion.posX, vecX * speed, (float) ConfigFront.speed_change_mult * mult);
                motion.posZ = CoroUtilMisc.adjVal((float) motion.posZ, vecZ * speed, (float) ConfigFront.speed_change_mult * mult);

                pos.posX += motion.posX;
                pos.posZ += motion.posZ;
            }
    }


    public void tickProgressionNormal() {
        if (world.getGameTime() % Math.max(ConfigFront.tick_rate, 1L) == 0L) {
            if (pos != null) {
                BlockPos pos = this.pos.toBlockPos();
                float temperature = WeatherUtil.getTemperature(world, pos);
                float humidity = WeatherUtil.getHumidity(world, pos);
                float pressure = WeatherUtil.getPressure(world, pos);


                if (type == 2 && temperature < 0.5F)
                    temperature = 0.5F;

                this.temperature = CoroUtilMisc.adjVal(this.temperature, temperature, 0.0001F * (float) ConfigFront.environment_change_mult);
                this.humidity = CoroUtilMisc.adjVal(this.humidity, humidity, 0.0005F * (float) ConfigFront.environment_change_mult);
                this.pressure = CoroUtilMisc.adjVal(this.pressure, pressure, 0.0002F * (float) ConfigFront.environment_change_mult);

                if (type == 1)
                    for (FrontObject front : manager.getFronts()) {
                        if (!front.equals(this) && front.type == 2 && front.pos.distanceSq(this.pos) - (front.size * 0.25F) <= 0.0F) {
                            activeStorms += front.activeStorms;
                            maxStorms += front.maxStorms;
                            storms += front.storms;

                            for (WeatherObject weather : front.getWeatherObjects()) {
                                weather.front = this;
                                front.systems.remove(weather.getUUID());
                            }

                            front.isDead = true;
                            type = 0;

                            motion = new Vec3(0.0D, 0.0D, 0.0D);
                        }
                    }
            }
        }
    }

    public StormObject createStorm(double posX, double posZ, int stage, Map<String, Boolean> flags) {
        if (isDying) return null;
        StormObject storm = new StormObject(this);
        storm.layer = layer;
        storm.isNatural = false;
        storm.temperature = 0.1F;
        storm.pos = new Vec3(posX, storm.getLayerHeight(), posZ);
        storm.rain = stage * 50.0F;
        storm.intensity = stage;
        storm.sizeRate = 1.0F;
        storm.stage = stage;
        storm.stageMax = storm.stage;

        if (flags != null)
            for (Entry<String, Boolean> flag : flags.entrySet()) {
                switch (flag.getKey().toLowerCase()) {
                    case "alwaysprogress":
                        storm.alwaysProgresses = flag.getValue();
                    case "neverDissipate":
                        storm.neverDissipate = flag.getValue();
                    case "isFirenado":
                        storm.isFirenado = flag.getValue();
                    case "shouldConvert":
                        storm.shouldConvert = flag.getValue();
                    case "isViolent":
                        storm.isViolent = flag.getValue();
                    case "shouldBuildHumidity":
                        storm.shouldBuildHumidity = flag.getValue();
                }
            }
        addWeatherObject(storm);
        return storm;
    }

    public StormObject createNaturalStorm() {
        return createNaturalStorm(null);
    }

    public StormObject createNaturalStorm(Entity target) {
        if (ConfigStorm.isLayerValid(layer) && !isDying) {
            StormObject storm = new StormObject(this);
            storm.layer = layer;
            storm.isNatural = true;

            if (isGlobal) {
                if (target == null)
                    return null;
                else
                    storm.pos = new Vec3(target.getX() + Maths.random(-ConfigSimulation.max_storm_spawning_distance, ConfigSimulation.max_storm_spawning_distance), storm.getLayerHeight(), target.getZ() + Maths.random(-ConfigSimulation.max_storm_spawning_distance, ConfigSimulation.max_storm_spawning_distance));
            } else
                storm.pos = new Vec3(pos.posX + Maths.random(-size, size), storm.getLayerHeight(), pos.posZ + Maths.random(-size, size));

            if (layer == 0 && Maths.chance(WeatherManagerServer.stormChanceToday * 0.01D))
                storm.initRealStorm();

            addWeatherObject(storm);
            return storm;
        } else
            return null;
    }


    public WeatherObject createWeatherObject(Class<? extends WeatherObject> clazz) {
        if (clazz != null && !isDying) {
            try {
                WeatherObject system = clazz.getConstructor(WeatherManager.class).newInstance(manager);
                addWeatherObject(system);
                return system;
            } catch (Exception e) {
                Weather2.error(e.getMessage());
            }
        }

        Weather2.error("Cannot create a weather object; clazz returned null");
        return null;
    }

    public void removeWeatherObject(UUID uuid) {
        WeatherObject system = systems.get(uuid);
        if (system == null) return;
        system.reset();
        manager.removeWeatherObject(uuid);

        if (!system.type.equals(Type.CLOUD))
            activeStorms--;

        system.reset();
        systems.remove(uuid);
        Weather2.debug("Weather " + uuid + " was removed from front " + this.uuid);
    }

    @OnlyIn(Dist.CLIENT)
    public void cleanupClient(boolean wipe) {
        systems.forEach((uuid, system) -> system.cleanupClient(wipe));
    }

    public void reset() {
        int size = systems.size();
        UUID[] keys = new UUID[systems.size()];
        keys = systems.keySet().toArray(keys);
        for (int i = 0; i < size; i++)
            removeWeatherObject(keys[i]);
    }

    public void aimStormAtPlayer(PlayerEntity entP) {
        Vec3 pos = this.pos;

        if (isGlobal)
            pos = new Vec3(0, 0, 0);

        if (entP == null)
            entP = manager.getWorld().getNearestPlayer(pos.posX, pos.posY, pos.posZ, -1, false);

        if (entP != null) {
            float yaw = -(float) (Maths.fastATan2(entP.getX() - pos.posX, entP.getZ() - pos.posZ) * 180.0D / Math.PI);
            int size = ConfigStorm.storm_aim_accuracy_in_angle;
            if (size > 0)
                yaw += Maths.random(size) - (size / 2);

            angle = yaw;

            Weather2.debug("Front " + uuid + " was aimed at player " + CoroUtilEntity.getName(entP));
        }
    }

    public void readNBT(CompoundNBT nbt) {
        uuid = nbt.getUUID("uuid");
        if (pos != null) {
            pos.posX = nbt.getDouble("posX");
            pos.posY = nbt.getDouble("posY");
            pos.posZ = nbt.getDouble("posZ");
            motion.posX = nbt.getDouble("motionX");
            motion.posY = nbt.getDouble("motionY");
            motion.posZ = nbt.getDouble("motionZ");

            temperature = nbt.getFloat("temperature");
            humidity = nbt.getFloat("humidity");
            pressure = nbt.getFloat("pressure");

            type = nbt.getInt("type");
            frontMultiplier = nbt.getFloat("frontMultiplier");
        }
        angle = nbt.getFloat("angle");
        layer = nbt.getInt("layer");
        maxStorms = nbt.getInt("maxStorms");
        storms = nbt.getInt("storms");
        size = nbt.getFloat("size");
    }

    public CompoundNBT writeNBT() {
        nbt.putUUID("uuid", uuid);
        if (pos != null) {
            nbt.putDouble("posX", pos.posX);
            nbt.putDouble("posY", pos.posY);
            nbt.putDouble("posZ", pos.posZ);
            nbt.putDouble("motionX", motion.posX);
            nbt.putDouble("motionY", motion.posY);
            nbt.putDouble("motionZ", motion.posZ);

            nbt.putFloat("temperature", temperature);
            nbt.putFloat("humidity", humidity);
            nbt.putFloat("pressure", pressure);

            nbt.putInt("type", type);
            nbt.putFloat("frontMultiplier", frontMultiplier);
        }
        nbt.putFloat("angle", angle);
        nbt.putInt("layer", layer);
        nbt.putInt("maxStorms", maxStorms);
        nbt.putInt("storms", storms);
        nbt.putFloat("size", size);

        return nbt;
    }

    public WeatherManager getWeatherManager() {
        return manager;
    }

    public WeatherObject getWeatherObject(UUID uuid) {
        return systems.get(uuid);
    }

    public List<WeatherObject> getWeatherObjects() {
        return new ArrayList<WeatherObject>(systems.values());
    }

    public World getWorld() {
        return manager.getWorld();
    }

    public int size() {
        return systems.size();
    }

    public UUID getUUID() {
        return uuid;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public void addWeatherObject(WeatherObject weather) {
        if (weather != null && weather.front.equals(this)) {
            storms++;

            if (!weather.type.equals(Type.CLOUD))
                activeStorms++;

            systems.put(weather.getUUID(), weather);
            if (ConfigStorm.storms_aim_at_player && !overrideAngle && weather instanceof StormObject && ((StormObject) weather).stageMax >= WeatherEnum.Stage.SEVERE.getStage())
                aimStormAtPlayer(null);
            manager.addWeatherObject(weather);
        }
    }

    public boolean contains(UUID uuid) {
        return systems.containsKey(uuid);
    }

    @Override
    public float getWindSpeed() {
        return 0.0F;
    }

    @Override
    public int getStage() {
        return 0;
    }

    @Override
    public void setStage(int stage) {
    }

    @Override
    public Vec3 getPos() {
        return pos;
    }

    @Override
    public boolean isDying() {
        return isDying;
    }

    @Override
    public String getName() {
        switch (type) {
            case 1:
                return "Cold Front";
            case 2:
                return "Warm Front";
            case 3:
                return "Occluded Front";
            default:
                return "Stationary Front";
        }
    }

    @Override
    public String getTypeName() {
        switch (type) {
            case 1:
                return "CF";
            case 2:
                return "WF";
            case 3:
                return "OF";
            default:
                return "SF";
        }
    }

    @Override
    public float getAngle() {
        return angle;
    }

    @Override
    public float getSpeed() {
        return (float) motion.speedSq();
    }
}