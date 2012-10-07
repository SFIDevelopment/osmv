package at.the.gogo.windig.fragments;

import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.LineChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import at.the.gogo.windig.R;
import at.the.gogo.windig.dto.WindEntry;
import at.the.gogo.windig.dto.WindEntryHolder;
import at.the.gogo.windig.util.Util;

public class GraphFragment extends Fragment {

	public final static int PAGE_TEMPSPEED = 0;
	public final static int PAGE_DIRECTION = 1;

	View view;
	GraphicalView mChartView1;
	GraphicalView mChartView2;
	int whichGraphToShow;

	public static GraphFragment newInstance(final int activeSiteIx,
			final int whichGraphToShow) {
		final GraphFragment f = new GraphFragment();
		f.whichGraphToShow = whichGraphToShow;

		// Supply num input as an argument.
		// Bundle args = new Bundle();
		// args.putInt("num", num);
		// f.setArguments(args);

		return f;
	}

	@Override
	public void onDestroy() {
		Util.d("on destroy");
		super.onDestroy();
	}

	@Override
	public void onPause() {
		Util.d("on pause");
		super.onPause();
	}

	public void refreshData(final boolean forceRefresh) {
		mChartView1 = getChartView(view, GraphFragment.PAGE_TEMPSPEED);
		mChartView2 = getChartView(view, GraphFragment.PAGE_DIRECTION);

	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.graph, container, false);
		refreshData(true);

		return view;
	}

	private GraphicalView getChartView(final View view, final int ix) {
		GraphicalView gView;
		final int[] colors = (ix == GraphFragment.PAGE_TEMPSPEED) ? new int[] {
				Color.GREEN, Color.YELLOW, Color.MAGENTA }
				: new int[] { Color.RED };
		final PointStyle[] styles = (ix == GraphFragment.PAGE_TEMPSPEED) ? new PointStyle[] {
				PointStyle.POINT, PointStyle.POINT, PointStyle.POINT }
				: new PointStyle[] { PointStyle.POINT };

		final XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		final XYMultipleSeriesRenderer renderers = new XYMultipleSeriesRenderer();

		final SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		//
		final int activeSite = Integer.parseInt(pref
				.getString("pref_site", "0"));

		final List<WindEntry> entries = WindEntryHolder.getInstance()
				.getEntries(activeSite, false);

		XYSeries windSpeedAvg = null;
		XYSeries windSpeedMax = null;
		XYSeries temperature = null;
		XYSeries direction = null;
		if ((ix == GraphFragment.PAGE_TEMPSPEED)) {
			windSpeedAvg = new XYSeries(getActivity().getText(
					R.string.graph_windspeed_avg).toString());
			windSpeedMax = new XYSeries(getActivity().getText(
					R.string.graph_windspeed_max).toString());
			temperature = new XYSeries(getActivity().getText(
					R.string.graph_temp).toString());
		} else {
			direction = new XYSeries(getActivity().getText(R.string.graph_dir)
					.toString());
		}
		int i = 0;

		for (final WindEntry entry : entries) {

			i++;
			if (entry != null) {
				if ((ix == GraphFragment.PAGE_TEMPSPEED)) { // TODO: we got a
															// NPE
															// here
					windSpeedMax.add(
							i,
							Double.parseDouble(entry.getValue(2).replace(",",
									".")));
					windSpeedAvg.add(
							i,
							Double.parseDouble(entry.getValue(4).replace(",",
									".")));
					temperature.add(
							i,
							Double.parseDouble(entry.getValue(5).replace(",",
									".")));
				} else {
					direction.add(
							i,
							Double.parseDouble(entry.getValue(3).replace(",",
									".")));
				}
			}
		}

		if ((ix == GraphFragment.PAGE_TEMPSPEED)) {
			dataset.addSeries(windSpeedAvg);

			dataset.addSeries(windSpeedMax);
			dataset.addSeries(temperature);
		} else {
			dataset.addSeries(direction);
		}

		setRenderer(renderers, colors, styles);

		renderers.setAntialiasing(true);
		renderers
				.setChartTitle((ix == GraphFragment.PAGE_TEMPSPEED) ? getActivity()
						.getText(R.string.graph_title1).toString()
						: getActivity().getText(R.string.graph_title2)
								.toString());

		renderers.setShowGrid(true);
		renderers.setXLabelsAlign(Align.RIGHT);
		renderers.setYLabelsAlign(Align.RIGHT);
		renderers.setZoomButtonsVisible(false);
		renderers.setApplyBackgroundColor(true);
		renderers.setBackgroundColor(Color.TRANSPARENT);
		renderers.setMarginsColor(0x00FF0000);

		final String[] types = (ix == GraphFragment.PAGE_TEMPSPEED) ? new String[] {
				LineChart.TYPE, LineChart.TYPE, LineChart.TYPE }
				: new String[] { LineChart.TYPE };

		gView = ChartFactory.getCombinedXYChartView(getActivity(), dataset,
				renderers, types);
		gView.setBackgroundColor(Color.TRANSPARENT);

		final LinearLayout layout = (LinearLayout) view
				.findViewById((ix == GraphFragment.PAGE_TEMPSPEED) ? R.id.chart1
						: R.id.chart2);
		layout.addView(gView);

		return gView;

	}

	protected void setRenderer(final XYMultipleSeriesRenderer renderers,
			final int[] colors, final PointStyle[] styles) {
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
				final int red = (colors[i] >> 16) & 0x000000FF;
				final int green = (colors[i] >> 8) & 0x000000FF;
				final int blue = (colors[i]) & 0x000000FF;

				renderer.setFillBelowLine(true); // i == length - 1
				renderer.setFillBelowLineColor(Color.argb(0x29, red, green,
						blue));
			}

			renderer.setColor(colors[i]);
			renderer.setLineWidth(3f);
			renderer.setPointStyle(styles[i]);

			renderers.addSeriesRenderer(renderer);

		}

	}

}
