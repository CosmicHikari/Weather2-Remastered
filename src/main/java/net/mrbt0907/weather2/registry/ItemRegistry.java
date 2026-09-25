package net.mrbt0907.weather2.registry;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.item.*;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Weather2.OLD_MODID);


    public static final RegistryObject<Item> radar = ITEMS.register("handheld_radar",
            () -> new ItemRadar(defaultProperties()));
    public static final RegistryObject<Item> sensor = ITEMS.register("handheld_sensor",
            () -> new ItemSensor(0, defaultProperties()));
    public static final RegistryObject<Item> thermometer = ITEMS.register("handheld_thermometer",
            () -> new ItemSensor(1, defaultProperties()));
    public static final RegistryObject<Item> hygrometer = ITEMS.register("handheld_hygrometer",
            () -> new ItemSensor(2, defaultProperties()));
    public static final RegistryObject<Item> anemometer = ITEMS.register("handheld_anemometer",
            () -> new ItemSensor(3, defaultProperties()));
    public static final RegistryObject<Item> radio = ITEMS.register("radio",
            () -> new Item(defaultProperties()));


    public static final RegistryObject<Item> itemMotor = ITEMS.register("motor",
            () -> new Item(defaultProperties()));
    public static final RegistryObject<Item> itemSpeaker = ITEMS.register("speaker",
            () -> new Item(defaultProperties()));
    public static final RegistryObject<Item> itemAntenna0 = ITEMS.register("antenna_0",
            () -> new Item(defaultProperties()));
    public static final RegistryObject<Item> itemAntenna1 = ITEMS.register("antenna_1",
            () -> new Item(defaultProperties()));
    public static final RegistryObject<Item> itemAntenna2 = ITEMS.register("antenna_2",
            () -> new Item(defaultProperties()));
    public static final RegistryObject<Item> itemCPU0 = ITEMS.register("cpu_0",
            () -> new Item(defaultProperties()));
    public static final RegistryObject<Item> itemCPU1 = ITEMS.register("cpu_1",
            () -> new Item(defaultProperties()));
    public static final RegistryObject<Item> itemCPU2 = ITEMS.register("cpu_2",
            () -> new Item(defaultProperties()));
    public static final RegistryObject<Item> itemBulb = ITEMS.register("bulb",
            () -> new Item(defaultProperties()));
    public static final RegistryObject<Item> itemDryBulb = ITEMS.register("bulb_dry",
            () -> new Item(defaultProperties()));
    public static final RegistryObject<Item> itemWetBulb = ITEMS.register("bulb_wet",
            () -> new Item(defaultProperties()));
    public static final RegistryObject<Item> itemLCD0 = ITEMS.register("lcd_0",
            () -> new Item(defaultProperties()));
    public static final RegistryObject<Item> itemLCD1 = ITEMS.register("lcd_1",
            () -> new Item(defaultProperties()));


    public static final RegistryObject<Item> itemSandLayer = ITEMS.register("sand_layer_placeable",
            () -> new ItemSandLayer(BlockRegistry.sand_layer.get(), defaultProperties()));
    public static final RegistryObject<Item> itemWeatherRecipe = ITEMS.register("weather_item",
            () -> new ItemWeatherRecipe(defaultProperties()));
    public static final RegistryObject<Item> itemPocketSand = ITEMS.register("pocket_sand",
            () -> new ItemPocketSand(defaultProperties()));


    private static Item.Properties defaultProperties() {
        return new Item.Properties().tab(Weather2.TAB);
    }


    public static RegistryObject<Item> registerBlockItem(String name, RegistryObject<? extends net.minecraft.block.Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), defaultProperties()));
    }


    public static RegistryObject<Item> registerBlockItem(String name, RegistryObject<? extends net.minecraft.block.Block> block, Item.Properties properties) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), properties));
    }
}