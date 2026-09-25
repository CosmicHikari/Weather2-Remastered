package net.mrbt0907.weather2.util;

import net.corosus.extendedrenderer.particle.entity.EntityRotFX;
import net.minecraft.client.particle.Particle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WeatherUtilEntityClient {

    private WeatherUtilEntityClient() {
    }

    public static boolean isEntityRotFX(Object obj) {
        return obj instanceof EntityRotFX;
    }

    public static float getParticleWeight(Object obj) {
        return WeatherUtilParticle.getParticleWeight((Particle) obj);
    }
}