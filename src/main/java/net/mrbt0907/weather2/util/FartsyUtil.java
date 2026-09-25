package net.mrbt0907.weather2.util;

public class FartsyUtil {
    //Gets the square root of a floating-point number, C++ style.
    public static float sqrtf(float x) {
        if (x < 0f) return Float.NaN;
        if (x == 0f || x == Float.POSITIVE_INFINITY) return x;

        int i = Float.floatToIntBits(x);
        i = (1 << 29) + (i >> 1) - (1 << 22);
        float y = Float.intBitsToFloat(i);
        // One or two Newton-Raphson iterations for better accuracy
        y = 0.5f * (y + x / y);
        y = 0.5f * (y + x / y);

        return y;
    }
}