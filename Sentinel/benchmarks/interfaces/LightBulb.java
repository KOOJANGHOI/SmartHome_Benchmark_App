package iotcode.interfaces;

import java.util.List;
import java.util.ArrayList;

public interface LightBulb {
	public void init();
	public void turnOff();
	public void turnOn();
	public boolean getState();
	public void setColor(double _hue, double _saturation, double _brightness);
	public void setTemperature(int _temperature);
	public double getBrightness();
	public double getHue();
	public double getSaturation();
	public int getTemperature();
	public double getBrightnessRangeLowerBound();
	public double getBrightnessRangeUpperBound();
	public double getHueRangeLowerBound();
	public double getHueRangeUpperBound();
	public double getSaturationRangeLowerBound();
	public double getSaturationRangeUpperBound();
	public int getTemperatureRangeLowerBound();
	public int getTemperatureRangeUpperBound();
}
