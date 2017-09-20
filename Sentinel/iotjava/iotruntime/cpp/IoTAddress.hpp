#ifndef _IOTADDRESS_HPP__
#define _IOTADDRESS_HPP__
#include <iostream>

using namespace std;


// IoTAddress class for iotruntime
// Implemented based on IoTAddress.java that is used to wrap address
//
// @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
// @version     1.0
// @since       2017-01-09

class IoTAddress
{
	public:

		// Constructor
		IoTAddress(string _sAddress) {

			inetAddress = _sAddress;
		}


		// Constructor
		IoTAddress() {
		}


		~IoTAddress() {
		}


		string getAddress() {

			return inetAddress;
		}


		string getURL(string strURLComplete) {

			return "http://" + inetAddress + strURLComplete;
		}


		// Custom hasher for IoTAddress / IoTDeviceAddress iterator
		size_t hash(IoTAddress const& devAddress) const {

			std::hash<std::string> hashVal;
			return hashVal(inetAddress);
		}


	// IoTAddress class properties
	protected:
		string inetAddress;
};
#endif	
