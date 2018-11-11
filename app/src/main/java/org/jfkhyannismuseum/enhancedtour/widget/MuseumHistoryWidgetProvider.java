package org.jfkhyannismuseum.enhancedtour.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import org.jfkhyannismuseum.enhancedtour.MainActivity;
import org.jfkhyannismuseum.enhancedtour.database.HistoryEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of App Widget functionality.
 */
public class MuseumHistoryWidgetProvider extends AppWidgetProvider {
    private static final String WIDGET_LOG_TAG = "WidgetProvider";
    public static List<HistoryEntry> mHistory = new ArrayList<>();
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), org.jfkhyannismuseum.enhancedtour.R.layout.museum_history_widget);
        // Set the adapter for the ListView
        Intent intent = new Intent(context, HistoryWidgetService.class);
        views.setRemoteAdapter(org.jfkhyannismuseum.enhancedtour.R.id.widget_list_history, intent);
        // Handle case when no history is found
        views.setEmptyView(org.jfkhyannismuseum.enhancedtour.R.id.widget_list_history, org.jfkhyannismuseum.enhancedtour.R.id.widget_text_no_history);

        // Pending intent to open the app's MainActivity for the given exhibit piece.
        Intent onClickIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, onClickIntent, 0);
        // Set as a PendingIntentTemplate because the HistoryWidgetService needs to fill in the id
        // of the piece that was clicked.
        views.setPendingIntentTemplate(org.jfkhyannismuseum.enhancedtour.R.id.widget_list_history, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
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

}

