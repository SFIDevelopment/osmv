package at.the.gogo.windig.widget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;
import at.the.gogo.windig.R;
import at.the.gogo.windig.dto.WindEntry;
import at.the.gogo.windig.dto.WindEntryHolder;
import at.the.gogo.windig.util.Util;
import at.the.gogo.windig.util.WindInfoHandler;

public class UpdateWidgetService extends Service {

    @Override
    public int onStartCommand(final Intent intent, final int flags,
            final int startId) {
        Util.d("UpdateWidgetService - Called");

        final AppWidgetManager appWidgetManager = AppWidgetManager
                .getInstance(getApplicationContext());

        final int[] appWidgetIds = intent
                .getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
        if (appWidgetIds.length > 0) {

            for (final int widgetId : appWidgetIds) {

                final WindigWidgetModel model = WindigWidgetModel
                        .retrieveModel(getApplicationContext(), widgetId);
                if (model != null) {
                    final int siteIx = Integer.parseInt(model.getSite());
                    final int speedIx = Integer.parseInt(model.getSpeed());

                    final WindEntry windEntry = WindEntryHolder.getInstance()
                            .getLastEntry(siteIx, false);

                    if (windEntry != null) {
                        final String[] site_title = getResources()
                                .getStringArray(R.array.site_url_title);

                        final RemoteViews remoteViews = new RemoteViews(
                                getPackageName(), R.layout.widget);

                        remoteViews.setTextViewText(R.id.site_title,
                                site_title[siteIx]);

                        remoteViews
                                .setImageViewResource(
                                        R.id.winddirectionimg,
                                        WindInfoHandler.getIconBasedInSpeedAndDegree(
                                                Double.parseDouble(windEntry
                                                        .getValue(4).replace(
                                                                ",", ".")),
                                                Integer.parseInt(windEntry
                                                        .getValue(3).replace(
                                                                ",", ".")),
                                                WindInfoHandler.minDegrees[siteIx],
                                                WindInfoHandler.maxDegrees[siteIx]));

                        setSpeed(remoteViews, windEntry, 2, R.id.windspeedmax,
                                speedIx);
                        setSpeed(remoteViews, windEntry, 4, R.id.windspeedavg,
                                speedIx);

                        remoteViews.setTextViewText(R.id.winddirection,
                                windEntry.getValue(3)
                                        + WindInfoHandler.postfixsshort[3]);
                        remoteViews.setTextViewText(R.id.temperature,
                                windEntry.getValue(5)
                                        + WindInfoHandler.postfixsshort[5]);
                        remoteViews.setTextViewText(R.id.huminity,
                                windEntry.getValue(6)
                                        + WindInfoHandler.postfixsshort[6]);
                        remoteViews.setTextViewText(
                                R.id.refreshtime,
                                windEntry.getValue(0) + " "
                                        + windEntry.getValue(1));

                        appWidgetManager.updateAppWidget(widgetId, remoteViews);
                    }
                }
            }
            stopSelf();
        }
        return Service.START_NOT_STICKY;

    }

    private void setSpeed(final RemoteViews remoteViews,
            final WindEntry windEntry, final int ix, final int resIx,
            final int speedIx) {

        final int speed = WindInfoHandler.convertSpeed(
                Double.parseDouble(windEntry.getValue(ix).replace(",", ".")),
                ix);
        final String text = speed + " "
                + getResources().getStringArray(R.array.speed_unit_title) // WindInfoHandler.speedText[speedIx]
                + WindInfoHandler.postfixsshort[ix];
        remoteViews.setTextViewText(resIx, text);

        remoteViews.setTextColor(resIx, WindInfoHandler.getSpeedColor(speed));

        // ......
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }
}
