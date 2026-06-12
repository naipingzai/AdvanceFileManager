/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  android.annotation.SuppressLint
 *  android.content.Context
 *  android.content.res.ColorStateList
 *  android.graphics.Canvas
 *  android.graphics.PorterDuff$Mode
 *  android.graphics.Rect
 *  android.graphics.drawable.Drawable
 *  android.graphics.drawable.Drawable$Callback
 *  android.os.Build$VERSION
 *  android.util.AttributeSet
 *  android.view.Gravity
 *  android.view.View
 *  android.widget.FrameLayout
 *  androidx.annotation.AttrRes
 *  androidx.annotation.NonNull
 *  androidx.annotation.Nullable
 *  androidx.annotation.RequiresApi
 *  androidx.annotation.StyleRes
 *  androidx.appcompat.widget.TintTypedArray
 *  androidx.core.graphics.drawable.DrawableCompat
 */
package com.advancefilemanager.lib.foregroundcompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.widget.TintTypedArray;
import androidx.core.graphics.drawable.DrawableCompat;
import com.advancefilemanager.lib.foregroundcompat.MoreDrawableCompat;

public class ForegroundHelper {
    private static final int[] STYLEABLE = new int[]{0x1010109, 0x1010200, 16843886, 16843885};
    private static final int STYLEABLE_ANDROID_FOREGROUND = 0;
    private static final int STYLEABLE_ANDROID_FOREGROUND_GRAVITY = 1;
    private static final int STYLEABLE_ANDROID_FOREGROUND_TINT_MODE = 2;
    private static final int STYLEABLE_ANDROID_FOREGROUND_TINT = 3;
    @NonNull
    private final View mView;
    private boolean mHasFrameworkImplementation;
    @Nullable
    private ForegroundInfo mForegroundInfo;

    public ForegroundHelper(@NonNull View view) {
        this.mView = view;
    }

    @SuppressLint(value={"RestrictedApi"})
    public void init(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        boolean bl = this.mHasFrameworkImplementation = this.mView instanceof FrameLayout || true;
        if (this.mHasFrameworkImplementation) {
            return;
        }
        TintTypedArray a = TintTypedArray.obtainStyledAttributes((Context)context, (AttributeSet)attrs, (int[])STYLEABLE, (int)defStyleAttr, (int)defStyleRes);
        if (a.hasValue(0)) {
            this.setSupportForeground(a.getDrawable(0));
        }
        if (a.hasValue(1)) {
            this.setSupportForegroundGravity(a.getInt(1, 0));
        }
        if (a.hasValue(2)) {
            this.setSupportForegroundTintMode(MoreDrawableCompat.parseTintMode(a.getInt(2, -1), null));
        }
        if (a.hasValue(3)) {
            this.setSupportForegroundTintList(a.getColorStateList(3));
        }
        a.recycle();
    }

    public void onVisibilityAggregated(boolean isVisible) {
        Drawable fg;
        if (this.mHasFrameworkImplementation) {
            return;
        }
        Drawable drawable = fg = this.mForegroundInfo != null ? this.mForegroundInfo.mDrawable : null;
        if (fg != null && isVisible != fg.isVisible()) {
            fg.setVisible(isVisible, false);
        }
    }

    public void draw(@NonNull Canvas canvas) {
        if (this.mHasFrameworkImplementation) {
            return;
        }
        this.onDrawForeground(canvas);
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        if (this.mHasFrameworkImplementation) {
            return;
        }
        this.resolveForegroundDrawable(layoutDirection);
    }

    private void resolveForegroundDrawable(int layoutDirection) {
        if (this.mForegroundInfo != null && this.mForegroundInfo.mDrawable != null) {
            DrawableCompat.setLayoutDirection((Drawable)this.mForegroundInfo.mDrawable, (int)layoutDirection);
        }
    }

    protected boolean verifyDrawable(@NonNull Drawable who) {
        if (this.mHasFrameworkImplementation) {
            return false;
        }
        return this.mForegroundInfo != null && this.mForegroundInfo.mDrawable == who;
    }

    public void drawableStateChanged() {
        Drawable fg;
        if (this.mHasFrameworkImplementation) {
            return;
        }
        int[] state = this.mView.getDrawableState();
        boolean changed = false;
        Drawable drawable = fg = this.mForegroundInfo != null ? this.mForegroundInfo.mDrawable : null;
        if (fg != null && fg.isStateful()) {
            changed |= fg.setState(state);
        }
        if (changed) {
            this.mView.invalidate();
        }
    }

    public void drawableHotspotChanged(float x, float y) {
        if (this.mHasFrameworkImplementation) {
            return;
        }
        if (this.mForegroundInfo != null && this.mForegroundInfo.mDrawable != null) {
            this.mForegroundInfo.mDrawable.setHotspot(x, y);
        }
    }

    public void jumpDrawablesToCurrentState() {
        if (this.mHasFrameworkImplementation) {
            return;
        }
        if (this.mForegroundInfo != null && this.mForegroundInfo.mDrawable != null) {
            this.mForegroundInfo.mDrawable.jumpToCurrentState();
        }
    }

    @Nullable
    public Drawable getSupportForeground() {
        return this.mForegroundInfo != null ? this.mForegroundInfo.mDrawable : null;
    }

