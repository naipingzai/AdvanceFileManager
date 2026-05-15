/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  android.annotation.TargetApi
 *  android.content.Context
 *  android.graphics.Bitmap
 *  android.graphics.Bitmap$Config
 *  android.graphics.Canvas
 *  android.graphics.Matrix
 *  android.graphics.Paint
 *  android.graphics.Paint$Style
 *  android.graphics.Path
 *  android.graphics.PorterDuff$Mode
 *  android.graphics.PorterDuffXfermode
 *  android.graphics.Rect
 *  android.graphics.RectF
 *  android.graphics.Region
 *  android.graphics.Xfermode
 *  android.graphics.drawable.AdaptiveIconDrawable
 *  android.graphics.drawable.Drawable
 *  androidx.annotation.NonNull
 *  androidx.annotation.Nullable
 */
package com.advancefilemanager.lib.appiconloader.iconloaderlib;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Xfermode;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import com.advancefilemanager.lib.appiconloader.iconloaderlib.BaseIconFactory;
import com.advancefilemanager.lib.appiconloader.iconloaderlib.GraphicsUtils;

public class IconNormalizer {
    private static final String TAG = "IconNormalizer";
    private static final boolean DEBUG = false;
    private static final float MAX_SQUARE_AREA_FACTOR = 0.6510417f;
    private static final float MAX_CIRCLE_AREA_FACTOR = 0.6597222f;
    private static final float CIRCLE_AREA_BY_RECT = 0.7853982f;
    private static final float LINEAR_SCALE_SLOPE = 0.040449437f;
    private static final int MIN_VISIBLE_ALPHA = 40;
    private static final float BOUND_RATIO_MARGIN = 0.05f;
    private static final float PIXEL_DIFF_PERCENTAGE_THRESHOLD = 0.005f;
    private static final float SCALE_NOT_INITIALIZED = 0.0f;
    public static final float ICON_VISIBLE_AREA_FACTOR = 0.92f;
    private final int mMaxSize;
    private final Bitmap mBitmap;
    private final Canvas mCanvas;
    private final Paint mPaintMaskShape;
    private final Paint mPaintMaskShapeOutline;
    private final byte[] mPixels;
    private final RectF mAdaptiveIconBounds;
    private float mAdaptiveIconScale;
    private boolean mEnableShapeDetection;
    private final float[] mLeftBorder;
    private final float[] mRightBorder;
    private final Rect mBounds;
    private final Path mShapePath;
    private final Matrix mMatrix;

