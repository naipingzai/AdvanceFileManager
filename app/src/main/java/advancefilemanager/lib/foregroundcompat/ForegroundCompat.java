/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  android.content.res.ColorStateList
 *  android.graphics.PorterDuff$Mode
 *  android.graphics.drawable.Drawable
 *  android.os.Build$VERSION
 *  android.view.View
 *  android.widget.FrameLayout
 *  androidx.annotation.NonNull
 *  androidx.annotation.Nullable
 */
package com.advancefilemanager.lib.foregroundcompat;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.advancefilemanager.lib.foregroundcompat.ForegroundCompatView;

public class ForegroundCompat {
    private ForegroundCompat() {
    }

    @Nullable
    public static Drawable getForeground(@NonNull View view) {
        if (view instanceof FrameLayout) {
            return ((FrameLayout)view).getForeground();
        }
        if (Build.VERSION.SDK_INT >= 23 && ForegroundCompat.isTargetingMOrAbove(view)) {
            return view.getForeground();
        }
        if (view instanceof ForegroundCompatView) {
            return ((ForegroundCompatView)view).getSupportForeground();
        }
        return null;
    }

    public static void setForeground(@NonNull View view, @Nullable Drawable foreground) {
        if (view instanceof FrameLayout) {
            ((FrameLayout)view).setForeground(foreground);
        } else if (Build.VERSION.SDK_INT >= 23 && ForegroundCompat.isTargetingMOrAbove(view)) {
            view.setForeground(foreground);
        } else if (view instanceof ForegroundCompatView) {
            ((ForegroundCompatView)view).setSupportForeground(foreground);
        }
    }

    public static int getForegroundGravity(@NonNull View view) {
        if (view instanceof FrameLayout) {
            return ((FrameLayout)view).getForegroundGravity();
        }
        if (Build.VERSION.SDK_INT >= 23 && ForegroundCompat.isTargetingMOrAbove(view)) {
            return view.getForegroundGravity();
        }
        if (view instanceof ForegroundCompatView) {
            return ((ForegroundCompatView)view).getSupportForegroundGravity();
        }
        return 0x800033;
    }

    public static void setForegroundGravity(@NonNull View view, int gravity) {
        if (view instanceof FrameLayout) {
            ((FrameLayout)view).setForegroundGravity(gravity);
        } else if (Build.VERSION.SDK_INT >= 23 && ForegroundCompat.isTargetingMOrAbove(view)) {
            view.setForegroundGravity(gravity);
        } else if (view instanceof ForegroundCompatView) {
            ((ForegroundCompatView)view).setSupportForegroundGravity(gravity);
        }
    }

    public static void setForegroundTintList(@NonNull View view, @Nullable ColorStateList tint) {
        if (view instanceof FrameLayout) {
            ((FrameLayout)view).setForegroundTintList(tint);
        } else if (Build.VERSION.SDK_INT >= 23 && ForegroundCompat.isTargetingMOrAbove(view)) {
            view.setForegroundTintList(tint);
        } else if (view instanceof ForegroundCompatView) {
            ((ForegroundCompatView)view).setSupportForegroundTintList(tint);
        }
    }

    @Nullable
    public static ColorStateList getForegroundTintList(@NonNull View view) {
        if (view instanceof FrameLayout) {
            return ((FrameLayout)view).getForegroundTintList();
        }
        if (Build.VERSION.SDK_INT >= 23 && ForegroundCompat.isTargetingMOrAbove(view)) {
            return view.getForegroundTintList();
        }
        if (view instanceof ForegroundCompatView) {
            return ((ForegroundCompatView)view).getSupportForegroundTintList();
        }
        return null;
    }

    public static void setForegroundTintMode(@NonNull View view, @Nullable PorterDuff.Mode tintMode) {
        if (view instanceof FrameLayout) {
            ((FrameLayout)view).setForegroundTintMode(tintMode);
        } else if (Build.VERSION.SDK_INT >= 23 && ForegroundCompat.isTargetingMOrAbove(view)) {
            view.setForegroundTintMode(tintMode);
        } else if (view instanceof ForegroundCompatView) {
            ((ForegroundCompatView)view).setSupportForegroundTintMode(tintMode);
        }
    }

    @Nullable
    public static PorterDuff.Mode getForegroundTintMode(@NonNull View view) {
        if (view instanceof FrameLayout) {
            return ((FrameLayout)view).getForegroundTintMode();
        }
        if (Build.VERSION.SDK_INT >= 23 && ForegroundCompat.isTargetingMOrAbove(view)) {
            return view.getForegroundTintMode();
        }
        if (view instanceof ForegroundCompatView) {
            return ((ForegroundCompatView)view).getSupportForegroundTintMode();
        }
        return null;
    }

    private static boolean isTargetingMOrAbove(@NonNull View view) {
        return view.getContext().getApplicationInfo().targetSdkVersion >= 23;
    }
}

