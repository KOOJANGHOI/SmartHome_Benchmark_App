package iotruntime.slave;

import java.io.Serializable;

import java.lang.UnsupportedOperationException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;


/** Class ISet is another wrapper class of IoTSet that is the
 *  actual implementation of @config IoTSet<...>.
 *  The function is the same as the IoTSet class, but this class
 *  is meant for the class instrumenter to have full access to
 *  our class object. The IoTSet class functions as an immutable
 *  interface to clients/users.
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-01-05
 */
public final class ISet<T> implements Serializable {

	/**
	 * Reference to an object Set<T>
	 */
	private Set<T> set;

	/**
	 * Empty class constructor
	 */
	protected ISet() {

		set = new HashSet<T>();
	}

	/**
	 * Class constructor (pass the reference to this mutable wrapper)
	 */
	protected ISet(Set<T> s) {

		set = s;
	}

	/**
	 * add() method inherited from Set interface
	 */
	public boolean add(T o) {

		return set.add(o);

	}

	/**
	 * clear() method inherited from Set interface
	 */
	public void clear() {

		set.clear();

	}

	/**
	 * contains() method inherited from Set interface
	 */
	public boolean contains(Object o) {

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

		return set.iterator();

	}

	/**
	 * remove() method inherited from Set interface
	 */
	public boolean remove(Object o) {

		return set.remove(o);

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

		return set;

	}
}