    IconNormalizer(Context context, int iconBitmapSize, boolean shapeDetection) {
        this.mMaxSize = iconBitmapSize * 2;
        this.mBitmap = Bitmap.createBitmap((int)this.mMaxSize, (int)this.mMaxSize, (Bitmap.Config)Bitmap.Config.ALPHA_8);
        this.mCanvas = new Canvas(this.mBitmap);
        this.mPixels = new byte[this.mMaxSize * this.mMaxSize];
        this.mLeftBorder = new float[this.mMaxSize];
        this.mRightBorder = new float[this.mMaxSize];
        this.mBounds = new Rect();
        this.mAdaptiveIconBounds = new RectF();
        this.mPaintMaskShape = new Paint();
        this.mPaintMaskShape.setColor(-65536);
        this.mPaintMaskShape.setStyle(Paint.Style.FILL);
        this.mPaintMaskShape.setXfermode((Xfermode)new PorterDuffXfermode(PorterDuff.Mode.XOR));
        this.mPaintMaskShapeOutline = new Paint();
        this.mPaintMaskShapeOutline.setStrokeWidth(2.0f * context.getResources().getDisplayMetrics().density);
        this.mPaintMaskShapeOutline.setStyle(Paint.Style.STROKE);
        this.mPaintMaskShapeOutline.setColor(-16777216);
        this.mPaintMaskShapeOutline.setXfermode((Xfermode)new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        this.mShapePath = new Path();
        this.mMatrix = new Matrix();
        this.mAdaptiveIconScale = 0.0f;
        this.mEnableShapeDetection = shapeDetection;
    }

    private static float getScale(float hullArea, float boundingArea, float fullArea) {
        float hullByRect = hullArea / boundingArea;
        float scaleRequired = hullByRect < 0.7853982f ? 0.6597222f : 0.6510417f + 0.040449437f * (1.0f - hullByRect);
        float areaScale = hullArea / fullArea;
        return areaScale > scaleRequired ? (float)Math.sqrt(scaleRequired / areaScale) : 1.0f;
    }

    @TargetApi(value=26)
    public static float normalizeAdaptiveIcon(Drawable d, int size, @Nullable RectF outBounds) {
        Rect tmpBounds = new Rect(d.getBounds());
        d.setBounds(0, 0, size, size);
        Path path = ((AdaptiveIconDrawable)d).getIconMask();
        Region region = new Region();
        region.setPath(path, new Region(0, 0, size, size));
        Rect hullBounds = region.getBounds();
        int hullArea = GraphicsUtils.getArea(region);
        if (outBounds != null) {
            float sizeF = size;
            outBounds.set((float)hullBounds.left / sizeF, (float)hullBounds.top / sizeF, 1.0f - (float)hullBounds.right / sizeF, 1.0f - (float)hullBounds.bottom / sizeF);
        }
        d.setBounds(tmpBounds);
        return IconNormalizer.getScale(hullArea, hullArea, size * size);
    }

    private boolean isShape(Path maskPath) {
        float iconRatio = (float)this.mBounds.width() / (float)this.mBounds.height();
        if (Math.abs(iconRatio - 1.0f) > 0.05f) {
            return false;
        }
        this.mMatrix.reset();
        this.mMatrix.setScale((float)this.mBounds.width(), (float)this.mBounds.height());
        this.mMatrix.postTranslate((float)this.mBounds.left, (float)this.mBounds.top);
        maskPath.transform(this.mMatrix, this.mShapePath);
        this.mCanvas.drawPath(this.mShapePath, this.mPaintMaskShape);
        this.mCanvas.drawPath(this.mShapePath, this.mPaintMaskShapeOutline);
        return this.isTransparentBitmap();
    }

    private boolean isTransparentBitmap() {
        ByteBuffer buffer = ByteBuffer.wrap(this.mPixels);
        buffer.rewind();
        this.mBitmap.copyPixelsToBuffer((Buffer)buffer);
        int index = this.mBounds.top * this.mMaxSize;
        int rowSizeDiff = this.mMaxSize - this.mBounds.right;
        int sum = 0;
        for (int y = this.mBounds.top; y < this.mBounds.bottom; ++y) {
            index += this.mBounds.left;
            for (int x = this.mBounds.left; x < this.mBounds.right; ++x) {
                if ((this.mPixels[index] & 0xFF) > 40) {
                    ++sum;
                }
                ++index;
            }
            index += rowSizeDiff;
        }
        float percentageDiffPixels = (float)sum / (float)(this.mBounds.width() * this.mBounds.height());
        return percentageDiffPixels < 0.005f;
    }

    public synchronized float getScale(@NonNull Drawable d, @Nullable RectF outBounds, @Nullable Path path, @Nullable boolean[] outMaskShape) {
        if (BaseIconFactory.ATLEAST_OREO && d instanceof AdaptiveIconDrawable) {
            if (this.mAdaptiveIconScale == 0.0f) {
                this.mAdaptiveIconScale = IconNormalizer.normalizeAdaptiveIcon(d, this.mMaxSize, this.mAdaptiveIconBounds);
            }
            if (outBounds != null) {
                outBounds.set(this.mAdaptiveIconBounds);
            }
            return this.mAdaptiveIconScale;
        }
        int width = d.getIntrinsicWidth();
        int height = d.getIntrinsicHeight();
        if (width <= 0 || height <= 0) {
            width = width <= 0 || width > this.mMaxSize ? this.mMaxSize : width;
            height = height <= 0 || height > this.mMaxSize ? this.mMaxSize : height;
        } else if (width > this.mMaxSize || height > this.mMaxSize) {
            int max = Math.max(width, height);
            width = this.mMaxSize * width / max;
            height = this.mMaxSize * height / max;
        }
        this.mBitmap.eraseColor(0);
        d.setBounds(0, 0, width, height);
        d.draw(this.mCanvas);
        ByteBuffer buffer = ByteBuffer.wrap(this.mPixels);
        buffer.rewind();
        this.mBitmap.copyPixelsToBuffer((Buffer)buffer);
        int topY = -1;
        int bottomY = -1;
        int leftX = this.mMaxSize + 1;
        int rightX = -1;
        int index = 0;
        int rowSizeDiff = this.mMaxSize - width;
        for (int y = 0; y < height; ++y) {
            int lastX = -1;
            int firstX = -1;
            for (int x = 0; x < width; ++x) {
                if ((this.mPixels[index] & 0xFF) > 40) {
                    if (firstX == -1) {
                        firstX = x;
                    }
                    lastX = x;
                }
                ++index;
            }
            index += rowSizeDiff;
            this.mLeftBorder[y] = firstX;
            this.mRightBorder[y] = lastX;
            if (firstX == -1) continue;
            bottomY = y;
            if (topY == -1) {
                topY = y;
            }
            leftX = Math.min(leftX, firstX);
            rightX = Math.max(rightX, lastX);
        }
        if (topY == -1 || rightX == -1) {
            return 1.0f;
        }
        IconNormalizer.convertToConvexArray(this.mLeftBorder, 1, topY, bottomY);
        IconNormalizer.convertToConvexArray(this.mRightBorder, -1, topY, bottomY);
        float area = 0.0f;
        for (int y = 0; y < height; ++y) {
            if (this.mLeftBorder[y] <= -1.0f) continue;
            area += this.mRightBorder[y] - this.mLeftBorder[y] + 1.0f;
        }
        this.mBounds.left = leftX;
        this.mBounds.right = rightX;
        this.mBounds.top = topY;
        this.mBounds.bottom = bottomY;
        if (outBounds != null) {
            outBounds.set((float)this.mBounds.left / (float)width, (float)this.mBounds.top / (float)height, 1.0f - (float)this.mBounds.right / (float)width, 1.0f - (float)this.mBounds.bottom / (float)height);
        }
        if (outMaskShape != null && this.mEnableShapeDetection && outMaskShape.length > 0) {
            outMaskShape[0] = this.isShape(path);
        }
        float rectArea = (bottomY + 1 - topY) * (rightX + 1 - leftX);
        return IconNormalizer.getScale(area, rectArea, width * height);
    }

    private static void convertToConvexArray(float[] xCoordinates, int direction, int topY, int bottomY) {
        int total = xCoordinates.length;
        float[] angles = new float[total - 1];
        int first = topY;
        int last = -1;
        float lastAngle = Float.MAX_VALUE;
        for (int i = topY + 1; i <= bottomY; ++i) {
            int start;
            if (xCoordinates[i] <= -1.0f) continue;
            if (lastAngle == Float.MAX_VALUE) {
                start = first;
            } else {
                float currentAngle = (xCoordinates[i] - xCoordinates[last]) / (float)(i - last);
                start = last;
                if ((currentAngle - lastAngle) * (float)direction < 0.0f) {
                    while (start > first && !(((currentAngle = (xCoordinates[i] - xCoordinates[--start]) / (float)(i - start)) - angles[start]) * (float)direction >= 0.0f)) {
                    }
                }
            }
            lastAngle = (xCoordinates[i] - xCoordinates[start]) / (float)(i - start);
            for (int j = start; j < i; ++j) {
                angles[j] = lastAngle;
                xCoordinates[j] = xCoordinates[start] + lastAngle * (float)(j - start);
            }
            last = i;
        }
    }

    public static int getNormalizedCircleSize(int size) {
        float area = (float)(size * size) * 0.6597222f;
        return (int)Math.round(Math.sqrt((double)(4.0f * area) / Math.PI));
    }
}
