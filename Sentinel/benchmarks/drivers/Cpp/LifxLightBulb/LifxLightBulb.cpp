#include <iostream>
#include <string>
#include <thread>

#include <pthread.h>

#include "LifxLightBulb.hpp"
#include "IoTSet.hpp"
#include "IoTDeviceAddress.hpp"

using namespace std;


// External functions to create, destroy and initialize this class object
extern "C" void* createLifxLightBulb(void** params) {
	// Arguments: IoTSet<IoTDeviceAddress*>* _devAddress, string macAddress
	return new LifxLightBulb((IoTSet<void*>*) params[0], *((string*) params[1]));
}


extern "C" void destroyLifxLightBulb(void* t) {
	LifxLightBulb* llb = (LifxLightBulb*) t;
	delete llb;
}


extern "C" void initLifxLightBulb(void* t) {
	LifxLightBulb* llb = (LifxLightBulb*) t;
	llb->init();
}


// Constructor
LifxLightBulb::LifxLightBulb() { 
	// LB1 macAddress: d0:73:d5:12:8e:30
	// LB1 macAddress: d0:73:d5:02:41:da
	string macAddress = "D073D5128E300000"; // bulbMacAddress: [-48, 115, -43, 18, -114, 48, 0, 0]
	//string macAddress = "D073D50241DA0000"; // bulbMacAddress: [-48, 115, -43, 2, 65, -38, 0, 0]
	/*bulbMacAddress[0] = 0xD0;
	bulbMacAddress[1] = 0x73;
	bulbMacAddress[2] = 0xD5;
	bulbMacAddress[3] = 0x02;
	bulbMacAddress[4] = 0x41;
	bulbMacAddress[5] = 0xDA;
	bulbMacAddress[6] = 0x00; 
	bulbMacAddress[7] = 0x00;*/

	char tmpMacAddress[16];
	strcpy(tmpMacAddress, macAddress.c_str());
	//test[0] = (char) strtol(strTest.c_str(), NULL, 16);
	for(int i=0; i<16; i=i+2) {
		// Take 2 digits and then convert
		char tmpMacByte[2];
		tmpMacByte[0] = tmpMacAddress[i];
		tmpMacByte[1] = tmpMacAddress[i+1];
		bulbMacAddress[i/2] = (char) strtol(tmpMacByte, NULL, 16);
	}
	//IoTRMIUtil::printBytes(bulbMacAddress, 8, false);
}


// Driver constructor always gets a pointer to device address, trailed by class arguments of generic type
LifxLightBulb::LifxLightBulb(IoTSet<void*>* _devAddress, string macAddress) {

	// Initialize macAddress
	char tmpMacAddress[16];
	strcpy(tmpMacAddress, macAddress.c_str());
	//test[0] = (char) strtol(strTest.c_str(), NULL, 16);
	for(int i=0; i<16; i=i+2) {
		// Take 2 digits and then convert
		char tmpMacByte[2];
		tmpMacByte[0] = tmpMacAddress[i];
		tmpMacByte[1] = tmpMacAddress[i+1];
		bulbMacAddress[i/2] = (char) strtol(tmpMacByte, NULL, 16);
	}
	//cout << "MAC address is set. Value: ";
	IoTRMIUtil::printBytes(bulbMacAddress, 8, false);
	// Logging
	int i=0;
	string file = "LifxLightBulb_cpp" + to_string(i) + ".log";
	while (ifstream(file.c_str())) {
		i++;
		file = "LifxLightBulb_cpp" + to_string(i) + ".log";
	}
	log.open(file);
	log << "MAC address is " << macAddress << endl;

	// Initialize device address
	lb_addresses = _devAddress;
	//cout << "Device address is set! " << endl;
}


LifxLightBulb::~LifxLightBulb() {

	// Clean up
	if (communicationSocket != NULL) {

		delete communicationSocket;
		communicationSocket = NULL;		
	}
	for(void* dev : *lb_addresses) {
		IoTDeviceAddress* dv = (IoTDeviceAddress*) dev;
		delete dv;
		dv = NULL;
	}
	if (lb_addresses != NULL) {

		delete lb_addresses;
		lb_addresses = NULL;		
	}
}


