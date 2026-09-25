package net.corosus.coroutillegacy.util;

import net.corosus.coroutillegacy.util.MMCQ.CMap;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Arrays;

public class ColorThief {


    public static int[][] getPalette(
            BufferedImage sourceImage,
            int colorCount,
            int quality,
            boolean ignoreWhite) {
        CMap cmap = getColorMap(sourceImage, colorCount, quality, ignoreWhite);
        if (cmap == null) {
            return null;
        }
        return cmap.palette();
    }


    public static CMap getColorMap(
            BufferedImage sourceImage,
            int colorCount,
            int quality,
            boolean ignoreWhite) {
        if (quality < 1) {
            throw new IllegalArgumentException("Specified quality should be greater then 0.");
        }

        int[][] pixelArray;

        switch (sourceImage.getType()) {
            case BufferedImage.TYPE_3BYTE_BGR:
            case BufferedImage.TYPE_4BYTE_ABGR:
                pixelArray = getPixelsFast(sourceImage, quality, ignoreWhite);
                break;

            default:
                pixelArray = getPixelsSlow(sourceImage, quality, ignoreWhite);
        }


        CMap cmap = MMCQ.quantize(pixelArray, colorCount);
        return cmap;
    }


    private static int[][] getPixelsFast(
            BufferedImage sourceImage,
            int quality,
            boolean ignoreWhite) {
        DataBufferByte imageData = (DataBufferByte) sourceImage.getRaster().getDataBuffer();
        byte[] pixels = imageData.getData();
        int pixelCount = sourceImage.getWidth() * sourceImage.getHeight();

        int colorDepth;
        int type = sourceImage.getType();
        switch (type) {
            case BufferedImage.TYPE_3BYTE_BGR:
                colorDepth = 3;
                break;

            case BufferedImage.TYPE_4BYTE_ABGR:
                colorDepth = 4;
                break;

            default:
                throw new IllegalArgumentException("Unhandled type: " + type);
        }

        int expectedDataLength = pixelCount * colorDepth;
        if (expectedDataLength != pixels.length) {
            throw new IllegalArgumentException(
                    "(expectedDataLength = " + expectedDataLength + ") != (pixels.length = "
                            + pixels.length + ")");
        }


        int numRegardedPixels = (pixelCount + quality - 1) / quality;

        int numUsedPixels = 0;
        int[][] pixelArray = new int[numRegardedPixels][];
        int offset, r, g, b, a;


        switch (type) {
            case BufferedImage.TYPE_3BYTE_BGR:
                for (int i = 0; i < pixelCount; i += quality) {
                    offset = i * 3;
                    b = pixels[offset] & 0xFF;
                    g = pixels[offset + 1] & 0xFF;
                    r = pixels[offset + 2] & 0xFF;


                    if (!(ignoreWhite && r > 250 && g > 250 && b > 250)) {
                        pixelArray[numUsedPixels] = new int[]{r, g, b};
                        numUsedPixels++;
                    }
                }
                break;

            case BufferedImage.TYPE_4BYTE_ABGR:
                for (int i = 0; i < pixelCount; i += quality) {
                    offset = i * 4;
                    a = pixels[offset] & 0xFF;
                    b = pixels[offset + 1] & 0xFF;
                    g = pixels[offset + 2] & 0xFF;
                    r = pixels[offset + 3] & 0xFF;


                    if (a >= 125 && !(ignoreWhite && r > 250 && g > 250 && b > 250)) {
                        pixelArray[numUsedPixels] = new int[]{r, g, b};
                        numUsedPixels++;
                    }
                }
                break;

            default:
                throw new IllegalArgumentException("Unhandled type: " + type);
        }


        return Arrays.copyOfRange(pixelArray, 0, numUsedPixels);
    }


    private static int[][] getPixelsSlow(
            BufferedImage sourceImage,
            int quality,
            boolean ignoreWhite) {
        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();

        int pixelCount = width * height;


        int numRegardedPixels = (pixelCount + quality - 1) / quality;

        int numUsedPixels = 0;

        int[][] res = new int[numRegardedPixels][];
        int r, g, b;

        for (int i = 0; i < pixelCount; i += quality) {
            int row = i / width;
            int col = i % width;
            int rgb = sourceImage.getRGB(col, row);

            r = (rgb >> 16) & 0xFF;
            g = (rgb >> 8) & 0xFF;
            b = (rgb) & 0xFF;
            if (!(ignoreWhite && r > 250 && g > 250 && b > 250)) {
                res[numUsedPixels] = new int[]{r, g, b};
                numUsedPixels++;
            }
        }

        return Arrays.copyOfRange(res, 0, numUsedPixels);
    }

}