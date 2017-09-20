#ifndef _CALLBACKINTERFACE_HPP__
#define _CALLBACKINTERFACE_HPP__

#include <iostream>

using namespace std;

class CallBackInterface {
	public:
		virtual int		printInt() = 0;
		virtual void	setInt(int _i) = 0;
};

#endif

