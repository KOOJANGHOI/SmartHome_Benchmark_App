
import java.lang.UnsupportedOperationException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;


/** Class IoTSet is the actual implementation of @config IoTSet<...>.
 *  Upon extracting DB information, SetInstrumenter class will use
 *  this class to actually instantiate the Set as IoTSet that uses
 *  Java Set<T> to implement; we don't provide interfaces to modify
 *  the contents, but we do provide means to read them out
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2015-12-01
 */
public final class IoTSet<T> {

	/**
	 * Reference to an object Set<T>
	 */
	private Set<T> set;

	/**
	 * Class constructor (pass the reference to this immutable wrapper)
	 */
	public IoTSet(Set<T> s) {

		set = s;
	}

	/**
	 * contains() method inherited from Set interface
	 */
	public boolean contains(T o) {

		return set.contains(o);

	}

	/**
	 * isEmpty() method inherited from Set interface
	 */
	public boolean isEmpty() {

		return set.isEmpty();

	}

	/**
	 * iterator() method inherited from Set interface
	 */
	public Iterator<T> iterator() {

		return new HashSet<T>(set).iterator();

	}

	/**
	 * size() method inherited from Set interface
	 */
	public int size() {

		return set.size();

	}

	/**
	 * values() method to return Set object values for easy iteration
	 */
	public Set<T> values() {

		return new HashSet<T>(set);

	}
}