    public void setSupportForeground(@Nullable Drawable foreground) {
        if (this.mForegroundInfo == null) {
            if (foreground == null) {
                return;
            }
            this.mForegroundInfo = new ForegroundInfo();
        }
        if (foreground == this.mForegroundInfo.mDrawable) {
            return;
        }
        if (this.mForegroundInfo.mDrawable != null) {
            if (this.mView.isAttachedToWindow()) {
                this.mForegroundInfo.mDrawable.setVisible(false, false);
            }
            this.mForegroundInfo.mDrawable.setCallback(null);
            this.mView.unscheduleDrawable(this.mForegroundInfo.mDrawable);
        }
        this.mForegroundInfo.mDrawable = foreground;
        if (foreground != null) {
            this.mView.setWillNotDraw(false);
            DrawableCompat.setLayoutDirection((Drawable)foreground, (int)this.mView.getLayoutDirection());
            if (foreground.isStateful()) {
                foreground.setState(this.mView.getDrawableState());
            }
            this.applyForegroundTint();
            if (this.mView.isAttachedToWindow()) {
                foreground.setVisible(this.mView.getWindowVisibility() == 0 && this.mView.isShown(), false);
            }
            foreground.setCallback((Drawable.Callback)this.mView);
        }
        this.mView.requestLayout();
        this.mView.invalidate();
    }

    public int getSupportForegroundGravity() {
        return this.mForegroundInfo != null ? this.mForegroundInfo.mGravity : 0x800033;
    }

    public void setSupportForegroundGravity(int gravity) {
        if (this.mForegroundInfo == null) {
            this.mForegroundInfo = new ForegroundInfo();
        }
        if (this.mForegroundInfo.mGravity != gravity) {
            if ((gravity & 0x800007) == 0) {
                gravity |= 0x800003;
            }
            if ((gravity & 0x70) == 0) {
                gravity |= 0x30;
            }
            this.mForegroundInfo.mGravity = gravity;
            this.mView.requestLayout();
        }
    }

    public void setSupportForegroundTintList(@Nullable ColorStateList tint) {
        if (this.mForegroundInfo == null) {
            this.mForegroundInfo = new ForegroundInfo();
        }
        if (this.mForegroundInfo.mTintInfo == null) {
            this.mForegroundInfo.mTintInfo = new TintInfo();
        }
        this.mForegroundInfo.mTintInfo.mTintList = tint;
        this.mForegroundInfo.mTintInfo.mHasTintList = true;
        this.applyForegroundTint();
    }

    @Nullable
    public ColorStateList getSupportForegroundTintList() {
        return this.mForegroundInfo != null && this.mForegroundInfo.mTintInfo != null ? this.mForegroundInfo.mTintInfo.mTintList : null;
    }

    public void setSupportForegroundTintMode(@Nullable PorterDuff.Mode tintMode) {
        if (this.mForegroundInfo == null) {
            this.mForegroundInfo = new ForegroundInfo();
        }
        if (this.mForegroundInfo.mTintInfo == null) {
            this.mForegroundInfo.mTintInfo = new TintInfo();
        }
        this.mForegroundInfo.mTintInfo.mTintMode = tintMode;
        this.mForegroundInfo.mTintInfo.mHasTintMode = true;
        this.applyForegroundTint();
    }

    @Nullable
    public PorterDuff.Mode getSupportForegroundTintMode() {
        return this.mForegroundInfo != null && this.mForegroundInfo.mTintInfo != null ? this.mForegroundInfo.mTintInfo.mTintMode : null;
    }

    private void applyForegroundTint() {
        if (this.mForegroundInfo != null && this.mForegroundInfo.mDrawable != null && this.mForegroundInfo.mTintInfo != null) {
            TintInfo tintInfo = this.mForegroundInfo.mTintInfo;
            if (tintInfo.mHasTintList || tintInfo.mHasTintMode) {
                this.mForegroundInfo.mDrawable = this.mForegroundInfo.mDrawable.mutate();
                if (tintInfo.mHasTintList) {
                    this.mForegroundInfo.mDrawable.setTintList(tintInfo.mTintList);
                }
                if (tintInfo.mHasTintMode) {
                    this.mForegroundInfo.mDrawable.setTintMode(tintInfo.mTintMode);
                }
                if (this.mForegroundInfo.mDrawable.isStateful()) {
                    this.mForegroundInfo.mDrawable.setState(this.mView.getDrawableState());
                }
            }
        }
    }

    private void onDrawForeground(@NonNull Canvas canvas) {
        Drawable foreground;
        Drawable drawable = foreground = this.mForegroundInfo != null ? this.mForegroundInfo.mDrawable : null;
        if (foreground != null) {
            Rect selfBounds = this.mForegroundInfo.mSelfBounds;
            Rect overlayBounds = this.mForegroundInfo.mOverlayBounds;
            selfBounds.set(0, 0, this.mView.getWidth(), this.mView.getHeight());
            int ld = this.mView.getLayoutDirection();
            Gravity.apply((int)this.mForegroundInfo.mGravity, (int)foreground.getIntrinsicWidth(), (int)foreground.getIntrinsicHeight(), (Rect)selfBounds, (Rect)overlayBounds, (int)ld);
            foreground.setBounds(overlayBounds);
            foreground.draw(canvas);
        }
    }

    private static class ForegroundInfo {
        public Drawable mDrawable;
        public TintInfo mTintInfo;
        public int mGravity = 119;
        public final Rect mSelfBounds = new Rect();
        public final Rect mOverlayBounds = new Rect();

        private ForegroundInfo() {
        }
    }

    private static class TintInfo {
        public ColorStateList mTintList;
        public PorterDuff.Mode mTintMode;
        public boolean mHasTintMode;
        public boolean mHasTintList;

        private TintInfo() {
        }
    }
}

