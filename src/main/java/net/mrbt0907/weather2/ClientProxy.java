package net.mrbt0907.weather2;

import net.corosus.extendedrenderer.ExtendedRenderer;
import net.corosus.extendedrenderer.shader.IShaderListener;
import net.corosus.extendedrenderer.shader.ShaderListenerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LightningBoltRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.gui.GuiConfigEditor;
import net.mrbt0907.weather2.api.WeatherAPI;
import net.mrbt0907.weather2.client.block.*;
import net.mrbt0907.weather2.client.entity.RenderFlyingBlock;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.client.foliage.FoliageEnhancerShader;
import net.mrbt0907.weather2.client.gui.GuiWeather;
import net.mrbt0907.weather2.client.rendering.manager.ParticleManagerEX;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.registry.EntityRegistry;
import net.mrbt0907.weather2.registry.TileEntityRegistry;
import net.mrbt0907.weather2.util.WeatherUtil;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {
    public static GuiWeather guiWeather;
    public static ClientTickHandler clientTickHandler;

    public ClientProxy() {
        ClientProxy.clientTickHandler = new ClientTickHandler();
    }

    @Override
    public void clientSetup() {
        super.clientSetup();


        MinecraftForge.EVENT_BUS.register(ClientProxy.clientTickHandler);

        initEntities();
        initTileEntities();
        ModLoadingContext.get().registerExtensionPoint(
                ExtensionPoint.CONFIGGUIFACTORY,
                () -> (minecraft, screen) -> new GuiConfigEditor()
        );
    }

    @Override
    public void commonSetup() {
        super.commonSetup();
        ShaderListenerRegistry.addListener(new IShaderListener() {
            @Override
            public void init() {
                FoliageEnhancerShader.shadersInit();
            }

            @Override
            public void reset() {
                FoliageEnhancerShader.shadersReset();
            }
        });
    }

    @Override
    public void postInit() {
        super.postInit();
        ClientProxy.guiWeather = new GuiWeather();
        WeatherAPI.refreshRenders(true);
        MinecraftForge.EVENT_BUS.register(ClientProxy.guiWeather);
        if (WeatherUtil.isAprilFoolsDay()) {
            ConfigClient.particle_renderer = "2";
            ConfigManager.save("Weather2 Remastered - Client");
        }

        ExtendedRenderer.rotEffRenderer = new ParticleManagerEX(
                Minecraft.getInstance().level,
                Minecraft.getInstance().getTextureManager()
        );
    }

    private void initEntities() {
        RenderingRegistry.registerEntityRenderingHandler(
                EntityRegistry.WEATHER_HAIL.get(),
                manager -> new RenderFlyingBlock(manager, net.minecraft.block.Blocks.ICE)
        );
        RenderingRegistry.registerEntityRenderingHandler(
                EntityRegistry.MOVING_BLOCK.get(),
                RenderFlyingBlock::new
        );
        RenderingRegistry.registerEntityRenderingHandler(
                EntityRegistry.LIGHTNING_BOLT.get(),
                LightningBoltRenderer::new
        );
    }

    private void initTileEntities() {
        ClientRegistry.bindTileEntityRenderer(
                TileEntityRegistry.TORNADO_SIREN_TILE.get(),
                RenderSiren::new
        );
        ClientRegistry.bindTileEntityRenderer(
                TileEntityRegistry.WIND_VANE_TILE.get(),
                RenderWindVane::new
        );
        ClientRegistry.bindTileEntityRenderer(
                TileEntityRegistry.WEATHER_FORECAST_TILE.get(),
                RenderRadar::new
        );
        ClientRegistry.bindTileEntityRenderer(
                TileEntityRegistry.WEATHER_FORECAST_2_TILE.get(),
                RenderRadar::new
        );
        ClientRegistry.bindTileEntityRenderer(
                TileEntityRegistry.WEATHER_FORECAST_3_TILE.get(),
                RenderRadar::new
        );
        ClientRegistry.bindTileEntityRenderer(
                TileEntityRegistry.WEATHER_MACHINE_TILE.get(),
                RenderWeatherConstructor::new
        );
        ClientRegistry.bindTileEntityRenderer(
                TileEntityRegistry.WEATHER_DEFLECTOR_TILE.get(),
                RenderWeatherDeflector::new
        );
        ClientRegistry.bindTileEntityRenderer(
                TileEntityRegistry.ANEMOMETER_TILE.get(),
                RenderAnemometer::new
        );
    }
}