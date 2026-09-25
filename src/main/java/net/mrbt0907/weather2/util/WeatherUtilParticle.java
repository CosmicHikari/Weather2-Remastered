package net.mrbt0907.weather2.util;

import net.corosus.extendedrenderer.particle.entity.EntityRotFX;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.weather2.mixins.accessor.ParticleAccessor;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

public class WeatherUtilParticle {
    public static Map<IParticleRenderType, Queue<Particle>> particles;

    public static int effLeafID = 0;
    public static int effRainID = 1;
    public static int effWindID = 2;
    public static int effSnowID = 3;


    public static Random rand = new Random();


    public static int getParticleAge(Particle ent) {
        return ((ParticleAccessor) ent).getAge();
    }


    public static void setParticleAge(Particle ent, int val) {
        ((ParticleAccessor) ent).setAge(val);
    }

    @SuppressWarnings("unchecked")
    @OnlyIn(Dist.CLIENT)
    public static void getFXLayers() {

        Field field = null;

        try {

            field = (ParticleManager.class).getDeclaredField("field_78876_b");
            field.setAccessible(true);
            WeatherUtilParticle.particles = (Map<IParticleRenderType, Queue<Particle>>) field.get(Minecraft.getInstance().particleEngine);
        } catch (Exception ex) {
            try {

                field = (ParticleManager.class).getDeclaredField("particles");
                field.setAccessible(true);
                WeatherUtilParticle.particles = (Map<IParticleRenderType, Queue<Particle>>) field.get(Minecraft.getInstance().particleEngine);
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static float getParticleWeight(Particle entity1) {
        if (entity1 instanceof EntityRotFX)
            return 5.0F + ((float) ((EntityRotFX) entity1).getAge() / 200);
        else if (entity1 instanceof Particle)
            return 5.0F + ((float) ((ParticleAccessor) entity1).getAge() / 200);

        return -1;
    }
}