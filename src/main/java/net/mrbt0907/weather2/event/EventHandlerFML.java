package net.mrbt0907.weather2.event;

import net.corosus.extendedrenderer.EventHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.mrbt0907.configex.gui.GuiConfigEditor;
import net.mrbt0907.weather2.ClientProxy;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.client.foliage.FoliageEnhancerShader;
import net.mrbt0907.weather2.config.ConfigFoliage;
import net.mrbt0907.weather2.config.ConfigMisc;

public class EventHandlerFML {
    public static boolean sleepFlag = false;
    public static boolean wasRain = false;
    public static int rainTime = 0;
    public static boolean wasThunder = false;
    public static int thunderTime = 0;

    public static boolean extraGrassLast;

    @SubscribeEvent
    public void tickServer(ServerTickEvent event) {
        if (event.phase == Phase.START) {
            ServerTickHandler.onTickInGame();
        }

        if (ConfigMisc.disable_rain_reset_upon_sleep) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                ServerWorld world = server.getLevel(World.OVERWORLD);
                if (world != null) {
                    if (event.phase == Phase.START) {
                        if (world.players().stream().allMatch(player -> player.isSleeping())) {
                            sleepFlag = true;
                            if (world.getLevelData() instanceof net.minecraft.world.storage.ServerWorldInfo) {
                                net.minecraft.world.storage.ServerWorldInfo worldInfo = (net.minecraft.world.storage.ServerWorldInfo) world.getLevelData();
                                wasRain = worldInfo.isRaining();
                                wasThunder = worldInfo.isThundering();
                                rainTime = worldInfo.getRainTime();
                                thunderTime = worldInfo.getThunderTime();
                            } else {
                                wasRain = world.getLevelData().isRaining();
                                wasThunder = world.getLevelData().isThundering();
                                rainTime = 0;
                                thunderTime = 0;
                            }
                        } else {
                            sleepFlag = false;
                        }
                    } else {
                        if (sleepFlag) {
                            if (world.getLevelData() instanceof net.minecraft.world.storage.ServerWorldInfo) {
                                net.minecraft.world.storage.ServerWorldInfo worldInfo = (net.minecraft.world.storage.ServerWorldInfo) world.getLevelData();
                                worldInfo.setRaining(wasRain);
                                worldInfo.setRainTime(rainTime);
                                worldInfo.setThundering(wasThunder);
                                worldInfo.setThunderTime(thunderTime);
                            } else {
                                world.getLevelData().setRaining(wasRain);
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void tickClient(ClientTickEvent event) {
        if (event.phase == Phase.START) {
            try {
                ClientProxy.clientTickHandler.onTickInGame();

                if (extraGrassLast != ConfigFoliage.enable_extra_grass)
                    extraGrassLast = ConfigFoliage.enable_extra_grass;

                boolean hackyLiveReplace = false;
                if (hackyLiveReplace && EventHandler.flagFoliageUpdate) {
                    Weather2.debug("CoroUtilLegacy detected a need to reload resource packs, initiating");
                    EventHandler.flagFoliageUpdate = false;
                    FoliageEnhancerShader.liveReloadModels();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @SubscribeEvent
    public void playerLoggedIn(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity)
            ServerTickHandler.syncServerConfigToClientPlayer((ServerPlayerEntity) event.getPlayer());
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void OnGuiOpen(GuiOpenEvent event) {
        if (ClientProxy.clientTickHandler != null && event.getGui() instanceof GuiConfigEditor && !ClientProxy.clientTickHandler.op)
            event.setCanceled(true);
    }
}