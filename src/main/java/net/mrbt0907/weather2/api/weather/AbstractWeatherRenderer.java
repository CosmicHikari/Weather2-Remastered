package net.mrbt0907.weather2.api.weather;

import net.corosus.coroutillegacy.config.ConfigCoroUtilLegacy;
import net.corosus.extendedrenderer.ExtendedRenderer;
import net.corosus.extendedrenderer.particle.behavior.ParticleBehaviorFog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.WeatherAPI;
import net.mrbt0907.weather2.client.entity.particle.ExtendedEntityRotFX;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.client.weather.WeatherManagerClient;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.mixins.accessor.ParticleAccessor;
import net.mrbt0907.weather2.weather.storm.WeatherObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractWeatherRenderer {
    public static final List<String> renderDebugInfo = new ArrayList<>();
    private static long delta, worldDelta;
    private final List<Particle> particles = new ArrayList<>();
    public WeatherObject system;
    public int particlesLeft;
    protected ParticleBehaviorFog particleBehaviorFog;
    private int particleLimit = 1;

    public AbstractWeatherRenderer(WeatherObject system) {
        refreshParticleLimit();
        this.system = system;
    }

    public final void tick() {
        int attempts = 0;

        if (particleBehaviorFog == null)
            particleBehaviorFog = new ParticleBehaviorFog(system.pos.toVec3Coro());
        else if (Minecraft.getInstance().getSingleplayerServer() == null
                || !(Minecraft.getInstance().screen instanceof IngameMenuScreen))
            particleBehaviorFog.tickUpdateList();

        Iterator<Particle> particles = this.particles.iterator();
        Particle particle;
        while (particles.hasNext()) {
            particle = particles.next();
            if (((ParticleAccessor) particle).isRemoved())
                particles.remove();
        }
        particles = null;

        particlesLeft = particleLimit - this.particles.size();

        if (system != null) {
            delta = System.nanoTime();
            while (attempts > -1)
                try {
                    onTick(ClientTickHandler.weatherManager);
                    attempts = -1;
                } catch (Exception e) {
                    attempts++;
                    if (attempts < 3) {
                        Weather2.warn("Particle renderer's onTick() has encountered an error. Retrying...");
                        e.printStackTrace();
                    } else {
                        Weather2.warn("Particle renderer's onTick() has failed to run correctly. Disabling particle renderer...");
                        e.printStackTrace();
                        ConfigClient.particle_renderer = "-1";
                        WeatherAPI.refreshRenders(false);
                        attempts = -1;
                    }
                }
            delta = (long) ((System.nanoTime() - delta) * 0.001F);

            if (worldDelta != ClientTickHandler.weatherManager.getWorld().getGameTime()) {
                worldDelta = ClientTickHandler.weatherManager.getWorld().getGameTime();
                renderDebugInfo.clear();
                renderDebugInfo.add("Renderer: " + WeatherAPI.getParticleRendererId());
                renderDebugInfo.add("Delta: " + delta + "si");
                if (ConfigClient.max_particles > 0) {
                    renderDebugInfo.add("Particle Count: " + this.particles.size() + "/" + particleLimit);
                    renderDebugInfo.add("Global Particle Count: " + ClientTickHandler.weatherManager.getParticleCount() + "/" + ConfigClient.max_particles);
                } else {
                    renderDebugInfo.add("Particle Count: " + this.particles.size());
                    renderDebugInfo.add("Global Particle Count: " + ClientTickHandler.weatherManager.getParticleCount());
                }

                List<String> extraDebugInfo = null;
                attempts = 0;
                while (attempts > -1)
                    try {
                        extraDebugInfo = onDebugInfo();
                        attempts = -1;
                    } catch (Exception e) {
                        attempts++;
                        if (attempts < 3) {
                            Weather2.warn("Particle renderer's onDebugInfo() has encountered an error. Retrying...");
                            e.printStackTrace();
                        } else {
                            Weather2.warn("Particle renderer's onTick() has failed to run correctly. Disabling particle renderer...");
                            e.printStackTrace();
                            ConfigClient.particle_renderer = "-1";
                            WeatherAPI.refreshRenders(false);
                            attempts = -1;
                        }
                    }

                if (extraDebugInfo != null) {
                    renderDebugInfo.add("-------   --------");
                    renderDebugInfo.addAll(extraDebugInfo);
                }
            }
        }
    }

    public abstract void onTick(WeatherManagerClient manager);

    public abstract void onParticleLimitRefresh(WeatherManagerClient manager, int newParticleLimit);

    public abstract List<String> onDebugInfo();

    public abstract void cleanupRenderer();

    public final void cleanup() {
        particles.clear();
        cleanupRenderer();
        if (particleBehaviorFog != null) {
            if (particleBehaviorFog.particles != null)
                particleBehaviorFog.particles.clear();
            particleBehaviorFog = null;
        }
    }

    public final ExtendedEntityRotFX spawnParticle(double x, double y, double z, int parRenderOrder) {
        return spawnParticle(x, y, z, parRenderOrder,
                ConfigCoroUtilLegacy.optimizedCloudRendering
                        ? net.mrbt0907.weather2.registry.ParticleRegistry.cloud32
                        : net.mrbt0907.weather2.registry.ParticleRegistry.cloud256);
    }

    public final ExtendedEntityRotFX spawnParticle(double x, double y, double z, int parRenderOrder,
                                                   TextureAtlasSprite tex) {
        if (!canSpawnParticle()) return null;

        double speed = 0D;
        Random rand = new Random();
        ExtendedEntityRotFX entityfx = new ExtendedEntityRotFX(
                ClientTickHandler.weatherManager.getWorld(), x, y, z,
                (rand.nextDouble() - rand.nextDouble()) * speed,
                0.0D,
                (rand.nextDouble() - rand.nextDouble()) * speed,
                tex);
        entityfx.pb = particleBehaviorFog;
        entityfx.renderOrder = parRenderOrder;
        particleBehaviorFog.initParticle(entityfx);

        entityfx.setCanCollide(false);
        entityfx.callUpdatePB = false;
        entityfx.setMaxAge((system.size / 2) + rand.nextInt(100));

        if (ConfigCoroUtilLegacy.optimizedCloudRendering) {
            entityfx.setMaxAge(400);
        } else {
            entityfx.setMaxAge(480);
            entityfx.setTicksFadeInMax(80);
            entityfx.setTicksFadeOutMax(400);
        }

        entityfx.setScale(entityfx.getScale() * (float) ConfigClient.particle_scale_mult);
        ExtendedRenderer.rotEffRenderer.addEffect(entityfx);
        particleBehaviorFog.particles.add(entityfx);
        this.particles.add(entityfx);
        if (ClientTickHandler.weatherManager != null)
            ClientTickHandler.weatherManager.addWeatherParticle(entityfx);

        particlesLeft--;
        return entityfx;
    }

    public final void refreshParticleLimit() {
        particleLimit = ClientTickHandler.weatherManager.getParticleLimit();
        int attempts = 0;
        while (attempts > -1)
            try {
                onParticleLimitRefresh(ClientTickHandler.weatherManager, particleLimit);
                attempts = -1;
            } catch (Exception e) {
                attempts++;
                if (attempts < 3) {
                    Weather2.warn("Particle renderer's onParticleLimitRefresh() has encountered an error. Retrying...");
                    e.printStackTrace();
                } else {
                    Weather2.warn("Particle renderer's onParticleLimitRefresh() has failed to run correctly. Disabling particle renderer...");
                    e.printStackTrace();
                    ConfigClient.particle_renderer = "-1";
                    WeatherAPI.refreshRenders(false);
                    attempts = -1;
                }
            }
    }

    public final boolean canSpawnParticle() {
        return particles.size() < particleLimit;
    }
}