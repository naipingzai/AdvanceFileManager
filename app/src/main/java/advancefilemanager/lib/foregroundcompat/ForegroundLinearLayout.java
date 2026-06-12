/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  android.content.Context
 *  android.content.res.ColorStateList
 *  android.graphics.Canvas
 *  android.graphics.PorterDuff$Mode
 *  android.graphics.drawable.Drawable
 *  android.util.AttributeSet
 *  android.view.View
 *  android.widget.LinearLayout
 *  androidx.annotation.AttrRes
 *  androidx.annotation.NonNull
 *  androidx.annotation.Nullable
 *  androidx.annotation.RequiresApi
 *  androidx.annotation.RestrictTo
 *  androidx.annotation.RestrictTo$Scope
 *  androidx.annotation.StyleRes
 */
package com.advancefilemanager.lib.foregroundcompat;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import com.advancefilemanager.lib.foregroundcompat.ForegroundCompatView;
import com.advancefilemanager.lib.foregroundcompat.ForegroundHelper;

public class ForegroundLinearLayout
extends LinearLayout
implements ForegroundCompatView {
    private final ForegroundHelper mForegroundHelper = new ForegroundHelper((View)this);

    public ForegroundLinearLayout(@NonNull Context context) {
        super(context);
        this.mForegroundHelper.init(context, null, 0, 0);
    }

    public ForegroundLinearLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mForegroundHelper.init(context, attrs, 0, 0);
    }

    public ForegroundLinearLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mForegroundHelper.init(context, attrs, defStyleAttr, 0);
    }

    public ForegroundLinearLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mForegroundHelper.init(context, attrs, defStyleAttr, defStyleRes);
    }

    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
        this.mForegroundHelper.onVisibilityAggregated(isVisible);
    }

    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
        this.mForegroundHelper.draw(canvas);
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        this.mForegroundHelper.onRtlPropertiesChanged(layoutDirection);
    }

    public boolean verifyDrawable(@NonNull Drawable who) {
        return super.verifyDrawable(who) || this.mForegroundHelper != null && this.mForegroundHelper.verifyDrawable(who);
    }

    public void drawableStateChanged() {
        super.drawableStateChanged();
        this.mForegroundHelper.drawableStateChanged();
    }

    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);
        this.mForegroundHelper.drawableHotspotChanged(x, y);
    }

    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        this.mForegroundHelper.jumpDrawablesToCurrentState();
    }

    @Override
    @Nullable
    @RestrictTo(value={RestrictTo.Scope.LIBRARY})
    public Drawable getSupportForeground() {
        return this.mForegroundHelper.getSupportForeground();
    }

    @Override
    @RestrictTo(value={RestrictTo.Scope.LIBRARY})
    public void setSupportForeground(@Nullable Drawable foreground) {
        this.mForegroundHelper.setSupportForeground(foreground);
    }

    @Override
    @RestrictTo(value={RestrictTo.Scope.LIBRARY})
    public int getSupportForegroundGravity() {
        return this.mForegroundHelper.getSupportForegroundGravity();
    }

    @Override
    @RestrictTo(value={RestrictTo.Scope.LIBRARY})
    public void setSupportForegroundGravity(int gravity) {
        this.mForegroundHelper.setSupportForegroundGravity(gravity);
    }

    @Override
    @RestrictTo(value={RestrictTo.Scope.LIBRARY})
    public void setSupportForegroundTintList(@Nullable ColorStateList tint) {
        this.mForegroundHelper.setSupportForegroundTintList(tint);
    }

    @Override
    @Nullable
    @RestrictTo(value={RestrictTo.Scope.LIBRARY})
    public ColorStateList getSupportForegroundTintList() {
        return this.mForegroundHelper.getSupportForegroundTintList();
    }

    @Override
    @RestrictTo(value={RestrictTo.Scope.LIBRARY})
    public void setSupportForegroundTintMode(@Nullable PorterDuff.Mode tintMode) {
        this.mForegroundHelper.setSupportForegroundTintMode(tintMode);
    }

    @Override
    @Nullable
    @RestrictTo(value={RestrictTo.Scope.LIBRARY})
    public PorterDuff.Mode getSupportForegroundTintMode() {
        return this.mForegroundHelper.getSupportForegroundTintMode();
    }
}

