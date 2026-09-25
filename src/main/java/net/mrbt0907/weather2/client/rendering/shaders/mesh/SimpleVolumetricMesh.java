package net.mrbt0907.weather2.client.rendering.shaders.mesh;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

public class SimpleVolumetricMesh {
    public final boolean vbo_enabled;
    public final int vbo_id;

    public final int length;
    public final int quality;

    public SimpleVolumetricMesh(int quality) {
        FloatBuffer buffer = SimpleVolumetricMesh.createVertices(quality);
        length = buffer.limit() / 3;
        this.quality = quality;


        vbo_id = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo_id);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        vbo_enabled = true;
    }

    protected static FloatBuffer createVertices(int quality) {
        int lat_bands = quality, long_bands = quality;
        FloatBuffer buffer = BufferUtils.createFloatBuffer(lat_bands * long_bands * 6 * 3);
        double theta1, theta2, phi1, phi2, lat_pi = Math.PI / lat_bands, long_pi = (Math.PI / long_bands) * 2;

        for (int lat = 0; lat < lat_bands; lat++) {
            theta1 = lat * lat_pi;
            theta2 = (lat + 1) * lat_pi;

            for (int lon = 0; lon < long_bands; lon++) {
                phi1 = lon * long_pi;
                phi2 = (lon + 1) * long_pi;

                float[] vertex_1 = SimpleVolumetricMesh.sphericalToCartesian(theta1, phi1);
                float[] vertex_2 = SimpleVolumetricMesh.sphericalToCartesian(theta2, phi1);
                float[] vertex_3 = SimpleVolumetricMesh.sphericalToCartesian(theta2, phi2);
                float[] vertex_4 = SimpleVolumetricMesh.sphericalToCartesian(theta1, phi2);


                buffer.put(vertex_1);
                buffer.put(vertex_2);
                buffer.put(vertex_3);

                buffer.put(vertex_3);
                buffer.put(vertex_4);
                buffer.put(vertex_1);
            }
        }


        buffer.flip();
        return buffer;
    }

    public static float[] sphericalToCartesian(double theta, double phi) {
        return new float[]{
                (float) (Math.sin(theta) * Math.cos(phi)),
                (float) Math.cos(theta),
                (float) (Math.sin(theta) * Math.sin(phi))
        };
    }

    public void bindVBO() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo_id);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
    }

    public void unbindVBO() {
        GL20.glDisableVertexAttribArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void delete() {
        if (vbo_enabled)
            GL15.glDeleteBuffers(vbo_id);
    }
}