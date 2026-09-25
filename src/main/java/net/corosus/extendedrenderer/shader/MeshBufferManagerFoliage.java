package net.corosus.extendedrenderer.shader;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.HashMap;
import java.util.Map;

public class MeshBufferManagerFoliage {

    public static int numInstances = 10000;

    public static HashMap<TextureAtlasSprite, InstancedMeshFoliage> lookupParticleToMesh = new HashMap<>();

    public static void setupMeshForParticle(TextureAtlasSprite sprite) {

        float[] positions = new float[]{
                -0.5f, 0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                0.5f, 0.5f, 0.0f
        };

        float[] texCoords = new float[]{
                sprite.getU0(), sprite.getV0(),
                sprite.getU0(), sprite.getV1(),
                sprite.getU1(), sprite.getV1(),
                sprite.getU1(), sprite.getV0()
        };

        int[] indices = new int[]{
                0, 1, 3, 3, 1, 2
        };

        InstancedMeshFoliage mesh = new InstancedMeshFoliage(positions, texCoords, indices, numInstances);

        if (!lookupParticleToMesh.containsKey(sprite)) {
            lookupParticleToMesh.put(sprite, mesh);
        } else {
            System.out.println("WARNING: duplicate entry attempt for particle sprite: " + sprite);
        }
    }

    public static void cleanup() {
        for (Map.Entry<TextureAtlasSprite, InstancedMeshFoliage> entry : lookupParticleToMesh.entrySet()) {
            entry.getValue().cleanup();
        }
        lookupParticleToMesh.clear();
    }

    public static void clearMapOnly() {
        lookupParticleToMesh.clear();
    }

    public static InstancedMeshFoliage getMesh(TextureAtlasSprite sprite) {
        return lookupParticleToMesh.get(sprite);
    }

    public static void setupMeshIfMissing(TextureAtlasSprite sprite) {
        if (!lookupParticleToMesh.containsKey(sprite)) {
            setupMeshForParticle(sprite);
        }
    }
}