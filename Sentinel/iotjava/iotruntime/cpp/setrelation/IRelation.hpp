#ifndef _IRELATION_HPP__
#define _IRELATION_HPP__
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
class IRelation final {
	private:
		unordered_multimap<K,V>* rel;
	public:
		IRelation();
		IRelation(unordered_multimap<K,V> const* r);
		~IRelation();
	public:
		typename unordered_multimap<K,V>::const_iterator find(const K& k);				// Find the object based on key
		typename unordered_multimap<K,V>::const_iterator insert(const pair<K,V>& val);	// Insert the object pair
		bool empty();																	// Test is empty?
		typename unordered_multimap<K,V>::const_iterator begin();						// Iterator
		typename unordered_multimap<K,V>::const_iterator end();							// Iterator
		std::pair<typename unordered_multimap<K,V>::const_iterator, 
			typename unordered_multimap<K,V>::const_iterator> 
			equal_range(const K& k);													// Equal range iterator
		int size();																		// Set size
		unordered_multimap<K,V> values();												// Return set contents
};


/**
 * Default constructor
 */
template <class K,class V>
IRelation<K,V>::IRelation() {

	rel = new unordered_multimap<K,V>();
}


/**
 * Useful constructor
 */
template <class K,class V>
IRelation<K,V>::IRelation(const unordered_multimap<K,V>* r) {

	rel = r;
}


/**
 * Default destructor
 */
template <class K,class V>
IRelation<K,V>::~IRelation() {

	if (rel != NULL)
		delete rel;
}


/**
 * Find the object k in the set
 */
template <class K,class V>
typename unordered_multimap<K,V>::const_iterator IRelation<K,V>::find(const K& k) {

	return rel->find(k);
}


/**
 * Insert object k into the set
 */
template <class K,class V>
typename unordered_multimap<K,V>::const_iterator IRelation<K,V>::insert(const pair<K,V>& val) {

	return rel->insert(val);
}


/**
 * Return the "begin" iterator
 */
template <class K,class V>
typename unordered_multimap<K,V>::const_iterator IRelation<K,V>::begin() {

	return rel->begin();
}


/**
 * Return the "end" iterator
 */
template <class K,class V>
typename unordered_multimap<K,V>::const_iterator IRelation<K,V>::end() {

	return rel->end();
}


/**
 * Return the "equal_range" iterator
 */
template <class K,class V>
std::pair<typename unordered_multimap<K,V>::const_iterator, 
	typename unordered_multimap<K,V>::const_iterator> 
	IRelation<K,V>::equal_range(const K& k) {

	return rel->equal_range(k);
}


/**
 * Return the size of the set
 */
template <class K,class V>
int IRelation<K,V>::size() {

	return rel->size();
}


/**
 * Return a new copy of the set
 */
template <class K,class V>
unordered_multimap<K,V> IRelation<K,V>::values() {

	return rel;
}
#endif




