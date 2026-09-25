package net.corosus.extendedrenderer.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.corosus.coroutillegacy.util.CoroUtilBlockLightCache;
import net.corosus.extendedrenderer.foliage.Foliage;
import net.corosus.extendedrenderer.particle.ParticleRegistry;
import net.corosus.extendedrenderer.shader.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL31;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;

public class FoliageRenderer {

    private static final ResourceLocation BLOCK_ATLAS = new ResourceLocation("minecraft", "textures/atlas/blocks.png");
    private static final FloatBuffer fogColorBuffer = BufferUtils.createFloatBuffer(4);
    public static FloatBuffer viewMatrixBuffer = BufferUtils.createFloatBuffer(16);
    public static int radialRange = 40;
    public static boolean testStaticLimit = false;
    public static long windTime = 0;
    public Transformation transformation;
    public ConcurrentHashMap<TextureAtlasSprite, List<Foliage>> foliage = new ConcurrentHashMap<>();
    public float windDir = 0;
    public float windSpeedSmooth = 0;
    public Lock lockVBO2 = new ReentrantLock();

    public FoliageRenderer() {
        transformation = new Transformation();
    }

    private static void setUniformSafe(ShaderProgram prog, String name, float value) {
        if (prog.uniforms.containsKey(name) && prog.uniforms.get(name) != null) {
            prog.setUniform(name, value);
        }
    }

    private static void setUniformSafe(ShaderProgram prog,
                                       float x, float y, float z, float w) {
        if (prog.uniforms.containsKey("fogColor") && prog.uniforms.get("fogColor") != null) {
            prog.setUniform("fogColor", x, y, z, w);
        }
    }

    public List<Foliage> getFoliageForSprite(TextureAtlasSprite sprite) {
        if (!foliage.containsKey(sprite)) {
            foliage.put(sprite, new ArrayList<>());
        }
        return foliage.get(sprite);
    }

    public void render(Entity entityIn, float partialTicks) {
        if (RotatingParticleManager.useShaders) {
            Minecraft mc = Minecraft.getInstance();

            RenderSystem.depthMask(true);
            mc.getTextureManager().bind(BLOCK_ATLAS);

            renderJustShaders(entityIn, partialTicks);
        }
    }

    public boolean getFlag(InstancedMeshFoliage mesh) {
        return mesh.dirtyVBO2Flag;
    }

    public void renderJustShaders(Entity entityIn, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();

        Matrix4fe projectionMatrix = new Matrix4fe();
        FloatBuffer buf = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloatv(GL11.GL_PROJECTION_MATRIX, buf);
        buf.rewind();
        Matrix4fe.get(projectionMatrix, 0, buf);

        boolean distantRendering = false;
        if (distantRendering) {
            float zNear = 0.05F;
            float zFar = (float) (mc.options.renderDistance * 16) * 4F;
            projectionMatrix.m22 = (zFar + zNear) / (zNear - zFar);
            projectionMatrix.m32 = (zFar + zFar) * zNear / (zNear - zFar);
        }

        Matrix4fe viewMatrix = new Matrix4fe();
        FloatBuffer buf2 = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, buf2);
        buf2.rewind();
        Matrix4fe.get(viewMatrix, 0, buf2);

        ShaderProgram shaderProgram = ShaderEngine.renderer.getShaderProgram("foliage");
        shaderProgram.bind();

        shaderProgram.setUniform("texture_sampler", 0);

        int glFogMode = GL11.glGetInteger(GL11.GL_FOG_MODE);
        int modeIndex = 0;
        if (glFogMode == GL11.GL_LINEAR) modeIndex = 0;
        else if (glFogMode == GL11.GL_EXP) modeIndex = 1;
        else if (glFogMode == GL11.GL_EXP2) modeIndex = 2;
        shaderProgram.setUniform("fogmode", modeIndex);

        float fogStart = GL11.glGetFloat(GL11.GL_FOG_START);
        float fogEnd = GL11.glGetFloat(GL11.GL_FOG_END);
        float fogDensity = GL11.glGetFloat(GL11.GL_FOG_DENSITY);
        float fogScale = (fogEnd != fogStart) ? 1.0f / (fogEnd - fogStart) : 0.0f;

        fogColorBuffer.clear();
        GL11.glGetFloatv(GL11.GL_FOG_COLOR, fogColorBuffer);
        fogColorBuffer.rewind();
        float fogR = fogColorBuffer.get();
        float fogG = fogColorBuffer.get();
        float fogB = fogColorBuffer.get();
        float fogA = fogColorBuffer.get();

        setUniformSafe(shaderProgram, "fogStart", fogStart);
        setUniformSafe(shaderProgram, "fogEnd", fogEnd);
        setUniformSafe(shaderProgram, "fogScale", fogScale);
        setUniformSafe(shaderProgram, "fogDensity", fogDensity);
        setUniformSafe(shaderProgram, fogR, fogG, fogB, fogA);

        shaderProgram.setUniform("partialTick", partialTicks);
        shaderProgram.setUniform("windDir", windDir - 135);
        shaderProgram.setUniform("windSpeed", windSpeedSmooth);

