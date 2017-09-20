#ifndef _IOTSET_HPP__
#define _IOTSET_HPP__
#include <iostream>
#include <string>
#include <unordered_set>

using namespace std;

/** This is the IoTSet implementation for C++
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-09-06
 */
template <class T>
class IoTSet final {
	private:
		const unordered_set<T>* set;
	public:
		IoTSet();
		IoTSet(const unordered_set<T>* s);
		~IoTSet();
	public:
		typename unordered_set<T>::const_iterator find(const T& k);	// Find the object
		bool empty();												// Test is empty?
		typename unordered_set<T>::const_iterator begin();			// Iterator
		typename unordered_set<T>::const_iterator end();			// Iterator
		int size();													// Set size
		unordered_set<T>* values();									// Return set contents
};


/**
 * Default constructor
 */
template <class T>
IoTSet<T>::IoTSet() {

}


/**
 * Useful constructor
 */
template <class T>
IoTSet<T>::IoTSet(const unordered_set<T>* s) {

	set = s;
}


/**
 * Default destructor
 */
template <class T>
IoTSet<T>::~IoTSet() {

}


/**
 * Find the object k in the set
 */
template <class T>
typename unordered_set<T>::const_iterator IoTSet<T>::find(const T& k) {

	return (new unordered_set<T>(*set))->find(k);
}


/**
 * Return the "begin" iterator
 */
template <class T>
typename unordered_set<T>::const_iterator IoTSet<T>::begin() {

	return (new unordered_set<T>(*set))->begin();
}


/**
 * Return the "end" iterator
 */
template <class T>
typename unordered_set<T>::const_iterator IoTSet<T>::end() {

	return (new unordered_set<T>(*set))->end();
}


/**
 * Return the size of the set
 */
template <class T>
int IoTSet<T>::size() {

	return set->size();
}


/**
 * Return a new copy of the set
 */
template <class T>
unordered_set<T>* IoTSet<T>::values() {

	return new unordered_set<T>(*set);
}
#endif

