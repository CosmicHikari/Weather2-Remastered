package net.corosus.extendedrenderer.shader;

import net.corosus.extendedrenderer.particle.ShaderManager;

public class ShaderProgramParticle extends ShaderProgram {

    private final int vertexShaderAttributePosition = 0;
    private final int vertexShaderAttributeTexCoord = 1;
    private final int vertexShaderAttributeModelMatrix = InstancedMeshParticle.vboSizeMesh;
    private final int vertexShaderAttributeBrightness = InstancedMeshParticle.vboSizeMesh + 4;
    private final int vertexShaderAttributeRGBA = InstancedMeshParticle.vboSizeMesh + 5;

    public ShaderProgramParticle(String name) throws Exception {
        super(name);
    }

    @Override
    public void setupAttribLocations() {
        ShaderManager.glBindAttribLocation(getProgramId(), vertexShaderAttributePosition, "position");
        ShaderManager.glBindAttribLocation(getProgramId(), vertexShaderAttributeTexCoord, "texCoord");
        ShaderManager.glBindAttribLocation(getProgramId(), vertexShaderAttributeModelMatrix, "modelMatrix");
        ShaderManager.glBindAttribLocation(getProgramId(), vertexShaderAttributeBrightness, "brightness");
        ShaderManager.glBindAttribLocation(getProgramId(), vertexShaderAttributeRGBA, "rgba");
    }
}