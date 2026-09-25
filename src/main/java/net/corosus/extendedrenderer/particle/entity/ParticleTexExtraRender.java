package net.corosus.extendedrenderer.particle.entity;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.corosus.coroutillegacy.util.CoroUtilBlockLightCache;
import net.corosus.coroutillegacy.util.CoroUtilMath;
import net.corosus.coroutillegacy.util.CoroUtilParticle;
import net.corosus.extendedrenderer.render.RotatingParticleManager;
import net.corosus.extendedrenderer.shader.InstancedMeshParticle;
import net.corosus.extendedrenderer.shader.Matrix4fe;
import net.corosus.extendedrenderer.shader.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.gen.Heightmap;

public class ParticleTexExtraRender extends ParticleTexFX {

    public boolean noExtraParticles = false;
    private int severityOfRainRate = 2;
    private int extraParticlesBaseAmount = 5;

    public ParticleTexExtraRender(ClientWorld worldIn,
                                  double posXIn, double posYIn, double posZIn,
                                  double mX, double mY, double mZ,
                                  TextureAtlasSprite par8Item) {
        super(worldIn, posXIn, posYIn, posZIn, mX, mY, mZ, par8Item);
    }

    public int getSeverityOfRainRate() {
        return severityOfRainRate;
    }

    public void setSeverityOfRainRate(int v) {
        severityOfRainRate = v;
    }

    public int getExtraParticlesBaseAmount() {
        return extraParticlesBaseAmount;
    }

    public void setExtraParticlesBaseAmount(int v) {
        extraParticlesBaseAmount = v;
    }

    @Override
    public void tickExtraRotations() {
        if (isSlantParticleToWind()) {
            rotationYaw = (float) Math.toDegrees(Math.atan2(zd, xd)) - 90;
            double motionXZ = Math.sqrt(xd * xd + zd * zd);
            rotationPitch = -(float) Math.toDegrees(Math.atan2(motionXZ, Math.abs(yd)));
        }
        if (!quatControl) {
            updateQuaternion(Minecraft.getInstance().getCameraEntity());
        }
    }

