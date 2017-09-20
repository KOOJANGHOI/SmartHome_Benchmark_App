#ifndef _IOTDEVICEADDRESS_HPP__
#define _IOTDEVICEADDRESS_HPP__
#include <iostream>

#include "IoTAddress.hpp"

using namespace std;

// IoTDeviceAddress class for iotruntime
// Implemented based on IoTDeviceAddress.java that is used to wrap device address
//
// @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
// @version     1.0
// @since       2017-01-09


class IoTDeviceAddress final : public IoTAddress
{

	public:

		// Constructor
		IoTDeviceAddress(string _sAddress, int _iSrcPort, int _iDstPort, bool _isSrcPortWildCard, bool _isDstPortWildCard) : IoTAddress(_sAddress) {

			iSrcPort = _iSrcPort;
			iDstPort = _iDstPort;
			isSrcPortWildCard = _isSrcPortWildCard;
			isDstPortWildCard = _isDstPortWildCard;
		}


		// Constructor
		IoTDeviceAddress() {
		}


		~IoTDeviceAddress() {
		}


		// Getter methods
		int getSourcePortNumber() {

			return iSrcPort;
		}


		int getDestinationPortNumber() {

			return iDstPort;
		}


		bool getIsSrcPortWildcard() {

			return isSrcPortWildCard;
		}


		bool getIsDstPortWildcard() {

			return isDstPortWildCard;
		}


		// Setter methods
		void setSrcPort(int port) {

			if (isDstPortWildCard) {
				iDstPort = port;
			}
		}


		void setDstPort(int port) {

			if (isSrcPortWildCard) {
				iSrcPort = port;
			}
		}


	// IoTDeviceAddress class properties
	private:
		int iSrcPort;
		int iDstPort;

		bool isSrcPortWildCard;
		bool isDstPortWildCard;
};
#endif	
