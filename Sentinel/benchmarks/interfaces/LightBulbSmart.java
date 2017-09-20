package iotcode.interfaces;

import java.util.List;
import java.util.ArrayList;

public interface LightBulbSmart {

	public void turnOn();
	public double getBrightnessRangeLowerBound();
	public void turnOff();
	public boolean getState();
	public double getBrightnessRangeUpperBound();
	public double getSaturation();
	public double getHueRangeLowerBound();
	public double getHue();
	public double getHueRangeUpperBound();
	public int getTemperature();
	public double getBrightness();
	public int getTemperatureRangeLowerBound();
	public int getTemperatureRangeUpperBound();
	public void setColor(double _hue, double _saturation, double _brightness);
	public void init();
	public double getSaturationRangeLowerBound();
	public double getSaturationRangeUpperBound();
	public void setTemperature(int _temperature);
}
