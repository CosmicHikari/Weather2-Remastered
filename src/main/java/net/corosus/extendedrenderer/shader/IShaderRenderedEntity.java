package net.corosus.extendedrenderer.shader;

import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

public interface IShaderRenderedEntity {

    Vector3f getPosition();

    Quaternion getQuaternion();

    Quaternion getQuaternionPrev();

    float getScale();

}