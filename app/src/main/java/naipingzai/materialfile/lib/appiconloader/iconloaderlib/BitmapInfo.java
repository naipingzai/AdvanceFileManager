/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  android.content.Context
 *  android.graphics.Bitmap
 *  android.graphics.Canvas
 *  android.graphics.drawable.Drawable
 *  android.os.UserHandle
 *  androidx.annotation.NonNull
 */
package naipingzai.materialfile.lib.appiconloader.iconloaderlib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import androidx.annotation.NonNull;
import naipingzai.materialfile.lib.appiconloader.iconloaderlib.BaseIconFactory;

public class BitmapInfo {
    public final Bitmap icon;
    public final int color;

    public BitmapInfo(Bitmap icon, int color) {
        this.icon = icon;
        this.color = color;
    }

    public static BitmapInfo fromBitmap(@NonNull Bitmap bitmap) {
        return BitmapInfo.of(bitmap, 0);
    }

    public static BitmapInfo of(@NonNull Bitmap bitmap, int color) {
        return new BitmapInfo(bitmap, color);
    }

    public static interface Extender {
        public BitmapInfo getExtendedInfo(Bitmap var1, int var2, BaseIconFactory var3, float var4, UserHandle var5);

        public void drawForPersistence(Canvas var1);

        public Drawable getThemedDrawable(Context var1);
    }
}
