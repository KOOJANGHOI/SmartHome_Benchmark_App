/** Class IoTSocketServer is a communication class
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
#ifndef _IOTSOCKETSERVER_HPP__
#define _IOTSOCKETSERVER_HPP__

#include "IoTSocket.hpp"

#define BACKLOG 10      // How many pending connections queue will hold 

class IoTSocketServer final : public IoTSocket
{
  public:
		IoTSocketServer(int iPort, bool* pResult);

		bool				connect();							// Accept a new connection

	protected:		
		bool				m_bReverse;							// Am I reversing byte order or not?
		int					m_iListen;							// Descriptor we are listening on
		struct sockaddr_in	m_addrMe;							// My address information
};


// Constructor
IoTSocketServer::IoTSocketServer(int iPort, bool* pResult) :
	IoTSocket(iPort, pResult) {

	m_iListen		= -1;

	if (pResult)
		*pResult = false;

	if ((m_iListen = socket(AF_INET, SOCK_STREAM, 0)) == -1) 
	{
		perror("IoTSocketServer: Socket error!");
		return;
	}

	m_addrMe.sin_family			= AF_INET;          // Host byte order 
	m_addrMe.sin_port			= htons(m_iPort);	// Short, network byte order 
	m_addrMe.sin_addr.s_addr	= INADDR_ANY;		// Auto-fill with my IP 
	memset(&(m_addrMe.sin_zero), 0, 8);				// Zero the rest of the struct 

	if (bind(m_iListen, (struct sockaddr *) &m_addrMe, sizeof(struct sockaddr)) == -1) 
	{
		// Note, this can fail if the server has just been shutdown and not enough time has elapsed.
		// See: http://www.developerweb.net/forum/showthread.php?t=2977 
		perror("IoTSocketServer: Bind error!");
		return;
	}

	if (listen(m_iListen, BACKLOG) == -1) 
	{
		perror("IoTSocketServer: Listen error!");
		return;
	}

	if (pResult)
		*pResult = true;
}


// Wait for somebody to connect to us on our port.
bool IoTSocketServer::connect()
{
	socklen_t iSinSize = (socklen_t) sizeof(struct sockaddr_in);

	if ((m_iSock = accept(m_iListen, (struct sockaddr *) &m_addrRemote, &iSinSize)) == -1) 
	{
		perror("IoTSocketServer: Accept connection error!");
		return false;
	}
	// The client sends us an int to indicate if we should
	// be reversing byte order on this connection.  The client 
	// is sending 0 or 1, so a reversed 0 still looks
	// like a 0, no worries mate!
	char temp[1];
	int iTotal = 0;
	int iResult = 0;
	while ((iTotal < 1) && (iResult != -1))
	{
		iResult = recv(m_iSock, temp, 1, 0);
		iTotal += iResult;
	}
	if (iResult == -1)
	{
		perror("IoTSocketServer: Receive data error!");
		return false;
	}

	int iVal = temp[0];

	if (iVal == 0) 
		m_bReverse = false;
	else 
		m_bReverse = true;

	return true;
}

#endif
