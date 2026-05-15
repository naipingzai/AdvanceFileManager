/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  android.annotation.TargetApi
 *  android.graphics.Bitmap
 *  android.graphics.Bitmap$Config
 *  android.graphics.Canvas
 *  android.graphics.Picture
 *  android.graphics.Rect
 *  android.graphics.RectF
 *  android.os.Build$VERSION
 *  androidx.annotation.ChecksSdkIntAtLeast
 */
package com.advancefilemanager.lib.appiconloader.iconloaderlib;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import androidx.annotation.ChecksSdkIntAtLeast;
import com.advancefilemanager.lib.appiconloader.iconloaderlib.GraphicsUtils;

public interface BitmapRenderer {
    @ChecksSdkIntAtLeast(api=28)
    public static final boolean USE_HARDWARE_BITMAP = Build.VERSION.SDK_INT >= 28;

    public static Bitmap createSoftwareBitmap(int width, int height, BitmapRenderer renderer) {
        GraphicsUtils.noteNewBitmapCreated();
        Bitmap result = Bitmap.createBitmap((int)width, (int)height, (Bitmap.Config)Bitmap.Config.ARGB_8888);
        renderer.draw(new Canvas(result));
        return result;
    }

    @TargetApi(value=28)
    public static Bitmap createHardwareBitmap(int width, int height, BitmapRenderer renderer) {
        if (!USE_HARDWARE_BITMAP) {
            return BitmapRenderer.createSoftwareBitmap(width, height, renderer);
        }
        GraphicsUtils.noteNewBitmapCreated();
        Picture picture = new Picture();
        renderer.draw(picture.beginRecording(width, height));
        picture.endRecording();
        return Bitmap.createBitmap((Picture)picture);
    }

    public static Bitmap createBitmap(Bitmap source, int x, int y, int width, int height) {
        if (Build.VERSION.SDK_INT >= 26 && source.getConfig() == Bitmap.Config.HARDWARE) {
            return BitmapRenderer.createHardwareBitmap(width, height, c -> c.drawBitmap(source, new Rect(x, y, x + width, y + height), new RectF(0.0f, 0.0f, (float)width, (float)height), null));
        }
        GraphicsUtils.noteNewBitmapCreated();
        return Bitmap.createBitmap((Bitmap)source, (int)x, (int)y, (int)width, (int)height);
    }

    public void draw(Canvas var1);
}
