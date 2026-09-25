package net.mrbt0907.weather2.client.event;

import net.corosus.extendedrenderer.ExtendedRenderer;
import net.corosus.extendedrenderer.render.FoliageRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.settings.CloudOption;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.WindReader;
import net.mrbt0907.weather2.client.NewSceneEnhancer;
import net.mrbt0907.weather2.client.foliage.FoliageEnhancerShader;
import net.mrbt0907.weather2.client.gui.GuiEZConfig;
import net.mrbt0907.weather2.client.weather.WeatherManagerClient;
import net.mrbt0907.weather2.config.ConfigFoliage;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.EZConfigParser;
import net.mrbt0907.weather2.network.packets.PacketData;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;

public class ClientTickHandler {
    public static World lastWorld;
    public static WeatherManagerClient weatherManager;
    public static FoliageEnhancerShader foliageEnhancer;

    public boolean hasOpenedConfig = false;
    public Button configButton;

    public GameRenderer oldRenderer;
    public float smoothAngle = 0;
    public float smoothAngleRotationalVelAccel = 0;
    public float smoothAngleAdj = 0.1F;
    public int prevDir = 0;
    public boolean extraGrassLast = ConfigFoliage.enable_extra_grass;
    public boolean op = false;

    public ClientTickHandler() {

        new Thread(NewSceneEnhancer.instance(), "Weather2 New Scene Enhancer").start();

        if (foliageEnhancer == null) {
            foliageEnhancer = new FoliageEnhancerShader();
            (new Thread(foliageEnhancer, "Weather2 Foliage Enhancer")).start();
        }

        op = ConfigManager.getPermissionLevel() > 3;
    }

    public static void resetClientWeather() {
        if (weatherManager != null) {
            Weather2.debug("Weather2: Detected old WeatherManagerClient with unloaded world, clearing its data");
            weatherManager.reset(true);
            weatherManager = null;
        }
    }

    public static void checkClientWeather() {
        try {
            World world = Minecraft.getInstance().level;
            if (weatherManager == null || world != lastWorld)
                init(world);
        } catch (Exception ex) {
            Weather2.debug("Weather2: Warning, client received packet before it was ready to use, and failed to init client weather due to null world");
        }
    }

    public static void init(World world) {

        if (weatherManager != null) {
            Weather2.debug("Weather2: Detected old WeatherManagerClient with active world, clearing its data");
            weatherManager.reset(true);
        }

        Weather2.debug("Weather2: Initializing WeatherManagerClient for client world and requesting full sync");

        lastWorld = world;
        weatherManager = new WeatherManagerClient(world);

        PacketData.sync();
    }

    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof IngameMenuScreen) {
            int scaledWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int scaledHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

            configButton = new Button(
                    (scaledWidth / 2) - 100,
                    0,
                    200,
                    20,
                    new StringTextComponent("Weather2 EZ Config"),
                    (button) -> Minecraft.getInstance().setScreen(new GuiEZConfig())
            );

            event.addWidget(configButton);
        }
    }

    public void onTickInGUI(Screen guiscreen) {

    }

    public void onTickInGame() {
        if (ConfigMisc.toaster_pc_mode) return;

        Minecraft mc = Minecraft.getInstance();
        World world = mc.level;

        if (world != null) {
            checkClientWeather();
            weatherManager.tick();


            ResourceLocation dimensionLocation = world.dimension().location();

            if (!ConfigMisc.aesthetic_mode && ConfigMisc.enable_forced_clouds_off && dimensionLocation.toString().equals("minecraft:overworld"))
                mc.options.renderClouds = CloudOption.OFF;


            if (EZConfigParser.isEffectsEnabled(dimensionLocation))
                NewSceneEnhancer.instance().tick();

            if (!EZConfigParser.isWeatherEnabled(dimensionLocation) && weatherManager.getFronts().size() > 1) {
                Weather2.debug("Removing all storms as the dimension weather is disabled");
                weatherManager.reset(false);
            }


            Vec3 pos = mc.player == null ? null : new Vec3(mc.player.blockPosition());
            float windDir = WindReader.getWindAngle(world, pos);
            float windSpeed = WindReader.getWindSpeed(world, pos) * 0.25F;

            float diff = Math.abs(windDir - smoothAngle);

            if (diff > 10) {
                if (smoothAngle > 180) smoothAngle -= 360;
                if (smoothAngle < -180) smoothAngle += 360;

                float bestMove = Maths.wrapDegrees(windDir - smoothAngle);

                smoothAngleAdj = windSpeed;

                if (Math.abs(bestMove) < 180) {
                    float realAdj = smoothAngleAdj;

                    if (realAdj * 2 > windSpeed) {
                        if (bestMove > 0) {
                            smoothAngleRotationalVelAccel -= realAdj;
                            if (prevDir < 0) {
                                smoothAngleRotationalVelAccel = 0;
                            }
                            prevDir = 1;
                        } else if (bestMove < 0) {
                            smoothAngleRotationalVelAccel += realAdj;
                            if (prevDir > 0) {
                                smoothAngleRotationalVelAccel = 0;
                            }
                            prevDir = -1;
                        }
                    }

                    if (smoothAngleRotationalVelAccel > 0.3 || smoothAngleRotationalVelAccel < -0.3) {
                        smoothAngle += smoothAngleRotationalVelAccel * 0.3F;
                    }

                    smoothAngleRotationalVelAccel *= 0.80F;
                }
            }

            if (!Minecraft.getInstance().isPaused()) {
                ExtendedRenderer.foliageRenderer.windDir = smoothAngle;

                float rate = 0.005F;

                if (ExtendedRenderer.foliageRenderer.windSpeedSmooth != windSpeed) {
                    if (ExtendedRenderer.foliageRenderer.windSpeedSmooth < windSpeed) {
                        if (ExtendedRenderer.foliageRenderer.windSpeedSmooth + rate > windSpeed) {
                            ExtendedRenderer.foliageRenderer.windSpeedSmooth = windSpeed;
                        } else {
                            ExtendedRenderer.foliageRenderer.windSpeedSmooth += rate;
                        }
                    } else {
                        if (ExtendedRenderer.foliageRenderer.windSpeedSmooth - rate < windSpeed) {
                            ExtendedRenderer.foliageRenderer.windSpeedSmooth = windSpeed;
                        } else {
                            ExtendedRenderer.foliageRenderer.windSpeedSmooth -= rate;
                        }
                    }
                }

                float baseTimeChangeRate = 60F;
                FoliageRenderer.windTime += (baseTimeChangeRate * ExtendedRenderer.foliageRenderer.windSpeedSmooth);
            }
        } else
            resetClientWeather();
    }
}