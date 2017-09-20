package iotruntime.slave;

import java.io.Serializable;
import java.lang.UnsupportedOperationException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Class IRelation is another wrapper class of IoTRelation that is the
 *  actual implementation of @config IoTRelation<...>.
 *  The function is the same as the IoTRelation class, but this class
 *  is meant for the class instrumenter to have full access to
 *  our class object. The IoTRelation class functions as an immutable
 *  interface to clients/users.
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-01-05
 */
public final class IRelation<K,V> implements Serializable {

	/**
	 * Reference to an object Map<T>
	 */
	private Map<K,HashSet<V> > mapRelation;

	/**
	 * Size counter
	 */
	private int iSize;

	/**
	 * Class constructor (create object in this mutable wrapper)
	 */
	protected IRelation() {

		mapRelation = new HashMap<K,HashSet<V> >();
	}

	/**
	 * Method containsKey() inherited from Map interface
	 *
	 * @param  key  The first Object that is usually a key in a Map
	 * @return      boolean
	 */
	public boolean containsKey(Object key) {

		return mapRelation.containsKey(key);

	}

	/**
	 * Method relationMap() returns this mapRelation object
	 *
	 * @return      Map<K,HashSet<V>>>
	 */
	public Map<K,HashSet<V> > relationMap() {

		return mapRelation;

	}

	/**
	 * Method entrySet() inherited from Map interface
	 *
	 * @return      Set<Map.Entry<K,HashSet<V>>>
	 */
	public Set<Map.Entry<K,HashSet<V>>> entrySet() {

		return mapRelation.entrySet();

	}

	/**
	 * Method keySet() inherited from Map interface
	 *
	 * @return      Set<K>
	 */
	public Set<K> keySet() {

		return mapRelation.keySet();

	}

	/**
	 * Method get() inherited from Map interface
	 *
	 * @param  key  The first Object that is usually a key in a Map
	 * @return      HashSet<V>
	 */
	public HashSet<V> get(Object key) {

		return mapRelation.get(key);

	}

	/**
	 * Method isEmpty() inherited from Map interface
	 *
	 * @return      boolean
	 */
	public boolean isEmpty() {

		return mapRelation.isEmpty();

	}

	/**
	 * put() method
	 * <p>
	 * We check whether the same object has existed or not
	 * If it has, then we don't insert it again if it is a key
	 * If it is a value and it maps to the same value, we don't
	 * try to create a new key; we just map it to the same key
	 * This method is just used the first time it is needed to
	 * add new objects, then it is going to be made immutable
	 * <p>
	 * Mental picture:
	 * -------------------------------
	 * |   Obj 1   |   Set Obj 1.1   |
	 * |           |       Obj 1.2   |
	 * |           |       Obj 1.2   |
	 * |           |       ...       |
	 * -------------------------------
	 * |   Obj 2   |   Set Obj 2.1   |
	 * -------------------------------
	 * |   Obj 3   |   Set Obj 3.1   |
	 * |           |       ...       |
	 * -------------------------------
	 *
	 * @param  key    The first Object that is usually a key in a Map
	 * @param  value  The second Object that is usually a value in a Map
	 * @return        boolean
	 */
	public boolean put(K key, V value) {

		HashSet<V> hsSecond;

		// Go to our map first
		if (mapRelation.containsKey(key)) {
			// Such a key (first element) exists already
			hsSecond = mapRelation.get(key);
		} else {
			// It is a new key ...
			hsSecond = new HashSet<V>();
			mapRelation.put(key, hsSecond);
		}

		// Go to our Set of objects
		if (hsSecond.contains(value)) {
			// This object exists
			return false;
		} else {
			// This is a new object
			iSize++;
			hsSecond.add(value);
			return true;
		}
	}

	/**
	 * size() method
	 *
	 * @return      int
	 */
	public int size() {

		return this.iSize;

	}
}
