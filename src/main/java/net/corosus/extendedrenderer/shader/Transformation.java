package net.corosus.extendedrenderer.shader;

import net.corosus.coroutillegacy.util.CoroUtilMath;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

public class Transformation {

    private final Matrix4fe modelViewMatrix;
    public Matrix4fe modelMatrix;

    public Transformation() {
        modelViewMatrix = new Matrix4fe();
        modelMatrix = new Matrix4fe();
    }

    public Matrix4fe buildModelViewMatrix(Matrix4fe modelMatrix, Matrix4fe viewMatrix) {
        return viewMatrix.mulAffine(modelMatrix, modelViewMatrix);
    }

    public Matrix4fe buildModelMatrix(IShaderRenderedEntity gameItem, Vector3f posCustom, float partialTicks) {
        Quaternion q = gameItem.getQuaternion();
        if (gameItem.getQuaternionPrev() != null) {
            q = CoroUtilMath.interpolate(gameItem.getQuaternionPrev(), gameItem.getQuaternion(), partialTicks);
        }

        float scaleAdj = gameItem.getScale() * 0.2F;

        Vector3f vecPos = posCustom != null ? posCustom : gameItem.getPosition();

        return modelMatrix.translationRotateScale(
                vecPos.x(), vecPos.y(), vecPos.z(),
                q.i(), q.j(), q.k(), q.r(),
                scaleAdj, scaleAdj, scaleAdj);
    }
}