// PUBLIC METHODS
// Initialize the lightbulb
void LifxLightBulb::init() {

	if (didAlreadyInit.exchange(true))
		return;

	log << "lb_addresses has: " << lb_addresses->size() << endl;
	unordered_set<void*>::const_iterator itr = lb_addresses->begin();
	IoTDeviceAddress* deviceAddress = (IoTDeviceAddress*) *itr;
	//cout << "Address: " << deviceAddress->getAddress() << endl;
	log << "Address: " << deviceAddress->getAddress() << endl;

	// Create IoTUDP socket
	communicationSocket = new IoTUDP(deviceAddress);

	//cout << "Host address: " << communicationSocket->getHostAddress() << endl;
	//cout << "Source port: " << communicationSocket->getSourcePort() << endl;
	//cout << "Destination port: " << communicationSocket->getDestinationPort() << endl << endl;
	log << "Host address: " << communicationSocket->getHostAddress() << endl;
	log << "Source port: " << communicationSocket->getSourcePort() << endl;
	log << "Destination port: " << communicationSocket->getDestinationPort() << endl << endl;

	// Launch the worker function in a separate thread.
	// 		NOTE: "this" pointer is passed into the detached thread because it does not belong
	// 			to this object anymore so if it executes certain methods of "this" object, then it needs
	// 			the correct references to stuff
	thread th1 (&LifxLightBulb::workerFunction, this, this);
	th1.detach();

	//cout << "Initialized LifxLightBulb!" << endl;
	log << "Initialized LifxLightBulb!" << endl;
	log.close();
}


void LifxLightBulb::turnOff() {

	//lock_guard<mutex> guard(bulbStateMutex);
	bulbStateMutex.lock();
	bulbIsOn = false;
	sendSetLightPowerPacket(0, 0);
	stateDidChange = true;
	bulbStateMutex.unlock();
}


void LifxLightBulb::turnOn() {

	//lock_guard<mutex> guard(bulbStateMutex);
	bulbStateMutex.lock();
	bulbIsOn = true;
	sendSetLightPowerPacket(65535, 0);
	stateDidChange = true;
	bulbStateMutex.unlock();
}


double LifxLightBulb::getHue() {
	double tmp = 0;
	settingBulbColorMutex.lock();
	tmp = ((double)currentHue / 65535.0) * 360.0;
	settingBulbColorMutex.unlock();

	return tmp;
}


double LifxLightBulb::getSaturation() {
	double tmp = 0;
	settingBulbColorMutex.lock();
	tmp = ((double)currentSaturation / 65535.0) * 360.0;
	settingBulbColorMutex.unlock();

	return tmp;
}


double LifxLightBulb::getBrightness() {
	double tmp = 0;
	settingBulbColorMutex.lock();
	tmp = ((double)currentBrightness / 65535.0) * 360.0;
	settingBulbColorMutex.unlock();

	return tmp;
}


int LifxLightBulb::getTemperature() {

	int tmp = 0;
	settingBulbTemperatureMutex.lock();
	tmp = currentTemperature;
	settingBulbTemperatureMutex.unlock();

	return tmp;
}


double LifxLightBulb::getHueRangeLowerBound() {
	if (!didGetBulbVersion) {
		return -1;
	}
	return ((double)hueLowerBound / 65535.0) * 360.0;
}


double LifxLightBulb::getHueRangeUpperBound() {
	if (!didGetBulbVersion) {
		return -1;
	}
	return ((double)hueUpperBound / 65535.0) * 360.0;
}


double LifxLightBulb::getSaturationRangeLowerBound() {
	if (!didGetBulbVersion) {
		return -1;
	}
	return ((double)saturationLowerBound / 65535.0) * 100.0;
}


double LifxLightBulb::getSaturationRangeUpperBound() {
	if (!didGetBulbVersion) {
		return -1;
	}
	return ((double)saturationUpperBound / 65535.0) * 100.0;
}


double LifxLightBulb::getBrightnessRangeLowerBound() {
	if (!didGetBulbVersion) {
		return -1;
	}
	return ((double)brightnessLowerBound / 65535.0) * 100.0;
}


double LifxLightBulb::getBrightnessRangeUpperBound() {
	if (!didGetBulbVersion) {
		return -1;
	}
	return ((double)brightnessUpperBound / 65535.0) * 100.0;
}


int LifxLightBulb::getTemperatureRangeLowerBound() {
	if (!didGetBulbVersion) {
		return -1;
	}
	return temperatureLowerBound;
}


int LifxLightBulb::getTemperatureRangeUpperBound() {
	if (!didGetBulbVersion) {
		return -1;
	}
	return temperatureUpperBound;
}


void LifxLightBulb::setTemperature(int _temperature) {

	settingBulbTemperatureMutex.lock();

	BulbColor* newColor = new BulbColor(currentHue, currentSaturation, currentBrightness, _temperature);
	sendSetLightColorPacket(newColor, 250);

	currentTemperature = _temperature;
	stateDidChange = true;

	settingBulbTemperatureMutex.unlock();
}


void LifxLightBulb::setColor(double _hue, double _saturation, double _brightness) {

	settingBulbColorMutex.lock();

	_hue /= 360.0;
	_saturation /= 100.0;
	_brightness /= 100.0;


	int newHue = (int)(_hue * 65535.0);
	int newSaturation = (int)(_saturation * 65535.0);
	int newBrightness = (int)(_brightness * 65535.0);

	BulbColor* newColor = new BulbColor(newHue, newSaturation, newBrightness, currentTemperature);
	sendSetLightColorPacket(newColor, 250);

	currentHue = newHue;
	currentSaturation = newSaturation;
	currentBrightness = newBrightness;
	stateDidChange = true;

	settingBulbColorMutex.unlock();
}


