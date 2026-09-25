package net.corosus.extendedrenderer.shader;

import java.io.InputStream;
import java.util.Scanner;

public class Utils {

    public static String loadResource(String fileName) throws Exception {
        String result;
        try (InputStream in = Utils.class.getClass().getResourceAsStream(fileName);
             Scanner scanner = new Scanner(in, "UTF-8")) {
            result = scanner.useDelimiter("\\A").next();
        }
        return result;
    }

    public static Matrix4fe setPerspective(float fovy, float aspect, float zNear, float zFar, boolean zZeroToOne) {
        Matrix4fe mat = new Matrix4fe();
        float h = (float) Math.tan(fovy * 0.5F);
        mat.m00 = (1.0F / (h * aspect));
        mat.m11 = (1.0F / h);
        boolean farInf = zFar > 0.0F && Float.isInfinite(zFar);
        boolean nearInf = zNear > 0.0F && Float.isInfinite(zNear);
        float e;
        if (farInf) {
            e = 1.0E-6F;
            mat.m22 = (e - 1.0F);
            mat.m32 = ((e - (zZeroToOne ? 1.0F : 2.0F)) * zNear);
        } else if (nearInf) {
            e = 1.0E-6F;
            mat.m22 = ((zZeroToOne ? 0.0F : 1.0F) - e);
            mat.m32 = (((zZeroToOne ? 1.0F : 2.0F) - e) * zFar);
        } else {
            mat.m22 = ((zZeroToOne ? zFar : zFar + zNear) / (zNear - zFar));
            mat.m32 = ((zZeroToOne ? zFar : zFar + zFar) * zNear / (zNear - zFar));
        }
        mat.m23 = (-1.0F);
        return mat;
    }

    public static Matrix4fe perspective(float fovy, float aspect, float near, float far) {
        Matrix4fe perspective = new Matrix4fe();
        float f = (float) (1f / Math.tan(Math.toRadians(fovy) / 2f));
        perspective.m00 = f / aspect;
        perspective.m11 = f;
        perspective.m22 = (far + near) / (near - far);
        perspective.m32 = -1f;
        perspective.m23 = (2f * far * near) / (near - far);
        perspective.m33 = 0f;
        return perspective;
    }

}