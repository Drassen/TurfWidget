package com.turfgame.widget.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;

/**
 * Renders the widget's stat values into bitmaps using the bundled "Insane
 * Hours" font. The typeface is loaded once and cached, since it was previously
 * re-read from assets on every single value drawn.
 */
public final class CustomText {

    private static final String FONT_ASSET = "fonts/Insanehours2.ttf";

    private static final int FONT_SIZE_SMALL = 16;
    private static final int FONT_SIZE_BIG = 20;

    private static final int LIGHT_COLOR = Color.argb(240, 255, 255, 255);
    private static final int GRAY_COLOR = Color.argb(240, 200, 200, 200);
    private static final int YELLOW_COLOR = Color.argb(255, 255, 222, 0);

    private static volatile Typeface cachedTypeface;

    private final float scale;
    private final Typeface typeface;

    public CustomText(Context context) {
        this.scale = context.getResources().getDisplayMetrics().density;
        this.typeface = loadTypeface(context);
    }

    private static Typeface loadTypeface(Context context) {
        Typeface typeface = cachedTypeface;
        if (typeface == null) {
            synchronized (CustomText.class) {
                typeface = cachedTypeface;
                if (typeface == null) {
                    typeface = Typeface.createFromAsset(context.getAssets(), FONT_ASSET);
                    cachedTypeface = typeface;
                }
            }
        }
        return typeface;
    }

    public Bitmap points(int value) {
        return textWithSuffix(Integer.toString(value), "p", FONT_SIZE_BIG);
    }

    public Bitmap pointsPerHour(int value) {
        return textWithPrefix(Integer.toString(value), "+", FONT_SIZE_SMALL);
    }

    public Bitmap zones(int value) {
        return textWithSuffix(Integer.toString(value), "z", FONT_SIZE_SMALL);
    }

    public Bitmap place(int value) {
        return plainText(Integer.toString(value), FONT_SIZE_SMALL);
    }

    private Paint newPaint(int fontSize) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setTypeface(typeface);
        paint.setTextSize(fontSize * scale);
        paint.setTextAlign(Align.LEFT);
        return paint;
    }

    private Bitmap plainText(String text, int fontSize) {
        Paint paint = newPaint(fontSize);
        paint.setColor(GRAY_COLOR);

        Paint.FontMetricsInt metrics = paint.getFontMetricsInt();
        int height = metrics.bottom - metrics.top;

        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int width = Math.max(1, bounds.right - bounds.left);

        Bitmap bitmap = Bitmap.createBitmap(width, Math.max(1, height), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(text, -bounds.left, height - metrics.bottom, paint);
        return bitmap;
    }

    private Bitmap textWithSuffix(String text, String suffix, int fontSize) {
        Paint paint = newPaint(fontSize);
        paint.setColor(YELLOW_COLOR);

        String full = text + suffix;
        Paint.FontMetricsInt metrics = paint.getFontMetricsInt();
        int height = metrics.bottom - metrics.top;

        Rect bounds = new Rect();
        paint.getTextBounds(full, 0, full.length(), bounds);
        int width = Math.max(1, bounds.right - bounds.left);

        Bitmap bitmap = Bitmap.createBitmap(width, Math.max(1, height), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        int baseline = height - metrics.bottom;

        canvas.drawText(text, -bounds.left, baseline, paint);

        paint.setColor(LIGHT_COLOR);
        float textWidth = paint.measureText(text);
        paint.setTextSize((fontSize - 2) * scale);
        canvas.drawText(suffix, -bounds.left + textWidth, baseline, paint);
        return bitmap;
    }

    private Bitmap textWithPrefix(String text, String prefix, int fontSize) {
        Paint paint = newPaint(fontSize);

        String full = prefix + text;
        Paint.FontMetricsInt metrics = paint.getFontMetricsInt();
        int height = metrics.bottom - metrics.top;

        Rect bounds = new Rect();
        paint.getTextBounds(full, 0, full.length(), bounds);
        int width = Math.max(1, bounds.right - bounds.left);

        Bitmap bitmap = Bitmap.createBitmap(width, Math.max(1, height), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        int baseline = height - metrics.bottom;

        paint.setColor(LIGHT_COLOR);
        paint.setTextSize((fontSize - 2) * scale);
        canvas.drawText(prefix, -bounds.left, baseline, paint);
        float prefixWidth = paint.measureText(prefix);

        paint.setColor(YELLOW_COLOR);
        paint.setTextSize(fontSize * scale);
        canvas.drawText(text, -bounds.left + prefixWidth, baseline, paint);
        return bitmap;
    }
}
