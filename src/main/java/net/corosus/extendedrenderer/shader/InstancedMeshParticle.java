package net.corosus.extendedrenderer.shader;

import net.corosus.extendedrenderer.particle.ShaderManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import java.nio.FloatBuffer;

public class InstancedMeshParticle extends Mesh {

    public static final int FLOAT_SIZE_BYTES = 4;
    public static final int VECTOR4F_SIZE_BYTES = 4 * FLOAT_SIZE_BYTES;
    public static final int MATRIX_SIZE_FLOATS = 4 * 4;
    public static final int MATRIX_SIZE_BYTES = MATRIX_SIZE_FLOATS * FLOAT_SIZE_BYTES;
    public static final int INSTANCE_SIZE_BYTES = MATRIX_SIZE_BYTES + FLOAT_SIZE_BYTES * (1 + 4);
    public static final int INSTANCE_SIZE_FLOATS = MATRIX_SIZE_FLOATS + 1 + 4;

    public static final int INSTANCE_SIZE_BYTES_TEST = FLOAT_SIZE_BYTES * 4;
    public static final int INSTANCE_SIZE_FLOATS_TEST = 4;
    public static int vboSizeMesh = 2;
    public final int numInstances;
    public final int instanceDataVBO;
    public FloatBuffer instanceDataBuffer;
    public int curBufferPos = 0;

    public InstancedMeshParticle(float[] positions, float[] textCoords, int[] indices, int numInstances) {
        super(positions, textCoords, indices);
        this.numInstances = numInstances;

        ShaderManager.glBindVertexArray(vaoId);

        instanceDataVBO = GL15.glGenBuffers();
        vboIdList.add(instanceDataVBO);
        instanceDataBuffer = BufferUtils.createFloatBuffer(numInstances * INSTANCE_SIZE_FLOATS);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, instanceDataVBO);

        int start = vboSizeMesh;
        int strideStart = 0;

        for (int i = 0; i < 4; i++) {
            ShaderManager.glVertexAttribPointer(start, 4, GL11.GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
            ShaderManager.glVertexAttribDivisor(start, 1);
            start++;
            strideStart += VECTOR4F_SIZE_BYTES;
        }

        ShaderManager.glVertexAttribPointer(start, 1, GL11.GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
        ShaderManager.glVertexAttribDivisor(start, 1);
        start++;
        strideStart += FLOAT_SIZE_BYTES;

        ShaderManager.glVertexAttribPointer(start, 4, GL11.GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
        ShaderManager.glVertexAttribDivisor(start, 1);
        start++;
        strideStart += VECTOR4F_SIZE_BYTES;

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        ShaderManager.glBindVertexArray(0);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        if (this.instanceDataBuffer != null) {
            this.instanceDataBuffer = null;
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

    public void initRenderVBO1() {
        int start = vboSizeMesh;
        int numElements = 4 + 1 + 1;
        for (int i = 0; i < numElements; i++) {
            ShaderManager.glEnableVertexAttribArray(start + i);
        }
    }

    public void endRenderVBO1() {
        int start = vboSizeMesh;
        int numElements = 4 + 1 + 1;
        for (int i = 0; i < numElements; i++) {
            ShaderManager.glDisableVertexAttribArray(start + i);
        }
    }
}