package at.the.gogo.windig.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

public class WindigWidgetProvider extends AppWidgetProvider {

    private static final String tag              = "WindigWidgetProvider";

    public static final String  APPWIDGET_UPDATE = "at.the.gogo.windig.wigdet.WIDGET_UPDATE";

    @Override
    public void onUpdate(final Context context,
            final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        Log.d(WindigWidgetProvider.tag, "onUpdate called");
        final int N = appWidgetIds.length;
        Log.d(WindigWidgetProvider.tag, "Number of widgets:" + N);
        startUpdateService(context, appWidgetIds);
        // for (int i = 0; i < N; i++) {
        // int appWidgetId = appWidgetIds[i];
        // updateAppWidget(context, appWidgetManager, appWidgetId);
        // }
    }

    @Override
    public void onDeleted(final Context context, final int[] appWidgetIds) {
        Log.d(WindigWidgetProvider.tag, "onDelete called");
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            Log.d(WindigWidgetProvider.tag, "deleting:" + appWidgetIds[i]);
            final WindigWidgetModel bwm = WindigWidgetModel.retrieveModel(
                    context, appWidgetIds[i]);
            if (bwm != null) {
                bwm.removePrefs(context);
            }
        }
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
            final Bundle extras = intent.getExtras();

            final int appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                onDeleted(context, new int[] { appWidgetId });
            }
        } else if ((WindigWidgetProvider.APPWIDGET_UPDATE.equals(action))
                || (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action))) {
            updateAppWidget(context, AppWidgetManager.getInstance(context));

        } else {
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onEnabled(final Context context) {
        Log.d(WindigWidgetProvider.tag, "onEnabled called");
        WindigWidgetModel.clearAllPreferences(context);
        final PackageManager packetManager = context.getPackageManager();
        packetManager.setComponentEnabledSetting(new ComponentName(
                "at.the.gogo.windig", ".widget.WindigWidgetProvider"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onDisabled(final Context context) {
        Log.d(WindigWidgetProvider.tag, "onDisabled called");
        WindigWidgetModel.clearAllPreferences(context);
        final PackageManager packetManager = context.getPackageManager();
        packetManager.setComponentEnabledSetting(new ComponentName(
                "at.the.gogo.windig", ".widget.WindigWidgetProvider"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void updateAppWidget(final Context context,
            final AppWidgetManager appWidgetManager) {
        final ComponentName thisWidget = new ComponentName(context,
                WindigWidgetProvider.class);
        final int[] widgetId = appWidgetManager.getAppWidgetIds(thisWidget);
        startUpdateService(context, widgetId);

        // for (int i = 0; i < widgetId.length; i++) {
        // updateAppWidget(context, appWidgetManager, widgetId[i]);
        // }

    }

    private void startUpdateService(final Context context, final int[] widgetId) {
        final Intent intent = new Intent(context.getApplicationContext(),
                UpdateWidgetService.class);

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetId);
        context.startService(intent);
    }

    // private void updateAppWidget(final Context context,
    // final AppWidgetManager appWidgetManager, final int appWidgetId) {
    // final WindigWidgetModel widgetModel = WindigWidgetModel.retrieveModel(
    // context, appWidgetId);
    // if (widgetModel == null) {
    // Log.d(WindigWidgetProvider.tag, "No widget model found for:"
    // + appWidgetId);
    // return;
    // }
    // // Build the intent to call the service
    // final Intent intent = new Intent(context.getApplicationContext(),
    // UpdateWidgetService.class);
    // final int[] ids = { appWidgetId };
    // intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
    //
    // // To react to a click we have to use a pending intent as the
    // // onClickListener is
    // // executed by the homescreen application
    // final PendingIntent pendingIntent = PendingIntent.getService(
    // context.getApplicationContext(), 0, intent,
    // PendingIntent.FLAG_UPDATE_CURRENT);
    //
    // final RemoteViews remoteViews = new RemoteViews(
    // context.getPackageName(), R.layout.widget);
    // remoteViews.setOnClickPendingIntent(R.id.refreshButton, pendingIntent);
    //
    // // Finally update all widgets with the information about the click
    // // listener
    // appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    //
    // // Update the widgets via the service
    // context.startService(intent);
    //
    // // ConfigureBDayWidgetActivity.updateAppWidget(context,
    // // appWidgetManager,
    // // bwm);
    // }

}
