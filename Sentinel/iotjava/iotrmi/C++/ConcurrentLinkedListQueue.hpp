#ifndef _CONCURRENTLINKEDLISTQUEUE_HPP__
#define _CONCURRENTLINKEDLISTQUEUE_HPP__
#include <iostream>
#include <mutex>

#include "IoTRMIUtil.hpp"

/** Class ConcurrentLinkedListQueue is a queue that can handle
 *  concurrent requests and packets for IoT communication via socket.
 *  <p>
 *  It stores object through a char pointer.
 *
 * @author      Rahmadi Trimananda <rtrimana @ uci.edu>
 * @version     1.0
 * @since       2017-01-27
 */

using namespace std;

mutex queueMutex;

class Node final {

	private:
		Node* next;
		char* value;
		int length;

	public:
		Node(char* val, int len);
		~Node();
		char* getValue();
		int getLength();
		Node* getNext();
		void setNext(Node* nxt);

};


class ConcurrentLinkedListQueue final {

	private:
		Node* tail;
		Node* head;

	public:
		ConcurrentLinkedListQueue();
		~ConcurrentLinkedListQueue();
		void enqueue(char* value, int length);	// Enqueue to tail
		char* dequeue();						// Dequeue from tail
		char* deQAndGetLength(int* length);		// Dequeue from tail and return length
};
#endif
