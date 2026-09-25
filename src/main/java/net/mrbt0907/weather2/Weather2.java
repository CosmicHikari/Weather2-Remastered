package net.mrbt0907.weather2;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.mrbt0907.configex.ConfigModEX;
import net.mrbt0907.configex.network.NetworkHandler;
import net.mrbt0907.weather2.api.WeatherAPI;
import net.mrbt0907.weather2.command.CommandWeather2;
import net.mrbt0907.weather2.compat.OCCompat;
import net.mrbt0907.weather2.config.*;
import net.mrbt0907.weather2.event.EventHandlerFML;
import net.mrbt0907.weather2.event.EventHandlerForge;
import net.mrbt0907.weather2.event.ServerTickHandler;
import net.mrbt0907.weather2.network.PacketNBT;
import net.mrbt0907.weather2.network.packets.PacketPocketSand;
import net.mrbt0907.weather2.player.PlayerData;
import net.mrbt0907.weather2.registry.*;
import net.mrbt0907.weather2.registry.condition.ConfigEnabledCondition;
import net.mrbt0907.weather2.weather.WeatherManagerServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Weather2.MODID)
public class Weather2 {
    public static final String MOD = "Weather 2 - Remastered";
    public static final String MODID = "weather2remaster";
    public static final String OLD_MODID = "weather2";
    public static final String VERSION = "3.0.0";
    public static final ItemGroup TAB = new ItemGroup(Weather2.MODID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(BlockRegistry.tornado_sensor.get());
        }
    };
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static final Logger LOGGER = LogManager.getLogger();
    public static Weather2 instance;
    public static CommonProxy proxy;

    public Weather2() {
        instance = this;

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        BlockRegistry.BLOCKS.register(modEventBus);
        BlockRegistry.ITEMS.register(modEventBus);
        TileEntityRegistry.TILE_ENTITIES.register(modEventBus);
        EntityRegistry.ENTITIES.register(modEventBus);
        ItemRegistry.ITEMS.register(modEventBus);
        SoundRegistry.SOUNDS.register(modEventBus);

        CraftingHelper.register(ConfigEnabledCondition.Serializer.INSTANCE);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::loadComplete);

        MinecraftForge.EVENT_BUS.register(new EventHandlerFML());
        MinecraftForge.EVENT_BUS.register(new EventHandlerForge());
        MinecraftForge.EVENT_BUS.register(this);

        proxy = DistExecutor.safeRunForDist(
                () -> ClientProxy::new,
                () -> CommonProxy::new
        );

        ConfigModEX.register(new ConfigMisc());
        ConfigModEX.register(new ConfigVolume());
        ConfigModEX.register(new ConfigClient());
        ConfigModEX.register(new ConfigFront());
        ConfigModEX.register(new ConfigStorm());
        ConfigModEX.register(new ConfigGrab());
        ConfigModEX.register(new ConfigSeason());
        ConfigModEX.register(new ConfigSimulation());
        ConfigModEX.register(new ConfigWind());
        ConfigModEX.register(new ConfigSand());
        ConfigModEX.register(new ConfigSnow());
        ConfigModEX.register(new ConfigFoliage());
        NetworkHandler.preInit();
        EZConfigParser.loadNBT();

        Weather2.info("Starting Weather2 - Remastered...");
    }

    public static void writeOutData(boolean unloadInstances) {
        try {
            for (WeatherManagerServer wm : ServerTickHandler.dimensionSystems.values()) {
                if (wm != null) {
                    wm.writeToFile();
                }
            }
            PlayerData.writeAllPlayerNBT(unloadInstances);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void info(Object message) {
        LOGGER.info(message);
    }

    public static void debug(Object message) {
        boolean isDebug = ConfigMisc.debug_mode;
        if (isDebug)
            LOGGER.info("[DEBUG] {}", message);
    }

    public static void warn(Object message) {
        boolean isDebug = ConfigMisc.debug_mode;
        if (isDebug)
            LOGGER.warn("{}", message);
    }

    public static void error(Object message) {
        Throwable exception;

        if (message instanceof Throwable)
            exception = (Throwable) message;
        else
            exception = new Exception(String.valueOf(message));

        exception.printStackTrace();
    }

    public static void fatal(Object message) {
        Error error;

        if (message instanceof Error)
            error = (Error) message;
        else
            error = new Error(String.valueOf(message));

        throw error;
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

        event.enqueueWork(() -> {
            int packetId = 0;
            PACKET_HANDLER.registerMessage(packetId++, PacketNBT.class,
                    PacketNBT::encode,
                    PacketNBT::decode,
                    PacketNBT::handle);

            PACKET_HANDLER.registerMessage(packetId++, PacketPocketSand.class,
                    PacketPocketSand::encode,
                    PacketPocketSand::decode,
                    PacketPocketSand::handle);

            proxy.commonSetup();

            OCCompat.initialize();

            StormNames.refreshNameList();
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        proxy.clientSetup();

        event.enqueueWork(() -> {
        });
    }

    private void loadComplete(final FMLLoadCompleteEvent event) {
        event.enqueueWork(() -> {
            proxy.postInit();
            EventHandlerFML.extraGrassLast = ConfigFoliage.enable_extra_grass;
        });
        Weather2.info("Weather2 - Remastered is online!");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {
        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onServerStarting(FMLServerStartingEvent event) {
            CommandWeather2.register(event.getServer().getCommands().getDispatcher());

        }

        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onServerStarted(FMLServerStartedEvent event) {

            WeatherAPI.refreshDimensionRules();
            WeatherAPI.refreshGrabRules();
        }

        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onServerStopped(FMLServerStoppedEvent event) {
            Weather2.writeOutData(true);
            ServerTickHandler.reset();
        }
    }
}