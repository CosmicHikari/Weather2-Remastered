package net.mrbt0907.weather2.client.rendering.shaders;

import net.mrbt0907.weather2.Weather2;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.nio.charset.StandardCharsets;


public class VolumetricsShader {

    public final int program_id;

    public final int fragment_id;

    public final int vertex_id;

    public final boolean valid;

    public VolumetricsShader(String vertex_path, String fragment_path) {
        vertex_id = VolumetricsShader.loadShader(vertex_path, GL20.GL_VERTEX_SHADER);
        fragment_id = VolumetricsShader.loadShader(fragment_path, GL20.GL_FRAGMENT_SHADER);

        if (vertex_id < 0 || fragment_id < 0) {
            program_id = -1;
            valid = false;
            return;
        }

        program_id = GL20.glCreateProgram();
        GL20.glAttachShader(program_id, fragment_id);
        GL20.glAttachShader(program_id, vertex_id);
        GL20.glLinkProgram(program_id);

        if (GL20.glGetProgrami(program_id, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            Weather2.warn("Shader program " + program_id + " failed to link successfully. Skipping shader loading...");
            Weather2.warn(GL20.glGetProgramInfoLog(program_id, 1024));
            valid = false;
            deleteShader();
            return;
        }
        valid = true;
    }

    protected static int loadShader(String path, int type) {
        String shader_contents = null;
        try {
            shader_contents = IOUtils.toString(VolumetricsShader.class.getResourceAsStream(path), StandardCharsets.UTF_8);
        } catch (Exception e) {
            Weather2.error(e);
            return -1;
        }

        int shader_id = GL20.glCreateShader(type);
        GL20.glShaderSource(shader_id, shader_contents);
        GL20.glCompileShader(shader_id);

        if (GL20.glGetShaderi(shader_id, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            Weather2.warn("Shader " + path + " failed to compile successfully. Skipping shader loading...");
            Weather2.warn(GL20.glGetShaderInfoLog(shader_id, 1024));
            GL20.glDeleteShader(shader_id);
            return -1;
        }
        return shader_id;
    }

    public void startShader() {
        GL20.glUseProgram(program_id);
    }

    public void stopShader() {
        GL20.glUseProgram(0);
    }

    public void deleteShader() {
        GL20.glDetachShader(program_id, fragment_id);
        GL20.glDetachShader(program_id, vertex_id);
        GL20.glDeleteProgram(program_id);
        GL20.glDeleteShader(fragment_id);
        GL20.glDeleteShader(vertex_id);
    }

    public int getParameter(String name) {
        return GL20.glGetUniformLocation(program_id, name);
    }
}