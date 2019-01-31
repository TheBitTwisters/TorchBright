package com.tbt.apps.torchbright;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class TorchWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.key_switch_torch), Context.MODE_PRIVATE);
            boolean isTorchOn = preferences.getBoolean(context.getString(R.string.value_switch_torch), false);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.torch_widget);
            if (isTorchOn) views.setImageViewResource(R.id.btn_torch_widget, R.drawable.on);
            else views.setImageViewResource(R.id.btn_torch_widget, R.drawable.off);
            appWidgetManager.updateAppWidget(appWidgetId, views);
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.torch_widget);
        Intent intent = new Intent(context.getString(R.string.action_process_torch));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.btn_torch_widget, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}

