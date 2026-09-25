package net.corosus.extendedrenderer.foliage;

import net.corosus.coroutillegacy.util.CoroUtilBlockLightCache;
import net.corosus.extendedrenderer.shader.IShaderRenderedEntity;
import net.corosus.extendedrenderer.shader.InstancedMeshFoliage;
import net.corosus.extendedrenderer.shader.Matrix4fe;
import net.corosus.extendedrenderer.shader.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class Foliage implements IShaderRenderedEntity {

    private static final Random rand = new Random(439875L);
    private static final PerlinNoiseGenerator angleNoise = new PerlinNoiseGenerator(new SharedSeedRandom(rand.nextLong()), Collections.singletonList(0));
    private static final PerlinNoiseGenerator delayNoise = new PerlinNoiseGenerator(new SharedSeedRandom(rand.nextLong()), Arrays.asList(0, 1, 2));
    public double posX;
    public double posY;
    public double posZ;
    public double prevPosX;
    public double prevPosY;
    public double prevPosZ;
    public float width = 1F;
    public float height = 1F;
    public float particleScale = 1F;
    public float particleRed = 1F;
    public float particleGreen = 1F;
    public float particleBlue = 1F;
    public float particleAlpha = 1F;
    public TextureAtlasSprite particleTexture;
    public float rotationYaw;
    public float rotationPitch;
    public Quaternion rotation = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
    public boolean rotateOrderXY = false;
    public float brightnessCache = 0.5F;
    public int animationID = 0;
    public int heightIndex = 0;
    public float looseness = 1;

    public Foliage(TextureAtlasSprite sprite) {
        particleTexture = sprite;
    }

    public BlockPos getBlockPosition() {
        return new BlockPos(posX, posY, posZ);
    }

    @Override
    public Vector3f getPosition() {
        return new Vector3f((float) posX, (float) posY, (float) posZ);
    }

    public void setPosition(BlockPos pos) {
        posX = pos.getX();
        posY = pos.getY();
        posZ = pos.getZ();
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
    }

    @Override
    public Quaternion getQuaternion() {
        return rotation;
    }

    @Override
    public Quaternion getQuaternionPrev() {
        return null;
    }

    @Override
    public float getScale() {
        return particleScale;
    }

    public void updateQuaternion(Entity camera) {
        Quaternion qY = new Quaternion(new Vector3f(0, 1, 0), (float) Math.toRadians(-this.rotationYaw - 180F), false);
        Quaternion qX = new Quaternion(new Vector3f(1, 0, 0), (float) Math.toRadians(-this.rotationPitch), false);
        if (this.rotateOrderXY) {
            qX.mul(qY);
            this.rotation = qX;
        } else {
            qY.mul(qX);
            this.rotation = qY;
        }
    }

    public void renderForShaderVBO1(InstancedMeshFoliage mesh, Transformation transformation, Matrix4fe viewMatrix, Entity entityIn,
                                    float partialTicks) {

        if (mesh.curBufferPosVBO1 >= mesh.numInstances) {
            return;
        }

        mesh.instanceDataBufferVBO1.put(InstancedMeshFoliage.INSTANCE_SIZE_FLOATS * (mesh.curBufferPosVBO1), particleAlpha);

        float brightness = CoroUtilBlockLightCache.getBrightnessFromLightmap(
                Minecraft.getInstance().level,
                (float) posX, (float) posY, (float) posZ
        );

        mesh.instanceDataBufferVBO1.put(InstancedMeshFoliage.INSTANCE_SIZE_FLOATS * (mesh.curBufferPosVBO1) + 1, brightness);

        mesh.curBufferPosVBO1++;
    }

    public void renderForShaderVBO2(InstancedMeshFoliage mesh, Transformation transformation, Matrix4fe viewMatrix, Entity entityIn,
                                    float partialTicks) {

        boolean autoGrowBuffer = false;
        if (mesh.curBufferPosVBO2 >= mesh.numInstances) {

            if (autoGrowBuffer) {
                mesh.numInstances *= 2;
                System.out.println("hit max mesh count, doubling in size to " + mesh.numInstances);
                FloatBuffer newBuffer = BufferUtils.createFloatBuffer(mesh.numInstances * InstancedMeshFoliage.INSTANCE_SIZE_FLOATS_SELDOM);
                mesh.instanceDataBufferVBO2.rewind();
                newBuffer.put(mesh.instanceDataBufferVBO2);
                mesh.instanceDataBufferVBO2.rewind();
                newBuffer.flip();
                mesh.instanceDataBufferVBO2 = newBuffer;
                mesh.instanceDataBufferVBO2.position(mesh.curBufferPosVBO2 * InstancedMeshFoliage.INSTANCE_SIZE_FLOATS_SELDOM);

                newBuffer = BufferUtils.createFloatBuffer(mesh.numInstances * InstancedMeshFoliage.INSTANCE_SIZE_FLOATS);
                newBuffer.clear();
                mesh.instanceDataBufferVBO1 = newBuffer;
            } else {
                return;
            }
        }

        float posX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - mesh.interpPosXThread);
        float posY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - mesh.interpPosYThread);
        float posZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - mesh.interpPosZThread);
        Vector3f pos = new Vector3f(posX, posY, posZ);

        Matrix4fe modelMatrix = transformation.buildModelMatrix(this, pos, partialTicks);

        modelMatrix.get(InstancedMeshFoliage.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPosVBO2), mesh.instanceDataBufferVBO2);

        int floatIndex = 0;
        mesh.instanceDataBufferVBO2.put(InstancedMeshFoliage.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPosVBO2) + InstancedMeshFoliage.MATRIX_SIZE_FLOATS
                + (floatIndex++), this.particleRed);
        mesh.instanceDataBufferVBO2.put(InstancedMeshFoliage.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPosVBO2) + InstancedMeshFoliage.MATRIX_SIZE_FLOATS
                + (floatIndex++), this.particleGreen);
        mesh.instanceDataBufferVBO2.put(InstancedMeshFoliage.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPosVBO2) + InstancedMeshFoliage.MATRIX_SIZE_FLOATS
                + (floatIndex++), this.particleBlue);
        mesh.instanceDataBufferVBO2.put(InstancedMeshFoliage.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPosVBO2) + InstancedMeshFoliage.MATRIX_SIZE_FLOATS
                + (floatIndex++), this.rotationYaw);

        mesh.instanceDataBufferVBO2.put(InstancedMeshFoliage.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPosVBO2) + InstancedMeshFoliage.MATRIX_SIZE_FLOATS
                + (floatIndex++), (float) delayNoise.getValue(this.posX, this.posZ, false));

        mesh.instanceDataBufferVBO2.put(InstancedMeshFoliage.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPosVBO2) + InstancedMeshFoliage.MATRIX_SIZE_FLOATS
                + (floatIndex++), animationID);

        mesh.instanceDataBufferVBO2.put(InstancedMeshFoliage.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPosVBO2) + InstancedMeshFoliage.MATRIX_SIZE_FLOATS
                + (floatIndex++), heightIndex);

        mesh.instanceDataBufferVBO2.put(InstancedMeshFoliage.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPosVBO2) + InstancedMeshFoliage.MATRIX_SIZE_FLOATS
                + (floatIndex++), looseness);

        mesh.curBufferPosVBO2++;
    }
}