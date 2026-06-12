/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  android.content.Context
 *  android.content.Intent$ShortcutIconResource
 *  android.content.pm.PackageManager
 *  android.content.res.Resources
 *  android.graphics.Bitmap
 *  android.graphics.Bitmap$Config
 *  android.graphics.Canvas
 *  android.graphics.Color
 *  android.graphics.DrawFilter
 *  android.graphics.Paint
 *  android.graphics.Paint$Align
 *  android.graphics.PaintFlagsDrawFilter
 *  android.graphics.Rect
 *  android.graphics.RectF
 *  android.graphics.drawable.AdaptiveIconDrawable
 *  android.graphics.drawable.BitmapDrawable
 *  android.graphics.drawable.ColorDrawable
 *  android.graphics.drawable.Drawable
 *  android.graphics.drawable.InsetDrawable
 *  android.os.Build$VERSION
 *  android.os.Process
 *  android.os.UserHandle
 *  androidx.annotation.ChecksSdkIntAtLeast
 *  androidx.annotation.NonNull
 *  com.advancefilemanager.lib.appiconloader.iconloaderlib.R$drawable
 */
package com.advancefilemanager.lib.appiconloader.iconloaderlib;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Process;
import android.os.UserHandle;
import androidx.annotation.NonNull;
import com.advancefilemanager.lib.appiconloader.iconloaderlib.BitmapInfo;
import com.advancefilemanager.lib.appiconloader.iconloaderlib.BitmapRenderer;
import com.advancefilemanager.lib.appiconloader.iconloaderlib.ColorExtractor;
import com.advancefilemanager.lib.appiconloader.iconloaderlib.FixedScaleDrawable;
import com.advancefilemanager.lib.appiconloader.iconloaderlib.IconNormalizer;
import com.advancefilemanager.R;
import com.advancefilemanager.lib.appiconloader.iconloaderlib.ShadowGenerator;

