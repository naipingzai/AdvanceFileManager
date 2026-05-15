/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  android.content.res.ColorStateList
 *  android.graphics.PorterDuff$Mode
 *  android.graphics.drawable.Drawable
 *  androidx.annotation.Nullable
 */
package com.advancefilemanager.lib.foregroundcompat;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;

interface ForegroundCompatView {
    @Nullable
    public Drawable getSupportForeground();

    public void setSupportForeground(@Nullable Drawable var1);

    public int getSupportForegroundGravity();

    public void setSupportForegroundGravity(int var1);

    public void setSupportForegroundTintList(@Nullable ColorStateList var1);

    @Nullable
    public ColorStateList getSupportForegroundTintList();

    public void setSupportForegroundTintMode(@Nullable PorterDuff.Mode var1);

    @Nullable
    public PorterDuff.Mode getSupportForegroundTintMode();
}