bool LifxLightBulb::getState() {

	bool tmp = false;

	bulbStateMutex.lock();
	tmp = bulbIsOn;
	bulbStateMutex.unlock();

	return tmp;
}


// PRIVATE METHODS
// Communication helpers
void LifxLightBulb::receivedPacket(char* packetData) {

	char headerBytes[36];
	for (int i = 0; i < 36; i++) {
		headerBytes[i] = packetData[i];
	}

	LifxHeader recHeader;
	recHeader.setFromBytes(headerBytes);

	// load the payload bytes (strip away the header)
	//char payloadBytes[recHeader.getSize()];
	char* payloadBytes = new char[recHeader.getSize()];
	for (int i = 36; i < recHeader.getSize(); i++) {
		payloadBytes[i - 36] = packetData[i];
	}

	int type = recHeader.getType();
	//cout << "Received: " << type << endl;

	DeviceStateService* dat = NULL;
	switch (type) {

		case 3:
			dat = parseDeviceStateServiceMessage(payloadBytes);
			//cout << "Service: " << dat->getService();
			//cout << "Port   : " << dat->getPort();
			// Avoid memory leak - delete this object
			delete dat;	
			break;

		case 33:
			handleStateVersionMessageReceived(payloadBytes);
			break;

		case 35:
			parseDeviceStateInfoMessage(payloadBytes);
			break;


		case 107:
			handleLightStateMessageReceived(payloadBytes);
			break;

		default:
			break;
			//cout << "unknown packet Type" << endl;
	}
	// Avoid memory leaks
	delete payloadBytes;
}


void LifxLightBulb::sendPacket(char* packetData, int len) {
	//cout << "sendPacket: About to send" << endl;
	lock_guard<mutex> guard(socketMutex);
	sendSocketFlag = true;
	communicationSocket->sendData(packetData, len);
	sendSocketFlag = false;
}


// Worker function which runs the while loop for receiving data from the bulb.
// Is blocking.
void LifxLightBulb::workerFunction(LifxLightBulb* llb) {

	// Need timeout on receives since we are not sure if a packet will be available
	// for processing so don't block waiting
	llb->communicationSocket->setTimeOut(50000);	// In milliseconds

	llb->turnOff();

	int64_t lastSentGetBulbVersionRequest = 0;	// time last request sent
	char dat[1024];

	llb->log << "Turning off and entering while loop!" << endl;

	while (true) {
		// Check if we got the bulb version yet
		// could have requested it but message could have gotten lost (UDP)
		if (!llb->didGetBulbVersion) {
			int64_t currentTime = (int64_t) time(NULL);
			if ((currentTime - lastSentGetBulbVersionRequest) > llb->GET_BULB_VERSION_RESEND_WAIT_SECONDS) {
				// Get the bulb version so we know what type of bulb this is.
				//cout << "Sending version packet! " << endl;
				llb->sendGetVersionPacket();
				lastSentGetBulbVersionRequest = currentTime;
			}
		}

		// Communication resource is busy so try again later
		if (llb->sendSocketFlag) {
			continue;
		}

		llb->socketMutex.lock();
		int ret = llb->communicationSocket->receiveData(dat, 1024);
		// Never forget to release!
		llb->socketMutex.unlock();

		// A packed arrived
		if (ret != -1) {
			llb->receivedPacket(dat);
		}

		// If a state change occurred then request the bulb state to ensure that the
		// bulb did indeed change its state to the correct state
		if (llb->stateDidChange) {
			llb->sendGetLightStatePacket();
		}

		// Wait a bit as to not tie up system resources
		this_thread::sleep_for (chrono::milliseconds(100));
		//cout << endl << "Sleep and wake up!" << endl;
	}
}


//  Sending
//  Device Messages
void LifxLightBulb::sendGetServicePacket() {
	LifxHeader header;
	header.setSize(36);
	header.setTagged(true);
	header.setMacAddress(bulbMacAddress);
	header.setSource(0);	// randomly picked
	header.setAck_required(false);
	header.setRes_required(false);
	header.setSequence(0);
	header.setType(2);

	char dataBytes[36];
	header.getHeaderBytes(dataBytes);

	sendPacket(dataBytes, 36);
}


void LifxLightBulb::sendGetHostInfoPacket() {
	LifxHeader header;
	header.setSize(36);
	header.setTagged(false);
	header.setMacAddress(bulbMacAddress);
	header.setSource(10);	// randomly picked
	header.setAck_required(false);
	header.setRes_required(false);
	header.setSequence(0);
	header.setType(12);

	char dataBytes[36];
	header.getHeaderBytes(dataBytes);

	sendPacket(dataBytes, 36);
}


