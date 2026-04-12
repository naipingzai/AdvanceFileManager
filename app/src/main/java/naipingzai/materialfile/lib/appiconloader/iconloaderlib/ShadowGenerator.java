/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  android.graphics.Bitmap
 *  android.graphics.BlurMaskFilter
 *  android.graphics.BlurMaskFilter$Blur
 *  android.graphics.Canvas
 *  android.graphics.Color
 *  android.graphics.MaskFilter
 *  android.graphics.Paint
 *  android.graphics.PorterDuff$Mode
 *  android.graphics.PorterDuffXfermode
 *  android.graphics.RectF
 *  android.graphics.Xfermode
 */
package naipingzai.materialfile.lib.appiconloader.iconloaderlib;

import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import naipingzai.materialfile.lib.appiconloader.iconloaderlib.BitmapRenderer;
import naipingzai.materialfile.lib.appiconloader.iconloaderlib.GraphicsUtils;

public class ShadowGenerator {
    public static final boolean ENABLE_SHADOWS = true;
    public static final float BLUR_FACTOR = 0.010416667f;
    public static final float KEY_SHADOW_DISTANCE = 0.020833334f;
    private static final int KEY_SHADOW_ALPHA = 61;
    private static final float HALF_DISTANCE = 0.5f;
    private static final int AMBIENT_SHADOW_ALPHA = 30;
    private final int mIconSize;
    private final Paint mBlurPaint;
    private final Paint mDrawPaint;
    private final BlurMaskFilter mDefaultBlurMaskFilter;

    public ShadowGenerator(int iconSize) {
        this.mIconSize = iconSize;
        this.mBlurPaint = new Paint(3);
        this.mDrawPaint = new Paint(3);
        this.mDefaultBlurMaskFilter = new BlurMaskFilter((float)this.mIconSize * 0.010416667f, BlurMaskFilter.Blur.NORMAL);
    }

    public synchronized void recreateIcon(Bitmap icon, Canvas out) {
        this.recreateIcon(icon, this.mDefaultBlurMaskFilter, 30, 61, out);
    }

    public synchronized void recreateIcon(Bitmap icon, BlurMaskFilter blurMaskFilter, int ambientAlpha, int keyAlpha, Canvas out) {
        int[] offset = new int[2];
        this.mBlurPaint.setMaskFilter((MaskFilter)blurMaskFilter);
        Bitmap shadow = icon.extractAlpha(this.mBlurPaint, offset);
        this.mDrawPaint.setAlpha(ambientAlpha);
        out.drawBitmap(shadow, (float)offset[0], (float)offset[1], this.mDrawPaint);
        this.mDrawPaint.setAlpha(keyAlpha);
        out.drawBitmap(shadow, (float)offset[0], (float)offset[1] + 0.020833334f * (float)this.mIconSize, this.mDrawPaint);
        this.mDrawPaint.setAlpha(255);
        out.drawBitmap(icon, 0.0f, 0.0f, this.mDrawPaint);
    }

    public static float getScaleForBounds(RectF bounds) {
        float bottomSpace;
        float scale = 1.0f;
        float minSide = Math.min(Math.min(bounds.left, bounds.right), bounds.top);
        if (minSide < 0.010416667f) {
            scale = 0.48958334f / (0.5f - minSide);
        }
        if (bounds.bottom < (bottomSpace = 0.03125f)) {
            scale = Math.min(scale, (0.5f - bottomSpace) / (0.5f - bounds.bottom));
        }
        return scale;
    }

    public static class Builder {
        public final RectF bounds = new RectF();
        public final int color;
        public int ambientShadowAlpha = 30;
        public float shadowBlur;
        public float keyShadowDistance;
        public int keyShadowAlpha = 61;
        public float radius;

        public Builder(int color) {
            this.color = color;
        }

        public Builder setupBlurForSize(int height) {
            this.shadowBlur = (float)height * 1.0f / 24.0f;
            this.keyShadowDistance = (float)height * 1.0f / 16.0f;
            return this;
        }

        public Bitmap createPill(int width, int height) {
            return this.createPill(width, height, (float)height / 2.0f);
        }

        public Bitmap createPill(int width, int height, float r) {
            this.radius = r;
            int centerX = Math.round((float)width / 2.0f + this.shadowBlur);
            int centerY = Math.round(this.radius + this.shadowBlur + this.keyShadowDistance);
            int center = Math.max(centerX, centerY);
            this.bounds.set(0.0f, 0.0f, (float)width, (float)height);
            this.bounds.offsetTo((float)center - (float)width / 2.0f, (float)center - (float)height / 2.0f);
            int size = center * 2;
            return BitmapRenderer.createHardwareBitmap(size, size, this::drawShadow);
        }

        public void drawShadow(Canvas c) {
            Paint p = new Paint(3);
            p.setColor(this.color);
            p.setShadowLayer(this.shadowBlur, 0.0f, this.keyShadowDistance, GraphicsUtils.setColorAlphaBound(-16777216, this.keyShadowAlpha));
            c.drawRoundRect(this.bounds, this.radius, this.radius, p);
            p.setShadowLayer(this.shadowBlur, 0.0f, 0.0f, GraphicsUtils.setColorAlphaBound(-16777216, this.ambientShadowAlpha));
            c.drawRoundRect(this.bounds, this.radius, this.radius, p);
            if (Color.alpha((int)this.color) < 255) {
                p.setXfermode((Xfermode)new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                p.clearShadowLayer();
                p.setColor(-16777216);
                c.drawRoundRect(this.bounds, this.radius, this.radius, p);
                p.setXfermode(null);
                p.setColor(this.color);
                c.drawRoundRect(this.bounds, this.radius, this.radius, p);
            }
        }
    }
}
