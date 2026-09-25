package net.corosus.coroutillegacy.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.mrbt0907.weather2.mixins.accessor.LightTextureAccessor;

import java.util.HashMap;


public class CoroUtilBlockLightCache {


    private static final int NUM_X_BITS = 1 + MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(30000000));
    private static final int NUM_Z_BITS = NUM_X_BITS;
    private static final int NUM_Y_BITS = 64 - NUM_X_BITS - NUM_Z_BITS;
    private static final long Y_MASK = (1L << NUM_Y_BITS) - 1L;
    private static final int Y_SHIFT = NUM_Z_BITS;
    private static final int X_SHIFT = Y_SHIFT + NUM_Y_BITS;
    private static final long Z_MASK = (1L << NUM_Z_BITS) - 1L;
    private static final long X_MASK = (1L << NUM_X_BITS) - 1L;
    public static HashMap<Long, Float> lookupPosToBrightness = new HashMap<>();
    public static HashMap<Integer, HashMap<Integer, HashMap<Integer, Float>>> lookupPosToBrightness2 = new HashMap<>();
    public static float brightnessPlayer = 0F;

    public static float getBrightnessCached(World world, float x, float y, float z) {


        boolean crazy = false;

        int xx = MathHelper.floor(x);
        int yy = MathHelper.floor(y);
        int zz = MathHelper.floor(z);

        if (crazy) {
            HashMap<Integer, HashMap<Integer, Float>> xxx = lookupPosToBrightness2.get(xx);
            HashMap<Integer, Float> yyy = null;
            if (xxx != null) {
                yyy = xxx.get(yy);
                if (yyy != null) {
                    Object brightness = yyy.get(zz);
                    if (brightness != null) {
                        return (Float) brightness;
                    }
                }
            }

            float brightnesss = getBrightnessNonLightmap(world, x, y, z);

            if (xxx == null) {
                xxx = new HashMap<>();
            }

            if (yyy == null) {
                yyy = new HashMap<>();
            }


            yyy.put(zz, brightnesss);
            xxx.put(yy, yyy);
            lookupPosToBrightness2.put(xx, xxx);
            return brightnesss;
        } else {
            long hash;


            hash = ((long) xx & X_MASK) << X_SHIFT | ((long) yy & Y_MASK) << Y_SHIFT | ((long) zz & Z_MASK) << 0;


            boolean containsWay = false;
            if (containsWay) {
                if (lookupPosToBrightness.containsKey(hash)) {
                    return lookupPosToBrightness.get(hash);
                } else {
                    float brightnesss = getBrightnessNonLightmap(world, x, y, z);
                    lookupPosToBrightness.put(hash, brightnesss + 0.001F);
                    return brightnesss;
                }
            } else {
                Object brightness = lookupPosToBrightness.get(hash);
                if (brightness != null) {
                    return (Float) brightness;
                } else {
                    float brightnesss = getBrightnessFromLightmap(world, x, y, z);
                    lookupPosToBrightness.put(hash, brightnesss);
                    return brightnesss;
                }
            }
        }
    }

    public static void clear() {
        lookupPosToBrightness.clear();
        lookupPosToBrightness2.clear();
    }

    public static float getBrightnessNonLightmap(World world, float x, float y, float z) {


        float brightnessSky = world.getSunAngle(1F);

        float brightnessBlock = world.getBrightness(LightType.BLOCK, new BlockPos(x, y, z)) / 15F;

        float brightness = brightnessSky;
        if (brightnessBlock > brightnessSky) {
            brightness = brightnessBlock;
        }
        return brightness;

    }

    public static float getBrightnessFromLightmap(World world, float x, float y, float z) {

        BlockPos pos = new BlockPos(x, y, z);
        int i = world.getBrightness(LightType.SKY, pos);
        int j = world.getBrightness(LightType.BLOCK, pos);


        try {
            net.minecraft.client.renderer.LightTexture lightTexture = Minecraft.getInstance().gameRenderer.lightTexture();


            NativeImage lightPixels = ((LightTextureAccessor) lightTexture).getLightPixels();


            int pixelX = j;
            int pixelY = i;


            int argb = lightPixels.getPixelRGBA(pixelX, pixelY);


            int a = (argb >> 24) & 0xFF;
            int b = (argb >> 16) & 0xFF;
            int g = (argb >> 8) & 0xFF;
            int r = argb & 0xFF;


            float brightness = (r * 0.299F + g * 0.587F + b * 0.114F) / 255.0F;

            return brightness;

        } catch (Exception e) {
            e.printStackTrace();

            return Math.max(i, j) / 15F;
        }

    }

}