/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  android.graphics.Bitmap
 *  android.graphics.Color
 *  android.util.SparseArray
 */
package naipingzai.materialfile.lib.appiconloader.iconloaderlib;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.SparseArray;
import java.util.Arrays;

public class ColorExtractor {
    private final int NUM_SAMPLES = 20;
    private final float[] mTmpHsv = new float[3];
    private final float[] mTmpHueScoreHistogram = new float[360];
    private final int[] mTmpPixels = new int[20];
    private final SparseArray<Float> mTmpRgbScores = new SparseArray<>();

    public int findDominantColorByHue(Bitmap bitmap) {
        return this.findDominantColorByHue(bitmap, 20);
    }

    public int findDominantColorByHue(Bitmap bitmap, int samples) {
        int width;
        int height = bitmap.getHeight();
        int sampleStride = (int)Math.sqrt(height * (width = bitmap.getWidth()) / samples);
        if (sampleStride < 1) {
            sampleStride = 1;
        }
        float[] hsv = this.mTmpHsv;
        Arrays.fill(hsv, 0.0f);
        float[] hueScoreHistogram = this.mTmpHueScoreHistogram;
        Arrays.fill(hueScoreHistogram, 0.0f);
        float highScore = -1.0f;
        int bestHue = -1;
        int[] pixels = this.mTmpPixels;
        Arrays.fill(pixels, 0);
        int pixelCount = 0;
        for (int y = 0; y < height; y += sampleStride) {
            for (int x = 0; x < width; x += sampleStride) {
                int argb = bitmap.getPixel(x, y);
                int alpha = 0xFF & argb >> 24;
                if (alpha < 128) continue;
                int rgb = argb | 0xFF000000;
                Color.colorToHSV((int)rgb, (float[])hsv);
                int hue = (int)hsv[0];
                if (hue < 0 || hue >= hueScoreHistogram.length) continue;
                if (pixelCount < samples) {
                    pixels[pixelCount++] = rgb;
                }
                float score = hsv[1] * hsv[2];
                int n = hue;
                hueScoreHistogram[n] = hueScoreHistogram[n] + score;
                if (!(hueScoreHistogram[hue] > highScore)) continue;
                highScore = hueScoreHistogram[hue];
                bestHue = hue;
            }
        }
        SparseArray<Float> rgbScores = this.mTmpRgbScores;
        rgbScores.clear();
        int bestColor = -16777216;
        highScore = -1.0f;
        for (int i = 0; i < pixelCount; ++i) {
            int rgb = pixels[i];
            Color.colorToHSV((int)rgb, (float[])hsv);
            int hue = (int)hsv[0];
            if (hue != bestHue) continue;
            float s = hsv[1];
            float v = hsv[2];
            int bucket = (int)(s * 100.0f) + (int)(v * 10000.0f);
            float score = s * v;
            Float oldTotal = rgbScores.get(bucket);
            float newTotal = oldTotal == null ? score : oldTotal.floatValue() + score;
            rgbScores.put(bucket, Float.valueOf(newTotal));
            if (!(newTotal > highScore)) continue;
            highScore = newTotal;
            bestColor = rgb;
        }
        return bestColor;
    }
}
