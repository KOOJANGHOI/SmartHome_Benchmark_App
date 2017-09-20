#ifndef _ISET_HPP__
#define _ISET_HPP__
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
class ISet final {
	private:
		unordered_set<T>* set;
	public:
		ISet();
		ISet(unordered_set<T> const* s);
		~ISet();
	public:
		typename unordered_set<T>::const_iterator find(const T& k);		// Find the object
		typename unordered_set<T>::const_iterator insert(const T& k);	// Insert the object
		bool empty();													// Test is empty?
		typename unordered_set<T>::const_iterator begin();				// Iterator
		typename unordered_set<T>::const_iterator end();				// Iterator
		int size();														// Set size
		unordered_set<T>* values();										// Return set contents
};


/**
 * Default constructor
 */
template <class T>
ISet<T>::ISet() {

	set = new unordered_set<T>();
}


/**
 * Useful constructor
 */
template <class T>
ISet<T>::ISet(const unordered_set<T>* s) {

	set = s;
}


/**
 * Default destructor
 */
template <class T>
ISet<T>::~ISet() {

	if (set != NULL)
		delete set;
}


/**
 * Find the object k in the set
 */
template <class T>
typename unordered_set<T>::const_iterator ISet<T>::find(const T& k) {

	return set->find(k);
}


/**
 * Insert object k into the set
 */
template <class T>
typename unordered_set<T>::const_iterator ISet<T>::insert(const T& k) {

	return set->insert(k);
}


/**
 * Return the "begin" iterator
 */
template <class T>
typename unordered_set<T>::const_iterator ISet<T>::begin() {

	return set->begin();
}


/**
 * Return the "end" iterator
 */
template <class T>
typename unordered_set<T>::const_iterator ISet<T>::end() {

	return set->end();
}


/**
 * Return the size of the set
 */
template <class T>
int ISet<T>::size() {

	return set->size();
}


/**
 * Return a new copy of the set
 */
template <class T>
unordered_set<T>* ISet<T>::values() {

	return set;
}
#endif

