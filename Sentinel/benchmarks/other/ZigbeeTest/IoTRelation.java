
import java.lang.UnsupportedOperationException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/** Class IoTRelation is the actual implementation of @config IoTRelation<...>.
 *  Upon extracting DB information, RelationInstrumenter class will use
 *  this class to actually instantiate the Map as IoTRelation uses a
 *  combination between a HashMap and a IoTSet; we don't provide interfaces
 *  to modify the contents, but we do provide means to read them out.
 *  The add method is just used the first time it is needed to add new objects,
 *  then it is going to be made immutable
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2015-12-01
 */
public final class IoTRelation<K, V> {

	/**
	 * Reference to an object Map<T>
	 */
	private Map<K,HashSet<V> > mapRelation;
	private int iSize;

	/**
	 * Class constructor (pass the reference to this immutable wrapper)
	 */
	protected IoTRelation(Map<K,HashSet<V>> mapRel, int _iSize) {
		mapRelation = mapRel;
		iSize = _iSize;
	}

	/**
	 * Method containsKey() inherited from Map interface
	 *
	 * @param  key  The first Object that is usually a key in a Map
	 * @return      boolean
	 */
	public boolean containsKey(K key) {

		return mapRelation.containsKey(key);

	}

	/**
	 * Method entrySet() inherited from Map interface
	 *
	 * @return      Set<Map.Entry<K,HashSet<V>>>
	 */
	public Set<Map.Entry<K,HashSet<V>>> entrySet() {

		return new HashSet<Map.Entry<K,HashSet<V>>>(mapRelation.entrySet());

	}

	/**
	 * Method keySet() inherited from Map interface
	 *
	 * @return      Set<K>
	 */
	public Set<K> keySet() {

		return new HashSet<K>(mapRelation.keySet());

	}

	/**
	 * Method get() inherited from Map interface
	 *
	 * @param  key  The first Object that is usually a key in a Map
	 * @return      HashSet<V>
	 */
	public HashSet<V> get(K key) {

		return new HashSet<V>(mapRelation.get(key));

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
	 * size() method
	 *
	 * @return      int
	 */
	public int size() {

		return this.iSize;

	}
}
