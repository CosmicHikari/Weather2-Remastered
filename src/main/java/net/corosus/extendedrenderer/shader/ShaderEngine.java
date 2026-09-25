package net.corosus.extendedrenderer.shader;

public class ShaderEngine {

    public static Renderer renderer;

    public static boolean init() {
        try {
            renderer = new Renderer();
            renderer.init();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static void cleanup() {
        if (renderer != null) renderer.cleanup();
        MeshBufferManagerParticle.cleanup();
        MeshBufferManagerFoliage.cleanup();
    }
}