package net.mrbt0907.weather2.client.rendering;

import net.corosus.extendedrenderer.particle.entity.EntityRotFX;
import net.mrbt0907.weather2.api.weather.AbstractWeatherRenderer;
import net.mrbt0907.weather2.api.weather.WeatherEnum.Stage;
import net.mrbt0907.weather2.client.weather.WeatherManagerClient;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.storm.StormObject;
import net.mrbt0907.weather2.weather.storm.WeatherObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

// currently unused
public class NewStormRenderer extends AbstractWeatherRenderer {
    private static final int STAGE_TORNADO = Stage.TORNADO.getStage();
    private final HashSet<EntityRotFX> particles_funnel;
    private int limit_funnel;

    public NewStormRenderer(WeatherObject system) {
        super(system);
        particles_funnel = new HashSet<EntityRotFX>();
    }

    public static void rotate(EntityRotFX particle, Vec3 top, Vec3 bottom) {
    }

    @Override
    public void onTick(WeatherManagerClient manager) {
        if (!(system instanceof StormObject)) return;
        StormObject storm = (StormObject) system;

        if (storm.stage >= NewStormRenderer.STAGE_TORNADO) {
            Vec3 funnel_pos = storm.pos;
            Vec3 funnel_base_pos = storm.pos_funnel_base;

            particles_funnel.removeIf(particle -> !particle.isAlive());
            for (EntityRotFX particle : particles_funnel) {
                NewStormRenderer.rotate(particle, funnel_pos, funnel_base_pos);
            }
        }
    }

    @Override
    public void onParticleLimitRefresh(WeatherManagerClient manager, int newParticleLimit) {
        limit_funnel = newParticleLimit;
    }

    @Override
    public void cleanupRenderer() {
        particles_funnel.clear();
    }

    @Override
    public List<String> onDebugInfo() {
        List<String> debugInfo = new ArrayList<String>();
        debugInfo.add("New Renderer - Version 3.0");
        debugInfo.add("");
        debugInfo.add("Funnel Particles: " + particles_funnel.size() + "/" + limit_funnel);
        return debugInfo;
    }
}