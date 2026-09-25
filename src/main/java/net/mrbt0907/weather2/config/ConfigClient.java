package net.mrbt0907.weather2.config;

import net.minecraftforge.fml.loading.FMLEnvironment;
import net.mrbt0907.configex.api.ConfigAnnotations.*;
import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.WeatherAPI;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.client.rendering.shaders.VolumetricRenderer;

import java.io.File;


public class ConfigClient implements IConfigEX {
    @Permission(0)
    @Comment("Determines the renderer used for storms and clouds. Accepts number ids and renderer ids. Ex: 0 or " + Weather2.MODID + ":normal uses the default renderer.")
    public static String particle_renderer = "0";
    @Permission(0)
    @Comment("Should the particles be rendered with volumetrics instead of particles?")
    public static boolean enable_volumetrics = false;
    @Hidden
    @Comment("Enables on screen debug information about the current particle renderer")
    public static boolean enable_debug_renderer = false; // production version, changed from true
    @Permission(0)
    @Comment("Enables falling leaves in the wind")
    public static boolean enable_falling_leaves = true;
    @Permission(0)
    @DoubleRange(min = 0.0D)
    @Comment("Particle rates for leaf, waterfall, and fire particles")
    public static double ambient_particle_rate = 0.6D;
    @Permission(0)
    @Comment("Enables water splashes in a waterfall")
    public static boolean enable_waterfall_splash = true;
    @Permission(0)
    @Comment("Enables fire particles that float in the wind")
    public static boolean enable_fire_particle = true;
    @Permission(0)
    @Comment("Enables all precipitation particle types")
    public static boolean enable_precipitation = true;
    @Permission(0)
    @Comment("Enables heavy splashes for rain in severe storms")
    public static boolean enable_precipitation_splash = true;
    @Permission(0)
    @Comment("Enables heavy downfall in severe storms")
    public static boolean enable_heavy_precipitation = true;
    @Permission(0)
    @Comment("Enables distant downfall in severe storms")
    public static boolean enable_distant_downfall = false;
    @Permission(0)
    @Comment("Enables tornado debris clouds in severe storms")
    public static boolean enable_tornado_debris = true;
    @Permission(0)
    @Comment("Enables tornado clouds to change colors based on what block is picked up")
    public static boolean enable_tornado_block_colors = true;
    @Permission(0)
    @Comment("Enables dust particles to kick up in high wind situations")
    public static boolean enable_wind_particle = true;
    @Permission(0)
    @Comment("Enables the ability to see cloud particles on normal clouds")
    public static boolean enable_cloud_rendering = true;
    @Permission(0)
    @DoubleRange(min = 0.0D)
    @Comment("Particle rates for rain, downfall, and ground splash particle types")
    public static double precipitation_particle_rate = 0.65D;
    @Permission(0)
    @DoubleRange(min = 0.0D)
    @Comment("Particle rates for bush, and sand particle types")
    public static double sandstorm_debris_particle_rate = 0.3D;
    @Permission(0)
    @DoubleRange(min = 0.0D)
    @Comment("Particle rates for dust particles")
    public static double sandstorm_dust_particle_rate = 0.4D;
    @Permission(0)
    @DoubleRange(min = 0.0D)
    @Comment("Particle rates for wind particles")
    public static double wind_particle_rate = 0.2D;
    @Permission(0)
    @DoubleRange(min = 0.0D)
    @Comment("Particle rates for distant downfall particles")
    public static double distant_downfall_particle_rate = 0.2D;
    @Permission(0)
    @IntegerRange(min = 0)
    @Comment("Particle delay in ticks for tornado debris particles")
    public static int ground_debris_particle_delay = 5;
    @Permission(0)
    @IntegerRange(min = 0)
    @Comment("Particle delay in ticks for storm particles\n(Tornado or hurricane particles)")
    public static int funnel_particle_delay = 10;
    @Permission(0)
    @IntegerRange(min = 1)
    @Comment("The delay in ticks for the particle thread")
    public static int scene_enhancer_thread_delay = 400;
    @Permission(0)
    @DoubleRange(min = 0.0D)
    @Comment("Maximum percent of cloud coverage, supports over 100% for extended full cloud sky coverage")
    public static double max_cloud_coverage_perc = 50.0D;
    @Permission(0)
    @DoubleRange(min = 0.0D)
    @Comment("Minimum percent of cloud coverage, supports negative for extended cloudless sky coverage")
    public static double min_cloud_coverage_perc = 0.0D;
    @Permission(0)
    @DoubleRange(min = 0.0D)
    @Comment("How much to randomly change cloud coverage % amount, performed every 10 seconds")
    public static double cloud_coverage_change_amount = 0.05D;
    @Permission(0)
    @Comment("Should particles render outside of the normal render distance?")
    public static boolean enable_extended_render_distance = true;
    @Permission(0)
    @DoubleRange(min = 0.0D)
    @Comment("Distance that particles can render up to in blocks. Lower = better performance but less storm visibility.")
    public static double extended_render_distance = 384.0D;
    @Permission(0)
    @IntegerRange(min = -1)
    @Comment("How many weather2 particles can exist at once. Set to -1 for infinite particles. A typical hailstorm spawns around 3500~ particles on ultra settings")
    public static int max_particles = 3000;
    @Permission(0)
    @DoubleRange(min = 0.0D)
    @Comment("Particle multiplier that adjusts how fast rain rates change. Set it higher to make rain change faster")
    public static double rain_change_mult = 1.0D;
    @Permission(0)
    @DoubleRange(min = 0.0D)
    @Comment("Particle scale multiplier that adjusts how big particles should be")
    public static double particle_scale_mult = 1.0D;
    @Permission(0)
    @FloatRange(min = 0.0F)
    @Comment("Camera shake multiplier that adjusts how intense camera shaking can be")
    public static float camera_shake_mult = 1.0F;
    @Permission(0)
    @IntegerRange(min = 1)
    @Comment("How many ticks between cloud particle spawning")
    public static int cloud_particle_delay = 3;
    @Permission(0)
    @Comment("Should vanilla rain show up when proxy override is true?")
    public static boolean enable_vanilla_rain = false;
    @Permission(0)
    @Comment("Enables rendering based on layers rather than based on player distance for all weather2 particles")
    public static boolean enable_legacy_rendering = false;
    @Permission(0)
    @Comment("Should sky lightning flash in the sky? Does not affect normal lightning bolts")
    public static boolean enable_sky_lightning = true;
    @Permission(0)
    @Comment("Height multiplier for Meso Cyclones. Valid range is 0.75 to 1.0 where 0.80 is default")
    @FloatRange(min = 0.75F, max = 1.0F)
    public static float meso_height = 0.80F;
    @Permission(0)
    @Comment("Green/Blue multiplier for coloring funnels. 1.0 = fully red, 0.0 = fully blue")
    @FloatRange(min = 0.0F, max = 1.0F)
    public static float funnel_greenblue_mult = 0.45F;
    @Permission(0)
    @Comment("Green/Blue multiplier for coloring mesocyclones. 1.0 = fully red, 0.0 = fully blue")
    @FloatRange(min = 0.0F, max = 1.0F)
    public static float meso_greenblue_mult = 0.42F;
    @Permission(0)
    @Comment("Green/Blue multiplier for coloring clouds. 1.0 = fully red, 0.0 = fully blue")
    @FloatRange(min = 0.0F, max = 1.0F)
    public static float cloud_greenblue_mult = 0.40F;
    @Permission(0)
    @DoubleRange(min = 0.0D)
    @Comment("Fog multiplier that adjusts how foggy storms can be")
    public static double fog_mult = 1.0D;
    @Permission(0)
    @DoubleRange(min = 0.0D)
    @Comment("Fog change rate that adjusts how fast fog changes states")
    public static double fog_change_rate = 1.0D;
    @Permission(0)
    @Comment("Enable or Disable Storm Fog. Also affects optifine compatibility.")
    public static boolean enable_custom_fog = true;

    @Override
    public String getName() {
        return "Weather2 Remastered - Client";
    }

    @Override
    public String getSaveLocation() {
        return Weather2.MODID + File.separator + "ConfigClient";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void onConfigChanged(Phase phase, int variables) {
        if (Phase.END.equals(phase) && FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT) {
            if (ConfigClient.enable_volumetrics) {
                if (VolumetricRenderer.shader == null)
                    VolumetricRenderer.startShader();
            } else {
                VolumetricRenderer.stopShader();
            }
            WeatherAPI.refreshRenders(false);
            if (ClientTickHandler.weatherManager != null)
                ClientTickHandler.weatherManager.refreshParticleLimit();
        }

    }

    @Override
    public void onValueChanged(String variable, Object oldValue, Object newValue) {
        //
    }
}