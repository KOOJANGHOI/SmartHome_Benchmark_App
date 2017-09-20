#ifndef _IOTUDP_HPP__
#define _IOTUDP_HPP__
#include <iostream>

#include "IoTDeviceAddress.hpp"

using namespace std;

// IoTUDP class for iotruntime
// Implemented based on IoTUDP.java that is used to wrap communication socket for UDP
//
// @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
// @version     1.0
// @since       2017-01-09

class IoTUDP final
{
	// IoTUDP class properties
	private:
		UDPSocket *socket;
		string strHostAddress;
		int iSrcPort;
		int iDstPort;
		bool didClose;
		int timeOut;

	public:

		// Constructor
		IoTUDP(IoTDeviceAddress* iotDevAdd) {

			strHostAddress = iotDevAdd->getAddress();
			iSrcPort = iotDevAdd->getSourcePortNumber();
			iDstPort = iotDevAdd->getDestinationPortNumber();
			timeOut = 0;

			socket = new UDPSocket(iSrcPort);
			if (socket == NULL) {
				perror("IoTUDP: UDP socket isn't initialized!");
			}
			didClose = false;
		}


		~IoTUDP() {
			// Clean up
			if (socket != NULL) {
		
				delete socket;
				socket = NULL;		
			}
		}


		string getHostAddress() {
			return strHostAddress;
		}


		int getSourcePort() {
			return iSrcPort;
		}


		int getDestinationPort() {
			return iDstPort;
		}


		void setTimeOut(int interval) {

			timeOut = interval;
		}


		// Send data packet
		void sendData(const void* buffer, int bufferLen) {
			unsigned short destinationPort = (unsigned short) iDstPort;
			socket->sendTo(buffer, bufferLen, strHostAddress, destinationPort);
		}


		// Receive data packet
		int receiveData(void* buffer, int iMaxDataLength) {
			unsigned short destinationPort = (unsigned short) iDstPort;
			//return socket->recvFrom(buffer, iMaxDataLength, strHostAddress, destinationPort);
			return socket->recvFrom(buffer, iMaxDataLength, strHostAddress, destinationPort, timeOut);
		}
};
#endif	
