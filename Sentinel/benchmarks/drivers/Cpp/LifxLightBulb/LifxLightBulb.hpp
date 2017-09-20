#ifndef _LIFXLIGHTBULB_HPP__
#define _LIFXLIGHTBULB_HPP__
#include <iostream>
#include <fstream>
#include <atomic>
#include <mutex>
#include <thread>
#include <chrono>

#include <string.h>
#include <time.h>

#include "LightBulb.hpp"
#include "Socket.hpp"
#include "IoTRMIUtil.hpp"
#include "IoTSet.hpp"
#include "IoTUDP.hpp"
#include "IoTDeviceAddress.hpp"
#include "Iterator.hpp"

// Helper classes for LifxLightBulb
#include "LifxHeader.hpp"
#include "BulbColor.hpp"
#include "DeviceStateGroup.hpp"
#include "DeviceStateHostFirmware.hpp"
#include "DeviceStateHostInfo.hpp"
#include "DeviceStateInfo.hpp"
#include "DeviceStateLocation.hpp"
#include "DeviceStateService.hpp"
#include "DeviceStateVersion.hpp"
#include "DeviceStateWifiFirmware.hpp"
#include "DeviceStateWifiInfo.hpp"
#include "LightState.hpp"


using namespace std;

// Driver LifxLightBulb
// Implemented based on LightBulb virtual class (interface)

//std::atomic
std::atomic<bool> didAlreadyInit(false);
std::atomic<bool> didGetBulbVersion(false);

class LifxLightBulb : public LightBulb
{
	private:
		// Constants
		const static int64_t GET_BULB_VERSION_RESEND_WAIT_SECONDS = 10;

		// Variables
		IoTUDP *communicationSocket;
		char bulbMacAddress[8];
		//TODO:
		//static Semaphore socketMutex = new Semaphore(1);
		bool sendSocketFlag = false;

		// Current Bulb Values
		int currentHue = 0;
		int currentSaturation = 0;
		int currentBrightness = 65535;
		int currentTemperature = 9000;
		bool bulbIsOn = false;

		//std::atomic
		atomic<bool> didAlreadyInit;
		atomic<bool> didGetBulbVersion;

		// Mutex locks
		mutex socketMutex;
		mutex settingBulbColorMutex;
		mutex settingBulbTemperatureMutex;
		mutex bulbStateMutex;

		// color and temperature ranges for the bulbs
		int hueLowerBound = 0;
		int hueUpperBound = 0;
		int saturationLowerBound = 0;
		int saturationUpperBound = 0;
		int brightnessLowerBound = 0;
		int brightnessUpperBound = 0;
		int temperatureLowerBound = 2500;
		int temperatureUpperBound = 9000;

		// Check if a state change was requested, used to poll the bulb for if the bulb did
		// preform the requested state change
		bool stateDidChange = false;

		// Device address
		IoTSet<void*>* lb_addresses;	// IoTSet<IoTDeviceAddress*>* lb_addresses

		// Logging
		ofstream log;
	public:

		// Constructor
		LifxLightBulb();
		//LifxLightBulb(IoTSet<IoTDeviceAddress*>* _devAddress, string macAddress);
		LifxLightBulb(IoTSet<void*>* _devAddress, string macAddress);
		~LifxLightBulb();
		// Initialize the lightbulb
		void init();
		void turnOff();
		void turnOn();
		double getHue();
		double getSaturation();
		double getBrightness();
		int getTemperature();
		double getHueRangeLowerBound();
		double getHueRangeUpperBound();
		double getSaturationRangeLowerBound();
		double getSaturationRangeUpperBound();
		double getBrightnessRangeLowerBound();
		double getBrightnessRangeUpperBound();
		int getTemperatureRangeLowerBound();
		int getTemperatureRangeUpperBound();
		void setTemperature(int _temperature);
		void setColor(double _hue, double _saturation, double _brightness);
		bool getState();

	private:
		// Private functions
		// Communication helpers
		void receivedPacket(char* packetData);
		void sendPacket(char* packetData, int len);
		// Worker function which runs the while loop for receiving data from the bulb.
		// Is blocking.
		void workerFunction(LifxLightBulb* llb);
		//  Sending
		//  Device Messages
		void sendGetServicePacket();
		void sendGetHostInfoPacket();
		void sendGetHostFirmwarePacket();
		void sendGetWifiInfoPacket();
		void sendGetWifiFirmwarePacket();
		void sendGetPowerPacket();
		void sendSetPowerPacket(int level);
		void sendGetLabelPacket();
		void sendSetLabelPacket(string label);
		void sendGetVersionPacket();
		void sendGetInfoPacket();
		void sendGetLocationPacket();
		void sendGetGroupPacket();
		//  Sending
		//  Light Messages
		void sendGetLightStatePacket();
		void sendSetLightColorPacket(BulbColor* bulbColor, long duration);
		void sendGetLightPowerPacket();
		void sendSetLightPowerPacket(int level, long duration);
		void sendEchoRequestPacket(char data[64]);
		// Receiving
		// Device Messages
		DeviceStateService* parseDeviceStateServiceMessage(char* payloadData);
		DeviceStateHostInfo* parseDeviceStateHostInfoMessage(char* payloadData);
		DeviceStateHostFirmware* parseDeviceStateHostFirmwareMessage(char* payloadData);
		DeviceStateWifiInfo* parseDeviceStateWifiInfoMessage(char* payloadData);
		DeviceStateWifiFirmware* parseDeviceStateWifiFirmwareMessage(char* payloadData);
		int parseStatePowerMessage(char* payloadData);
		DeviceStateVersion* parseDeviceStateVersionMessage(char* payloadData);
		DeviceStateInfo* parseDeviceStateInfoMessage(char* payloadData);
		DeviceStateLocation* parseDeviceStateLocationMessage(char* payloadData);
		DeviceStateGroup* parseDeviceStateGroupMessage(char* payloadData);
		// Receiving
		// Light Messages
		LightState* parseLightStateMessage(char* payloadData);
		int parseLightStatePowerMessage(char* payloadData);
		// Private Handlers
		void handleStateVersionMessageReceived(char* payloadData);
		void handleLightStateMessageReceived(char* payloadData);
};

#endif
