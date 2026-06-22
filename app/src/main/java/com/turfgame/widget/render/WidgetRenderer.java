package com.turfgame.widget.render;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import com.turfgame.widget.R;
import com.turfgame.widget.Settings;
import com.turfgame.widget.SettingsActivity;
import com.turfgame.widget.TurfWidget;
import com.turfgame.widget.model.CharStats;
import com.turfgame.widget.net.TurfApiException;

/**
 * Builds the widget's {@link RemoteViews} and pushes them to every instance of
 * the widget. All click handlers are (re)bound on every full update so the
 * widget can never end up displayed without working tap targets.
 */
public final class WidgetRenderer {

    private WidgetRenderer() {
    }

    /**
     * Shows the loading spinner without disturbing the currently displayed
     * stats. Uses a partial update so the existing images and click handlers
     * are preserved while the network request is in flight.
     */
    public static void showLoading(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), layoutId(context));
        views.setViewVisibility(R.id.refresh, View.GONE);
        views.setViewVisibility(R.id.ProgressBarWrapper, View.VISIBLE);
        pushPartial(context, views);
    }

    public static void showStats(Context context, CharStats stats, boolean zonesLost) {
        RemoteViews views = baseView(context);
        CustomText text = new CustomText(context);

        views.setTextViewText(R.id.error, "");
        views.setImageViewBitmap(R.id.points, text.points(stats.points()));
        views.setImageViewBitmap(R.id.hour, text.pointsPerHour(stats.pointsPerHour()));
        views.setImageViewBitmap(R.id.zones, text.zones(stats.zones()));
        views.setImageViewResource(R.id.placestar, R.drawable.star);
        views.setImageViewBitmap(R.id.place, text.place(stats.place()));
        views.setImageViewResource(R.id.refresh, zonesLost ? R.drawable.alert : R.drawable.refresh);

        pushFull(context, views);
    }

    public static void showError(Context context, TurfApiException.Reason reason) {
        RemoteViews views = baseView(context);
        views.setTextViewText(R.id.error, context.getString(messageFor(reason)));
        clearImages(views);
        views.setImageViewResource(R.id.refresh, R.drawable.refresh);
        pushFull(context, views);
    }

    private static RemoteViews baseView(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), layoutId(context));
        views.setViewVisibility(R.id.ProgressBarWrapper, View.GONE);
        views.setViewVisibility(R.id.refresh, View.VISIBLE);
        return views;
    }

    private static void clearImages(RemoteViews views) {
        views.setImageViewResource(R.id.points, R.drawable.empty);
        views.setImageViewResource(R.id.hour, R.drawable.empty);
        views.setImageViewResource(R.id.zones, R.drawable.empty);
        views.setImageViewResource(R.id.placestar, R.drawable.empty);
        views.setImageViewResource(R.id.place, R.drawable.empty);
    }

    private static int messageFor(TurfApiException.Reason reason) {
        switch (reason) {
            case NO_ACCOUNT:
                return R.string.error_no_account;
            case NETWORK:
                return R.string.error_network;
            case SERVER:
                return R.string.error_server;
            case PARSE:
            default:
                return R.string.error_parse;
        }
    }

    private static int layoutId(Context context) {
        return "widgetclean".equals(Settings.layout(context)) ? R.layout.widgetclean : R.layout.widget;
    }

    private static void bindClickHandlers(Context context, RemoteViews views) {
        views.setOnClickPendingIntent(R.id.option,
                activity(context, 0, new Intent(context, SettingsActivity.class)));

        PendingIntent launch = broadcast(context, 1,
                new Intent(context, TurfWidget.class).setAction(TurfWidget.ACTION_LAUNCH_TURF));
        views.setOnClickPendingIntent(R.id.TextWrapper, launch);
        views.setOnClickPendingIntent(R.id.error, launch);

        views.setOnClickPendingIntent(R.id.refresh,
                broadcast(context, 2,
                        new Intent(context, TurfWidget.class).setAction(TurfWidget.ACTION_REFRESH)));
    }

    private static void pushFull(Context context, RemoteViews views) {
        bindClickHandlers(context, views);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = widgetIds(context, manager);
        if (ids.length > 0) {
            manager.updateAppWidget(ids, views);
        }
    }

    private static void pushPartial(Context context, RemoteViews views) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = widgetIds(context, manager);
        if (ids.length > 0) {
            manager.partiallyUpdateAppWidget(ids, views);
        }
    }

    private static int[] widgetIds(Context context, AppWidgetManager manager) {
        return manager.getAppWidgetIds(new ComponentName(context, TurfWidget.class));
    }

    private static PendingIntent broadcast(Context context, int requestCode, Intent intent) {
        return PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | immutableFlag());
    }

    private static PendingIntent activity(Context context, int requestCode, Intent intent) {
        return PendingIntent.getActivity(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | immutableFlag());
    }

    private static int immutableFlag() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0;
    }
}
