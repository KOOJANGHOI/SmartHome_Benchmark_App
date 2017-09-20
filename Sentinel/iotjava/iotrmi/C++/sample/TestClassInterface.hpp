#ifndef _TESTCLASSINTERFACE_HPP__
#define _TESTCLASSINTERFACE_HPP__

#include <iostream>
#include <vector>
#include "CallBackInterface.hpp"
#include "StructC.hpp"
#include "EnumC.hpp"

using namespace std;

class TestClassInterface {
	public:
		virtual void	setA(int _int) = 0;
		virtual void	setB(float _float) = 0;
		virtual void	setC(string _string) = 0;
		virtual string	sumArray(vector<string> newA) = 0;
		//virtual int64_t	sumArray(vector<int> newA) = 0;
		virtual int		setAndGetA(int newA) = 0;
		virtual int		setACAndGetA(string newC, int newA) = 0;
		virtual void	registerCallback(CallBackInterface* _cb) = 0;
		virtual void	registerCallback(vector<CallBackInterface*> _cb) = 0;
		virtual int		callBack() = 0;
		virtual vector<data>	handleStruct(vector<data> vecData) = 0;
		virtual vector<EnumC>	handleEnum(vector<EnumC> vecEn) = 0;
};

#endif

