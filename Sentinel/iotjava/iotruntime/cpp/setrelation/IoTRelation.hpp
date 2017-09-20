#ifndef _IOTRELATION_HPP__
#define _IOTRELATION_HPP__
#include <iostream>
#include <string>
#include <unordered_map>

using namespace std;

/** This is the IoTRelation implementation for C++
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-09-06
 */
template <class K,class V>
class IoTRelation final {
	private:
		const unordered_multimap<K,V>* rel;
	public:
		IoTRelation();
		//IoTRelation(unordered_multimap<K,V> const& s);
		IoTRelation(const unordered_multimap<K,V>* s);
		~IoTRelation();
	public:
		typename unordered_multimap<K,V>::const_iterator find(const K& k);	// Find the object based on key
		bool empty();														// Test is empty?
		typename unordered_multimap<K,V>::const_iterator begin();			// Iterator
		typename unordered_multimap<K,V>::const_iterator end();				// Iterator
		std::pair<typename unordered_multimap<K,V>::const_iterator, 
			typename unordered_multimap<K,V>::const_iterator> 
			equal_range(const K& k);										// Equal range iterator
		int size();															// Set size
		unordered_multimap<K,V>* values();									// Return set contents
};


/**
 * Default constructor
 */
template <class K,class V>
IoTRelation<K,V>::IoTRelation() {

}


/**
 * Useful constructor
 */
template <class K,class V>
//IoTRelation<K,V>::IoTRelation(const unordered_multimap<K,V>& r) {
IoTRelation<K,V>::IoTRelation(const unordered_multimap<K,V>* r) {

	rel = r;
}


/**
 * Default destructor
 */
template <class K,class V>
IoTRelation<K,V>::~IoTRelation() {

}


/**
 * Find the object k in the set
 */
template <class K,class V>
typename unordered_multimap<K,V>::const_iterator IoTRelation<K,V>::find(const K& k) {

	return (new unordered_multimap<K,V>(*rel))->find(k);
}


/**
 * Return the "begin" iterator
 */
template <class K,class V>
typename unordered_multimap<K,V>::const_iterator IoTRelation<K,V>::begin() {

	return (new unordered_multimap<K,V>(*rel))->begin();
}


/**
 * Return the "end" iterator
 */
template <class K,class V>
typename unordered_multimap<K,V>::const_iterator IoTRelation<K,V>::end() {

	return (new unordered_multimap<K,V>(*rel))->end();
}


/**
 * Return the "equal_range" iterator
 */
template <class K,class V>
std::pair<typename unordered_multimap<K,V>::const_iterator, 
	typename unordered_multimap<K,V>::const_iterator> 
	IoTRelation<K,V>::equal_range(const K& k) {

	return (new unordered_multimap<K,V>(*rel))->equal_range(k);
}


/**
 * Return the size of the set
 */
template <class K,class V>
int IoTRelation<K,V>::size() {

	return rel->size();
}


/**
 * Return a new copy of the set
 */
template <class K,class V>
unordered_multimap<K,V>* IoTRelation<K,V>::values() {

	return new unordered_multimap<K,V>(*rel);
}
#endif




