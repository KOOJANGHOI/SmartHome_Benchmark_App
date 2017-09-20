package iotcode.interfaces;

// RMI Packages
import java.rmi.Remote;
import java.rmi.RemoteException;

/** Class LightBulb interface for the light bulb devices.
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0
 * @since       2016-01-27
 */


public interface LightBulb extends Remote {

	/** Method to turn the light bulb on (Physically illuminate the area).
	 *
	 *   @param None.
	 *
	 *   @return [void] None.
	 */

	public void turnOff() throws RemoteException;

	/** Method to turn the light bulb off.
	 *
	 *   @return [void] None.
	 */
	public void turnOn() throws RemoteException;


	/** Method to get the current on/off state of the light bulb.
	 *
	 *   @return [boolean] True means bulb on.
	 */
	public boolean getState() throws RemoteException;


	/** Method to set the light bulb color using Standard Hue, Saturation and Brightness
	 * conventions. See "http://www.tydac.ch/color/" for reference.
	 *
	 *   @param _hue [double]: Hue value (in degrees).
	 *   @param _saturation [double]: Saturation value (percentage).
	 *   @param _brightness [double]: Brightness value (percentage).
	 *
	 *   @return [void] None.
	 */
	public void setColor(double _hue, double _saturation, double _brightness) throws RemoteException;


	/** Method to set the color temperature.
	 *
	 *   @param _temperature [int]: Color temperature in degrees kelvin.
	 *
	 *   @return [void] None.
	 */
	public void setTemperature(int _temperature) throws RemoteException;


	/** Method to get the current hue value of the bulb.
	 *
	 *   @return [double] Current hue value of the bulb in degrees.
	 */
	public double getHue() throws RemoteException;


	/** Method to get the current saturation value of the bulb.
	 *
	 *   @return [double] Current saturation value of the bulb as a percentage.
	 */
	public double getSaturation() throws RemoteException;


	/** Method to get the current brightness value of the bulb.
	 *
	 *   @return [double] Current brightness value of the bulb as a percentage.
	 */
	public double getBrightness() throws RemoteException;


	/** Method to get the current color temperature value of the bulb.
	 *
	 *   @return [double] Current color temperature value of the bulb in kelvin.
	 */
	public int getTemperature() throws RemoteException;


	/** Method to get the hue range lower bound supported by the bulb.
	 *
	 *   @return [double] Hue lower bound in degrees.
	 */
	public double getHueRangeLowerBound() throws RemoteException;


	/** Method to get the hue range upper bound supported by the bulb.
	 *
	 *   @return [double] Hue upper bound in degrees.
	 */
	public double getHueRangeUpperBound() throws RemoteException;


	/** Method to get the saturation range lower bound supported by the bulb.
	 *
	 *   @return [double] Saturation lower bound as a percentage.
	 */
	public double getSaturationRangeLowerBound() throws RemoteException;


	/** Method to get the saturation range upper bound supported by the bulb.
	 *
	 *   @return [double] Saturation upper bound as a percentage.
	 */
	public double getSaturationRangeUpperBound() throws RemoteException;


	/** Method to get the brightness range lower bound supported by the bulb.
	 *
	 *   @return [double] Brightness lower bound as a percentage.
	 */
	public double getBrightnessRangeLowerBound() throws RemoteException;


	/** Method to get the brightness range upper bound supported by the bulb.
	 *
	 *   @return [double] Brightness upper bound as a percentage.
	 */
	public double getBrightnessRangeUpperBound() throws RemoteException;


	/** Method to get the temperature range lower bound supported by the bulb.
	 *
	 *   @return [int] Temperature lower bound as a percentage.
	 */
	public int getTemperatureRangeLowerBound() throws RemoteException;


	/** Method to get the temperature range upper bound supported by the bulb.
	 *
	 *   @return [int] Temperature upper bound as a percentage.
	 */
	public int getTemperatureRangeUpperBound() throws RemoteException;


	/** Method to initialize the bulb, if the bulb needs to be initialized.
	 *
	 *   @return [void] None.
	 */
	public void init() throws RemoteException;

}














