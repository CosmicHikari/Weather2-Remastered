package net.mrbt0907.weather2.weather;

import net.corosus.coroutillegacy.util.CoroUtilEntOrParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Type;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.config.ConfigWind;
import net.mrbt0907.weather2.network.packets.PacketWind;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.util.WeatherUtil;
import net.mrbt0907.weather2.util.WeatherUtilEntity;
import net.mrbt0907.weather2.weather.storm.SandstormObject;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.WeatherObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class WindManager {
    private final Map<Vec3, WeatherObject> cache = new HashMap<Vec3, WeatherObject>();
    public WeatherManager manager;
    public float windAngle = 0.0F;
    public float windSpeed = 0.0F;
    public float windAngleTarget = 0.0F;
    public float windSpeedTarget = 0.0F;
    public float windAngleGust = 0.0F;
    public float windSpeedGust = 0.0F;
    public int windTimeGust = 0;
    private long nextWindRefresh;


    public WindManager(WeatherManager parManager) {
        manager = parManager;
        windAngle = Maths.random(360);
        nextWindRefresh = 0L;
    }

    public void tick() {

        if (!manager.isClient()) {
            if (!ConfigWind.enable) {
                windSpeed = 0.0F;
                windSpeedTarget = 0.0F;
                windSpeedGust = 0.0F;
                windTimeGust = 0;
            } else {
                if (manager.world.getGameTime() % 200L == 0L)
                    cache.clear();

                if (manager.getWorld().getGameTime() >= nextWindRefresh) {
                    nextWindRefresh = manager.getWorld().getGameTime() + Maths.random(ConfigWind.windRefreshMin, ConfigWind.windRefreshMax);
                    windSpeedTarget = (float) Maths.random(ConfigWind.windSpeedMin, ConfigWind.windSpeedMax);
                    windAngleTarget += (float) Maths.random(-ConfigWind.windAngleChangeMax, ConfigWind.windAngleChangeMax);

                    windAngleTarget = windAngle % 360.0F;
                }

                tickWindChange();

                if (ConfigWind.enableWindAffectsEntities) {
                    World world = manager.getWorld();
                    if (world instanceof ServerWorld) {
                        ServerWorld serverWorld = (ServerWorld) world;

                        for (Entity entity : serverWorld.getAllEntities()) {
                            if (entity != null && entity.isAlive() && entity instanceof LivingEntity && WeatherUtilEntity.isEntityOutside(entity, true)) {
                                Vec3 a = getWindVectors(
                                        new Vec3(entity.getX(), entity.getY(), entity.getZ()),
                                        new Vec3(entity.getDeltaMovement().x, entity.getDeltaMovement().y, entity.getDeltaMovement().z),
                                        (float) (WeatherUtilEntity.getWeight(entity) * 8F * ConfigWind.windEntityWeightMult * (entity.isInWater() ? ConfigWind.windSwimmingWeightMult : 1.0F)),
                                        0.05F,
                                        5.0F
                                );
                                entity.setDeltaMovement(a.posX, a.posY, a.posZ);
                            }
                        }
                    }
                }
            }
        } else if (!WeatherUtil.isPaused())
            if (ConfigWind.enable)
                tickClient();
            else {
                windSpeed = 0.0F;
                windSpeedTarget = 0.0F;
                windSpeedGust = 0.0F;
                windTimeGust = 0;
            }
    }

    @OnlyIn(Dist.CLIENT)
    public void tickClient() {
        Minecraft mc = Minecraft.getInstance();
        tickWindChangeClient();

        if (manager.world.getGameTime() % 200L == 0L)
            cache.clear();

        if (ConfigWind.enableWindAffectsEntities && mc.player != null && WeatherUtilEntity.isEntityOutside(mc.player, true)) {
            Vec3 a = getWindVectors(new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ()), new Vec3(mc.player.getDeltaMovement().x, mc.player.getDeltaMovement().y, mc.player.getDeltaMovement().z), (float) (WeatherUtilEntity.getWeight(mc.player) * 8.0F * ConfigWind.windPlayerWeightMult * (mc.player.isInWater() ? ConfigWind.windSwimmingWeightMult : 1.0F)), 0.05F, 5.0F);


            mc.player.setDeltaMovement(a.posX, a.posY, a.posZ);
        }
    }

    public void tickWindChange() {

        if (windAngle != windAngleTarget) {
            float difference = windAngle + -(windAngle > 180 && windAngleTarget <= 180 ? windAngleTarget + 360.0F : windAngle <= 180 && windAngleTarget > 180 ? windAngleTarget + -360.0F : windAngleTarget);
            float change = (float) (1.95F * ConfigWind.windChangeMult);
            if (Math.abs(difference) > change)
                if (difference > 0.0F)
                    windAngle -= change;
                else
                    windAngle += change;
            else
                windAngle = windAngleTarget;

            windAngle = windAngle % 360.0F;
        }


        if (windSpeed != windSpeedTarget) {
            float difference = windSpeed - windSpeedTarget;
            float change = (float) (0.015F * ConfigWind.windChangeMult);
            if (Math.abs(difference) > change)
                if (windSpeed > windSpeedTarget)
                    windSpeed -= change;
                else
                    windSpeed += change;
            else
                windSpeed = windSpeedTarget;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void tickWindChangeClient() {

        if (windAngle != windAngleTarget) {
            float difference = windAngle + -(windAngle > 180 && windAngleTarget <= 180 ? windAngleTarget + 360.0F : windAngle <= 180 && windAngleTarget > 180 ? windAngleTarget + -360.0F : windAngleTarget);
            float change = (float) (1.95F * ConfigWind.windChangeMult);
            if (Math.abs(difference) > change)
                if (difference > 0.0F)
                    windAngle -= change;
                else
                    windAngle += change;
            else
                windAngle = windAngleTarget;

            windAngle = windAngle % 360.0F;
        }


        if (windSpeed != windSpeedTarget) {
            float difference = windSpeed - windSpeedTarget;
            float change = (float) (0.015F * ConfigWind.windChangeMult);
            if (Math.abs(difference) > change)
                if (windSpeed > windSpeedTarget)
                    windSpeed -= change;
                else
                    windSpeed += change;
            else
                windSpeed = windSpeedTarget;
        }
    }

    public CompoundNBT nbtSyncForClient() {
        CompoundNBT data = new CompoundNBT();

        data.putFloat("windSpeedTarget", windSpeedTarget);
        data.putFloat("windAngleTarget", windAngleTarget);
        data.putFloat("windSpeedGust", windSpeedGust);
        data.putFloat("windAngleGust", windAngleGust);
        data.putInt("windTimeGust", windTimeGust);

        return data;
    }

    public void nbtSyncFromServer(CompoundNBT parNBT) {

        windSpeedTarget = parNBT.getFloat("windSpeedTarget");
        windAngleTarget = parNBT.getFloat("windAngleTarget");
        windSpeedGust = parNBT.getFloat("windSpeedGust");
        windAngleGust = parNBT.getFloat("windAngleGust");
        windTimeGust = parNBT.getInt("windTimeGust");
    }

    public void syncData() {
        if (manager instanceof WeatherManagerServer)
            PacketWind.update(manager.dim, this);
    }

    public void reset() {
        manager = null;
    }

    public void getEntityWindVectors(Object ent) {
        getEntityWindVectors(ent, 0.1F, 0.5F);
    }


    public void getEntityWindVectors(Object ent, float multiplier, float maxSpeed) {

        Vec3 pos = manager.world.isClientSide ? new Vec3(Minecraft.getInstance().player.blockPosition()) : new Vec3(CoroUtilEntOrParticle.getPosX(ent), CoroUtilEntOrParticle.getPosY(ent), CoroUtilEntOrParticle.getPosZ(ent));
        Vec3 motion = getWindVectors(pos, new Vec3(CoroUtilEntOrParticle.getMotionX(ent), CoroUtilEntOrParticle.getMotionY(ent), CoroUtilEntOrParticle.getMotionZ(ent)), WeatherUtilEntity.getWeight(ent), multiplier, maxSpeed);

        CoroUtilEntOrParticle.setMotionX(ent, motion.posX);
        CoroUtilEntOrParticle.setMotionZ(ent, motion.posZ);
    }

    public Vec3 applyWindForceImpl(Vec3 pos, Vec3 motion, float weight) {
        return getWindVectors(pos, motion, weight, 1F / 20F, 0.5F);
    }


    public Vec3 getWindVectors(Vec3 pos, Vec3 motion, float weight, float multiplier, float maxSpeed) {
        float windAngle = getWindAngle(pos);
        float windSpeed = getWindSpeed(pos);

        float windX = (float) -Maths.fastSin(Math.toRadians(windAngle)) * windSpeed;
        float windZ = (float) Maths.fastCos(Math.toRadians(windAngle)) * windSpeed;

        float objX = (float) motion.posX;
        float objZ = (float) motion.posZ;

        float windWeight = 1F;
        float objWeight = weight;


        if (objWeight == 0.0F)
            objWeight = 0.001F;
        else if (objWeight < 0.0F)
            return motion;


        float weightDiff = windWeight / objWeight;

        float vecX = (objX - windX) * weightDiff;
        float vecZ = (objZ - windZ) * weightDiff;

        vecX *= multiplier;
        vecZ *= multiplier;


        Vec3 newMotion = motion.copy();
        newMotion.posX = Maths.clamp(objX - vecX, -maxSpeed, maxSpeed);
        newMotion.posZ = Maths.clamp(objZ - vecZ, -maxSpeed, maxSpeed);
        return newMotion;
    }

    public float getWindSpeed(Vec3 pos) {
        if (pos == null) return manager.windManager.windSpeed;

        WeatherObject wo = getWeatherObject(pos);

        if (wo != null) {
            float size = (wo.size * 0.90F);
            return Math.max(manager.windManager.windSpeed, (float) ((wo instanceof SandstormObject ? 7.5F : ((StormObject) wo).windSpeed) * Math.min((size - wo.pos.distanceSq(pos) + (wo instanceof SandstormObject ? size : ((StormObject) wo).funnelSize)) / size, 1.0F)));
        } else
            return manager.windManager.windSpeed;
    }

    public float getWindAngle(Vec3 pos) {
        if (pos == null) return manager.windManager.windAngle;

        WeatherObject wo = getWeatherObject(pos);
        if (wo != null) {
            float yaw = (-((float) Maths.fastATan2(wo.posGround.posX - pos.posX, wo.posGround.posZ - pos.posZ)) * 180.0F / (float) Math.PI) + 360.0F;
            return yaw % 360.0F;
        } else
            return manager.windManager.windAngle;
    }

    public Vec3 getWindForce() {
        float windX = (float) -Maths.fastSin(Math.toRadians(windAngle)) * windSpeed;
        float windZ = (float) Maths.fastCos(Math.toRadians(windAngle)) * windSpeed;
        return new Vec3(windX, 0, windZ);
    }

    private WeatherObject getWeatherObject(Vec3 pos) {
        for (Entry<Vec3, WeatherObject> entry : cache.entrySet()) {
            if (pos.distanceSq(entry.getKey()) < 300.0D)
                return entry.getValue();
        }

        WeatherObject wo = manager.getClosestWeather(pos, ConfigStorm.max_storm_size + 50.0D, 0, Integer.MAX_VALUE, Type.CLOUD);
        cache.put(pos, wo);
        return wo;
    }

    public void readFromNBT(CompoundNBT data) {
        windSpeedTarget = data.getFloat("windSpeedTarget");
        windAngleTarget = data.getFloat("windAngleTarget");
        windSpeedGust = data.getFloat("windSpeedGust");
        windAngleGust = data.getFloat("windAngleGust");
        windTimeGust = data.getInt("windTimeGust");

    }

    public CompoundNBT writeToNBT(CompoundNBT data) {
        data.putFloat("windSpeedTarget", windSpeedTarget);
        data.putFloat("windAngleTarget", windAngleTarget);

        data.putFloat("windSpeedGust", windSpeedGust);
        data.putFloat("windAngleGust", windAngleGust);
        data.putInt("windTimeGust", windTimeGust);

        return data;
    }
}