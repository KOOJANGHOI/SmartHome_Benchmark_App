/** Class IoTSocket is a base class for IoTSocketServer.cpp
 *  and IoTSocketClient.cpp that provide interfaces to connect 
 *  to either Java or C++ socket endpoint
 *  <p>
 *  Adapted from Java/C++ socket implementation
 *  by Keith Vertanen
 *  @see        <a href="https://www.keithv.com/software/socket/</a>
 *
 * @author      Rahmadi Trimananda <rtrimana @ uci.edu>
 * @version     1.0
 * @since       2016-10-17
 */
#ifndef _IOTSOCKET_HPP__
#define _IOTSOCKET_HPP__

// Adds in the send/recv acks after each message.
#define DEBUG_ACK

static const int SOCKET_BUFF_SIZE = 64000;
// Before, it was too short as we were just using 1 byte to receive the length
// Now, we allocate 4 bytes (a size of integer) to receive the message length
static const int MSG_LEN_SIZE = 4;

#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>

#include <netinet/in.h>
#include <netdb.h>
#include <sys/socket.h>
#include <sys/wait.h>
#include <unistd.h>

#include "IoTRMIUtil.hpp"

// Duplicated from winsock2.h
#define SD_RECEIVE      0x00
#define SD_SEND         0x01
#define SD_BOTH         0x02

mutex sendBytesMutex;
mutex recvBytesMutex;
mutex sendAckMutex;
mutex recvAckMutex;

class IoTSocket {
	public:
		IoTSocket(int iPort, bool* pResult);
		~IoTSocket();

		bool				close();								// Close the socket
		bool				sendBytes(char* pVals, int _iLen);		// Send a set of bytes
		char*				receiveBytes(char* pVals, int* len);  	// Receive a set of bytes

	protected:		
		int					m_iPort;							// Port I'm listening on
		int					m_iSock;							// Socket connection
		struct sockaddr_in	m_addrRemote;						// Connector's address information
		double*				m_pBuffer;							// Reuse the same memory for buffer

	private:
		bool				receiveAck();
		bool				sendAck();
};


// Constructor
IoTSocket::IoTSocket(int iPort, bool* pResult) {

	m_iPort 	= iPort;
	m_iSock 	= -1;
	m_pBuffer 	= NULL;

	// Allocate our temporary buffers that are used to convert data types
	m_pBuffer = (double *) malloc(sizeof(double) * SOCKET_BUFF_SIZE);
	if (!m_pBuffer) {
		perror("IoTSocket: Failed to malloc buffer!");
		return;
	}

}


// Destructor
IoTSocket::~IoTSocket() {

	if (m_pBuffer) {
		free(m_pBuffer);
		m_pBuffer = NULL;
	}
}


// Send bytes over the wire
bool IoTSocket::sendBytes(char* pVals, int iLen) {

	// Critical section that is used by different objects
	lock_guard<mutex> guard(sendBytesMutex);

	int i = 0;
	char size[MSG_LEN_SIZE];
	// Convert int to byte array and fix endianness
	IoTRMIUtil::intToByteArray(iLen, size);

	if (send(m_iSock, size, MSG_LEN_SIZE, 0) == -1) {
		perror("IoTSocket: Send size error!");
		return false;
	}

	IoTRMIUtil::printBytes(size, 4, false);

	if (send(m_iSock, (char *) pVals, iLen, 0) == -1) {
		perror("IoTSocket: Send bytes error!");
		return false;
	}

#ifdef DEBUG_ACK
	if (!receiveAck())
		return false;
	if (!sendAck())
		return false;
#endif

	return true;
}


// Receive bytes, returns number of bytes received
// Generate an array of char on the heap and return it
char* IoTSocket::receiveBytes(char* pVals, int* len)
{
	// Critical section that is used by different objects
	lock_guard<mutex> guard(recvBytesMutex);

	int			i				= 0;
	int			j				= 0;
	char*		pTemp			= NULL;
	int			iTotalBytes		= 0;
	int			iNumBytes		= 0;
	bool		bEnd			= false;

	int iTotal = 0;
	int iResult = 0;
	char size[MSG_LEN_SIZE];
	
	while ((iTotal < 1) && (iResult != -1)) {
		iResult = recv(m_iSock, size, MSG_LEN_SIZE, 0);		
		iTotal += iResult;
	}
	if (iResult == -1) {
		perror("IoTSocket: Receive size error!");
		return NULL;
	}
	// Convert byte to int array based on correct endianness
	int iLen = 0;
	IoTRMIUtil::byteArrayToInt(&iLen, size);

	// To be returned from this method...
	*len = iLen;
	pVals = new char[iLen];
	pTemp = (char *) m_pBuffer;
	// We receiving the incoming ints one byte at a time.
	while (!bEnd) {
		if ((iNumBytes = recv(m_iSock, pTemp, SOCKET_BUFF_SIZE, 0)) == -1) {
			perror("IoTSocket: Receive error!");
			return NULL;
		}
		for (i = 0; i < iNumBytes; i++) {
			pVals[j] = pTemp[i];
			j++;
		}
		iTotalBytes += iNumBytes;
		if (iTotalBytes == iLen)
			bEnd = true;
	}

#ifdef DEBUG_ACK
	if (!sendAck())
		return NULL;
	if (!receiveAck())
		return NULL;
#endif
	return pVals;
}


// Shut down the socket
bool IoTSocket::close()
{
	if (shutdown(m_iSock, SD_BOTH) == -1) {
		perror("IoTSocket: Close error!");
		return false;
	}

	return true;
}


// Receive a short ack from the client 
bool IoTSocket::receiveAck()
{
	// Critical section that is used by different objects
	lock_guard<mutex> guard(recvAckMutex);
	char temp[1];
	int iTotal = 0;
	int iResult = 0;
	while ((iTotal < 1) && (iResult != -1)) {

		iResult = recv(m_iSock, temp, 1, 0);	
		iTotal += iResult;
	}
	if (iResult == -1) {

		perror("IoTSocket: ReceiveAck error!");
		return false;
	}

	return true;
}	


// Send a short ack to the client 
bool IoTSocket::sendAck()
{
	// Critical section that is used by different objects
	lock_guard<mutex> guard(sendAckMutex);
	char temp[1];
	temp[0] = 42;

	if (send(m_iSock, temp, 1, 0) == -1)
		return false;
	return true;
}

#endif
