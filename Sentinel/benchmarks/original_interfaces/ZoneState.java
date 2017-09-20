package iotcode.interfaces;

public class ZoneState {
	private int zoneNumber = 0;
	private boolean onOffState = false;
	private int duration = -1;

	public ZoneState(int _zoneNumber, boolean _onOffState, int _duration) {
		zoneNumber = _zoneNumber;
		onOffState = _onOffState;
		duration = _duration;
	}

	public int getZoneNumber() {
		return zoneNumber;
	}

	public boolean getOnOffState() {
		return onOffState;
	}

	public int getDuration() {
		return duration;
	}

	public void setOnOffState(boolean _onOffState) {
		onOffState = _onOffState;
	}

	public void setDuration(int _duration) {
		duration = _duration;
	}



	public String toString() {
		String retString = "Zone Number: ";
		retString += Integer.toString(zoneNumber);
		retString += "\t On/Off State: ";

		if (onOffState) {
			retString += "On";
		} else {
			retString += "Off";
		}
		retString += "\t Duration: ";
		retString += Integer.toString(duration);

		return retString;
	}
}
