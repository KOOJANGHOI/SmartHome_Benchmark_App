//package iotcode.interfaces;

import java.util.List;
import java.util.ArrayList;

public interface SmartthingsSensorCallback {
	//public void newReadingAvailable(int _sensorId, int _value, boolean _activeValue);
	public void newReadingAvailable(SmartthingsSensor cb);
}
