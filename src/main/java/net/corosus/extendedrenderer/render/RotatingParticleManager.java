package net.corosus.extendedrenderer.render;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.corosus.coroutillegacy.config.ConfigCoroUtilLegacy;
import net.corosus.extendedrenderer.particle.ParticleRegistry;
import net.corosus.extendedrenderer.particle.ShaderManager;
import net.corosus.extendedrenderer.particle.entity.EntityRotFX;
import net.corosus.extendedrenderer.shader.*;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import javax.annotation.Nullable;
import java.nio.FloatBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;

@OnlyIn(Dist.CLIENT)
public class RotatingParticleManager {

    private static final ResourceLocation PARTICLE_TEXTURES =
            new ResourceLocation("textures/particle/particles.png");
    public static int debugParticleRenderCount;
    public static int lastAmountToRender;
    public static boolean useShaders;

    public static FloatBuffer projectionMatrixBuffer = BufferUtils.createFloatBuffer(16);
    public static FloatBuffer viewMatrixBuffer = BufferUtils.createFloatBuffer(16);

    public static boolean forceShaderReset = false;
    private static boolean forceVBO2Update = false;
    public final LinkedHashMap<TextureAtlasSprite, List<ArrayDeque<Particle>[][]>> fxLayers
            = new LinkedHashMap<>();
    private final TextureManager renderer;
    private final Queue<Particle> queueEntityFX = Queues.newArrayDeque();
    protected ClientWorld world;

    public RotatingParticleManager(ClientWorld worldIn, TextureManager rendererIn) {
        this.world = worldIn;
        this.renderer = rendererIn;
    }

    public static void markDirtyVBO2() {
        forceVBO2Update = true;
    }

    @SuppressWarnings("unchecked")
    public void initNewArrayData(TextureAtlasSprite sprite) {
        List<ArrayDeque<Particle>[][]> list = new ArrayList<>();
        list.add(0, new ArrayDeque[4][]);
        list.add(1, new ArrayDeque[4][]);
        list.add(2, new ArrayDeque[4][]);
        for (ArrayDeque<Particle>[][] entry : list) {
            for (int i = 0; i < 4; ++i) {
                entry[i] = new ArrayDeque[2];
                for (int j = 0; j < 2; ++j) {
                    entry[i][j] = Queues.newArrayDeque();
                }
            }
        }
        fxLayers.put(sprite, list);
    }

    public void addEffect(Particle effect) {
        if (effect == null) return;
        this.queueEntityFX.add(effect);
    }

    public void updateEffects() {
        for (int i = 0; i < 4; ++i) {
            this.updateEffectLayer(i);
        }

        if (!this.queueEntityFX.isEmpty()) {
            RotatingParticleManager.markDirtyVBO2();

            for (Particle particle = this.queueEntityFX.poll();
                 particle != null;
                 particle = this.queueEntityFX.poll()) {

                if (!(particle instanceof EntityRotFX)) continue;
                EntityRotFX rotFX = (EntityRotFX) particle;

                int j = 1;
                int k = rotFX.shouldDisableDepth() ? 0 : 1;
                int renderOrder = rotFX.renderOrder;

                TextureAtlasSprite tex = rotFX.getParticleTexture();
                if (tex == null) continue;

                if (!fxLayers.containsKey(tex)) {
                    initNewArrayData(tex);
                }

                ArrayDeque<Particle>[][] entry = fxLayers.get(tex).get(renderOrder);

                if (entry[j][k].size() >= 16384) {
                    Particle oldest = entry[j][k].getFirst();
                    if (oldest instanceof EntityRotFX)
                        ((EntityRotFX) oldest).setExpired();
                    else
                        oldest.remove();
                    entry[j][k].removeFirst();
                }

                entry[j][k].add(particle);
            }
        }
    }

    private void updateEffectLayer(int layer) {
        for (int i = 0; i < 2; ++i) {
            for (Map.Entry<TextureAtlasSprite, List<ArrayDeque<Particle>[][]>> entry1 : fxLayers.entrySet()) {
                for (ArrayDeque<Particle>[][] entry2 : entry1.getValue()) {
                    this.tickParticleList(entry2[layer][i]);
                }
            }
        }
    }

    private void tickParticleList(ArrayDeque<Particle> queue) {
        if (!queue.isEmpty()) {
            Iterator<Particle> iterator = queue.iterator();

            while (iterator.hasNext()) {
                Particle particle = iterator.next();
                this.tickParticle(particle);

                if (!particle.isAlive()) {
                    iterator.remove();
                    RotatingParticleManager.markDirtyVBO2();
                }
            }
        }
    }

