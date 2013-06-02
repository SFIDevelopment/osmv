package org.anize.ur.life.wimp.models;

public class GeoCodeResult {

	// brutal kurz!!!

	private String line1;
	private String line2;
	private String line3;
	private String line4;

	private String country;
	private String city;

	public GeoCodeResult() {
		line1 = "";
		line2 = "";
		line3 = "";
		line4 = "";
		country = "";
		city = "";
	}

	@Override
	public String toString() {

		final StringBuilder builder = new StringBuilder();
		// builder.append("Location:");

		if ((line1 != null) && (line1.length() > 0)) {
			builder.append(line1);
		}
		if ((line2 != null) && (line2.length() > 0)) {
			builder.append(", ").append(line2);
		}
		if ((line3 != null) && (line3.length() > 0)) {
			builder.append(", ").append(line3);
		}
		if ((line4 != null) && (line4.length() > 0)) {
			builder.append(", ").append(line4);
		}
		if ((city != null) && (city.length() > 0)) {
			builder.append(", ").append(city);
		}
		if ((country != null) && (country.length() > 0)) {
			builder.append(", ").append(country);
		}

		return builder.toString();

	}

	public String getLine1() {
		return line1;
	}

	public void setLine1(final String line1) {
		this.line1 = line1;
	}

	public String getLine2() {
		return line2;
	}

	public void setLine2(final String line2) {
		this.line2 = line2;
	}

	public String getLine3() {
		return line3;
	}

	public void setLine3(final String line3) {
		this.line3 = line3;
	}

	public String getLine4() {
		return line4;
	}

	public void setLine4(final String line4) {
		this.line4 = line4;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(final String country) {
		this.country = country;
	}

	public String getCity() {
		return city;
	}

	public void setCity(final String city) {
		this.city = city;
	}

}
