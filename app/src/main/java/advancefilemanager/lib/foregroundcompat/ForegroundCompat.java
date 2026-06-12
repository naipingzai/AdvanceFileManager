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
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ForegroundCompat {
    private ForegroundCompat() {
    }

    @Nullable
    public static Drawable getForeground(@NonNull View view) {
        if (view instanceof FrameLayout) {
            return ((FrameLayout)view).getForeground();
        }
        return view.getForeground();
    }

    public static void setForeground(@NonNull View view, @Nullable Drawable foreground) {
        if (view instanceof FrameLayout) {
            ((FrameLayout)view).setForeground(foreground);
        } else {
            view.setForeground(foreground);
        }
    }

    public static int getForegroundGravity(@NonNull View view) {
        if (view instanceof FrameLayout) {
            return ((FrameLayout)view).getForegroundGravity();
        }
        return view.getForegroundGravity();
    }

    public static void setForegroundGravity(@NonNull View view, int gravity) {
        if (view instanceof FrameLayout) {
            ((FrameLayout)view).setForegroundGravity(gravity);
        } else {
            view.setForegroundGravity(gravity);
        }
    }

    public static void setForegroundTintList(@NonNull View view, @Nullable ColorStateList tint) {
        if (view instanceof FrameLayout) {
            ((FrameLayout)view).setForegroundTintList(tint);
        } else {
            view.setForegroundTintList(tint);
        }
    }

    @Nullable
    public static ColorStateList getForegroundTintList(@NonNull View view) {
        if (view instanceof FrameLayout) {
            return ((FrameLayout)view).getForegroundTintList();
        }
        return view.getForegroundTintList();
    }

    public static void setForegroundTintMode(@NonNull View view, @Nullable PorterDuff.Mode tintMode) {
        if (view instanceof FrameLayout) {
            ((FrameLayout)view).setForegroundTintMode(tintMode);
        } else {
            view.setForegroundTintMode(tintMode);
        }
    }

    @Nullable
    public static PorterDuff.Mode getForegroundTintMode(@NonNull View view) {
        if (view instanceof FrameLayout) {
            return ((FrameLayout)view).getForegroundTintMode();
        }
        return view.getForegroundTintMode();
    }
}
