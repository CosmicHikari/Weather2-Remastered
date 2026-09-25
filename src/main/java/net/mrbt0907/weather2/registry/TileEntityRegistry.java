package net.mrbt0907.weather2.registry;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.block.tile.*;

@SuppressWarnings("unused")
public class TileEntityRegistry {
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES =
            DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Weather2.MODID);
    public static final RegistryObject<TileEntityType<TileRadar>> WEATHER_FORECAST_TILE =
            TILE_ENTITIES.register("weather_forecast",
                    () -> TileEntityType.Builder.of(() -> new TileRadar(0),
                            BlockRegistry.weather_radar.get()).build(null));
    public static final RegistryObject<TileEntityType<TileRadar>> WEATHER_FORECAST_2_TILE =
            TILE_ENTITIES.register("weather_forecast_2",
                    () -> TileEntityType.Builder.of(() -> new TileRadar(1),
                            BlockRegistry.weather_doppler_radar.get()).build(null));
    public static final RegistryObject<TileEntityType<TileRadar>> WEATHER_FORECAST_3_TILE =
            TILE_ENTITIES.register("weather_forecast_3",
                    () -> TileEntityType.Builder.of(() -> new TileRadar(2),
                            BlockRegistry.weather_pulse_radar.get()).build(null));
    public static final RegistryObject<TileEntityType<TileSiren>> TORNADO_SIREN_TILE =
            TILE_ENTITIES.register("tornado_siren",
                    () -> TileEntityType.Builder.of(TileSiren::new,
                            BlockRegistry.emergency_siren.get()).build(null));

    public static final RegistryObject<TileEntityType<TileEntityTSirenManual>> TORNADO_SIREN_MANUAL_TILE =
            TILE_ENTITIES.register("tornado_siren_manual",
                    () -> TileEntityType.Builder.of(TileEntityTSirenManual::new,
                            BlockRegistry.emergency_siren_manual.get()).build(null));

    public static final RegistryObject<TileEntityType<TileWindVane>> WIND_VANE_TILE =
            TILE_ENTITIES.register("wind_vane",
                    () -> TileEntityType.Builder.of(TileWindVane::new,
                            BlockRegistry.wind_vane.get()).build(null));


    public static final RegistryObject<TileEntityType<TileComputerRadar>> COMPUTER_RADAR_TILE =
            TILE_ENTITIES.register("computer_radar",
                    () -> TileEntityType.Builder.of(
                            TileComputerRadar::new,
                            BlockRegistry.computer_radar.get()).build(null));

    public static final RegistryObject<TileEntityType<TileWeatherConstructor>> WEATHER_MACHINE_TILE =
            TILE_ENTITIES.register("weather_machine",
                    () -> TileEntityType.Builder.of(TileWeatherConstructor::new,
                            BlockRegistry.weather_constructor.get()).build(null));

    public static final RegistryObject<TileEntityType<TileWeatherDeflector>> WEATHER_DEFLECTOR_TILE =
            TILE_ENTITIES.register("weather_deflector",
                    () -> TileEntityType.Builder.of(TileWeatherDeflector::new,
                            BlockRegistry.weather_deflector.get()).build(null));

    public static final RegistryObject<TileEntityType<TileAnemometer>> ANEMOMETER_TILE =
            TILE_ENTITIES.register("anemometer",
                    () -> TileEntityType.Builder.of(TileAnemometer::new,
                            BlockRegistry.anemometer.get()).build(null));

    public static final RegistryObject<TileEntityType<TileMachine>> MACHINE_CASE_TILE =
            TILE_ENTITIES.register("machine_case",
                    () -> TileEntityType.Builder.of(TileMachine::new,
                            BlockRegistry.machineCase.get()).build(null));

    public static final RegistryObject<TileEntityType<TileRadioTransmitter>> RADIO_TRANSMITTER_TILE =
            TILE_ENTITIES.register("radio_transmitter",
                    () -> TileEntityType.Builder.of(TileRadioTransmitter::new,
                            BlockRegistry.radio.get()).build(null));
}