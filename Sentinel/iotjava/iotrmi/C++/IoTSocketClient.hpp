/** Class IoTSocketClient is a communication class
 *  that provides interfaces to connect to either
 *  Java or C++ socket endpoint. It inherits the
 *  methods from IoTSocket.
 *  <p>
 *  Adapted from Java/C++ socket implementation
 *  by Keith Vertanen
 *  @see        <a href="https://www.keithv.com/software/socket/</a>
 *
 * @author      Rahmadi Trimananda <rtrimana @ uci.edu>
 * @version     1.0
 * @since       2016-08-17
 */
#ifndef _IOTSOCKETCLIENT_HPP__
#define _IOTSOCKETCLIENT_HPP__

#include "IoTSocket.hpp"

class IoTSocketClient final : public IoTSocket
{
	public:
		IoTSocketClient(int iPort, const char* pStrHost, bool bReverse, bool* pResult);
};


// Constructor
IoTSocketClient::IoTSocketClient(int iPort, const char* pStrHost, bool bReverse, bool* pResult) :
	IoTSocket(iPort, pResult) {

	struct hostent*	he = NULL;

	if (pResult)
		*pResult = false;

	if ((he = gethostbyname(pStrHost)) == NULL) {

		perror("IoTSocketClient: Gethostbyname error!");
		return;
	}

	if ((m_iSock = socket(AF_INET, SOCK_STREAM, 0)) == -1) {

		perror("IoTSocketClient: Socket error!");
		return;
	}

	m_addrRemote.sin_family		= AF_INET;        
	m_addrRemote.sin_port		= htons(m_iPort);      
	m_addrRemote.sin_addr		= *((struct in_addr *) he->h_addr); 
	memset(&(m_addrRemote.sin_zero), 0, 8);

	// Make socket client wait for socket server to be ready
	while (connect(m_iSock, (struct sockaddr *) &m_addrRemote, sizeof(struct sockaddr)) == -1) { }

	// Send out request for reversed bits or not
	char temp[1];
	if (bReverse) {

		temp[0] = 1;
		if (send(m_iSock, temp, 1, 0) == -1)
		{
			perror("IoTSocketClient: Send 1 error!");
			return;
		}
	} else {
		temp[0] = 0;
		if (send(m_iSock, temp, 1, 0) == -1)
		{
			perror("IoTSocketClient: Send 2 error!");
			return;
		}
	}
	
	if (pResult)
		*pResult = true;
}


#endif
