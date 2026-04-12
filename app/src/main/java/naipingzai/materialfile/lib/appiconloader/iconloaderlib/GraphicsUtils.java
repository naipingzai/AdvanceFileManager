/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  android.graphics.Rect
 *  android.graphics.Region
 *  android.graphics.RegionIterator
 *  androidx.annotation.ColorInt
 */
package naipingzai.materialfile.lib.appiconloader.iconloaderlib;

import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.RegionIterator;
import androidx.annotation.ColorInt;

public class GraphicsUtils {
    private static final String TAG = "GraphicsUtils";
    public static Runnable sOnNewBitmapRunnable = () -> {};

    @ColorInt
    public static int setColorAlphaBound(int color, int alpha) {
        if (alpha < 0) {
            alpha = 0;
        } else if (alpha > 255) {
            alpha = 255;
        }
        return color & 0xFFFFFF | alpha << 24;
    }

    public static int getArea(Region r) {
        RegionIterator itr = new RegionIterator(r);
        int area = 0;
        Rect tempRect = new Rect();
        while (itr.next(tempRect)) {
            area += tempRect.width() * tempRect.height();
        }
        return area;
    }

    public static void noteNewBitmapCreated() {
        sOnNewBitmapRunnable.run();
    }
}
