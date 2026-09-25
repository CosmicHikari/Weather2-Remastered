package net.corosus.extendedrenderer.shader;

import net.corosus.extendedrenderer.particle.ShaderManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

public abstract class ShaderProgram {

    private final String name;
    private final int programId;
    public Map<String, Integer> uniforms;
    private int vertexShaderId;
    private int fragmentShaderId;

    public ShaderProgram(String name) throws Exception {
        this.name = name;
        programId = GL20.glCreateProgram();
        if (programId == 0) {
            throw new Exception("Could not create Shader");
        }
        uniforms = new HashMap<>();
    }

    public void createUniform(String uniformName) throws Exception {
        int uniformLocation = GL20.glGetUniformLocation(programId, uniformName);
        if (uniformLocation < 0) {
            System.out.println("!!!!!!!! GLSL OPTIMIZATION WARNING MAYBE: Could not find uniform: " + uniformName);
        }
        uniforms.put(uniformName, uniformLocation);
    }

    public void setUniform(String uniformName, Matrix4fe value) {
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        value.get(fb);
        GL20.glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
    }

    public void setUniformEfficient(String uniformName, Matrix4fe value, FloatBuffer buffer) {
        value.get(buffer);
        GL20.glUniformMatrix4fv(uniforms.get(uniformName), false, buffer);
    }

    public void setUniform(String uniformName, int value) {
        GL20.glUniform1i(uniforms.get(uniformName), value);
    }

    public void setUniform(String uniformName, float x, float y, float z, float w) {
        GL20.glUniform4f(uniforms.get(uniformName), x, y, z, w);
    }

    public void setUniform(String uniformName, float value) {
        GL20.glUniform1f(uniforms.get(uniformName), value);
    }

    public void createVertexShader(String shaderCode) throws Exception {
        vertexShaderId = createShader(shaderCode, GL20.GL_VERTEX_SHADER);
    }

    public void createFragmentShader(String shaderCode) throws Exception {
        fragmentShaderId = createShader(shaderCode, GL20.GL_FRAGMENT_SHADER);
    }


    protected int createShader(String shaderCode, int shaderType) throws Exception {
        int shaderId = GL20.glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new Exception("Error creating shader. Type: " + shaderType);
        }

        ShaderManager.glShaderSource(shaderId, shaderCode);
        GL20.glCompileShader(shaderId);

        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == 0) {
            throw new Exception("Error compiling Shader code: " + GL20.glGetShaderInfoLog(shaderId, 1024));
        }

        GL20.glAttachShader(programId, shaderId);

        if (shaderType == GL20.GL_VERTEX_SHADER) {
            setupAttribLocations();
        }

        return shaderId;
    }

    public abstract void setupAttribLocations();

    public void link() throws Exception {
        GL20.glLinkProgram(programId);
        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking Shader code: " + GL20.glGetProgramInfoLog(programId, 1024));
        }

        if (vertexShaderId != 0) {
            GL20.glDetachShader(programId, vertexShaderId);
        }
        if (fragmentShaderId != 0) {
            GL20.glDetachShader(programId, fragmentShaderId);
        }

        GL20.glValidateProgram(programId);
        if (GL20.glGetProgrami(programId, GL20.GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating Shader code: " + GL20.glGetProgramInfoLog(programId, 1024));
        }
    }

    public void bind() {
        GL20.glUseProgram(programId);
    }

    public void unbind() {
        GL20.glUseProgram(0);
    }

    public void cleanup() {
        unbind();
        if (programId != 0) {
            GL20.glDeleteProgram(programId);
        }
    }

    public String getName() {
        return this.name;
    }

    public int getProgramId() {
        return programId;
    }
}