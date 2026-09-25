package net.mrbt0907.weather2.block.tile;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Stage;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.event.ServerTickHandler;
import net.mrbt0907.weather2.network.packets.PacketWeatherObject;
import net.mrbt0907.weather2.registry.TileEntityRegistry;
import net.mrbt0907.weather2.weather.WeatherManagerServer;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.WeatherObject;

import java.util.UUID;

public class TileWeatherConstructor extends TileEntity implements ITickableTileEntity {
    public int stage = 1;
    public int maxStage = 7;

    public StormObject lastTickStormObject = null;
    public UUID lastTickStormObjectID = null;

    public TileWeatherConstructor() {
        this(TileEntityRegistry.WEATHER_MACHINE_TILE.get());
    }

    public TileWeatherConstructor(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public void cycleWeatherType(boolean reverse) {
        maxStage = ConfigStorm.disable_tornados || ConfigMisc.disable_weather_machine_cyclones ? 5 : 7;
        stage = (stage + (reverse ? -1 : 1)) % (maxStage + 1);

        if (stage < 0)
            stage = maxStage;

        setStormSettings();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        killStorm();
    }

    public void setStormSettings() {
        if (lastTickStormObject != null) {
            System.out.println(stage);
            switch (stage) {
                case 1:
                    lastTickStormObject.rain = 0.0F;
                    lastTickStormObject.stormType = StormObject.StormType.LAND.ordinal();
                    lastTickStormObject.stage = Stage.NORMAL.getStage();
                    lastTickStormObject.intensity = 0.01F;
                    lastTickStormObject.updateType();
                    break;
                case 2:
                    lastTickStormObject.rain = 100.0F;
                    lastTickStormObject.stormType = StormObject.StormType.LAND.ordinal();
                    lastTickStormObject.stage = Stage.RAIN.getStage();
                    lastTickStormObject.intensity = 0.99F;
                    lastTickStormObject.updateType();
                    break;
                case 3:
                    lastTickStormObject.rain = 300.0F;
                    lastTickStormObject.stormType = StormObject.StormType.LAND.ordinal();
                    lastTickStormObject.stage = Stage.THUNDER.getStage();
                    lastTickStormObject.intensity = 1.99F;
                    lastTickStormObject.updateType();
                    break;
                case 4:
                    lastTickStormObject.rain = 500.0F;
                    lastTickStormObject.stormType = StormObject.StormType.LAND.ordinal();
                    lastTickStormObject.stage = Stage.SEVERE.getStage();
                    lastTickStormObject.intensity = 2.99F;
                    lastTickStormObject.hail = 0.0F;
                    lastTickStormObject.updateType();
                    break;
                case 5:
                    lastTickStormObject.rain = ConfigStorm.max_rain_buildup;
                    lastTickStormObject.stormType = StormObject.StormType.LAND.ordinal();
                    lastTickStormObject.stage = Stage.SEVERE.getStage();
                    lastTickStormObject.intensity = 2.99F;
                    lastTickStormObject.hail = 150.0F;
                    lastTickStormObject.updateType();
                    break;
                case 6:
                    lastTickStormObject.stormType = StormObject.StormType.LAND.ordinal();
                    lastTickStormObject.stage = Stage.TORNADO.getStage() + 1;
                    lastTickStormObject.intensity = 4.99F;
                    lastTickStormObject.updateType();
                    break;
                case 7:
                    lastTickStormObject.stormType = StormObject.StormType.WATER.ordinal();
                    lastTickStormObject.stage = Stage.HURRICANE.getStage();
                    lastTickStormObject.intensity = 4.99F;
                    lastTickStormObject.updateType();
                    break;
                default:
                    killStorm();
                    break;
            }
        }
    }

    public void createStorm() {
        if (lastTickStormObject == null) {
            if (!ConfigMisc.aesthetic_mode) {
                WeatherManagerServer manager = ServerTickHandler.dimensionSystems.get(level.dimension());
                if (manager != null) {
                    StormObject so = manager.getGlobalFront().createStorm(worldPosition.getX(), worldPosition.getZ(), 1, null);
                    so.isNatural = false;
                    so.canProgress = false;
                    so.overrideMotion = true;
                    so.overrideAngle = true;
                    so.shouldConvert = false;
                    so.shouldBuildHumidity = false;
                    so.temperature = 40;
                    so.stageMax = 5;

                    PacketWeatherObject.create(manager.getDimension(), so);
                    lastTickStormObject = so;
                    lastTickStormObjectID = so.getUUID();
                    setStormSettings();
                }
            }
        }
    }

    public void killStorm() {
        if (lastTickStormObject != null) {
            lastTickStormObject.setDead();
            lastTickStormObject = null;
            lastTickStormObjectID = null;
        }
    }

    @Override
    public void tick() {
        if (!level.isClientSide) {
            if (stage > 0 && level.getGameTime() % 40 == 0) {
                WeatherManagerServer manager = ServerTickHandler.dimensionSystems.get(level.dimension());
                if (manager != null) {
                    if (lastTickStormObject == null) {
                        if (lastTickStormObjectID != null) {
                            WeatherObject system = manager.getGlobalFront().getWeatherObject(lastTickStormObjectID);
                            if (system != null) {
                                lastTickStormObject = (StormObject) system;
                                Weather2.debug("Weather machine reobtained storm " + system.getUUID());
                            } else {
                                createStorm();
                                Weather2.debug("Weather machine created storm " + lastTickStormObjectID + " because old storm no longer exists");
                            }
                        } else {
                            createStorm();
                            Weather2.debug("Weather machine created storm " + lastTickStormObjectID);
                        }
                    } else if (lastTickStormObject.isDead) {
                        lastTickStormObject = null;
                        lastTickStormObjectID = null;
                        createStorm();
                        Weather2.debug("Weather machine created storm " + lastTickStormObjectID + " because old storm has died");
                    }
                }
            }
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT var1) {
        super.save(var1);
        CompoundNBT data = new CompoundNBT();
        data.putInt("weatherType", stage);
        if (lastTickStormObjectID != null)
            data.putUUID("lastTickStormObjectID", lastTickStormObjectID);

        var1.put("weatherMachine", data);
        return var1;
    }

    @Override
    public void load(BlockState state, CompoundNBT var1) {
        super.load(state, var1);
        if (var1.contains("weatherMachine")) {
            CompoundNBT data = var1.getCompound("weatherMachine");
            stage = data.getInt("weatherType");
            if (data.hasUUID("lastTickStormObjectID"))
                lastTickStormObjectID = data.getUUID("lastTickStormObjectID");
        }
    }
}