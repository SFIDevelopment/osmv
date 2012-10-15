package at.the.gogo.windig.dto;

public class WindEntry {

	String values[];

	public WindEntry() {
		values = new String[7];
	}

	public String getValue(final int index) {
		return values[index];
	}

	public void setValue(final String value, final int index) {
		values[index] = value;
	}

	// public String getDate() {
	// return date;
	// }
	//
	// public void setDate(String date) {
	// this.date = date;
	// }
	//
	// public String getTime() {
	// return time;
	// }
	//
	// public void setTime(String time) {
	// this.time = time;
	// }
	//
	// public String getWiGeMax() {
	// return WiGeMax;
	// }
	//
	// public void setWiGeMax(String wiGeMax) {
	// WiGeMax = wiGeMax;
	// }
	//
	// public String getWiRi() {
	// return WiRi;
	// }
	//
	// public void setWiRi(String wiRi) {
	// WiRi = wiRi;
	// }
	//
	// public String getWiGeAve() {
	// return WiGeAve;
	// }
	//
	// public void setWiGeAve(String wiGeAve) {
	// WiGeAve = wiGeAve;
	// }
	//
	// public String getLTAve() {
	// return LTAve;
	// }
	//
	// public void setLTAve(String lTAve) {
	// LTAve = lTAve;
	// }
	//
	// public String getRHAVE() {
	// return RHAVE;
	// }
	//
	// public void setRHAVE(String rHAVE) {
	// RHAVE = rHAVE;
	// }

}