    private void tickParticle(final Particle particle) {
        try {
            particle.tick();
        } catch (Throwable throwable) {
            net.minecraft.crash.CrashReport crashreport =
                    net.minecraft.crash.CrashReport.forThrowable(throwable, "Ticking Rotating Particle");
            net.minecraft.crash.CrashReportCategory crashreportcategory =
                    crashreport.addCategory("Particle being ticked");
            crashreportcategory.setDetail("Rotating Particle", particle::toString);
            crashreportcategory.setDetail("Particle Type", () -> "TERRAIN_TEXTURE");
            throw new net.minecraft.crash.ReportedException(crashreport);
        }
    }

    @SuppressWarnings("deprecation")
    public void renderParticles(Entity entityIn, ActiveRenderInfo activeRenderInfo, float partialTicks, MatrixStack matrixStack) {

        boolean useParticleShaders = useShaders && ConfigCoroUtilLegacy.particleShaders;

        Vector3f fwd = activeRenderInfo.getLookVector();
        Vector3f up = activeRenderInfo.getUpVector();
        float leftX = up.y() * fwd.z() - up.z() * fwd.y();
        float leftZ = up.x() * fwd.y() - up.y() * fwd.x();
        float f = leftX;
        float f1 = up.y();
        float f2 = leftZ;
        float f3 = up.x();
        float f4 = up.z();

        Vector3d camPos = activeRenderInfo.getPosition();
        EntityRotFX.interpPosX = camPos.x;
        EntityRotFX.interpPosY = camPos.y;
        EntityRotFX.interpPosZ = camPos.z;

        debugParticleRenderCount = 0;

        if (useParticleShaders) {
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.cloud256_test);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.cloud256_fire);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.cloud256);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.downfall3);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.cloud256_6);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.rain_white);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.snow);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.leaf);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.debris_1);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.debris_2);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.debris_3);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.tumbleweed);
        }

        Transformation transformation = null;
        Matrix4fe viewMatrix = null;

        int glCalls = 0;
        int trueRenderCount = 0;
        int particles = 0;

        if (useParticleShaders) {
            ShaderProgram shaderProgram = ShaderEngine.renderer.getShaderProgram("particle");
            transformation = ShaderEngine.renderer.transformation;
            shaderProgram.bind();

            Matrix4fe projectionMatrix = new Matrix4fe();
            projectionMatrixBuffer.clear();
            GL11.glGetFloatv(GL11.GL_PROJECTION_MATRIX, projectionMatrixBuffer);
            projectionMatrixBuffer.rewind();
            Matrix4fe.get(projectionMatrix, 0, projectionMatrixBuffer);

            viewMatrix = new Matrix4fe();
            viewMatrixBuffer.clear();
            GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, viewMatrixBuffer);
            viewMatrixBuffer.rewind();
            Matrix4fe.get(viewMatrix, 0, viewMatrixBuffer);

            Matrix4fe modelViewMatrix = projectionMatrix.mul(viewMatrix);
            shaderProgram.setUniformEfficient("modelViewMatrixCamera", modelViewMatrix, viewMatrixBuffer);
            shaderProgram.setUniform("texture_sampler", 0);

            int glFogMode = GL11.glGetInteger(GL11.GL_FOG_MODE);
            int modeIndex;
            if (glFogMode == GL11.GL_LINEAR) {
                modeIndex = 0;
            } else if (glFogMode == GL11.GL_EXP) {
                modeIndex = 1;
            } else if (glFogMode == GL11.GL_EXP2) {
                modeIndex = 2;
            } else {
                modeIndex = 0;
            }
            shaderProgram.setUniform("fogmode", modeIndex);
        }

        for (Map.Entry<TextureAtlasSprite, List<ArrayDeque<Particle>[][]>> entry1 : fxLayers.entrySet()) {

            InstancedMeshParticle mesh = null;

            if (useParticleShaders) {
                mesh = MeshBufferManagerParticle.getMesh(entry1.getKey());
                if (mesh == null) {
                    MeshBufferManagerParticle.setupMeshForParticle(entry1.getKey());
                    mesh = MeshBufferManagerParticle.getMesh(entry1.getKey());
                }
            }

            if (mesh != null || !useParticleShaders) {
                for (ArrayDeque<Particle>[][] entry : entry1.getValue()) {
                    for (int i_nf = 0; i_nf < 3; ++i_nf) {
                        final int i = i_nf;

                        for (int j = 0; j < 2; ++j) {
                            if (!entry[i][j].isEmpty()) {

                                switch (j) {
                                    case 0:
                                        RenderSystem.depthMask(false);
                                        break;
                                    case 1:
                                        RenderSystem.depthMask(true);
                                        break;
                                }

                                switch (i) {
                                    default:
                                    case 0:
                                        this.renderer.bind(PARTICLE_TEXTURES);
                                        break;
                                    case 1:
                                        this.renderer.bind(AtlasTexture.LOCATION_BLOCKS);
                                        break;
                                }

                                if (useParticleShaders) {

                                    mesh.initRender();
                                    mesh.initRenderVBO1();

                                    mesh.instanceDataBuffer.clear();
                                    mesh.curBufferPos = 0;
                                    particles = entry[i][j].size();

                                    for (final Particle particle : entry[i][j]) {
                                        if (particle instanceof EntityRotFX) {
                                            EntityRotFX part = (EntityRotFX) particle;
                                            part.renderParticleForShader(
                                                    mesh, transformation, viewMatrix,
                                                    entityIn, partialTicks,
                                                    f, f4, f1, f2, f3);
                                        }
                                    }

                                    mesh.instanceDataBuffer.limit(
                                            mesh.curBufferPos * InstancedMeshParticle.INSTANCE_SIZE_FLOATS);

                                    GL15.glBindBuffer(GL_ARRAY_BUFFER, mesh.instanceDataVBO);
                                    ShaderManager.glBufferData(
                                            GL_ARRAY_BUFFER, mesh.instanceDataBuffer, GL_DYNAMIC_DRAW);

                                    ShaderManager.glDrawElementsInstanced(
                                            GL_TRIANGLES, mesh.getVertexCount(),
                                            GL11.GL_UNSIGNED_INT, 0, mesh.curBufferPos);

                                    glCalls++;
                                    trueRenderCount += mesh.curBufferPos;

                                    GL15.glBindBuffer(GL_ARRAY_BUFFER, 0);
                                    mesh.endRenderVBO1();
                                    mesh.endRender();

                                } else {
                                    RenderSystem.enableAlphaTest();
                                    RenderSystem.alphaFunc(GL11.GL_GREATER, 0.004f);  // changed from 0.1f
                                    RenderSystem.enableBlend();
                                    RenderSystem.defaultBlendFunc();

                                    Tessellator tessellator = Tessellator.getInstance();
                                    BufferBuilder vertexbuffer = tessellator.getBuilder();
                                    vertexbuffer.begin(GL_QUADS, DefaultVertexFormats.PARTICLE);

                                    for (final Particle particle : entry[i][j]) {
                                        particle.render(vertexbuffer, activeRenderInfo, partialTicks);
                                        debugParticleRenderCount++;
                                    }

                                    tessellator.end();
                                }
                            }
                        }
                    }
                }
            }
        }

        forceVBO2Update = false;

        if (useParticleShaders) {
            ShaderEngine.renderer.getShaderProgram("particle").unbind();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL20.glUseProgram(0);
        }

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.disableAlphaTest();
    }

    @SuppressWarnings("deprecation")
    public void renderLitParticles(Entity entityIn, ActiveRenderInfo activeRenderInfo, float partialTick) {
        RenderSystem.enableAlphaTest();
        RenderSystem.alphaFunc(GL11.GL_GREATER, 0.1f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        for (Map.Entry<TextureAtlasSprite, List<ArrayDeque<Particle>[][]>> entry1 : fxLayers.entrySet()) {
            for (ArrayDeque<Particle>[][] entry : entry1.getValue()) {
                for (int i = 0; i < 2; ++i) {
                    ArrayDeque<Particle> queue = entry[3][i];
                    if (!queue.isEmpty()) {
                        Tessellator tessellator = Tessellator.getInstance();
                        BufferBuilder vertexbuffer = tessellator.getBuilder();

                        vertexbuffer.begin(GL_QUADS, DefaultVertexFormats.PARTICLE);

                        for (Particle particle : queue) {
                            particle.render(vertexbuffer, activeRenderInfo, partialTick);
                        }

                        tessellator.end();
                    }
                }
            }
        }

        RenderSystem.disableAlphaTest();
        RenderSystem.disableBlend();
    }

    public void clearEffects(@Nullable ClientWorld worldIn) {
        this.world = worldIn;

        for (Map.Entry<TextureAtlasSprite, List<ArrayDeque<Particle>[][]>> entry1 : fxLayers.entrySet()) {
            for (ArrayDeque<Particle>[][] entry : entry1.getValue()) {
                for (int i = 0; i < entry.length; i++) {
                    for (int j = 0; j < entry[i].length; j++) {
                        if (entry[i][j] != null) {
                            entry[i][j].clear();
                        }
                    }
                }
            }
        }
    }
}