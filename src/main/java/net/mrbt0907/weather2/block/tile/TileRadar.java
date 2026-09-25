package net.mrbt0907.weather2.block.tile;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.weather2.api.weather.IWeatherDetectable;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Type;
import net.mrbt0907.weather2.client.gui.elements.GuiRadarObject;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.registry.TileEntityRegistry;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.storm.FrontObject;
import net.mrbt0907.weather2.weather.storm.WeatherObject;

import java.util.ArrayList;
import java.util.List;

public class TileRadar extends TileEntity implements ITickableTileEntity {

    private final int pingMaxLength = 60;
    public int pingRate = 200;
    public double pingRange = 1024.0D;
    public int showType = 0;
    public boolean showRating = false;
    public boolean showEF = false;
    public boolean showDirection = false;
    public boolean showWindSpeed = false;
    public boolean liveRadar = false;
    public float fadeRate = 0.05F;
    public float renderAlpha = 1.0F;
    public float renderRange = 2.0F;
    public float smoothAngle = 0;
    public float smoothSpeed = 0;
    public float smoothAngleRotationalVel = 0;
    public float smoothAngleRotationalVelAccel = 0;
    public float smoothAngleAdj = 0.1F;
    public float smoothSpeedAdj = 0.1F;
    public IWeatherDetectable system = null;
    public List<GuiRadarObject> systems = new ArrayList<>();
    private int tier = 0;
    private int pingLength = 60;

    public TileRadar() {
        this(0);
    }

    public TileRadar(int tier) {
        super(getTileEntityType(tier));
        this.tier = tier;
        refresh();
    }


    private static TileEntityType<TileRadar> getTileEntityType(int tier) {
        switch (tier) {
            case 1:
                return TileEntityRegistry.WEATHER_FORECAST_2_TILE.get();
            case 2:
                return TileEntityRegistry.WEATHER_FORECAST_3_TILE.get();
            default:
                return TileEntityRegistry.WEATHER_FORECAST_TILE.get();
        }
    }

    @Override
    public void tick() {
        if (level != null && level.isClientSide)
            tickClient();
    }

    public int getTier() {
        return tier;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(
                worldPosition.getX() - renderRange,
                worldPosition.getY(),
                worldPosition.getZ() - renderRange,
                worldPosition.getX() + renderRange,
                worldPosition.getY() + 4,
                worldPosition.getZ() + renderRange
        );
    }

    private void refresh() {
        showEF = ConfigStorm.enable_ef_scale || tier > 1;

        switch (tier) {
            case 1:
                pingRate = 150;
                pingRange = ConfigMisc.doppler_radar_range;
                renderRange = 2.0F;
                showRating = true;
                showDirection = true;
                showWindSpeed = true;
                liveRadar = true;
                break;
            case 2:
                pingRate = 100;
                pingRange = ConfigMisc.pulse_doppler_radar_range;
                renderRange = 3.0F;
                showRating = true;
                showDirection = false;
                showWindSpeed = false;
                liveRadar = false;
                break;
            default:
                pingRate = 200;
                pingRange = ConfigMisc.radar_range;
                renderRange = 1.0F;
                showType = 0;
                showRating = false;
                showDirection = false;
                showWindSpeed = false;
                liveRadar = false;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void tickClient() {
        if (level.getGameTime() % pingRate == 0) {
            BlockPos pos = getBlockPos();

            if (ConfigMisc.debug_mode_radar) {
                WeatherObject weatherSystem = net.mrbt0907.weather2.client.event.ClientTickHandler.weatherManager
                        .getClosestWeather(new Vec3(pos.getX(), pos.getY(), pos.getZ()), pingRange);
                this.system = (weatherSystem == null || weatherSystem.isDead ? null : weatherSystem);
            } else
                system = null;

            systems.clear();

            for (FrontObject front : net.mrbt0907.weather2.client.event.ClientTickHandler.weatherManager.getFronts()) {
                if (!front.isGlobal()) {
                    if (Maths.distanceSq(pos.getX(), pos.getY(), pos.getZ(),
                            front.pos.posX, pos.getY(), front.pos.posZ) <= pingRange) {
                        systems.add(new GuiRadarObject(front));
                        front.getWeatherObjects().forEach(so -> {
                            if ((ConfigMisc.debug_mode_radar || !so.type.equals(Type.CLOUD))
                                    && Maths.distanceSq(pos.getX(), pos.getY(), pos.getZ(),
                                    so.pos.posX, pos.getY(), so.pos.posZ) <= pingRange)
                                systems.add(new GuiRadarObject(so));
                        });
                    }
                } else {
                    front.getWeatherObjects().forEach(so -> {
                        if ((ConfigMisc.debug_mode_radar || !so.type.equals(Type.CLOUD))
                                && Maths.distanceSq(pos.getX(), pos.getY(), pos.getZ(),
                                so.pos.posX, pos.getY(), so.pos.posZ) <= pingRange)
                            systems.add(new GuiRadarObject(so));
                    });
                }
            }

            pingLength = pingMaxLength;
            renderAlpha = 1.0F;
        }

        if (pingLength == 0) {
            renderAlpha = Math.max(0.0F, renderAlpha - fadeRate);
        }

        if (pingLength > 0)
            pingLength--;
    }

    @OnlyIn(Dist.CLIENT)
    public double getPingRange(int tier) {
        return tier == 1 ? ConfigMisc.doppler_radar_range
                : tier == 2 ? ConfigMisc.pulse_doppler_radar_range
                : ConfigMisc.radar_range;
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        tag = super.save(tag);
        tag.putInt("tier", tier);
        return tag;
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);
        if (tag.contains("tier")) {
            tier = tag.getInt("tier");
            fadeRate = tier == 0 ? 0.0035F : tier == 1 ? 0.001F : 0.0005F;
        }
        refresh();
    }
}