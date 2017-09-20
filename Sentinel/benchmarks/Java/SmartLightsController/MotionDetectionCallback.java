package SmartLightsController;

/** Interface MotionDetectionCallback for allowing callbacks from the MotionDetection class.
 *
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0
 * @since       2016-03-21
 */

public interface MotionDetectionCallback {

	/** Callback method for when motion is detected.
	 *
	 *   @param _md [MotionDetection].
	 *
	 *   @return [void] None.
	 */
	public void motionDetected(MotionDetection _md);
}
