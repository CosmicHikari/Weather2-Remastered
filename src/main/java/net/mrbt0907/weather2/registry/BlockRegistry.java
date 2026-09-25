package net.mrbt0907.weather2.registry;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.block.*;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Weather2.OLD_MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Weather2.OLD_MODID);

    public static final RegistryObject<Block> wire = registerBlock("wire", () -> new BlockSensor());
    public static final RegistryObject<Block> radio = registerBlock("radio_transmitter", () -> new BlockRadio(Material.CLAY));
    public static final RegistryObject<Block> wind_chimes = registerBlockNoItem("wind_chimes", () -> new BlockNewSiren());
    public static final RegistryObject<Block> air_horn_siren = registerBlockNoItem("air_horn_siren", () -> new BlockNewSiren());
    public static final RegistryObject<Block> emergency_siren_alt = registerBlockNoItem("emergency_siren_alt", () -> new BlockNewSiren());
    public static final RegistryObject<Block> emergency_siren_alt_manual = registerBlockNoItem("emergency_siren_alt_manual", () -> new BlockNewSiren());
    public static final RegistryObject<Block> weather_doppler_radar = registerBlock("weather_forecast_2", () -> new BlockNewRadar(1));
    public static final RegistryObject<Block> weather_pulse_radar = registerBlock("weather_forecast_3", () -> new BlockNewRadar(2));
    public static final RegistryObject<Block> weather_humidifier = registerBlockNoItem("weather_humidifier", () -> new BlockNewWeatherConstructor());
    public static final RegistryObject<Block> weather_humidifier_2 = registerBlockNoItem("weather_humidifier_2", () -> new BlockNewWeatherConstructor());
    public static final RegistryObject<Block> weather_conditioner = registerBlockNoItem("weather_conditioner", () -> new BlockNewWeatherDeflector());
    public static final RegistryObject<Block> weather_conditioner_2 = registerBlockNoItem("weather_conditioner_2", () -> new BlockNewWeatherDeflector());
    public static final RegistryObject<Block> wind_vane = registerBlock("wind_vane", () -> new BlockWindVane());
    public static final RegistryObject<Block> anemometer = registerBlock("anemometer",
            () -> new BlockAnemometer(Block.Properties.of(Material.METAL)
                    .strength(2.0f)
                    .noOcclusion()
            )
    );
    public static final RegistryObject<Block> machineCase = registerBlock("machine_case", () -> new BlockMachine(Material.CLAY));
    public static final RegistryObject<Block> stormSensor = registerBlock("storm_sensor", () -> new BlockNewSensor(Material.CLAY, 0));
    public static final RegistryObject<Block> humiditySensor = registerBlock("humidity_sensor", () -> new BlockNewSensor(Material.CLAY, 1));
    public static final RegistryObject<Block> rainSensor = registerBlock("rain_sensor", () -> new BlockNewSensor(Material.CLAY, 2));
    public static final RegistryObject<Block> temperatureSensor = registerBlock("temperature_sensor", () -> new BlockNewSensor(Material.CLAY, 3));
    public static final RegistryObject<Block> windSensor = registerBlock("wind_sensor", () -> new BlockNewSensor(Material.CLAY, 4));
    public static final RegistryObject<Block> barometerSensor = registerBlock("barometer_sensor", () -> new BlockNewSensor(Material.CLAY, 5));
    public static final RegistryObject<Block> tornado_sensor = registerBlock("tornado_sensor", () -> new BlockSensor());
    public static final RegistryObject<Block> emergency_siren_manual = registerBlock("tornado_siren_manual", () -> new BlockTSirenManual());
    public static final RegistryObject<Block> emergency_siren = registerBlock("tornado_siren", () -> new BlockSiren());
    public static final RegistryObject<Block> weather_radar = registerBlock("weather_forecast", () -> new BlockNewRadar(0));
    public static final RegistryObject<Block> computer_radar = registerBlock("computer_radar", () -> new BlockComputerRadar());
    public static final RegistryObject<Block> weather_constructor = registerBlock("weather_machine", () -> new BlockWeatherConstructor());
    public static final RegistryObject<Block> weather_deflector = registerBlock("weather_deflector", () -> new BlockWeatherDeflector());
    public static final RegistryObject<Block> sand_layer = registerBlockNoCreativeTab("sand_layer", () -> new BlockSandLayer());

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<T> registerBlockNoItem(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    private static <T extends Block> RegistryObject<T> registerBlockNoCreativeTab(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItemNoTab(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        ITEMS.register(name, () -> new BlockItem(block.get(),
                new Item.Properties().tab(Weather2.TAB)));
    }

    private static <T extends Block> void registerBlockItemNoTab(String name, RegistryObject<T> block) {
        ITEMS.register(name, () -> new BlockItem(block.get(),
                new Item.Properties()));
    }
}