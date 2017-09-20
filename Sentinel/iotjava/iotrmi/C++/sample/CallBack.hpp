#ifndef _CALLBACK_HPP__
#define _CALLBACK_HPP__

#include <iostream>
#include "CallBackInterface.hpp"

using namespace std;

class CallBack : public CallBackInterface {
	public:
		//CallBack();
		CallBack(int _i);
		//~CallBack();

		int		printInt();
		void	setInt(int _i);

	private:		
		int		intA;
};


// Constructor
CallBack::CallBack(int _i) {

	intA = _i;
}


int CallBack::printInt() {

	cout << "Integer: " << intA << endl;
	return intA;
}


void CallBack::setInt(int _i) {

	intA = _i;
}

#endif

