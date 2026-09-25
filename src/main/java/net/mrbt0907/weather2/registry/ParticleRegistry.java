package net.mrbt0907.weather2.registry;

import com.mojang.blaze3d.systems.RenderSystem;
import net.corosus.extendedrenderer.render.RotatingParticleManager;
import net.corosus.extendedrenderer.shader.MeshBufferManagerFoliage;
import net.corosus.extendedrenderer.shader.MeshBufferManagerParticle;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.mrbt0907.weather2.Weather2;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Weather2.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ParticleRegistry {
    public static TextureAtlasSprite cloud;
    public static TextureAtlasSprite cloud_legacy;
    public static TextureAtlasSprite cloud256;
    public static TextureAtlasSprite cloud256_light;
    public static TextureAtlasSprite cloud256_meso;
    public static TextureAtlasSprite cloud256_meso_wall;
    public static TextureAtlasSprite cloud256_fire;
    public static TextureAtlasSprite cloud32;
    public static TextureAtlasSprite rainLight;
    public static TextureAtlasSprite rainHeavy;
    public static TextureAtlasSprite rainSplash;
    public static TextureAtlasSprite distant_downfall;
    public static TextureAtlasSprite tornado256;

    public static TextureAtlasSprite radarIconReflectivityF;
    public static TextureAtlasSprite radarIconReflectivityE;
    public static TextureAtlasSprite radarIconReflectivityD;
    public static TextureAtlasSprite radarIconReflectivityC;
    public static TextureAtlasSprite radarIconReflectivityB;
    public static TextureAtlasSprite radarIconReflectivityA;
    public static TextureAtlasSprite radarIconCloud;
    public static TextureAtlasSprite radarIconRain;
    public static TextureAtlasSprite radarIconSnow;
    public static TextureAtlasSprite radarIconLightning;
    public static TextureAtlasSprite radarIconWind;
    public static TextureAtlasSprite radarIconHail;
    public static TextureAtlasSprite radarIconTornado;
    public static TextureAtlasSprite radarIconCyclone;
    public static TextureAtlasSprite radarIconSandstorm;
    public static TextureAtlasSprite radarIconWarmFront;
    public static TextureAtlasSprite radarIconColdFront;
    public static TextureAtlasSprite radarIconOccludedFront;
    public static TextureAtlasSprite radarIconStationaryFront;
    public static TextureAtlasSprite concerned;

    @SubscribeEvent
    public static void onTextureStitchPre(TextureStitchEvent.Pre event) {
        if (!event.getMap().location().equals(AtlasTexture.LOCATION_BLOCKS))
            return;

        MeshBufferManagerParticle.clearMapOnly();
        MeshBufferManagerFoliage.clearMapOnly();
        RenderSystem.recordRenderCall(() -> {
            MeshBufferManagerParticle.cleanup();
            MeshBufferManagerFoliage.cleanup();
        });

        event.addSprite(new ResourceLocation(Weather2.OLD_MODID, "particles/cloud256"));
        event.addSprite(new ResourceLocation(Weather2.OLD_MODID, "particles/cloud256_light"));
        event.addSprite(new ResourceLocation(Weather2.OLD_MODID, "particles/cloud256_meso"));
        event.addSprite(new ResourceLocation(Weather2.OLD_MODID, "particles/cloud256_meso_wall"));
        event.addSprite(new ResourceLocation(Weather2.OLD_MODID, "particles/cloud_legacy"));
        event.addSprite(new ResourceLocation(Weather2.OLD_MODID, "particles/cloud256_fire"));
        event.addSprite(new ResourceLocation(Weather2.OLD_MODID, "particles/cloud32"));
        event.addSprite(new ResourceLocation(Weather2.OLD_MODID, "particles/rain_light"));
        event.addSprite(new ResourceLocation(Weather2.OLD_MODID, "particles/rain_heavy"));
        event.addSprite(new ResourceLocation(Weather2.OLD_MODID, "particles/rain_splash"));
        event.addSprite(new ResourceLocation(Weather2.OLD_MODID, "particles/distant_downfall"));
        event.addSprite(new ResourceLocation(Weather2.OLD_MODID, "particles/tornado256"));
        event.addSprite(new ResourceLocation(Weather2.OLD_MODID, "radar/radar_icon_cloud"));
        event.addSprite(new ResourceLocation(Weather2.OLD_MODID, "radar/radar_icon_rain"));
        event.addSprite(new ResourceLocation(Weather2.OLD_MODID, "radar/radar_icon_snow"));
        event.addSprite(new ResourceLocation(Weather2.OLD_MODID, "radar/radar_icon_lightning"));
        event.addSprite(new ResourceLocation(Weather2.OLD_MODID, "radar/radar_icon_wind"));
        event.addSprite(new ResourceLocation(Weather2.OLD_MODID, "radar/radar_icon_hail"));
        event.addSprite(new ResourceLocation(Weather2.OLD_MODID, "radar/radar_icon_tornado"));
        event.addSprite(new ResourceLocation(Weather2.OLD_MODID, "radar/radar_icon_cyclone"));
        event.addSprite(new ResourceLocation(Weather2.OLD_MODID, "radar/radar_icon_sandstorm"));
        event.addSprite(new ResourceLocation(Weather2.OLD_MODID, "radar/radar_icon_warm_front"));
        event.addSprite(new ResourceLocation(Weather2.OLD_MODID, "radar/radar_icon_cold_front"));
        event.addSprite(new ResourceLocation(Weather2.OLD_MODID, "radar/radar_icon_occluded_front"));
        event.addSprite(new ResourceLocation(Weather2.OLD_MODID, "radar/radar_icon_stationary_front"));
        event.addSprite(new ResourceLocation(Weather2.OLD_MODID, "particles/concerned"));
    }

    @SubscribeEvent
    public static void onTextureStitchPost(TextureStitchEvent.Post event) {
        if (!event.getMap().location().equals(AtlasTexture.LOCATION_BLOCKS))
            return;

        cloud256 = event.getMap().getSprite(new ResourceLocation(Weather2.OLD_MODID, "particles/cloud256"));
        cloud256_light = event.getMap().getSprite(new ResourceLocation(Weather2.OLD_MODID, "particles/cloud256_light"));
        cloud256_meso = event.getMap().getSprite(new ResourceLocation(Weather2.OLD_MODID, "particles/cloud256_meso"));
        cloud256_meso_wall = event.getMap().getSprite(new ResourceLocation(Weather2.OLD_MODID, "particles/cloud256_meso_wall"));
        cloud_legacy = event.getMap().getSprite(new ResourceLocation(Weather2.OLD_MODID, "particles/cloud_legacy"));
        cloud256_fire = event.getMap().getSprite(new ResourceLocation(Weather2.OLD_MODID, "particles/cloud256_fire"));
        cloud32 = event.getMap().getSprite(new ResourceLocation(Weather2.OLD_MODID, "particles/cloud32"));
        rainLight = event.getMap().getSprite(new ResourceLocation(Weather2.OLD_MODID, "particles/rain_light"));
        rainHeavy = event.getMap().getSprite(new ResourceLocation(Weather2.OLD_MODID, "particles/rain_heavy"));
        rainSplash = event.getMap().getSprite(new ResourceLocation(Weather2.OLD_MODID, "particles/rain_splash"));
        distant_downfall = event.getMap().getSprite(new ResourceLocation(Weather2.OLD_MODID, "particles/distant_downfall"));
        tornado256 = event.getMap().getSprite(new ResourceLocation(Weather2.OLD_MODID, "particles/tornado256"));
        radarIconCloud = event.getMap().getSprite(new ResourceLocation(Weather2.OLD_MODID, "radar/radar_icon_cloud"));
        radarIconRain = event.getMap().getSprite(new ResourceLocation(Weather2.OLD_MODID, "radar/radar_icon_rain"));
        radarIconSnow = event.getMap().getSprite(new ResourceLocation(Weather2.OLD_MODID, "radar/radar_icon_snow"));
        radarIconLightning = event.getMap().getSprite(new ResourceLocation(Weather2.OLD_MODID, "radar/radar_icon_lightning"));
        radarIconWind = event.getMap().getSprite(new ResourceLocation(Weather2.OLD_MODID, "radar/radar_icon_wind"));
        radarIconHail = event.getMap().getSprite(new ResourceLocation(Weather2.OLD_MODID, "radar/radar_icon_hail"));
        radarIconTornado = event.getMap().getSprite(new ResourceLocation(Weather2.OLD_MODID, "radar/radar_icon_tornado"));
        radarIconCyclone = event.getMap().getSprite(new ResourceLocation(Weather2.OLD_MODID, "radar/radar_icon_cyclone"));
        radarIconSandstorm = event.getMap().getSprite(new ResourceLocation(Weather2.OLD_MODID, "radar/radar_icon_sandstorm"));
        radarIconWarmFront = event.getMap().getSprite(new ResourceLocation(Weather2.OLD_MODID, "radar/radar_icon_warm_front"));
        radarIconColdFront = event.getMap().getSprite(new ResourceLocation(Weather2.OLD_MODID, "radar/radar_icon_cold_front"));
        radarIconOccludedFront = event.getMap().getSprite(new ResourceLocation(Weather2.OLD_MODID, "radar/radar_icon_occluded_front"));
        radarIconStationaryFront = event.getMap().getSprite(new ResourceLocation(Weather2.OLD_MODID, "radar/radar_icon_stationary_front"));
        concerned = event.getMap().getSprite(new ResourceLocation(Weather2.OLD_MODID, "particles/concerned"));

        if (RotatingParticleManager.useShaders)
            RotatingParticleManager.forceShaderReset = true;
    }
}