void LifxLightBulb::sendGetHostFirmwarePacket() {
	LifxHeader header;
	header.setSize(36);
	header.setTagged(false);
	header.setMacAddress(bulbMacAddress);
	header.setSource(10);	// randomly picked
	header.setAck_required(false);
	header.setRes_required(false);
	header.setSequence(0);
	header.setType(14);

	char dataBytes[36];
	header.getHeaderBytes(dataBytes);

	sendPacket(dataBytes, 36);
}


void LifxLightBulb::sendGetWifiInfoPacket() {
	LifxHeader header;
	header.setSize(36);
	header.setTagged(false);
	header.setMacAddress(bulbMacAddress);
	header.setSource(10);	// randomly picked
	header.setAck_required(false);
	header.setRes_required(false);
	header.setSequence(0);
	header.setType(16);

	char dataBytes[36];
	header.getHeaderBytes(dataBytes);

	sendPacket(dataBytes, 36);
}


void LifxLightBulb::sendGetWifiFirmwarePacket() {
	LifxHeader header;
	header.setSize(36);
	header.setTagged(false);
	header.setMacAddress(bulbMacAddress);
	header.setSource(10);	// randomly picked
	header.setAck_required(false);
	header.setRes_required(false);
	header.setSequence(0);
	header.setType(18);

	char dataBytes[36];
	header.getHeaderBytes(dataBytes);

	sendPacket(dataBytes, 36);
}


void LifxLightBulb::sendGetPowerPacket() {
	LifxHeader header;
	header.setSize(36);
	header.setTagged(false);
	header.setMacAddress(bulbMacAddress);
	header.setSource(10);	// randomly picked
	header.setAck_required(false);
	header.setRes_required(false);
	header.setSequence(0);
	header.setType(20);

	char dataBytes[36];
	header.getHeaderBytes(dataBytes);

	sendPacket(dataBytes, 36);
}


void LifxLightBulb::sendSetPowerPacket(int level) {
	// Currently only 0 and 65535 are supported
	// This is a fix for now
	if ((level != 65535) && (level != 0)) {
		cerr << "Invalid parameter values" << endl;
		exit(1);
	}

	if ((level > 65535) || (level < 0)) {
		cerr << "Invalid parameter values" << endl;
		exit(1);
	}

	char packetBytes[38];

	LifxHeader header;
	header.setSize(38);
	header.setTagged(false);
	header.setMacAddress(bulbMacAddress);
	header.setSource(10);	// randomly picked
	header.setAck_required(false);
	header.setRes_required(false);
	header.setSequence(0);
	header.setType(21);
	char headerBytes[36];
	header.getHeaderBytes(headerBytes);

	for (int i = 0; i < 36; i++) {
		packetBytes[i] = headerBytes[i];
	}

	packetBytes[36] = (char)(level & 0xFF);
	packetBytes[37] = (char)((level >> 8) & 0xFF);

	sendPacket(packetBytes, 38);
}


void LifxLightBulb::sendGetLabelPacket() {
	LifxHeader header;
	header.setSize(36);
	header.setTagged(false);
	header.setMacAddress(bulbMacAddress);
	header.setSource(10); // randomly picked
	header.setAck_required(false);
	header.setRes_required(false);
	header.setSequence(0);
	header.setType(23);

	char dataBytes[36];
	header.getHeaderBytes(dataBytes);

	sendPacket(dataBytes, 36);
}


void LifxLightBulb::sendSetLabelPacket(string label) {
	// Currently only 0 and 65535 are supported
	// This is a fix for now
	if (label.length() != 32) {
		cerr << "Invalid parameter values, label must be 32 bytes long" << endl;
		exit(1);
	}

	char packetBytes[68];

	LifxHeader header;
	header.setSize(68);
	header.setTagged(false);
	header.setMacAddress(bulbMacAddress);
	header.setSource(10); // randomly picked
	header.setAck_required(false);
	header.setRes_required(false);
	header.setSequence(0);
	header.setType(24);
	char headerBytes[36];
	header.getHeaderBytes(headerBytes);

	for (int i = 0; i < 36; i++) {
		packetBytes[i] = headerBytes[i];
	}

	for (int i = 0; i < 32; i++) {
		packetBytes[i + 36] = label.c_str()[i];
	}

	sendPacket(packetBytes, 68);
}


void LifxLightBulb::sendGetVersionPacket() {
	LifxHeader header;
	header.setSize(36);
	header.setTagged(false);
	header.setMacAddress(bulbMacAddress);
	header.setSource(10); // randomly picked
	header.setAck_required(false);
	header.setRes_required(false);
	header.setSequence(0);
	header.setType(32);

	char dataBytes[36];
	header.getHeaderBytes(dataBytes);

	sendPacket(dataBytes, 36);
}


