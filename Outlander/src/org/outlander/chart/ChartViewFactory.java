package org.outlander.chart;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.CubicLineChart;
import org.achartengine.chart.LineChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.outlander.model.PoiPoint;
import org.outlander.model.Route;
import org.outlander.model.Track;
import org.outlander.model.TrackPoint;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;

/**
 * Visualization of the trackinformation.
 * 
 * @author Jim Fandango
 */
public class ChartViewFactory {

    public static GraphicalView getRouteChartView(final Context context, final Route route) {

        GraphicalView gView;

        final int[] colors = new int[] { Color.GREEN };
        final PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE };
        

        final XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        final XYMultipleSeriesRenderer renderers = new XYMultipleSeriesRenderer();

        final XYSeries trackHeight = new XYSeries("Height");

        int i = 0;

        for (final PoiPoint point : route.getPoints()) {
            i++;

            trackHeight.add(i, point.getAlt());
        }

        dataset.addSeries(trackHeight);

        setRenderer(renderers, colors, styles);

        renderers.setAntialiasing(true);
        renderers.setChartTitle("Route Info");

        renderers.setShowGrid(true);
        renderers.setXLabelsAlign(Align.RIGHT);
        renderers.setYLabelsAlign(Align.RIGHT);
        renderers.setZoomButtonsVisible(false);
        renderers.setApplyBackgroundColor(true);
        renderers.setBackgroundColor(Color.TRANSPARENT);
        renderers.setMarginsColor(0x00FF0000);
        

        final String[] types = new String[] { CubicLineChart.TYPE };

        gView = ChartFactory.getCombinedXYChartView(context, dataset, renderers, types);

        gView.setBackgroundColor(Color.TRANSPARENT);

        return gView;

    }

    public static GraphicalView getTrackChartView(final Context context, final Track track) {

        GraphicalView gView;

        final int[] colors = new int[] { Color.GREEN, Color.YELLOW };
        final PointStyle[] styles = new PointStyle[] { PointStyle.POINT, PointStyle.POINT };

        final XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        final XYMultipleSeriesRenderer renderers = new XYMultipleSeriesRenderer();

        final XYSeries trackSpeedAvg = new XYSeries("Speed");
        final XYSeries trackHeight = new XYSeries("Height");

        int i = 0;

        for (final TrackPoint trackPoint : track.getPoints()) {
            i++;
            trackSpeedAvg.add(i, trackPoint.speed);
            trackHeight.add(i, trackPoint.alt);
        }

        dataset.addSeries(trackSpeedAvg);
        dataset.addSeries(trackHeight);

        setRenderer(renderers, colors, styles);

        renderers.setAntialiasing(true);
        renderers.setChartTitle("Track Info");

        renderers.setShowGrid(true);
        renderers.setXLabelsAlign(Align.RIGHT);
        renderers.setYLabelsAlign(Align.RIGHT);
        renderers.setZoomButtonsVisible(false);
        renderers.setApplyBackgroundColor(true);
        renderers.setBackgroundColor(Color.TRANSPARENT);
        renderers.setMarginsColor(0x00FF0000);

        final String[] types = new String[] { CubicLineChart.TYPE, LineChart.TYPE };

        gView = ChartFactory.getCombinedXYChartView(context, dataset, renderers, types);

        gView.setBackgroundColor(Color.TRANSPARENT);

        return gView;

    }

    protected static void setRenderer(final XYMultipleSeriesRenderer renderers, final int[] colors, final PointStyle[] styles) {

        renderers.setAxisTitleTextSize(16);
        renderers.setChartTitleTextSize(20);
        renderers.setLabelsTextSize(15);
        renderers.setLegendTextSize(15);
        renderers.setPointSize(5f);
        renderers.setMargins(new int[] { 20, 30, 15, 20 });
        final int length = colors.length;
        for (int i = 0; i < length; i++) {
            final XYSeriesRenderer renderer = new XYSeriesRenderer();
            if (i == 0) {
                renderer.setFillBelowLine(i == 0);
                renderer.setFillBelowLineColor(Color.argb(0x29, 0xFF, 0x00, 0x00));
            }
            renderer.setColor(colors[i]);
            renderer.setLineWidth(3f);
            renderer.setPointStyle(styles[i]);

            renderers.addSeriesRenderer(renderer);

        }

    }

}
