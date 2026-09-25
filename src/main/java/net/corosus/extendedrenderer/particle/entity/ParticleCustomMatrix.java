package net.corosus.extendedrenderer.particle.entity;

import net.corosus.extendedrenderer.shader.InstancedMeshParticle;
import net.corosus.extendedrenderer.shader.Matrix4fe;
import net.corosus.extendedrenderer.shader.Transformation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3f;

public class ParticleCustomMatrix extends ParticleTexFX {

    public float angleX;
    public float angleY;
    public float angleZ;
    public float yy;

    public ParticleCustomMatrix(ClientWorld worldIn,
                                double posXIn, double posYIn, double posZIn,
                                double mX, double mY, double mZ,
                                TextureAtlasSprite par8Item) {
        super(worldIn, posXIn, posYIn, posZIn, mX, mY, mZ, par8Item);
    }

    public static float lerpDegrees(float start, float end, float amount) {
        float difference = Math.abs(end - start);
        if (difference > 180) {
            if (end > start) start += 360;
            else end += 360;
        }
        float value = start + (end - start) * amount;
        if (value >= 0 && value <= 360) return value;
        return value % 360;
    }

    @Override
    public void renderParticleForShader(InstancedMeshParticle mesh,
                                        Transformation transformation,
                                        Matrix4fe viewMatrix,
                                        Entity entityIn,
                                        float partialTicks,
                                        float rotationX, float rotationZ,
                                        float rotationYZ, float rotationXY, float rotationXZ) {
        if (mesh.curBufferPos >= mesh.numInstances) return;

        float posX = (float) (xo + (x - xo) * partialTicks - interpPosX);
        float posY = (float) (yo + (y - yo) * partialTicks - interpPosY);
        float posZ = (float) (zo + (z - zo) * partialTicks - interpPosZ);

        Vector3f pos = new Vector3f(posX, posY, posZ);

        Matrix4fe matrixFunnel = new Matrix4fe();
        matrixFunnel.rotateY(angleY);
        matrixFunnel.rotateX(angleX);
        matrixFunnel.translate(new Vector3f(0, yy, 0));

        if (rotationAroundCenter < rotationAroundCenterPrev) {
            rotationAroundCenterPrev -= 360;
        }
        float deltaRot = rotationAroundCenterPrev
                + (rotationAroundCenter - rotationAroundCenterPrev) * partialTicks;

        matrixFunnel.translate(new Vector3f(
                (float) Math.sin(Math.toRadians(deltaRot)) * rotationDistAroundCenter,
                0,
                (float) Math.cos(Math.toRadians(deltaRot)) * rotationDistAroundCenter));

        Vector3f posExtraRot = matrixFunnel.getTranslation();
        pos.add(posExtraRot.x(), posExtraRot.y(), posExtraRot.z());

        Matrix4fe modelMatrix = transformation.buildModelMatrix(this, pos, partialTicks);
        modelMatrix.get(InstancedMeshParticle.INSTANCE_SIZE_FLOATS * mesh.curBufferPos, mesh.instanceDataBuffer);

        mesh.instanceDataBuffer.put(
                InstancedMeshParticle.INSTANCE_SIZE_FLOATS * mesh.curBufferPos + InstancedMeshParticle.MATRIX_SIZE_FLOATS,
                brightnessCache);

        int i = 0;
        mesh.instanceDataBuffer.put(InstancedMeshParticle.INSTANCE_SIZE_FLOATS * mesh.curBufferPos + InstancedMeshParticle.MATRIX_SIZE_FLOATS + 1 + (i++), rCol);
        mesh.instanceDataBuffer.put(InstancedMeshParticle.INSTANCE_SIZE_FLOATS * mesh.curBufferPos + InstancedMeshParticle.MATRIX_SIZE_FLOATS + 1 + (i++), gCol);
        mesh.instanceDataBuffer.put(InstancedMeshParticle.INSTANCE_SIZE_FLOATS * mesh.curBufferPos + InstancedMeshParticle.MATRIX_SIZE_FLOATS + 1 + (i++), bCol);
        mesh.instanceDataBuffer.put(InstancedMeshParticle.INSTANCE_SIZE_FLOATS * mesh.curBufferPos + InstancedMeshParticle.MATRIX_SIZE_FLOATS + 1 + (i), alpha);

        mesh.curBufferPos++;
    }
}