void LifxLightBulb::sendGetInfoPacket() {
	LifxHeader header;
	header.setSize(36);
	header.setTagged(false);
	header.setMacAddress(bulbMacAddress);
	header.setSource(10); // randomly picked
	header.setAck_required(false);
	header.setRes_required(false);
	header.setSequence(0);
	header.setType(34);

	char dataBytes[36];
	header.getHeaderBytes(dataBytes);

	sendPacket(dataBytes, 36);
}


void LifxLightBulb::sendGetLocationPacket() {
	LifxHeader header;
	header.setSize(36);
	header.setTagged(false);
	header.setMacAddress(bulbMacAddress);
	header.setSource(10); // randomly picked
	header.setAck_required(false);
	header.setRes_required(false);
	header.setSequence(0);
	header.setType(34);

	char dataBytes[36];
	header.getHeaderBytes(dataBytes);

	sendPacket(dataBytes, 36);
}


void LifxLightBulb::sendGetGroupPacket() {
	LifxHeader header;
	header.setSize(36);
	header.setTagged(false);
	header.setMacAddress(bulbMacAddress);
	header.setSource(10); // randomly picked
	header.setAck_required(false);
	header.setRes_required(false);
	header.setSequence(0);
	header.setType(51);

	char dataBytes[36];
	header.getHeaderBytes(dataBytes);

	sendPacket(dataBytes, 36);
}


//  Sending
//  Light Messages
void LifxLightBulb::sendGetLightStatePacket() {
	LifxHeader header;
	header.setSize(36);
	header.setTagged(false);
	header.setMacAddress(bulbMacAddress);
	header.setSource(10); // randomly picked
	header.setAck_required(false);
	header.setRes_required(false);
	header.setSequence(0);
	header.setType(101);

	char dataBytes[36];
	header.getHeaderBytes(dataBytes);

	sendPacket(dataBytes, 36);
}


void LifxLightBulb::sendSetLightColorPacket(BulbColor* bulbColor, long duration) {

	if ((duration > 4294967295l) || (duration < 0)) {
		cerr << "Invalid parameter value, duration out of range (0 - 4294967295)" << endl;
		exit(1);
	}

	char packetBytes[49];

	LifxHeader header;
	header.setSize(49);
	header.setTagged(false);
	header.setMacAddress(bulbMacAddress);
	header.setSource(10); // randomly picked
	header.setAck_required(false);
	header.setRes_required(false);
	header.setSequence(0);
	header.setType(102);
	char headerBytes[36];
	header.getHeaderBytes(headerBytes);

	for (int i = 0; i < 36; i++) {
		packetBytes[i] = headerBytes[i];
	}

	// 1 reserved packet
	packetBytes[37] = (char)(bulbColor->getHue() & 0xFF);
	packetBytes[38] = (char)((bulbColor->getHue() >> 8) & 0xFF);

	packetBytes[39] = (char)(bulbColor->getSaturation() & 0xFF);
	packetBytes[40] = (char)((bulbColor->getSaturation() >> 8) & 0xFF);

	packetBytes[41] = (char)(bulbColor->getBrightness() & 0xFF);
	packetBytes[42] = (char)((bulbColor->getBrightness() >> 8) & 0xFF);

	packetBytes[43] = (char)(bulbColor->getKelvin() & 0xFF);
	packetBytes[44] = (char)((bulbColor->getKelvin() >> 8) & 0xFF);

	packetBytes[45] = (char)((duration >> 0) & 0xFF);
	packetBytes[46] = (char)((duration >> 8) & 0xFF);
	packetBytes[47] = (char)((duration >> 16) & 0xFF);
	packetBytes[48] = (char)((duration >> 24) & 0xFF);

	sendPacket(packetBytes, 49);
	// Avoid memory leak - delete object
	delete bulbColor;
}


void LifxLightBulb::sendGetLightPowerPacket() {
	LifxHeader header;
	header.setSize(36);
	header.setTagged(false);
	header.setMacAddress(bulbMacAddress);
	header.setSource(10); // randomly picked
	header.setAck_required(false);
	header.setRes_required(false);
	header.setSequence(0);
	header.setType(116);

	char dataBytes[36];
	header.getHeaderBytes(dataBytes);

	sendPacket(dataBytes, 36);
}


void LifxLightBulb::sendSetLightPowerPacket(int level, long duration) {

	if ((level > 65535) || (duration > 4294967295l)
		    || (level < 0) || (duration < 0)) {
		cerr << "Invalid parameter values" << endl;
		exit(1);
	}

	char packetBytes[42];


	LifxHeader header;
	header.setSize(42);
	header.setTagged(false);
	header.setMacAddress(bulbMacAddress);
	header.setSource(10);	// randomly picked
	header.setAck_required(false);
	header.setRes_required(false);
	header.setSequence(0);
	header.setType(117);
	char headerBytes[36];
	header.getHeaderBytes(headerBytes);

	for (int i = 0; i < 36; i++) {
		packetBytes[i] = headerBytes[i];
	}

	packetBytes[36] = (char)(level & 0xFF);
	packetBytes[37] = (char)((level >> 8) & 0xFF);

	packetBytes[38] = (char)((duration >> 0) & 0xFF);
	packetBytes[39] = (char)((duration >> 8) & 0xFF);
	packetBytes[40] = (char)((duration >> 16) & 0xFF);
	packetBytes[41] = (char)((duration >> 24) & 0xFF);

	sendPacket(packetBytes, 42);
}


