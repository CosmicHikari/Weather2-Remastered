package net.mrbt0907.weather2.client.rendering.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.corosus.extendedrenderer.particle.entity.EntityRotFX;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.client.entity.particle.ExtendedEntityRotFX;
import net.mrbt0907.weather2.client.rendering.shaders.mesh.SimpleVolumetricMesh;
import net.mrbt0907.weather2.mixins.accessor.ParticleAccessor;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;
import java.util.List;

public class VolumetricRenderer {
    public static final String FRAGMENT_SHADER_PATH = "/assets/" + Weather2.OLD_MODID + "/shaders/program/volumetric_clouds.fsh";
    public static final String VERTEX_SHADER_PATH = "/assets/" + Weather2.OLD_MODID + "/shaders/program/volumetric_clouds.vsh";
    public static final int texture_width = 3;
    public static VolumetricsShader shader;
    public static SimpleVolumetricMesh mesh;
    public static int texture_id;
    public static int texture_height;

    public static void startShader() {
        if (VolumetricRenderer.shader != null && VolumetricRenderer.shader.valid) return;
        VolumetricRenderer.shader = new VolumetricsShader(VolumetricRenderer.VERTEX_SHADER_PATH, VolumetricRenderer.FRAGMENT_SHADER_PATH);
        if (!VolumetricRenderer.shader.valid) {
            VolumetricRenderer.shader = null;
            VolumetricRenderer.mesh = null;
            return;
        }

        VolumetricRenderer.mesh = new SimpleVolumetricMesh(10);

    }

    public static void stopShader() {
        if (VolumetricRenderer.shader != null) {
            VolumetricRenderer.shader.deleteShader();
            VolumetricRenderer.shader = null;
        }
        if (VolumetricRenderer.mesh != null) {
            VolumetricRenderer.mesh.delete();
            VolumetricRenderer.mesh = null;
        }
    }

    public static void render(Entity entity, List<Particle> particles, float partialTicks) {
        if (VolumetricRenderer.shader == null) return;

        particles.removeIf(particle -> !(particle instanceof EntityRotFX) || particle instanceof ExtendedEntityRotFX && !((ExtendedEntityRotFX) particle).isVolumetric());


        RenderSystem.pushMatrix();
        VolumetricRenderer.shader.startShader();


        GL20.glUniform3f(VolumetricRenderer.shader.getParameter("camera"),
                (float) entity.getX(),
                (float) entity.getY(),
                (float) entity.getZ());
        GL20.glUniform1i(VolumetricRenderer.shader.getParameter("quality"), VolumetricRenderer.mesh.quality);


        VolumetricRenderer.mesh.bindVBO();


        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        RenderSystem.depthMask(false);


        int size = particles.size();
        for (int i = 0; i < size; i++) {
            ExtendedEntityRotFX fx = (ExtendedEntityRotFX) particles.get(i);
            ParticleAccessor accessor = (ParticleAccessor) fx;

            int packed = fx.getLightColor(partialTicks);
            int blockLight = (packed >> 4) & 0xF;

            float brightness = Math.max(blockLight / 15.0f,
                    accessor.getLevel().getSunAngle(partialTicks));

            GL20.glUniform3f(VolumetricRenderer.shader.getParameter("particle_pos"),
                    (float) fx.getPosX(),
                    (float) fx.getPosY(),
                    (float) fx.getPosZ());
            GL20.glUniform1f(VolumetricRenderer.shader.getParameter("particle_height"), fx.getScale() * 0.04F);
            GL20.glUniform1f(VolumetricRenderer.shader.getParameter("particle_width"), fx.getScale() * 0.04F);
            GL20.glUniform2f(VolumetricRenderer.shader.getParameter("particle_rotation"), fx.rotationYaw, fx.rotationPitch);
            GL20.glUniform4f(VolumetricRenderer.shader.getParameter("color"),
                    fx.getRedColorF(),
                    fx.getGreenColorF(),
                    fx.getBlueColorF(),
                    fx.getAlphaF());
            GL20.glUniform1f(VolumetricRenderer.shader.getParameter("brightness"), brightness);
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, VolumetricRenderer.mesh.length);
        }


        RenderSystem.depthMask(true);
        VolumetricRenderer.mesh.unbindVBO();

        VolumetricRenderer.shader.stopShader();
        RenderSystem.popMatrix();
    }

    public static void createParameterTexture() {
        VolumetricRenderer.texture_height = 1000;


        VolumetricRenderer.texture_id = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, VolumetricRenderer.texture_id);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, VolumetricRenderer.texture_width, VolumetricRenderer.texture_height, 0, GL11.GL_RGBA, GL11.GL_FLOAT, (FloatBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    public static void updateParameterTexture(Entity entity, List<Particle> particles, float partialTicks) {

        VolumetricRenderer.texture_height = particles.size();

        FloatBuffer buffer = BufferUtils.createFloatBuffer(VolumetricRenderer.texture_width * VolumetricRenderer.texture_height * 4);
        EntityRotFX fx;
        double ix, iy, iz;
        for (Particle particle : particles) {
            fx = (EntityRotFX) particle;

            ix = fx.getPosX() - entity.getX();
            iy = fx.getPosY() - entity.getY();
            iz = fx.getPosZ() - entity.getZ();

            buffer.put((float) ix).put((float) iy).put((float) iz);

            buffer.put(fx.getScale()).put(fx.getScale());

            buffer.put(fx.getRedColorF()).put(fx.getGreenColorF()).put(fx.getBlueColorF()).put(fx.getAlphaF());
            buffer.put(1).put(1.0F).put(1.0F);
        }
        buffer.flip();


        GL11.glBindTexture(GL11.GL_TEXTURE_2D, VolumetricRenderer.texture_id);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA16, VolumetricRenderer.texture_width, VolumetricRenderer.texture_height, 0, GL11.GL_RGBA, GL11.GL_FLOAT, buffer);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }
}