public class BaseIconFactory
implements AutoCloseable {
    private static final String TAG = "BaseIconFactory";
    private static final int DEFAULT_WRAPPER_BACKGROUND = -1;
    static final boolean ATLEAST_OREO = true;
    static final boolean ATLEAST_P = true;
    private static final float ICON_BADGE_SCALE = 0.444f;
    private final Rect mOldBounds = new Rect();
    protected final Context mContext;
    private final Canvas mCanvas;
    private final PackageManager mPm;
    private final ColorExtractor mColorExtractor;
    private boolean mDisableColorExtractor;
    private boolean mBadgeOnLeft = false;
    protected final int mFillResIconDpi;
    protected final int mIconBitmapSize;
    private IconNormalizer mNormalizer;
    private ShadowGenerator mShadowGenerator;
    private final boolean mShapeDetection;
    private Drawable mWrapperIcon;
    private int mWrapperBackgroundColor = -1;
    private Bitmap mUserBadgeBitmap;
    private final Paint mTextPaint = new Paint(3);
    private static final float PLACEHOLDER_TEXT_SIZE = 20.0f;
    private static int PLACEHOLDER_BACKGROUND_COLOR = Color.rgb((int)245, (int)245, (int)245);

    protected BaseIconFactory(Context context, int fillResIconDpi, int iconBitmapSize, boolean shapeDetection) {
        this.mContext = context.getApplicationContext();
        this.mShapeDetection = shapeDetection;
        this.mFillResIconDpi = fillResIconDpi;
        this.mIconBitmapSize = iconBitmapSize;
        this.mPm = this.mContext.getPackageManager();
        this.mColorExtractor = new ColorExtractor();
        this.mCanvas = new Canvas();
        this.mCanvas.setDrawFilter((DrawFilter)new PaintFlagsDrawFilter(4, 2));
        this.mTextPaint.setTextAlign(Paint.Align.CENTER);
        this.mTextPaint.setColor(PLACEHOLDER_BACKGROUND_COLOR);
        this.mTextPaint.setTextSize(context.getResources().getDisplayMetrics().density * 20.0f);
        this.clear();
    }

    public BaseIconFactory(Context context, int fillResIconDpi, int iconBitmapSize) {
        this(context, fillResIconDpi, iconBitmapSize, false);
    }

    protected void clear() {
        this.mWrapperBackgroundColor = -1;
        this.mDisableColorExtractor = false;
        this.mBadgeOnLeft = false;
    }

    public ShadowGenerator getShadowGenerator() {
        if (this.mShadowGenerator == null) {
            this.mShadowGenerator = new ShadowGenerator(this.mIconBitmapSize);
        }
        return this.mShadowGenerator;
    }

    public IconNormalizer getNormalizer() {
        if (this.mNormalizer == null) {
            this.mNormalizer = new IconNormalizer(this.mContext, this.mIconBitmapSize, this.mShapeDetection);
        }
        return this.mNormalizer;
    }

    public BitmapInfo createIconBitmap(Intent.ShortcutIconResource iconRes) {
        try {
            Resources resources = this.mPm.getResourcesForApplication(iconRes.packageName);
            if (resources != null) {
                int id = resources.getIdentifier(iconRes.resourceName, null, null);
                return this.createBadgedIconBitmap(resources.getDrawableForDensity(id, this.mFillResIconDpi), Process.myUserHandle(), false);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    public BitmapInfo createIconBitmap(String placeholder, int color) {
        Bitmap placeholderBitmap = Bitmap.createBitmap((int)this.mIconBitmapSize, (int)this.mIconBitmapSize, (Bitmap.Config)Bitmap.Config.ARGB_8888);
        this.mTextPaint.setColor(color);
        Canvas canvas = new Canvas(placeholderBitmap);
        canvas.drawText(placeholder, (float)(this.mIconBitmapSize / 2), (float)(this.mIconBitmapSize * 5 / 8), this.mTextPaint);
        AdaptiveIconDrawable drawable2 = new AdaptiveIconDrawable((Drawable)new ColorDrawable(PLACEHOLDER_BACKGROUND_COLOR), (Drawable)new BitmapDrawable(this.mContext.getResources(), placeholderBitmap));
        Bitmap icon = this.createIconBitmap((Drawable)drawable2, 0.92f);
        return BitmapInfo.of(icon, this.extractColor(icon));
    }

    public BitmapInfo createIconBitmap(Bitmap icon) {
        if (this.mIconBitmapSize != icon.getWidth() || this.mIconBitmapSize != icon.getHeight()) {
            icon = this.createIconBitmap((Drawable)new BitmapDrawable(this.mContext.getResources(), icon), 1.0f);
        }
        return BitmapInfo.of(icon, this.extractColor(icon));
    }

    public BitmapInfo createShapedIconBitmap(Bitmap icon, UserHandle user) {
        Drawable d = new FixedSizeBitmapDrawable(icon);
        float inset = AdaptiveIconDrawable.getExtraInsetFraction();
        inset /= 1.0f + 2.0f * inset;
        d = new AdaptiveIconDrawable(new ColorDrawable(-16777216), new InsetDrawable(d, inset, inset, inset, inset));
        return this.createBadgedIconBitmap(d, user, true);
    }

    public BitmapInfo createBadgedIconBitmap(Drawable icon, UserHandle user, boolean shrinkNonAdaptiveIcons) {
        return this.createBadgedIconBitmap(icon, user, shrinkNonAdaptiveIcons, false, null);
    }

    public BitmapInfo createBadgedIconBitmap(Drawable icon, UserHandle user, int iconAppTargetSdk) {
        return this.createBadgedIconBitmap(icon, user, iconAppTargetSdk, false);
    }

    public BitmapInfo createBadgedIconBitmap(Drawable icon, UserHandle user, int iconAppTargetSdk, boolean isInstantApp) {
        return this.createBadgedIconBitmap(icon, user, iconAppTargetSdk, isInstantApp, null);
    }

    public BitmapInfo createBadgedIconBitmap(Drawable icon, UserHandle user, int iconAppTargetSdk, boolean isInstantApp, float[] scale) {
        boolean shrinkNonAdaptiveIcons = true;
        return this.createBadgedIconBitmap(icon, user, shrinkNonAdaptiveIcons, isInstantApp, scale);
    }

    public Bitmap createScaledBitmapWithoutShadow(Drawable icon, int iconAppTargetSdk) {
        boolean shrinkNonAdaptiveIcons = true;
        return this.createScaledBitmapWithoutShadow(icon, shrinkNonAdaptiveIcons);
    }

    public BitmapInfo createBadgedIconBitmap(@NonNull Drawable icon, UserHandle user, boolean shrinkNonAdaptiveIcons, boolean isInstantApp, float[] scale) {
        if (scale == null) {
            scale = new float[1];
        }
        icon = this.normalizeAndWrapToAdaptiveIcon(icon, shrinkNonAdaptiveIcons, null, scale);
        Bitmap bitmap = this.createIconBitmap(icon, scale[0]);
        if (icon instanceof AdaptiveIconDrawable) {
            this.mCanvas.setBitmap(bitmap);
            this.getShadowGenerator().recreateIcon(Bitmap.createBitmap((Bitmap)bitmap), this.mCanvas);
            this.mCanvas.setBitmap(null);
        }
        if (isInstantApp) {
            this.badgeWithDrawable(bitmap, this.mContext.getDrawable(R.drawable.ic_instant_app_badge));
        }
        if (user != null) {
            FixedSizeBitmapDrawable drawable2 = new FixedSizeBitmapDrawable(bitmap);
            Drawable badged = this.mPm.getUserBadgedIcon((Drawable)drawable2, user);
            bitmap = badged instanceof BitmapDrawable ? ((BitmapDrawable)badged).getBitmap() : this.createIconBitmap(badged, 1.0f);
        }
        int color = this.extractColor(bitmap);
        return icon instanceof BitmapInfo.Extender ? ((BitmapInfo.Extender)icon).getExtendedInfo(bitmap, color, this, scale[0], user) : BitmapInfo.of(bitmap, color);
    }

    public Bitmap getUserBadgeBitmap(UserHandle user) {
        if (this.mUserBadgeBitmap == null) {
            Bitmap bitmap = Bitmap.createBitmap((int)this.mIconBitmapSize, (int)this.mIconBitmapSize, (Bitmap.Config)Bitmap.Config.ARGB_8888);
            Drawable badgedDrawable = this.mPm.getUserBadgedIcon((Drawable)new FixedSizeBitmapDrawable(bitmap), user);
            if (badgedDrawable instanceof BitmapDrawable) {
                this.mUserBadgeBitmap = ((BitmapDrawable)badgedDrawable).getBitmap();
            } else {
                badgedDrawable.setBounds(0, 0, this.mIconBitmapSize, this.mIconBitmapSize);
                this.mUserBadgeBitmap = BitmapRenderer.createSoftwareBitmap(this.mIconBitmapSize, this.mIconBitmapSize, arg_0 -> ((Drawable)badgedDrawable).draw(arg_0));
            }
        }
        return this.mUserBadgeBitmap;
    }

    public Bitmap createScaledBitmapWithoutShadow(Drawable icon, boolean shrinkNonAdaptiveIcons) {
        RectF iconBounds = new RectF();
        float[] scale = new float[1];
        icon = this.normalizeAndWrapToAdaptiveIcon(icon, shrinkNonAdaptiveIcons, iconBounds, scale);
        return this.createIconBitmap(icon, Math.min(scale[0], ShadowGenerator.getScaleForBounds(iconBounds)));
    }

    public void setBadgeOnLeft(boolean badgeOnLeft) {
        this.mBadgeOnLeft = badgeOnLeft;
    }

    public void setWrapperBackgroundColor(int color) {
        this.mWrapperBackgroundColor = Color.alpha((int)color) < 255 ? -1 : color;
    }

    public void disableColorExtraction() {
        this.mDisableColorExtractor = true;
    }

    private Drawable normalizeAndWrapToAdaptiveIcon(@NonNull Drawable icon, boolean shrinkNonAdaptiveIcons, RectF outIconBounds, float[] outScale) {
        if (icon == null) {
            return null;
        }
        float scale = 1.0f;
        if (shrinkNonAdaptiveIcons) {
            if (this.mWrapperIcon == null) {
                this.mWrapperIcon = this.mContext.getDrawable(R.drawable.adaptive_icon_drawable_wrapper).mutate();
            }
            AdaptiveIconDrawable dr = (AdaptiveIconDrawable)this.mWrapperIcon;
            dr.setBounds(0, 0, 1, 1);
            boolean[] outShape = new boolean[1];
            scale = this.getNormalizer().getScale(icon, outIconBounds, dr.getIconMask(), outShape);
            if (!(icon instanceof AdaptiveIconDrawable) && !outShape[0]) {
                FixedScaleDrawable fsd = (FixedScaleDrawable)dr.getForeground();
                fsd.setDrawable(icon);
                fsd.setScale(scale);
                icon = dr;
                scale = this.getNormalizer().getScale(icon, outIconBounds, null, null);
                ((ColorDrawable)dr.getBackground()).setColor(this.mWrapperBackgroundColor);
            }
        } else {
            scale = this.getNormalizer().getScale(icon, outIconBounds, null, null);
        }
        outScale[0] = scale;
        return icon;
    }

    public void badgeWithDrawable(Bitmap target, Drawable badge) {
        this.mCanvas.setBitmap(target);
        this.badgeWithDrawable(this.mCanvas, badge);
        this.mCanvas.setBitmap(null);
    }

    public void badgeWithDrawable(Canvas target, Drawable badge) {
        int badgeSize = BaseIconFactory.getBadgeSizeForIconSize(this.mIconBitmapSize);
        if (this.mBadgeOnLeft) {
            badge.setBounds(0, this.mIconBitmapSize - badgeSize, badgeSize, this.mIconBitmapSize);
        } else {
            badge.setBounds(this.mIconBitmapSize - badgeSize, this.mIconBitmapSize - badgeSize, this.mIconBitmapSize, this.mIconBitmapSize);
        }
        badge.draw(target);
    }

    private Bitmap createIconBitmap(Drawable icon, float scale) {
        return this.createIconBitmap(icon, scale, this.mIconBitmapSize);
    }

    public Bitmap createIconBitmap(@NonNull Drawable icon, float scale, int size) {
        Bitmap bitmap = Bitmap.createBitmap((int)size, (int)size, (Bitmap.Config)Bitmap.Config.ARGB_8888);
        if (icon == null) {
            return bitmap;
        }
        this.mCanvas.setBitmap(bitmap);
        this.mOldBounds.set(icon.getBounds());
        if (icon instanceof AdaptiveIconDrawable) {
            int offset = Math.max((int)Math.ceil(0.010416667f * (float)size), Math.round((float)size * (1.0f - scale) / 2.0f));
            icon.setBounds(0, 0, size - 2 * offset, size - 2 * offset);
            this.mCanvas.translate((float)offset, (float)offset);
            if (icon instanceof BitmapInfo.Extender) {
                ((BitmapInfo.Extender)icon).drawForPersistence(this.mCanvas);
            } else {
                icon.draw(this.mCanvas);
            }
            this.mCanvas.translate((float)(-offset), (float)(-offset));
        } else {
            if (icon instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable)icon;
                Bitmap b = bitmapDrawable.getBitmap();
                if (bitmap != null && b.getDensity() == 0) {
                    bitmapDrawable.setTargetDensity(this.mContext.getResources().getDisplayMetrics());
                }
            }
            int width = size;
            int height = size;
            int intrinsicWidth = icon.getIntrinsicWidth();
            int intrinsicHeight = icon.getIntrinsicHeight();
            if (intrinsicWidth > 0 && intrinsicHeight > 0) {
                float ratio = (float)intrinsicWidth / (float)intrinsicHeight;
                if (intrinsicWidth > intrinsicHeight) {
                    height = (int)((float)width / ratio);
                } else if (intrinsicHeight > intrinsicWidth) {
                    width = (int)((float)height * ratio);
                }
            }
            int left = (size - width) / 2;
            int top = (size - height) / 2;
            icon.setBounds(left, top, left + width, top + height);
            this.mCanvas.save();
            this.mCanvas.scale(scale, scale, (float)(size / 2), (float)(size / 2));
            icon.draw(this.mCanvas);
            this.mCanvas.restore();
        }
        icon.setBounds(this.mOldBounds);
        this.mCanvas.setBitmap(null);
        return bitmap;
    }

    @Override
    public void close() {
        this.clear();
    }

    public BitmapInfo makeDefaultIcon(UserHandle user) {
        return this.createBadgedIconBitmap(BaseIconFactory.getFullResDefaultActivityIcon(this.mFillResIconDpi), user, Build.VERSION.SDK_INT);
    }

    public static Drawable getFullResDefaultActivityIcon(int iconDpi) {
        return Resources.getSystem().getDrawableForDensity(17301651, iconDpi);
    }

    public BitmapInfo badgeBitmap(Bitmap source, BitmapInfo badgeInfo) {
        Bitmap icon = BitmapRenderer.createHardwareBitmap(this.mIconBitmapSize, this.mIconBitmapSize, c -> {
            this.getShadowGenerator().recreateIcon(source, c);
            this.badgeWithDrawable(c, (Drawable)new FixedSizeBitmapDrawable(badgeInfo.icon));
        });
        return BitmapInfo.of(icon, badgeInfo.color);
    }

    private int extractColor(Bitmap bitmap) {
        return this.mDisableColorExtractor ? 0 : this.mColorExtractor.findDominantColorByHue(bitmap);
    }

    public static int getBadgeSizeForIconSize(int iconSize) {
        return (int)(0.444f * (float)iconSize);
    }

    private static class FixedSizeBitmapDrawable
    extends BitmapDrawable {
        public FixedSizeBitmapDrawable(Bitmap bitmap) {
            super(null, bitmap);
        }

        public int getIntrinsicHeight() {
            return this.getBitmap().getWidth();
        }

        public int getIntrinsicWidth() {
            return this.getBitmap().getWidth();
        }
    }
}