void LifxLightBulb::sendEchoRequestPacket(char data[64]) {

	char packetBytes[100];

	LifxHeader header;
	header.setSize(100);
	header.setTagged(false);
	header.setMacAddress(bulbMacAddress);
	header.setSource(10); // randomly picked
	header.setAck_required(false);
	header.setRes_required(false);
	header.setSequence(0);
	header.setType(58);
	char headerBytes[36];
	header.getHeaderBytes(headerBytes);

	for (int i = 0; i < 36; i++) {
		packetBytes[i] = headerBytes[i];
	}

	for (int i = 0; i < 64; i++) {
		packetBytes[i + 36] = data[i];
	}

	sendPacket(packetBytes, 100);
}


// Receiving
// Device Messages
DeviceStateService* LifxLightBulb::parseDeviceStateServiceMessage(char* payloadData) {
	int service = payloadData[0];
	int64_t port = ((payloadData[3] & 0xFF) << 24);
	port |= ((payloadData[2] & 0xFF) << 16);
	port |= ((payloadData[1] & 0xFF) << 8);
	port |= (payloadData[0] & 0xFF);

	return new DeviceStateService(service, port);
}


DeviceStateHostInfo* LifxLightBulb::parseDeviceStateHostInfoMessage(char* payloadData) {
	long signal = ((payloadData[3] & 0xFF) << 24);
	signal |= ((payloadData[2] & 0xFF) << 16);
	signal |= ((payloadData[1] & 0xFF) << 8);
	signal |= (payloadData[0] & 0xFF);

	long tx = ((payloadData[7] & 0xFF) << 24);
	tx |= ((payloadData[6] & 0xFF) << 16);
	tx |= ((payloadData[5] & 0xFF) << 8);
	tx |= (payloadData[4] & 0xFF);

	long rx = ((payloadData[11] & 0xFF) << 24);
	rx |= ((payloadData[10] & 0xFF) << 16);
	rx |= ((payloadData[9] & 0xFF) << 8);
	rx |= (payloadData[8] & 0xFF);

	return new DeviceStateHostInfo(signal, tx, rx);
}


DeviceStateHostFirmware* LifxLightBulb::parseDeviceStateHostFirmwareMessage(char* payloadData) {
	long build = 0;
	for (int i = 0; i < 8; i++) {
		build += ((int64_t) payloadData[i] & 0xffL) << (8 * i);
	}

	// 8 reserved bytes

	int64_t version = ((payloadData[19] & 0xFF) << 24);
	version |= ((payloadData[18] & 0xFF) << 16);
	version |= ((payloadData[17] & 0xFF) << 8);
	version |= (payloadData[16] & 0xFF);

	return new DeviceStateHostFirmware(build, version);
}


DeviceStateWifiInfo* LifxLightBulb::parseDeviceStateWifiInfoMessage(char* payloadData) {
	int64_t signal = ((payloadData[3] & 0xFF) << 24);
	signal |= ((payloadData[2] & 0xFF) << 16);
	signal |= ((payloadData[1] & 0xFF) << 8);
	signal |= (payloadData[0] & 0xFF);

	int64_t tx = ((payloadData[7] & 0xFF) << 24);
	tx |= ((payloadData[6] & 0xFF) << 16);
	tx |= ((payloadData[5] & 0xFF) << 8);
	tx |= (payloadData[4] & 0xFF);

	int64_t rx = ((payloadData[11] & 0xFF) << 24);
	rx |= ((payloadData[10] & 0xFF) << 16);
	rx |= ((payloadData[9] & 0xFF) << 8);
	rx |= (payloadData[8] & 0xFF);

	return new DeviceStateWifiInfo(signal, tx, rx);
}


DeviceStateWifiFirmware* LifxLightBulb::parseDeviceStateWifiFirmwareMessage(char* payloadData) {
	long build = 0;
	for (int i = 0; i < 8; i++) {
		build += ((int64_t) payloadData[i] & 0xffL) << (8 * i);
	}

	// 8 reserved bytes

	int64_t version = ((payloadData[19] & 0xFF) << 24);
	version |= ((payloadData[18] & 0xFF) << 16);
	version |= ((payloadData[17] & 0xFF) << 8);
	version |= (payloadData[16] & 0xFF);

	return new DeviceStateWifiFirmware(build, version);
}


int LifxLightBulb::parseStatePowerMessage(char* payloadData) {
	int level = ((payloadData[1] & 0xFF) << 8);
	level |= (payloadData[0] & 0xFF);
	return level;
}


