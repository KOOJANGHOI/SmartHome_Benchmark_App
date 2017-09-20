package iotruntime.master;

/** Class RuntimeOutput is a class that controls the verboseness
 *  of the runtime system
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-05-11
 */
public final class RuntimeOutput {

	/**
	 * print() method to print messages
	 *
	 * @param	strMessage	Message to print out
	 * @return  void
	 */
	public static void print(String strMessage, boolean bVerbose) {

		if (bVerbose == true) {
			System.out.println(strMessage);
		}
	}
}
