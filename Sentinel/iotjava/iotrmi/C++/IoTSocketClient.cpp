#include <iostream>
#include <string>
#include "IoTSocketClient.hpp"

using namespace std;

#define SIZE 10		 	 /* how many items per packet */
#define NUM_PACKS 3	 /* number of times we'll do it */

int main(int argc, char *argv[])
{
	char D[SIZE];

	/* if no command line arguments passed, we'll default to 
		these two port number */
	int port = 5010;
	int rev = 0;
	bool bResult = false;

	fflush(NULL);
  	IoTSocketClient mylink(port, "127.0.0.1", rev, &bResult);

	if (!bResult)
	{
		printf("Failed to create Client object!\n");
		return 0;
	}	

	printf("Client, made connection...\n");
	fflush(NULL);

	/* put some dummy data in our arrays */

	for (int i = 0; i < SIZE; i++)
	{
		D[i] = i;
	}

	for (int i = 0; i < NUM_PACKS; i++)
	{
		printf("Client, receiving bytes, iteration %d\n", i);
		fflush(NULL);
		mylink.receiveBytes(D);
	}

	char str[50];
	char* str2;
   	fflush(NULL);
	str2 = mylink.receiveBytes(str);
	string s(str2);
	cout << "Received text: " << s << endl;

	printf("Client, closing connection...\n");
	fflush(NULL);
	mylink.close();

	printf("Client, done...\n");
	fflush(NULL);
	return 0;
}
