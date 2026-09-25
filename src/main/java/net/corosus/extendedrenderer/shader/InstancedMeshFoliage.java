package net.corosus.extendedrenderer.shader;

import net.corosus.extendedrenderer.particle.ShaderManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import java.nio.FloatBuffer;

public class InstancedMeshFoliage extends Mesh {

    public static final int FLOAT_SIZE_BYTES = 4;

    public static final int VECTOR4F_SIZE_BYTES = 4 * FLOAT_SIZE_BYTES;

    public static final int MATRIX_SIZE_FLOATS = 4 * 4;

    public static final int MATRIX_SIZE_BYTES = MATRIX_SIZE_FLOATS * FLOAT_SIZE_BYTES;
    public static final int INSTANCE_SIZE_BYTES_SELDOM = MATRIX_SIZE_BYTES + FLOAT_SIZE_BYTES * 8;
    public static final int INSTANCE_SIZE_BYTES = FLOAT_SIZE_BYTES * 2;
    public static final int INSTANCE_SIZE_FLOATS = 2;
    public static final int INSTANCE_SIZE_FLOATS_SELDOM = MATRIX_SIZE_FLOATS + 8;
    public static int vboSizeMesh = 2;
    public final int instanceDataVBO1;
    public final int instanceDataVBO2;
    public int numInstances;
    public FloatBuffer instanceDataBufferVBO1;
    public FloatBuffer instanceDataBufferVBO2;
    public FloatBuffer instanceDataBufferSeldom2;
    public int curBufferPosVBO1 = 0;
    public int curBufferPosVBO2 = 0;
    public int curBufferPosVBO2Thread = 0;
    public boolean dirtyVBO2Flag = false;
    public double interpPosX;
    public double interpPosY;
    public double interpPosZ;
    public double interpPosXThread;
    public double interpPosYThread;
    public double interpPosZThread;

    public int lastRemovalCount = 0;
    public int lastAdditionCount = 0;

    public InstancedMeshFoliage(float[] positions, float[] textCoords, int[] indices, int numInstances) {
        super(positions, textCoords, indices);

        this.numInstances = numInstances;

        ShaderManager.glBindVertexArray(vaoId);

        instanceDataVBO1 = GL15.glGenBuffers();
        vboIdList.add(instanceDataVBO1);
        instanceDataBufferVBO1 = BufferUtils.createFloatBuffer(numInstances * INSTANCE_SIZE_FLOATS);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, instanceDataVBO1);
        int start = vboSizeMesh;
        int strideStart = 0;

        ShaderManager.glVertexAttribPointer(start, 2, GL11.GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
        ShaderManager.glVertexAttribDivisor(start, 1);
        start++;
        strideStart += FLOAT_SIZE_BYTES;

        strideStart = 0;

        instanceDataVBO2 = GL15.glGenBuffers();
        vboIdList.add(instanceDataVBO2);
        instanceDataBufferVBO2 = BufferUtils.createFloatBuffer(numInstances * INSTANCE_SIZE_FLOATS_SELDOM);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, instanceDataVBO2);

        for (int i = 0; i < 4; i++) {
            ShaderManager.glVertexAttribPointer(start, 4, GL11.GL_FLOAT, false, INSTANCE_SIZE_BYTES_SELDOM, strideStart);
            ShaderManager.glVertexAttribDivisor(start, 1);
            start++;
            strideStart += VECTOR4F_SIZE_BYTES;
        }

        ShaderManager.glVertexAttribPointer(start, 4, GL11.GL_FLOAT, false, INSTANCE_SIZE_BYTES_SELDOM, strideStart);
        ShaderManager.glVertexAttribDivisor(start, 1);
        start++;
        strideStart += VECTOR4F_SIZE_BYTES;

        ShaderManager.glVertexAttribPointer(start, 4, GL11.GL_FLOAT, false, INSTANCE_SIZE_BYTES_SELDOM, strideStart);
        ShaderManager.glVertexAttribDivisor(start, 1);
        start++;
        strideStart += VECTOR4F_SIZE_BYTES;

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        ShaderManager.glBindVertexArray(0);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        if (this.instanceDataBufferVBO1 != null) {
            this.instanceDataBufferVBO1 = null;
        }

        if (this.instanceDataBufferVBO2 != null) {
            this.instanceDataBufferVBO2 = null;
        }
    }

    @Override
    public void initRender() {
        super.initRender();
    }

    @Override
    public void endRender() {
        super.endRender();
    }

    public int getAttribSizeVBO1() {
        return 1;
    }

    public int getAttribSizeVBO2() {
        return 6;
    }

    public void initRenderVBO1() {
        int start = vboSizeMesh;
        for (int i = 0; i < getAttribSizeVBO1(); i++) {
            ShaderManager.glEnableVertexAttribArray(start + i);
        }
    }

    public void endRenderVBO1() {
        int start = vboSizeMesh;
        for (int i = 0; i < getAttribSizeVBO1(); i++) {
            ShaderManager.glDisableVertexAttribArray(start + i);
        }
    }

    public void initRenderVBO2() {
        int start = vboSizeMesh;
        for (int i = 0; i < getAttribSizeVBO2(); i++) {
            ShaderManager.glEnableVertexAttribArray(start + getAttribSizeVBO1() + i);
        }
    }

    public void endRenderVBO2() {
        int start = vboSizeMesh;
        for (int i = 0; i < getAttribSizeVBO2(); i++) {
            ShaderManager.glDisableVertexAttribArray(start + getAttribSizeVBO1() + i);
        }
    }
}