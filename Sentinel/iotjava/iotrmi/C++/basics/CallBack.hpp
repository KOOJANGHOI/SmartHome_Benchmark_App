#ifndef _CALLBACK_HPP__
#define _CALLBACK_HPP__

#include <iostream>

using namespace std;

class CallBack : public CallBackInterface {
	public:
		CallBack(int _i);

		int		printInt();
		void	setInt(int _i);
		void	needCallback(TestClassComplete* tc);

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

void CallBack::needCallback(TestClassComplete* tc) {

	cout << endl << "Short from TestClass: " << tc->getShort(1234) << endl << endl;
}

#endif

