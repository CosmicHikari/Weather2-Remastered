package net.corosus.coroutillegacy.util;

import it.unimi.dsi.fastutil.ints.IntArrays;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.mrbt0907.weather2.mixins.accessor.TextureAtlasSpriteAccessor;

import java.awt.image.BufferedImage;

public class CoroUtilColor {

    public static int[] getColors(BlockState state) {
        IBakedModel model = Minecraft.getInstance()
                .getBlockRenderer()
                .getBlockModelShaper()
                .getBlockModel(state);

        if (model != null) {
            TextureAtlasSprite sprite = model.getParticleIcon();
            TextureAtlasSprite missing = Minecraft.getInstance()
                    .getModelManager()
                    .getMissingModel()
                    .getParticleIcon();

            if (sprite != null && sprite != missing) {
                return getColors(sprite);
            }
        }
        return IntArrays.EMPTY_ARRAY;
    }

    public static int[] getColors(TextureAtlasSprite sprite) {
        if (sprite == null) return IntArrays.EMPTY_ARRAY;

        int width = sprite.getWidth();
        int height = sprite.getHeight();
        int frames = sprite.getFrameCount();

        BufferedImage img = new BufferedImage(width, height * frames,
                BufferedImage.TYPE_4BYTE_ABGR);

        NativeImage[] images = ((TextureAtlasSpriteAccessor) sprite).getMainImage();
        if (images == null || images.length == 0) return IntArrays.EMPTY_ARRAY;

        NativeImage nativeImage = images[0];
        for (int i = 0; i < frames; i++) {
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int raw = nativeImage.getPixelRGBA(x, y);

                    int a = (raw >> 24) & 0xFF;
                    int b = (raw >> 16) & 0xFF;
                    int g = (raw >> 8) & 0xFF;
                    int r = (raw) & 0xFF;
                    pixels[y * width + x] = (a << 24) | (r << 16) | (g << 8) | b;
                }
            }
            img.setRGB(0, i * height, width, height, pixels, 0, width);
        }

        int[][] colorData = ColorThief.getPalette(img, 6, 5, true);
        if (colorData != null) {
            int[] ret = new int[colorData.length];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = getColor(colorData[i]);
            }
            return ret;
        }
        return IntArrays.EMPTY_ARRAY;
    }

    private static int getColor(int[] colorData) {
        return 0xFF000000
                | (colorData[0] << 16)
                | (colorData[1] << 8)
                | colorData[2];
    }
}