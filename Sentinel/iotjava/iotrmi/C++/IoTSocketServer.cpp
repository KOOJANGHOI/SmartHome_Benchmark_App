#include <iostream>
#include <string>
#include "IoTSocketServer.hpp"
#include "IoTRMIUtil.hpp"

using namespace std;


#define SIZE 10		  /* how many items per packet */
#define NUM_PACKS 3   /* number of times we'll do it */

int main(int argc, char *argv[])
{
	char D[SIZE];
	bool bResult = false;

	/* if no command line arguments passed, we'll default to 
		these two port number */
	int port = 5010;
	
	fflush(NULL);

	IoTSocketServer mylink(port, &bResult);
	if (!bResult)
	{
		printf("Failed to create Server object!\n");
		return 0;
	}

	/* put some dummy data in our arrays */
	for (int i = 0,j = 100; i < SIZE; i++, j--)
	{
		//D[i] = i;
		D[i] = j;
	}
	printf("Server, waiting for connection...\n");
	fflush(NULL);
	mylink.connect();
	printf("Server, got a connection...\n");
	fflush(NULL);

	char bytes[24];
	mylink.receiveBytes(bytes);
	cout << "Received bytes: ";
	IoTRMIUtil::printBytes(bytes, 24, false);

	printf("Server, closing connection...\n");
	fflush(NULL);
	mylink.close();

	printf("Server, done...\n");
	fflush(NULL);
	return 0;
}