DeviceStateVersion* LifxLightBulb::parseDeviceStateVersionMessage(char* payloadData) {
	int64_t vender = ((payloadData[3] & 0xFF) << 24);
	vender |= ((payloadData[2] & 0xFF) << 16);
	vender |= ((payloadData[1] & 0xFF) << 8);
	vender |= (payloadData[0] & 0xFF);

	int64_t product = ((payloadData[7] & 0xFF) << 24);
	product |= ((payloadData[6] & 0xFF) << 16);
	product |= ((payloadData[5] & 0xFF) << 8);
	product |= (payloadData[4] & 0xFF);

	int64_t version = ((payloadData[11] & 0xFF) << 24);
	version |= ((payloadData[10] & 0xFF) << 16);
	version |= ((payloadData[9] & 0xFF) << 8);
	version |= (payloadData[8] & 0xFF);

	return new DeviceStateVersion(vender, product, version);
}


DeviceStateInfo* LifxLightBulb::parseDeviceStateInfoMessage(char* payloadData) {
	int64_t time = 0;
	int64_t upTime = 0;
	int64_t downTime = 0;
	for (int i = 0; i < 8; i++) {
		time += ((int64_t) payloadData[i] & 0xffL) << (8 * i);
		upTime += ((int64_t) payloadData[i + 8] & 0xffL) << (8 * i);
		downTime += ((int64_t) payloadData[i + 16] & 0xffL) << (8 * i);
	}

	return new DeviceStateInfo(time, upTime, downTime);
}


DeviceStateLocation* LifxLightBulb::parseDeviceStateLocationMessage(char* payloadData) {
	char location[16];
	for (int i = 0; i < 16; i++) {
		location[i] = payloadData[i];
	}

	char labelBytes[32];
	for (int i = 0; i < 32; i++) {
		labelBytes[i] = payloadData[i + 16];
	}

	int64_t updatedAt = 0;
	for (int i = 0; i < 8; i++) {
		updatedAt += ((int64_t) payloadData[48] & 0xffL) << (8 * i);
	}

	string str(labelBytes);
	return new DeviceStateLocation(location, str, updatedAt);
}


DeviceStateGroup* LifxLightBulb::parseDeviceStateGroupMessage(char* payloadData) {
	char group[16];
	for (int i = 0; i < 16; i++) {
		group[i] = payloadData[i];
	}

	char labelBytes[32];
	for (int i = 0; i < 32; i++) {
		labelBytes[i] = payloadData[i + 16];
	}

	int64_t updatedAt = 0;
	for (int i = 0; i < 8; i++) {
		updatedAt += ((int64_t) payloadData[48] & 0xffL) << (8 * i);
	}

	string str(labelBytes);
	return new DeviceStateGroup(group, str, updatedAt);
}


// Receiving
// Light Messages
LightState* LifxLightBulb::parseLightStateMessage(char* payloadData) {

	char colorData[8];
	for (int i = 0; i < 8; i++) {
		colorData[i] = payloadData[i];
	}
	//BulbColor color(colorData);
	BulbColor* color = new BulbColor(colorData);

	int power = ((payloadData[11] & 0xFF) << 8);
	power |= (payloadData[10] & 0xFF);

	string label(payloadData);

	char labelArray[32];
	for (int i = 0; i < 32; i++) {
		labelArray[i] = payloadData[12 + i];
	}

	return new LightState(color, power, label);
}


int LifxLightBulb::parseLightStatePowerMessage(char* payloadData) {
	int level = ((payloadData[1] & 0xFF) << 8);
	level |= (payloadData[0] & 0xFF);
	return level;
}


// Private Handlers
void LifxLightBulb::handleStateVersionMessageReceived(char* payloadData) {

	DeviceStateVersion* deviceState = parseDeviceStateVersionMessage(payloadData);
	int productNumber = (int)deviceState->getProduct();

	bool isColor = false;

	if (productNumber == 1) {// Original 1000
		isColor = true;
	} else if (productNumber == 3) {//Color 650
		isColor = true;
	} else if (productNumber == 10) {// White 800 (Low Voltage)
		isColor = false;
	} else if (productNumber == 11) {// White 800 (High Voltage)
		isColor = false;
	} else if (productNumber == 18) {// White 900 BR30 (Low Voltage)
		isColor = false;
	} else if (productNumber == 20) {// Color 1000 BR30
		isColor = true;
	} else if (productNumber == 22) {// Color 1000
		isColor = true;
	}

	if (isColor) {
		hueLowerBound = 0;
		hueUpperBound = 65535;
		saturationLowerBound = 0;
		saturationUpperBound = 65535;
		brightnessLowerBound = 0;
		brightnessUpperBound = 65535;
		temperatureLowerBound = 2500;
		temperatureUpperBound = 9000;
	} else {
		hueLowerBound = 0;
		hueUpperBound = 0;
		saturationLowerBound = 0;
		saturationUpperBound = 0;
		brightnessLowerBound = 0;
		brightnessUpperBound = 65535;// still can dim bulb
		temperatureLowerBound = 2500;
		temperatureUpperBound = 9000;
	}

	didGetBulbVersion.exchange(true);
	// Avoid memory leak - delete this object
	delete deviceState;
}


