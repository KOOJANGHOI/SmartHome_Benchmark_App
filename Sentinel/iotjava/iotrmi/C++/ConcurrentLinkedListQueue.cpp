#include <iostream>
#include <thread>

#include "ConcurrentLinkedListQueue.hpp"

using namespace std;


Node::Node(char* val, int len) {

	value = val;
	length = len;
	next = NULL;
}


Node::~Node() {

	/*if (next != NULL) {
		delete next;
		next = NULL;
	}
	if (value != NULL) {
		delete value;
		value = NULL;
	}*/
}


char* Node::getValue() {

	return value;
}


int Node::getLength() {

	return length;
}


Node* Node::getNext() {

	return next;
}


void Node::setNext(Node* nxt) {

	next = nxt;
}


ConcurrentLinkedListQueue::ConcurrentLinkedListQueue() {

	tail = NULL;
	head = NULL;
}


ConcurrentLinkedListQueue::~ConcurrentLinkedListQueue() {

	char* val = NULL;
	do {	// Dequeue and free everything up
		val = dequeue();
	} while(val != NULL);
}


void ConcurrentLinkedListQueue::enqueue(char* value, int length) {

	lock_guard<mutex> guard(queueMutex);
	if (tail == NULL && head == NULL) {	// first element
		tail = new Node(value, length);
		head = tail;	// Both tail and head point to the first element
	} else {	// Next elements
		Node* newEl = new Node(value, length);
		tail->setNext(newEl);
		tail = newEl;
	}
}


// Return element and remove from list
char* ConcurrentLinkedListQueue::dequeue() {

	lock_guard<mutex> guard(queueMutex);
	if (tail == NULL && head == NULL) {	// empty
		return NULL;
	} else {
		Node* retEl = head;
		if (head->getNext() == NULL) {
			head = NULL;
			tail = NULL;
		} else
			head = head->getNext();
		char* retVal = retEl->getValue();
		// Prepare retEl for deletion
		retEl->setNext(NULL);
		delete retEl;
		// Return just the value
		return retVal;
	}
}

// Return element, length, and remove it from list
char* ConcurrentLinkedListQueue::deQAndGetLength(int* length) {

	lock_guard<mutex> guard(queueMutex);
	if (tail == NULL && head == NULL) {	// empty
		*length = 0;
		return 0;
	} else {
		Node* retEl = head;
		if (head->getNext() == NULL) {
			head = NULL;
			tail = NULL;
		} else
			head = head->getNext();
		char* retVal = retEl->getValue();
		*length = retEl->getLength();
		// Prepare retEl for deletion
		retEl->setNext(NULL);
		delete retEl;
		// Return just the value
		//cout << "Print bytes inside dequeue: ";
		//IoTRMIUtil::printBytes(*((char**) retVal), *length, false);
		//cout << "Dequeuing: " << *((char**) retVal) << endl;
		//cout << "Dequeuing address: " << std::ref(retVal) << endl;
		return retVal;
	}
}



