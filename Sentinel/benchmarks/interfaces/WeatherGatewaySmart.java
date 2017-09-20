package iotcode.interfaces;

import java.util.List;
import java.util.ArrayList;

public interface WeatherGatewaySmart {

	public double getInchesPerWeek();
	public double getInchesPerMinute();
	public int getDaysToWaterOn();
	public void registerCallback(WeatherGatewayCallback _callbackTo);
	public void stop();
	public void start();
	public void init();
	public int getWeatherZipCode();
}
