package net.corosus.extendedrenderer.shader;

import net.corosus.extendedrenderer.particle.ShaderManager;

public class ShaderProgramFoliage extends ShaderProgram {

    public ShaderProgramFoliage(String name) throws Exception {
        super(name);
    }


    @Override
    public void setupAttribLocations() {
        int id = getProgramId();
        ShaderManager.glBindAttribLocation(id, 0, "position");
        ShaderManager.glBindAttribLocation(id, 1, "texCoord");
        ShaderManager.glBindAttribLocation(id, 2, "alphaBrightness");
        ShaderManager.glBindAttribLocation(id, 3, "modelMatrix");
        ShaderManager.glBindAttribLocation(id, 7, "rgba");
        ShaderManager.glBindAttribLocation(id, 8, "meta");

    }


    @Override
    public void link() throws Exception {
        super.link();
        createUniforms();
    }

    private void createUniforms() throws Exception {

        createUniform("modelViewMatrixCamera");
        createUniform("time");
        createUniform("partialTick");
        createUniform("windDir");
        createUniform("windSpeed");


        createUniform("texture_sampler");
        createUniform("fogmode");


        createUniform("fogStart");
        createUniform("fogEnd");
        createUniform("fogScale");
        createUniform("fogDensity");
        createUniform("fogColor");
    }
}