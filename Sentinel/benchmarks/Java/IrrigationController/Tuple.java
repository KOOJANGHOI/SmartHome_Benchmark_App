package IrrigationController;

/** Class Tuple used for storing a pair of objects together
 *
 * Referenced from: http://stackoverflow.com/questions/2670982/using-pairs-or-2-tuples-in-java
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0
 * @since       2016-01-27
 */

public class Tuple<X, Y> {

	// The tuple object internal objects have no protection
	// Assume that the user will use these in a safe way
	public final X x;
	public final Y y;


	/** Constructor
	 *
	 *   @param x [X], x value of tuple.
	 *   @param y [Y], y value of tuple.
	 *
	 */
	public Tuple(X x, Y y) {
		this.x = x;
		this.y = y;
	}
}
