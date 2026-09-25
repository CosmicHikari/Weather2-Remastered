package net.corosus.extendedrenderer.shader;

import net.corosus.extendedrenderer.particle.ShaderManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Mesh {

    public static final int MAX_WEIGHTS = 4;
    public static int extraRenders = 10;
    protected int vaoId;
    protected List<Integer> vboIdList = new ArrayList<>();
    private int vertexCount;

    public Mesh(float[] positions, float[] textCoords, int[] indices) {
        vertexCount = indices.length;

        vaoId = ShaderManager.glGenVertexArrays();
        ShaderManager.glBindVertexArray(vaoId);

        int posVboId = GL15.glGenBuffers();
        vboIdList.add(posVboId);
        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(positions.length);
        verticesBuffer.put(positions).flip();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, posVboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);
        ShaderManager.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);

        int texVboId = GL15.glGenBuffers();
        vboIdList.add(texVboId);
        FloatBuffer textCoordsBuffer = BufferUtils.createFloatBuffer(textCoords.length);
        textCoordsBuffer.put(textCoords).flip();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, texVboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, textCoordsBuffer, GL15.GL_STATIC_DRAW);
        ShaderManager.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 0, 0);

        int idxVboId = GL15.glGenBuffers();
        vboIdList.add(idxVboId);
        IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indices.length);
        indicesBuffer.put(indices).flip();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, idxVboId);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        ShaderManager.glBindVertexArray(0);
    }

    protected static float[] createEmptyFloatArray(int length, float defaultValue) {
        float[] result = new float[length];
        Arrays.fill(result, defaultValue);
        return result;
    }

    protected static int[] createEmptyIntArray(int length, int defaultValue) {
        int[] result = new int[length];
        Arrays.fill(result, defaultValue);
        return result;
    }

    protected void initRender() {
        ShaderManager.glBindVertexArray(getVaoId());
        ShaderManager.glEnableVertexAttribArray(0);
        ShaderManager.glEnableVertexAttribArray(1);
    }

    protected void endRender() {
        ShaderManager.glDisableVertexAttribArray(0);
        ShaderManager.glDisableVertexAttribArray(1);
        ShaderManager.glBindVertexArray(0);
    }

    public void render() {
        initRender();
        endRender();
    }

    public int getVaoId() {
        return vaoId;
    }

    public void setVaoId(int vaoId) {
        this.vaoId = vaoId;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void setVertexCount(int vertexCount) {
        this.vertexCount = vertexCount;
    }

    public void cleanup() {
        ShaderManager.glDisableVertexAttribArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        for (int vboId : vboIdList) {
            GL15.glDeleteBuffers(vboId);
        }
        ShaderManager.glBindVertexArray(0);
        ShaderManager.glDeleteVertexArrays(vaoId);
    }
}