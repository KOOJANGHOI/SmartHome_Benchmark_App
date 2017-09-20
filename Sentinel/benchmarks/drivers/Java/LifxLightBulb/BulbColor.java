package iotcode.LifxLightBulb;

import java.security.InvalidParameterException;

public class BulbColor {

	private int hue;
	private int saturation;
	private int brightness;
	private int kelvin;

	public BulbColor(int _hue, int _saturation, int _brightness, int _kelvin) {

		if ((hue > 65535) || (hue < 0)) {
			throw new InvalidParameterException("BulbColor: Invalid parameter value for _hue (0-65535)");
		}

		if ((saturation > 65535) || (saturation < 0)) {
			throw new InvalidParameterException("BulbColor: Invalid parameter value for _saturation (0-65535)");
		}

		if ((brightness > 65535) || (brightness < 0)) {
			throw new InvalidParameterException("BulbColor: Invalid parameter value for _brightness (0-65535)");
		}

		if ((kelvin > 65535) || (kelvin < 0)) {
			throw new InvalidParameterException("BulbColor: Invalid parameter value for _kelvin (0-65535)");
		}

		hue = _hue;
		saturation = _saturation;
		brightness = _brightness;
		kelvin = _kelvin;
	}

	public BulbColor(byte[] data) {
		hue = ((data[1] & 0xFF) << 8);
		hue |= (data[0] & 0xFF);

		saturation = ((data[3] & 0xFF) << 8);
		saturation |= (data[2] & 0xFF);

		brightness = ((data[5] & 0xFF) << 8);
		brightness |= (data[4] & 0xFF);

		kelvin = ((data[7] & 0xFF) << 8);
		kelvin |= (data[6] & 0xFF);
	}

	public int getHue() {
		return hue;
	}

	public int getSaturation() {
		return saturation;
	}

	public int getBrightness() {
		return brightness;
	}

	public int getKelvin() {
		return kelvin;
	}
}


