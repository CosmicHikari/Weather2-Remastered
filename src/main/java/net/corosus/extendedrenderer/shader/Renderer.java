package net.corosus.extendedrenderer.shader;

import net.corosus.coroutillegacy.forge.CoroUtilLegacy;
import net.corosus.coroutillegacy.util.CoroUtilFile;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;


public class Renderer {

    private final HashMap<String, ShaderProgram> lookupNameToProgram = new HashMap<>();


    public Transformation transformation;

    public Renderer() {
        transformation = new Transformation();
    }

    public void init() throws Exception {


        ShaderProgram shaderProgram = new ShaderProgramParticle("particle");

        String vertex = CoroUtilFile.getContentsFromResourceLocation(new ResourceLocation(CoroUtilLegacy.modID, "shaders/particle.vs"));
        String fragment = CoroUtilFile.getContentsFromResourceLocation(new ResourceLocation(CoroUtilLegacy.modID, "shaders/particle.fs"));

        shaderProgram.createVertexShader(vertex);
        shaderProgram.createFragmentShader(fragment);
        shaderProgram.link();

        shaderProgram.createUniform("modelViewMatrixCamera");
        shaderProgram.createUniform("texture_sampler");


        shaderProgram.createUniform("fogmode");
        shaderProgram.createUniform("fogColor");
        shaderProgram.createUniform("fogStart");
        shaderProgram.createUniform("fogEnd");
        shaderProgram.createUniform("fogDensity");


        shaderProgram.createUniform("lightmap_sampler");

        lookupNameToProgram.put(shaderProgram.getName(), shaderProgram);


        shaderProgram = new ShaderProgramFoliage("foliage");

        vertex = CoroUtilFile.getContentsFromResourceLocation(new ResourceLocation(CoroUtilLegacy.modID, "shaders/foliage.vs"));
        fragment = CoroUtilFile.getContentsFromResourceLocation(new ResourceLocation(CoroUtilLegacy.modID, "shaders/foliage.fs"));

        shaderProgram.createVertexShader(vertex);
        shaderProgram.createFragmentShader(fragment);
        shaderProgram.link();

        shaderProgram.createUniform("modelViewMatrixCamera");
        shaderProgram.createUniform("texture_sampler");
        shaderProgram.createUniform("time");
        shaderProgram.createUniform("partialTick");
        shaderProgram.createUniform("windDir");
        shaderProgram.createUniform("windSpeed");


        shaderProgram.createUniform("fogmode");
        shaderProgram.createUniform("fogColor");
        shaderProgram.createUniform("fogStart");
        shaderProgram.createUniform("fogEnd");
        shaderProgram.createUniform("fogDensity");


        shaderProgram.createUniform("lightmap_sampler");

        lookupNameToProgram.put(shaderProgram.getName(), shaderProgram);
    }

    public void clear() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    public void cleanup() {
        for (ShaderProgram shaderProgram : lookupNameToProgram.values()) {
            shaderProgram.cleanup();
        }
        lookupNameToProgram.clear();
    }

    public ShaderProgram getShaderProgram(String name) {
        return lookupNameToProgram.get(name);
    }
}