package net.mrbt0907.weather2.block.tile;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.weather.WeatherEnum;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.event.ServerTickHandler;
import net.mrbt0907.weather2.registry.TileEntityRegistry;
import net.mrbt0907.weather2.weather.WeatherManagerServer;
import net.mrbt0907.weather2.weather.storm.FrontObject;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.WeatherObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TileComputerRadar extends TileEntity implements ITickableTileEntity {
    public static final int SCAN_RATE = 100;
    public static final int MIN_RANGE = 64;
    public final List<Map<String, Object>> cachedStorms = new ArrayList<>();
    public final List<Map<String, Object>> cachedFronts = new ArrayList<>();
    private int detectionRange = -1;

    public TileComputerRadar() {
        this(TileEntityRegistry.COMPUTER_RADAR_TILE.get());
    }

    public TileComputerRadar(net.minecraft.tileentity.TileEntityType<?> type) {
        super(type);
    }

    public static int getConfiguredMaxRange() {
        return (int) ConfigMisc.computer_radar_range;
    }

    static Map<String, Object> buildStormTable(WeatherObject wo, double distSq, double radarX, double radarZ) {
        Map<String, Object> t = new HashMap<>();

        t.put("id", wo.getUUID() != null ? wo.getUUID().toString() : "");
        t.put("type", wo.type != null ? wo.type.name() : "UNKNOWN");
        t.put("x", wo.pos.posX);
        t.put("y", wo.pos.posY);
        t.put("z", wo.pos.posZ);
        t.put("groundY", wo.posGround.posY);

        t.put("dx", wo.motion.posX);
        t.put("dy", wo.motion.posY);
        t.put("dz", wo.motion.posZ);

        t.put("distanceSq", distSq);
        t.put("distance", Math.sqrt(distSq));

        double bDx = wo.pos.posX - radarX;
        double bDz = wo.pos.posZ - radarZ;
        double bearing = Math.toDegrees(Math.atan2(bDx, -bDz));
        if (bearing < 0) bearing += 360;
        t.put("bearing", bearing);

        double speed = Math.sqrt(wo.motion.posX * wo.motion.posX + wo.motion.posZ * wo.motion.posZ);
        double heading = Math.toDegrees(Math.atan2(wo.motion.posX, -wo.motion.posZ));
        if (heading < 0) heading += 360;
        t.put("speed", speed);
        t.put("heading", heading);

        if (speed > 0) {
            double relVx = wo.motion.posX - 0;
            double relVz = wo.motion.posZ - 0;
            double dot = (-bDx) * relVx + (-bDz) * relVz;
            double eta = dot > 0 ? Math.sqrt(distSq) / speed : -1;
            t.put("etaTicks", eta);
        } else {
            t.put("etaTicks", -1.0);
        }

        t.put("ticks", wo.ticks);
        t.put("size", wo.size);

        if (wo instanceof StormObject) {
            StormObject so = (StormObject) wo;
            t.put("name", so.name != null ? so.name : "");
            t.put("fullName", so.getName());
            t.put("typeName", so.getTypeName());
            t.put("stage", so.stage);
            t.put("stageMax", so.stageMax);
            t.put("layer", so.layer);
            t.put("stormType", so.stormType == StormObject.StormType.WATER.ordinal() ? "WATER" : "LAND");
            t.put("intensity", (double) so.intensity);
            t.put("intensityRate", (double) so.intensityRate);
            t.put("intensityMax", (double) so.intensityMax);
            t.put("sizeRate", (double) so.sizeRate);
            t.put("formingStrength", (double) so.formingStrength);
            t.put("windSpeed", (double) so.windSpeed);
            t.put("funnelSize", (double) so.funnelSize);
            t.put("rain", (double) so.rain);
            t.put("rainRate", (double) so.rainRate);
            t.put("hail", (double) so.hail);
            t.put("hailRate", (double) so.hailRate);
            t.put("temperature", (double) so.temperature);
            t.put("lightning", (double) so.lightning);
            t.put("angle", (double) so.angle);
            t.put("flyingBlocks", so.flyingBlocks);
            t.put("currentTopY", so.currentTopYBlock);
            t.put("revives", so.revives);
            t.put("maxRevives", so.maxRevives);
            t.put("isTornado", wo.type == WeatherEnum.Type.TORNADO);
            t.put("isTropicalStorm", wo.type == WeatherEnum.Type.TROPICAL_STORM);
            t.put("isCyclone", wo.type == WeatherEnum.Type.TROPICAL_STORM || wo.type == WeatherEnum.Type.HURRICANE);
            t.put("isHurricane", wo.type == WeatherEnum.Type.HURRICANE);
            t.put("isSevere", so.isSevere());
            t.put("isDeadly", so.isDeadly());
            t.put("isViolent", so.isViolent);
            t.put("isFirenado", so.isFirenado);
            t.put("isSpout", so.isSpout);
            t.put("isNatural", so.isNatural);
            t.put("isStorm", so.isStorm());
            t.put("canProgress", so.canProgress);
            t.put("alwaysProgresses", so.alwaysProgresses);
            t.put("neverDissipate", so.neverDissipate);
            t.put("overrideAngle", so.overrideAngle);
            t.put("overrideMotion", so.overrideMotion);
            t.put("isDrizzling", so.isDrizzling());
            t.put("isRaining", so.isRaining());
            t.put("isHailing", so.isHailing());
            t.put("hasDownfall", so.hasDownfall());
        } else {
            t.put("name", "");
            t.put("fullName", wo.type != null ? wo.type.name() : "UNKNOWN");
            t.put("typeName", "");
            t.put("stage", 0);
            t.put("stageMax", 0);
            t.put("layer", 0);
            t.put("stormType", "UNKNOWN");
            t.put("intensity", 0.0);
            t.put("intensityRate", 0.0);
            t.put("intensityMax", 0.0);
            t.put("sizeRate", 0.0);
            t.put("formingStrength", 0.0);
            t.put("windSpeed", 0.0);
            t.put("funnelSize", 0.0);
            t.put("rain", 0.0);
            t.put("rainRate", 0.0);
            t.put("hail", 0.0);
            t.put("hailRate", 0.0);
            t.put("temperature", 0.0);
            t.put("lightning", 0.0);
            t.put("angle", 0.0);
            t.put("flyingBlocks", 0);
            t.put("currentTopY", 0);
            t.put("revives", 0);
            t.put("maxRevives", 0);
            t.put("isTornado", false);
            t.put("isTropicalStorm", false);
            t.put("isCyclone", false);
            t.put("isHurricane", false);
            t.put("isSevere", false);
            t.put("isDeadly", false);
            t.put("isViolent", false);
            t.put("isFirenado", false);
            t.put("isSpout", false);
            t.put("isNatural", false);
            t.put("isStorm", false);
            t.put("canProgress", false);
            t.put("alwaysProgresses", false);
            t.put("neverDissipate", false);
            t.put("overrideAngle", false);
            t.put("overrideMotion", false);
            t.put("isDrizzling", false);
            t.put("isRaining", false);
            t.put("isHailing", false);
            t.put("hasDownfall", false);
        }
        return t;
    }

    static Map<String, Object> buildFrontTable(FrontObject front) {
        Map<String, Object> t = new HashMap<>();
        t.put("id", front.getUUID().toString());
        t.put("name", front.getName());
        t.put("typeName", front.getTypeName());
        t.put("type", front.type);

        if (front.pos != null) {
            t.put("x", front.pos.posX);
            t.put("y", front.pos.posY);
            t.put("z", front.pos.posZ);
            t.put("dx", front.motion.posX);
            t.put("dz", front.motion.posZ);
            t.put("temperature", (double) front.temperature);
            t.put("humidity", (double) front.humidity);
            t.put("pressure", (double) front.pressure);
        } else {
            t.put("x", 0.0);
            t.put("y", 0.0);
            t.put("z", 0.0);
            t.put("dx", 0.0);
            t.put("dz", 0.0);
            t.put("temperature", 0.0);
            t.put("humidity", 0.0);
            t.put("pressure", 0.0);
        }

        t.put("angle", (double) front.angle);
        t.put("size", (double) front.size);
        t.put("layer", front.layer);
        t.put("stormCount", front.storms);
        t.put("activeStormCount", front.activeStorms);
        t.put("maxStorms", front.maxStorms);
        t.put("isDying", front.isDying);
        t.put("isGlobal", front.isGlobal());
        t.put("frontMultiplier", (double) front.frontMultiplier);
        return t;
    }

    @Override
    public void tick() {
        if (level == null || level.isClientSide) return;
        if (level.getGameTime() % SCAN_RATE == 0) doScan();
    }

    public void doScan() {
        cachedStorms.clear();
        cachedFronts.clear();
        WeatherManagerServer wm = ServerTickHandler.dimensionSystems.get(level.dimension());

        if (wm == null) return;
        double range = getDetectionRange();
        double rangeSq = range * range;
        double radarX = getBlockPos().getX();
        double radarZ = getBlockPos().getZ();

        for (FrontObject front : wm.getFronts()) {
            boolean frontInRange;

            if (front.isGlobal()) {
                frontInRange = true;
            } else {
                double fdx = front.pos.posX - radarX;
                double fdz = front.pos.posZ - radarZ;
                frontInRange = (fdx * fdx + fdz * fdz) <= rangeSq;
            }

            if (frontInRange)
                cachedFronts.add(buildFrontTable(front));

            for (WeatherObject wo : front.getWeatherObjects()) {
                if (wo.isDead) continue;
                if (wo.type == WeatherEnum.Type.CLOUD) continue;
                if (!(wo instanceof StormObject)) continue;

                double dx = wo.pos.posX - radarX;
                double dz = wo.pos.posZ - radarZ;
                double distSq = dx * dx + dz * dz;

                if (distSq <= rangeSq) cachedStorms.add(buildStormTable(wo, distSq, radarX, radarZ));
            }
        }
    }

    public int getDetectionRange() {
        if (detectionRange < 0) detectionRange = getConfiguredMaxRange();
        return detectionRange;
    }

    public void setDetectionRange(int range) {
        int max = getConfiguredMaxRange();
        if (range < MIN_RANGE || range > max) {
            Weather2.warn(String.format(
                    "Attempted to set invalid radar range: %d (allowed %d–%d)",
                    range, MIN_RANGE, max));
            return;
        }
        this.detectionRange = range;
        setChanged();
        if (level != null && !level.isClientSide) doScan();
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);
        tag.putInt("DetectionRange", getDetectionRange());
        return tag;
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        if (tag.contains("DetectionRange")) {
            int saved = tag.getInt("DetectionRange");
            int max = getConfiguredMaxRange();
            if (saved < MIN_RANGE || saved > max) {
                Weather2.warn(String.format(
                        "Loaded out-of-range radar range (%d), clamping to config max (%d)",
                        saved, max));
                detectionRange = max;
            } else {
                detectionRange = saved;
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.invalidateCaps();
    }
}