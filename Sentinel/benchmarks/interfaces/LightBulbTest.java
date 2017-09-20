package iotcode.interfaces;

import java.util.List;
import java.util.ArrayList;

public interface LightBulbTest {

	public void turnOn();
	public double getBrightness();
	public void turnOff();
	public boolean getState();
	public void setColor(double _hue, double _saturation, double _brightness);
	public double getSaturation();
	public void init();
	public void setTemperature(int _temperature);
	public double getHue();
	public int getTemperature();
}
