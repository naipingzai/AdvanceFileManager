/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  android.content.res.Resources
 *  android.content.res.Resources$Theme
 *  android.graphics.Canvas
 *  android.graphics.drawable.ColorDrawable
 *  android.graphics.drawable.Drawable
 *  android.graphics.drawable.DrawableWrapper
 *  android.util.AttributeSet
 *  androidx.annotation.RequiresApi
 *  org.xmlpull.v1.XmlPullParser
 */
package com.advancefilemanager.lib.appiconloader.iconloaderlib;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.util.AttributeSet;
import androidx.annotation.RequiresApi;
import org.xmlpull.v1.XmlPullParser;

@RequiresApi(value=26)
public class FixedScaleDrawable
extends DrawableWrapper {
    private static final float LEGACY_ICON_SCALE = 0.46669f;
    private float mScaleX = 0.46669f;
    private float mScaleY = 0.46669f;

    public FixedScaleDrawable() {
        super((Drawable)new ColorDrawable());
    }

    public void draw(Canvas canvas) {
        int saveCount = canvas.save();
        canvas.scale(this.mScaleX, this.mScaleY, this.getBounds().exactCenterX(), this.getBounds().exactCenterY());
        super.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs) {
    }

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) {
    }

    public void setScale(float scale) {
        float h = this.getIntrinsicHeight();
        float w = this.getIntrinsicWidth();
        this.mScaleX = scale * 0.46669f;
        this.mScaleY = scale * 0.46669f;
        if (h > w && w > 0.0f) {
            this.mScaleX *= w / h;
        } else if (w > h && h > 0.0f) {
            this.mScaleY *= h / w;
        }
    }
}
