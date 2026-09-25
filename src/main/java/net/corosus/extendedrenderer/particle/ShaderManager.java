package net.corosus.extendedrenderer.particle;

import net.corosus.coroutillegacy.forge.CULog;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;

public class ShaderManager {

    private static boolean check = true;

    private static boolean canUseShaders = false;
    private static boolean canUseShadersInstancedRendering = false;

    private static boolean useARBInstancedRendering = false;
    private static boolean useARBShaders = false;
    private static boolean useARBVBO = false;
    private static boolean useARBVAO = false;
    private static boolean useARBInstancedArrays = false;

    public static boolean canUseShadersInstancedRendering() {
        if (check) {
            check = false;
            queryGLCaps();
        }
        return canUseShadersInstancedRendering;
    }

    public static void disableShaders() {
        canUseShaders = false;
        canUseShadersInstancedRendering = false;
    }

    public static void queryGLCaps() {
        GLCapabilities caps = GL.getCapabilities();

        CULog.log("Extended Renderer: Detected GLSL version: "
                + GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION));

        useARBVBO = !caps.OpenGL15 && caps.GL_ARB_vertex_buffer_object;

        if (caps.OpenGL21 ||
                (caps.GL_ARB_vertex_shader && caps.GL_ARB_fragment_shader && caps.GL_ARB_shader_objects)) {
            canUseShaders = true;
            useARBShaders = !caps.OpenGL21;

            if (caps.OpenGL33 ||
                    (caps.GL_ARB_draw_instanced && caps.GL_ARB_instanced_arrays && caps.GL_ARB_vertex_array_object)) {
                canUseShadersInstancedRendering = true;

                if (caps.OpenGL33) {
                    useARBInstancedRendering = false;
                    useARBInstancedArrays = false;
                    useARBVAO = false;
                } else {
                    useARBInstancedRendering = true;
                    useARBInstancedArrays = true;
                    useARBVAO = true;
                }
            } else {
                CULog.log("Extended Renderer WARNING: Unable to use instanced rendering shaders, OpenGL33: "
                        + caps.OpenGL33
                        + ", (GL_ARB_draw_instanced: " + caps.GL_ARB_draw_instanced
                        + ", GL_ARB_instanced_arrays: " + caps.GL_ARB_instanced_arrays
                        + ", GL_ARB_vertex_array_object: " + caps.GL_ARB_vertex_array_object + ")");
                canUseShadersInstancedRendering = false;
            }
        } else {
            CULog.log("Extended Renderer WARNING: Unable to use shaders, OpenGL21: " + caps.OpenGL21
                    + ", (GL_ARB_vertex_shader: " + caps.GL_ARB_vertex_shader
                    + ", GL_ARB_fragment_shader: " + caps.GL_ARB_fragment_shader
                    + ", GL_ARB_shader_objects: " + caps.GL_ARB_shader_objects + ")");
            canUseShadersInstancedRendering = false;
        }
    }

    public static void glDrawElementsInstanced(int mode, int indices_count, int type,
                                               long indices_buffer_offset, int primcount) {
        if (useARBInstancedRendering) {
            ARBDrawInstanced.glDrawElementsInstancedARB(mode, indices_count, type,
                    indices_buffer_offset, primcount);
        } else {
            GL31.glDrawElementsInstanced(mode, indices_count, type, indices_buffer_offset, primcount);
        }
    }

    public static void glShaderSource(int shader, CharSequence string) {
        if (useARBShaders) {
            ARBShaderObjects.glShaderSourceARB(shader, string);
        } else {
            GL20.glShaderSource(shader, string);
        }
    }

    public static void glBindAttribLocation(int program, int index, CharSequence name) {
        if (useARBShaders) {
            ARBVertexShader.glBindAttribLocationARB(program, index, name);
        } else {
            GL20.glBindAttribLocation(program, index, name);
        }
    }

    public static void glVertexAttribPointer(int index, int size, int type, boolean normalized,
                                             int stride, long buffer_offset) {
        if (useARBShaders) {
            ARBVertexShader.glVertexAttribPointerARB(index, size, type, normalized, stride, buffer_offset);
        } else {
            GL20.glVertexAttribPointer(index, size, type, normalized, stride, buffer_offset);
        }
    }

    public static void glEnableVertexAttribArray(int index) {
        if (useARBShaders) {
            ARBVertexShader.glEnableVertexAttribArrayARB(index);
        } else {
            GL20.glEnableVertexAttribArray(index);
        }
    }

    public static void glDisableVertexAttribArray(int index) {
        if (useARBShaders) {
            ARBVertexShader.glDisableVertexAttribArrayARB(index);
        } else {
            GL20.glDisableVertexAttribArray(index);
        }
    }

    public static void glBufferData(int target, FloatBuffer data, int usage) {
        if (useARBVBO) {
            ARBVertexBufferObject.glBufferDataARB(target, data, usage);
        } else {
            GL15.glBufferData(target, data, usage);
        }
    }

    /*
    public static void glBufferData(int target, IntBuffer data, int usage) {
        if (useARBVBO) {
            ARBVertexBufferObject.glBufferDataARB(target, data, usage);
        } else {
            GL15.glBufferData(target, data, usage);
        }
    }
     */

    public static void glVertexAttribDivisor(int index, int divisor) {
        if (useARBInstancedArrays) {
            ARBInstancedArrays.glVertexAttribDivisorARB(index, divisor);
        } else {
            GL33.glVertexAttribDivisor(index, divisor);
        }
    }

    public static void glBindVertexArray(int array) {
        if (useARBVAO) {
            ARBVertexArrayObject.glBindVertexArray(array);
        } else {
            GL30.glBindVertexArray(array);
        }
    }

    public static void glDeleteVertexArrays(int array) {
        if (useARBVAO) {
            ARBVertexArrayObject.glDeleteVertexArrays(array);
        } else {
            GL30.glDeleteVertexArrays(array);
        }
    }

    public static int glGenVertexArrays() {
        if (useARBVAO) {
            return ARBVertexArrayObject.glGenVertexArrays();
        } else {
            return GL30.glGenVertexArrays();
        }
    }

    public static void resetCheck() {
        check = true;
    }
}