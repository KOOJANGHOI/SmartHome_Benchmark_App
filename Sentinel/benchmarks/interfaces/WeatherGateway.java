package iotcode.interfaces;

import java.util.List;
import java.util.ArrayList;

public interface WeatherGateway {
	public void init();
	public void start();
	public void stop();
	public double getInchesPerWeek();
	public int getWeatherZipCode();
	public int getDaysToWaterOn();
	public double getInchesPerMinute();
	public void registerCallback(WeatherGatewaySmartCallback _callbackTo);
}
