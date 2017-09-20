package iotcode.interfaces;

import java.util.List;
import java.util.ArrayList;

public interface WeatherGatewaySmartCallback {

	public void informationRetrieved(double _inchesPerWeek, int _weatherZipCode, int _daysToWaterOn, double _inchesPerMinute);
}