    @Override
    public void render(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {

        Vector3f fwd = renderInfo.getLookVector();
        Vector3f up = renderInfo.getUpVector();

        float leftX = up.y() * fwd.z() - up.z() * fwd.y();
        float leftZ = up.x() * fwd.y() - up.y() * fwd.x();

        float rotationX = leftX;
        float rotationZ = up.y();
        float rotationYZ = leftZ;
        float rotationXY = up.x();
        float rotationXZ = up.z();

        if (!facePlayer) {
            rotationX = MathHelper.cos(this.rotationYaw * (float) Math.PI / 180.0F);
            rotationYZ = MathHelper.sin(this.rotationYaw * (float) Math.PI / 180.0F);
            rotationXY = -rotationYZ * MathHelper.sin(this.rotationPitch * (float) Math.PI / 180.0F);
            rotationXZ = rotationX * MathHelper.sin(this.rotationPitch * (float) Math.PI / 180.0F);
            rotationZ = MathHelper.cos(this.rotationPitch * (float) Math.PI / 180.0F);
        } else {
            if (isSlantParticleToWind()) {
                rotationXZ = (float) -zd;
                rotationXY = (float) -xd;
            }
        }

        float u0 = sprite != null ? sprite.getU0() : 0f;
        float u1 = sprite != null ? sprite.getU1() : 1f;
        float v0 = sprite != null ? sprite.getV0() : 0f;
        float v1 = sprite != null ? sprite.getV1() : 1f;

        float fixY = 0;
        if (sprite != null) {
            float part = 16F / 3F;
            float offset = 0;
            float posBottom = (float) (this.y - 10D);

            float height = this.level.getHeightmapPos(
                    Heightmap.Type.MOTION_BLOCKING,
                    new BlockPos(this.x, this.y, this.z)).getY();

            if (posBottom < height) {
                offset = height - posBottom;
                fixY = 0;
                if (offset > part) offset = part;
            }
        }

        int renderAmount = noExtraParticles
                ? 1
                : Math.min(extraParticlesBaseAmount + (Math.max(0, severityOfRainRate - 1)) * 5,
                CoroUtilParticle.maxRainDrops);

        float scale1 = 0.1F * particleScale;
        float scale2 = scale1, scale3 = scale1, scale4 = scale1;

        int lightVal = 15728640;
        int lU = lightVal & 0xFFFF;
        int lV = (lightVal >> 16) & 0xFFFF;

        try {
            for (int ii = 0; ii < renderAmount; ii++) {
                float f5 = (float) (xo + (x - xo) * partialTicks - interpPosX);
                float f6 = (float) (yo + (y - yo) * partialTicks - interpPosY) + fixY;
                float f7 = (float) (zo + (z - zo) * partialTicks - interpPosZ);

                double xx = 0, zz = 0, yy = 0;
                if (ii != 0) {
                    xx = CoroUtilParticle.rainPositions[ii].xCoord;
                    zz = CoroUtilParticle.rainPositions[ii].zCoord;
                    yy = CoroUtilParticle.rainPositions[ii].yCoord;
                    f5 += xx;
                    f6 += yy;
                    f7 += zz;
                }

                if (isDontRenderUnderTopmostBlock()) {
                    int h = this.level.getHeightmapPos(
                            Heightmap.Type.MOTION_BLOCKING,
                            new BlockPos(this.x + xx, this.y, this.z + zz)).getY();
                    if (this.y + yy <= h) continue;
                }

                if (ii != 0) RotatingParticleManager.debugParticleRenderCount++;

                Vector3d[] avec3d = new Vector3d[]{
                        new Vector3d(-rotationX * scale1 - rotationXY * scale1,
                                -rotationZ * scale1,
                                -rotationYZ * scale1 - rotationXZ * scale1),
                        new Vector3d(-rotationX * scale2 + rotationXY * scale2,
                                rotationZ * scale2,
                                -rotationYZ * scale2 + rotationXZ * scale2),
                        new Vector3d(rotationX * scale3 + rotationXY * scale3,
                                rotationZ * scale3,
                                rotationYZ * scale3 + rotationXZ * scale3),
                        new Vector3d(rotationX * scale4 - rotationXY * scale4,
                                -rotationZ * scale4,
                                rotationYZ * scale4 - rotationXZ * scale4)
                };

                buffer.vertex(f5 + avec3d[0].x, f6 + avec3d[0].y, f7 + avec3d[0].z).uv(u1, v1).color(rCol, gCol, bCol, alpha).uv2(lU, lV).endVertex();
                buffer.vertex(f5 + avec3d[1].x, f6 + avec3d[1].y, f7 + avec3d[1].z).uv(u1, v0).color(rCol, gCol, bCol, alpha).uv2(lU, lV).endVertex();
                buffer.vertex(f5 + avec3d[2].x, f6 + avec3d[2].y, f7 + avec3d[2].z).uv(u0, v0).color(rCol, gCol, bCol, alpha).uv2(lU, lV).endVertex();
                buffer.vertex(f5 + avec3d[3].x, f6 + avec3d[3].y, f7 + avec3d[3].z).uv(u0, v1).color(rCol, gCol, bCol, alpha).uv2(lU, lV).endVertex();
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void renderParticleForShader(InstancedMeshParticle mesh,
                                        Transformation transformation,
                                        Matrix4fe viewMatrix,
                                        Entity entityIn,
                                        float partialTicks,
                                        float rotationX, float rotationZ,
                                        float rotationYZ, float rotationXY, float rotationXZ) {
        float posX = (float) (xo + (x - xo) * partialTicks);
        float posY = (float) (yo + (y - yo) * partialTicks);
        float posZ = (float) (zo + (z - zo) * partialTicks);

        int renderAmount = noExtraParticles
                ? 1
                : Math.min(extraParticlesBaseAmount + (Math.max(0, severityOfRainRate - 1)) * 5,
                CoroUtilParticle.maxRainDrops);

        for (int iii = 0; iii < renderAmount; iii++) {
            if (mesh.curBufferPos >= mesh.numInstances) return;

            Vector3f pos;
            if (iii != 0) {
                pos = new Vector3f(
                        posX + (float) CoroUtilParticle.rainPositions[iii].xCoord,
                        posY + (float) CoroUtilParticle.rainPositions[iii].yCoord,
                        posZ + (float) CoroUtilParticle.rainPositions[iii].zCoord);
            } else {
                pos = new Vector3f(posX, posY, posZ);
            }

            if (isDontRenderUnderTopmostBlock()) {
                int height = this.level.getHeightmapPos(
                        Heightmap.Type.MOTION_BLOCKING,
                        new BlockPos(pos.x(), this.y, pos.z())).getY();
                if (pos.y() <= height) continue;
            }

            pos.add(-(float) interpPosX, -(float) interpPosY, -(float) interpPosZ);

            Quaternion q = this.rotation;
            if (this.rotationPrev != null) {
                q = CoroUtilMath.interpolate(this.rotationPrev, this.rotation, partialTicks);
            }

            float scaleAdj = particleScale * 0.2F;

            Matrix4fe modelMatrix = new Matrix4fe();
            modelMatrix.translationRotateScale(
                    pos.x(), pos.y(), pos.z(),
                    q.i(), q.j(), q.k(), q.r(),
                    scaleAdj, scaleAdj, scaleAdj);

            modelMatrix.get(InstancedMeshParticle.INSTANCE_SIZE_FLOATS * mesh.curBufferPos, mesh.instanceDataBuffer);

            float brightness = fastLight
                    ? CoroUtilBlockLightCache.brightnessPlayer
                    : CoroUtilBlockLightCache.getBrightnessCached(level, (float) this.x, (float) this.y, (float) this.z);

            mesh.instanceDataBuffer.put(
                    InstancedMeshParticle.INSTANCE_SIZE_FLOATS * mesh.curBufferPos + InstancedMeshParticle.MATRIX_SIZE_FLOATS,
                    brightness);

            int i = 0;
            mesh.instanceDataBuffer.put(InstancedMeshParticle.INSTANCE_SIZE_FLOATS * mesh.curBufferPos + InstancedMeshParticle.MATRIX_SIZE_FLOATS + 1 + (i++), rCol);
            mesh.instanceDataBuffer.put(InstancedMeshParticle.INSTANCE_SIZE_FLOATS * mesh.curBufferPos + InstancedMeshParticle.MATRIX_SIZE_FLOATS + 1 + (i++), gCol);
            mesh.instanceDataBuffer.put(InstancedMeshParticle.INSTANCE_SIZE_FLOATS * mesh.curBufferPos + InstancedMeshParticle.MATRIX_SIZE_FLOATS + 1 + (i++), bCol);
            mesh.instanceDataBuffer.put(InstancedMeshParticle.INSTANCE_SIZE_FLOATS * mesh.curBufferPos + InstancedMeshParticle.MATRIX_SIZE_FLOATS + 1 + (i), alpha);

            mesh.curBufferPos++;
        }
    }

    @Override
    public void updateQuaternion(Entity camera) {
        if (camera != null) {
            if (facePlayer) {
                rotationYaw = camera.yRot;
                rotationPitch = camera.xRot;
            } else if (facePlayerYaw) {
                rotationYaw = camera.yRot;
            }
        }

        Quaternion qY = new Quaternion(Vector3f.YP, -rotationYaw - 180F, true);
        Quaternion qX = new Quaternion(Vector3f.XP, -rotationPitch, true);

        if (rotateOrderXY) {
            rotation = qX.copy();
            rotation.mul(qY);
        } else {
            rotation = qY.copy();
            rotation.mul(qX);

            if (extraYRotation != 0) {
                Quaternion qExtra = new Quaternion(Vector3f.YP, extraYRotation, true);
                rotation.mul(qExtra);
            }
        }
    }
}