        try {
            shaderProgram.setUniform("time", (int) windTime);
        } catch (Exception ex) {
            //
        }

        MeshBufferManagerFoliage.setupMeshIfMissing(ParticleRegistry.potato);
        MeshBufferManagerFoliage.setupMeshIfMissing(ParticleRegistry.chicken);
        for (int i = 0; i < ParticleRegistry.listFish.size(); i++) {
            MeshBufferManagerFoliage.setupMeshIfMissing(ParticleRegistry.listFish.get(i));
        }
        for (int i = 0; i < ParticleRegistry.listSeaweed.size(); i++) {
            MeshBufferManagerFoliage.setupMeshIfMissing(ParticleRegistry.listSeaweed.get(i));
        }

        for (TextureAtlasSprite sprite : foliage.keySet()) {
            MeshBufferManagerFoliage.setupMeshIfMissing(sprite);
        }

        for (Map.Entry<TextureAtlasSprite, List<Foliage>> entry : foliage.entrySet()) {

            InstancedMeshFoliage mesh = MeshBufferManagerFoliage.getMesh(entry.getKey());

            if (mesh == null) {
                System.out.println("NULL MESH FOR: " + entry.getKey().toString());
                continue;
            }

            mesh.initRender();
            mesh.initRenderVBO1();
            mesh.initRenderVBO2();

            boolean updateVBO1 = true;

            ActiveRenderInfo mainCam = Minecraft.getInstance().gameRenderer.getMainCamera();
            Vector3d eyePos = mainCam.getPosition();

            if (lockVBO2.tryLock()) {
                try {
                    List<Foliage> listFoliage = entry.getValue();

                    if (getFlag(mesh)) {
                        mesh.instanceDataBufferVBO2.rewind();
                        GL15.glBindBuffer(GL_ARRAY_BUFFER, mesh.instanceDataVBO2);
                        GL15.glBufferData(GL_ARRAY_BUFFER, mesh.instanceDataBufferVBO2, GL_DYNAMIC_DRAW);
                        mesh.curBufferPosVBO2Thread = mesh.curBufferPosVBO2;
                        mesh.dirtyVBO2Flag = false;
                    }

                    if (updateVBO1) {
                        mesh.instanceDataBufferVBO1.clear();
                        mesh.curBufferPosVBO1 = 0;

                        try {
                            for (Foliage foliageEntry : listFoliage) {
                                foliageEntry.particleAlpha = 1F;
                                foliageEntry.brightnessCache = CoroUtilBlockLightCache.brightnessPlayer;
                                foliageEntry.renderForShaderVBO1(mesh, transformation, viewMatrix,
                                        entityIn, partialTicks);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        if (testStaticLimit) {
                            mesh.instanceDataBufferVBO1.limit(30000 * InstancedMeshFoliage.INSTANCE_SIZE_FLOATS);
                        } else {
                            mesh.instanceDataBufferVBO1.limit(mesh.curBufferPosVBO1 * InstancedMeshFoliage.INSTANCE_SIZE_FLOATS);
                        }

                        GL15.glBindBuffer(GL_ARRAY_BUFFER, mesh.instanceDataVBO1);
                        GL15.glBufferData(GL_ARRAY_BUFFER, mesh.instanceDataBufferVBO1, GL_DYNAMIC_DRAW);
                    }
                } finally {
                    lockVBO2.unlock();
                }
            }

            float offsetX = (float) (eyePos.x - mesh.interpPosXThread);
            float offsetY = (float) (eyePos.y - mesh.interpPosYThread);
            float offsetZ = (float) (eyePos.z - mesh.interpPosZThread);

            Matrix4fe matrixFix = new Matrix4fe();
            matrixFix = matrixFix.translationRotateScale(
                    -offsetX, -offsetY, -offsetZ,
                    0, 0, 0, 1,
                    1, 1, 1);

            projectionMatrix = new Matrix4fe();
            buf = BufferUtils.createFloatBuffer(16);
            GL11.glGetFloatv(GL11.GL_PROJECTION_MATRIX, buf);
            buf.rewind();
            Matrix4fe.get(projectionMatrix, 0, buf);

            Matrix4fe modelViewMatrix = projectionMatrix.mul(viewMatrix);
            matrixFix = modelViewMatrix.mul(matrixFix);

            shaderProgram.setUniformEfficient("modelViewMatrixCamera", matrixFix, viewMatrixBuffer);

            if (mesh.curBufferPosVBO2Thread > 0) {
                GL31.glDrawElementsInstanced(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT,
                        0, mesh.curBufferPosVBO2Thread);
            }

            GL15.glBindBuffer(GL_ARRAY_BUFFER, 0);

            mesh.endRenderVBO1();
            mesh.endRenderVBO2();
            mesh.endRender();
        }

        ShaderEngine.renderer.getShaderProgram("foliage").unbind();

        RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE1);
        RenderSystem.bindTexture(0);
        RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0);
    }
}