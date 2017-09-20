package SmartLightsController;

/** Class ColorTemperature to store the color and temperature data.
 *
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0
 * @since       2016-01-27
 */
public class ColorTemperature {

	// As a percentage
	public double hue;
	public double saturation;
	public double brightness;

	// In Kelvin
	public int temperature;

	/** Constructor
	 */
	public ColorTemperature() {
		this.hue = 0;
		this.saturation = 0;
		this.brightness = 0;
		this.temperature = 0;
	}

	/** Constructor
	 *
	 *   @param _hue         [double], Hue as a percentage.
	 *   @param _saturation  [double], Saturation as a percentage.
	 *   @param _brightness  [double], Brightness as a percentage.
	 *   @param _temperature [double], Temperature as kelvin.
	 *
	 */
	public ColorTemperature(double _hue, double _saturation, double _brightness, int _temperature) {
		this.hue = _hue;
		this.saturation = _saturation;
		this.brightness = _brightness;
		this.temperature = _temperature;
	}
}