void LifxLightBulb::handleLightStateMessageReceived(char* payloadData) {
	LightState* lightState = parseLightStateMessage(payloadData);

	BulbColor* color = lightState->getColor();
	int power = lightState->getPower();

	//cout << "color->getHue(): " << color->getHue() << " - currentHue: " << currentHue << endl;
	//cout << "color->getSaturation(): " << color->getSaturation() << " - currentSaturation: " << currentSaturation << endl;
	//cout << "color->getBrightness(): " << color->getBrightness() << " - currentBrightness: " << currentBrightness << endl;
	//cout << "color->getKelvin(): " << color->getKelvin() << " - currentTemperature: " << currentTemperature << endl;

	bool bulbWrongColor = false;
	bulbWrongColor = bulbWrongColor || (color->getHue() != currentHue);
	bulbWrongColor = bulbWrongColor || (color->getSaturation() != currentSaturation);
	bulbWrongColor = bulbWrongColor || (color->getBrightness() != currentBrightness);
	bulbWrongColor = bulbWrongColor || (color->getKelvin() != currentTemperature);


	// gets set to true if any of the below if statements are taken
	stateDidChange = false;

	if (bulbWrongColor) {
		BulbColor* newColor = new BulbColor(currentHue, currentSaturation, currentBrightness, currentTemperature);
		sendSetLightColorPacket(newColor, 250);
		//cout << "Failed Check 1" << endl;
	}

	bulbStateMutex.lock();
	bool bulbIsOnTmp = bulbIsOn;
	bulbStateMutex.unlock();

	if ((!bulbIsOnTmp) && (power != 0)) {
		turnOff();
		//cout << "Failed Check 2:  " << endl;

	}

	if (bulbIsOnTmp && (power < 65530)) {
		turnOn();
		//cout << "Failed Check 3:  " << endl;

	}
	// Avoid memory leak - delete object
	delete lightState;
	delete color;
}


// Functions for the main function
void onOff(LifxLightBulb *llb) {

	for (int i = 0; i < 2; i++) {
		llb->turnOff();
		//cout << "Turning off!" << endl;
		this_thread::sleep_for (chrono::milliseconds(1000));
		llb->turnOn();
		//cout << "Turning on!" << endl;
		this_thread::sleep_for (chrono::milliseconds(1000));
	}
}


void adjustTemp(LifxLightBulb *llb) {

	for (int i = 2500; i < 9000; i += 100) {
		//cout << "Adjusting Temp: " << i << endl;
		llb->setTemperature(i);
		this_thread::sleep_for (chrono::milliseconds(100));
	}
	//cout << "Adjusted temperature to 9000!" << endl;
	for (int i = 9000; i > 2500; i -= 100) {
		//cout << "Adjusting Temp: " << i << endl;
		llb->setTemperature(i);
		this_thread::sleep_for (chrono::milliseconds(100));
	}
	//cout << "Adjusted temperature to 2500!" << endl;
}


void adjustBright(LifxLightBulb *llb) {
	for (int i = 100; i > 0; i -= 10) {
		//cout << "Adjusting Brightness: " << i << endl;
		llb->setColor(llb->getHue(), llb->getSaturation(), i);
		this_thread::sleep_for (chrono::milliseconds(100));
	}
	//cout << "Adjusted brightness to 0!" << endl;
	for (int i = 0; i < 100; i += 10) {
		//cout << "Adjusting Brightness: " << i << endl;
		llb->setColor(llb->getHue(), llb->getSaturation(), i);
		this_thread::sleep_for (chrono::milliseconds(100));
	}
	//cout << "Adjusting brightness to 100!" << endl;
}


int main(int argc, char *argv[])
{
	string macAddress1 = "D073D5128E300000";
	//string macAddress = "D073D50241DA0000";
	string devIPAddress1 = "192.168.2.126";
	//string devIPAddress = "192.168.2.232";
	IoTDeviceAddress* devAddress1 = new IoTDeviceAddress(devIPAddress1, 12345, 56700, false, false);
	unordered_set<void*>* myset1 = new unordered_set<void*>();
	myset1->insert(devAddress1);

	IoTSet<void*>* setDevAddress1 = new IoTSet<void*>(myset1);
	LifxLightBulb *llb1 = new LifxLightBulb(setDevAddress1, macAddress1);
	cout << "Generated LifxLightBulb object!" << endl;
	llb1->init();
	llb1->turnOn();
	onOff(llb1);
	adjustTemp(llb1);
	adjustBright(llb1);
//	llb->turnOff();

//	delete devAddress1;
//	delete llb1;

	return 0;
}
