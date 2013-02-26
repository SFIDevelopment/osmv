package at.the.gogo.windig.util;

import android.graphics.Color;
import at.the.gogo.windig.R;

public class WindInfoHandler {

	public final static int[] red_arrows = { R.drawable.arrow_n_red,
			R.drawable.arrow_ne_red, R.drawable.arrow_e_red,
			R.drawable.arrow_se_red, R.drawable.arrow_s_red,
			R.drawable.arrow_sw_red, R.drawable.arrow_w_red,
			R.drawable.arrow_nw_red, };

	public final static int[] green_arrows = { R.drawable.arrow_n_green,
			R.drawable.arrow_ne_green, R.drawable.arrow_e_green,
			R.drawable.arrow_se_green, R.drawable.arrow_s_green,
			R.drawable.arrow_sw_green, R.drawable.arrow_w_green,
			R.drawable.arrow_nw_green, };

	public final static int[] yellow_arrows = { R.drawable.arrow_n_yellow,
			R.drawable.arrow_ne_yellow, R.drawable.arrow_e_yellow,
			R.drawable.arrow_se_yellow, R.drawable.arrow_s_yellow,
			R.drawable.arrow_sw_yellow, R.drawable.arrow_w_yellow,
			R.drawable.arrow_nw_yellow, };

	public final static String[] site_urls = {
			"http://www.zonga.biz/spitzerberg/windmessung/Stationsdaten.html",
			"http://members.aon.at/himre/bilder/Stationsdaten.html",
			"http://www.zonga.biz/spitzerberg/windmessung/Stationsdaten.html" };

	public final static int[] speedZones = { 15, 30 };
	public final static int[] minDegrees = { 220, 100, 100 };
	public final static int[] maxDegrees = { 359, 200, 200 };

	// http://www.engineeringtoolbox.com/velocity-units-converter-d_1035.html
	public final static double speedConversionFactors[] = { 1, 0.278, 1.94,
			0.621371192, 0.91 };
	// public final static String speedText[] = { "km/h", "m/s",
	// "knt", "mi/h", "ft/s" };

	public final static String postfixs[] = { "", "", "(max)", "°", "", "°C",
			"% huminity" };
	// (ave)

	public final static String postfixsshort[] = { "", "", " (max)", "°", "",
			"°C", "%" };

	public static int getArrowIndex(final int degree) {
		int index = 0;

		final int divisor = (360 / WindInfoHandler.red_arrows.length);

		index = (degree / divisor);

		if (index >= WindInfoHandler.red_arrows.length) {
			index = 0;
		}

		return index;
	}

	public static int getIconBasedInSpeedAndDegree(final double speed,
			final int degree, final int goodmin, final int goodmax) {

		final int iconIndex = getArrowIndex(degree);

		int arrowId = 0;
		int colorx = 0;

		if (speed > 30) {
			colorx = 0; // arrowId = red_arrows[iconIndex];
		} else if ((speed > 15) || (speed < 26)) {
			colorx = 1; // arrowId = green_arrows[iconIndex];
		} else {
			colorx = 2; // arrowId = yellow_arrows[iconIndex];
		}

		if (colorx == 1) {
			if ((degree >= goodmin) && (degree <= goodmax)) {
				// stay green
			} else {
				colorx = 2;
			}
		}

		switch (colorx) {
		case 0:
			arrowId = WindInfoHandler.red_arrows[iconIndex];
			break;
		case 1:
			arrowId = WindInfoHandler.green_arrows[iconIndex];
			break;
		case 2:
			arrowId = WindInfoHandler.yellow_arrows[iconIndex];
			break;
		}

		return arrowId;
	}

	public static int convertSpeed(final double speedInKmh,
			final int conversionIx) {
		final int newspeed = (int) (speedInKmh * WindInfoHandler.speedConversionFactors[conversionIx]);

		return newspeed;
	}

	public static int getSpeedColor(final double speedInKmh) {
		int color = Color.DKGRAY; // darker yellow...

		if ((speedInKmh > WindInfoHandler.speedZones[0])
				&& (speedInKmh < WindInfoHandler.speedZones[1])) {
			color = Color.GREEN;
		} else if ((speedInKmh > WindInfoHandler.speedZones[1])) {
			color = Color.RED;
		}
		return color;
	}

}
