#include <iostream>
#include <chrono>
#include <thread>

#include "Lifxtest.hpp"
#include "LifxLightBulb.cpp"
#include "LabRoom.cpp"
#include "Iterator.hpp"


// External create, destroy, and init functions
extern "C" void* createLifxtest(void** params) {
	// Arguments: IoTSet<void*>* lifx_light_bulb
	//return new Lifxtest((IoTSet<void*>*) params[0]);
	return new Lifxtest((IoTSet<void*>*) params[0], (IoTSet<void*>*) params[1], (IoTRelation<void*,void*>*) params[2]);
}


extern "C" void destroyLifxtest(void* t) {
	Lifxtest* lt = (Lifxtest*) t;
	delete lt;
}


extern "C" void initLifxtest(void* t) {
	Lifxtest* lt = (Lifxtest*) t;
	lt->init();
}


// Empty constructor (for testing)
Lifxtest::Lifxtest() {
	log.open("Lifxtest_object_cpp.log");
	log << "lifx_light_bulb initialized!" << endl;
}


// Constructor with only 1 IoTSet object (lifx_light_bulb)
Lifxtest::Lifxtest(IoTSet<void*>* _lifx_light_bulb) {

	log.open("Lifxtest_object_cpp.log");
	lifx_light_bulb = _lifx_light_bulb;
	log << "lifx_light_bulb initialized!" << endl;
}


// Constructor with 2 IoTSet and 1 IoTRelation objects
Lifxtest::Lifxtest(IoTSet<void*>* _lifx_light_bulb, IoTSet<void*>* _lab_room, IoTRelation<void*,void*>* _roomLightRelation) {

	log.open("Lifxtest_object_cpp.log");
	lifx_light_bulb = _lifx_light_bulb;
	lab_room = _lab_room;
	roomLightRelation = _roomLightRelation;
	log << "lifx_light_bulb initialized!" << endl;
}


// Constructor with void** argument
Lifxtest::Lifxtest(void** args) {

	log.open("Lifxtest_object_cpp.log");
	lifx_light_bulb = (IoTSet<void*>*) args[0];
	log << "lifx_light_bulb initialized!" << endl;
}


Lifxtest::~Lifxtest() {
}


/*void Lifxtest::init() {

	unordered_set<void*>* bulbSet = lifx_light_bulb->values();
	//for (unordered_set<void*>::const_iterator itr = bulbSet->begin(); itr != bulbSet->end(); ++itr) {
	log << "Get into Lifxtest init()!" << endl;
	for (auto itr = bulbSet->begin(); itr != bulbSet->end(); ++itr) {
		log << "Iteration init()!" << endl;
		//LightBulb* lifx = (LightBulb*) *itr;
		LightBulbTest* lifx = (LightBulbTest*) *itr;
		log << "Getting object!" << endl;
		lifx->init();
		log << "Executing init!" << endl;
		this_thread::sleep_for (chrono::milliseconds(1000));

		for (int i = 0; i < 10; i++) {

			lifx->init();
//			lifx->turnOff();
			//cout << "Turning off!" << endl;
			log << "Turning off!" << endl;
			this_thread::sleep_for (chrono::milliseconds(1000));
//			lifx->turnOn();
			//cout << "Turning on!" << endl;
			log << "Turning on!" << i << endl;
			this_thread::sleep_for (chrono::milliseconds(1000));
		}
		
*/
/*		for (int i = 2500; i < 9000; i += 100) {
			//cout << "Adjusting Temp: " << i << endl;
			log << "Adjusting Temp: " << i << endl;
			lifx->setTemperature(i);
			this_thread::sleep_for (chrono::milliseconds(100));
		}

		for (int i = 9000; i > 2500; i -= 100) {
			//cout << "Adjusting Temp: " << i << endl;
			log << "Adjusting Temp: " << i << endl;
			lifx->setTemperature(i);
			this_thread::sleep_for (chrono::milliseconds(100));
		}

		for (int i = 100; i > 0; i -= 10) {
			//cout << "Adjusting Brightness: " << i << endl;
			log << "Adjusting Brightness: " << i << endl;
			lifx->setColor(lifx->getHue(), lifx->getSaturation(), i);
			this_thread::sleep_for (chrono::milliseconds(500));
		}

		for (int i = 0; i < 100; i += 10) {
			//cout << "Adjusting Brightness: " << i << endl;
			log << "Adjusting Brightness: " << i << endl;
			lifx->setColor(lifx->getHue(), lifx->getSaturation(), i);
			this_thread::sleep_for (chrono::milliseconds(500));
		}
		lifx->turnOff();
	}

	log << "End of iteration.. closing!" << endl;
	log.close();
	//while(true) { }	// Testing infinite loop - will need to do "pkill IoTSlave"
}*/


void Lifxtest::init() {

	unordered_set<void*>* bulbSet = lifx_light_bulb->values();
	unordered_set<void*>* roomSet = lab_room->values();
	unordered_multimap<void*,void*>* roomLightRel = roomLightRelation->values();
	log << "Size of map: " << roomLightRel->size() << endl;
	//for (unordered_set<void*>::const_iterator itr = bulbSet->begin(); itr != bulbSet->end(); ++itr) {
	for (auto itr = roomSet->begin(); itr != roomSet->end(); ++itr) {
		log << "Getting Room!" << endl;
		//Room* rs = (Room*) *itr;
		RoomSmart* rs = (RoomSmart*) *itr;
		log << "Getting Room! ID: " << rs->getRoomID() << endl;
		auto itrLight = roomLightRel->find(rs);

		if (itrLight == roomLightRel->end())
			log << "No match!" << endl;
		//while (itrLight != roomLightRel->end()) {
		else {
			//LightBulb* lifx = (LightBulb*) itrLight->second;
			log << "Getting LightBulb!" << endl;
			LightBulbTest* lifx = (LightBulbTest*) itrLight->second;
			log << "Executing init!" << endl;
			lifx->init();
			for (int i = 0; i < 10; i++) {
				lifx->turnOff();
				//cout << "Turning off!" << endl;
				log << "Turning off!" << endl;
				this_thread::sleep_for (chrono::milliseconds(1000));
				lifx->turnOn();
				//cout << "Turning on!" << endl;
				log << "Turning on!" << i << endl;
				this_thread::sleep_for (chrono::milliseconds(1000));
			}
			for (int i = 2500; i < 9000; i += 100) {
				//cout << "Adjusting Temp: " << i << endl;
				log << "Adjusting Temp: " << i << endl;
				lifx->setTemperature(i);
				this_thread::sleep_for (chrono::milliseconds(100));
			}

			for (int i = 9000; i > 2500; i -= 100) {
				//cout << "Adjusting Temp: " << i << endl;
				log << "Adjusting Temp: " << i << endl;
				lifx->setTemperature(i);
				this_thread::sleep_for (chrono::milliseconds(100));
			}

			for (int i = 100; i > 0; i -= 10) {
				//cout << "Adjusting Brightness: " << i << endl;
				log << "Adjusting Brightness: " << i << endl;
				lifx->setColor(lifx->getHue(), lifx->getSaturation(), i);
				this_thread::sleep_for (chrono::milliseconds(500));
			}

			for (int i = 0; i < 100; i += 10) {
				//cout << "Adjusting Brightness: " << i << endl;
				log << "Adjusting Brightness: " << i << endl;
				lifx->setColor(lifx->getHue(), lifx->getSaturation(), i);
				this_thread::sleep_for (chrono::milliseconds(500));
			}
			//++itrLight;
		}
		log << "End of one LightBulb!" << endl << endl;
	}
		
	log << "End of iteration.. closing!" << endl;
	log.close();
	//while(true) { }	// Testing infinite loop - will need to do "pkill IoTSlave"
}

/*
int main(int argc, char *argv[])
{
	// LightBulb #1
	string macAddress1 = "D073D5128E300000";
	string devIPAddress1 = "192.168.2.126";
	IoTDeviceAddress* devAddress1 = new IoTDeviceAddress(devIPAddress1, 12345, 56700, false, false);
	unordered_set<void*>* myset1 = new unordered_set<void*>();
	myset1->insert(devAddress1);
	IoTSet<void*>* setDevAddress1 = new IoTSet<void*>(myset1);
	LifxLightBulb *llb1 = new LifxLightBulb(setDevAddress1, macAddress1);
	//cout << "Generated LifxLightBulb object!" << endl;

	// LightBulb #2
	string macAddress2 = "D073D50241DA0000";
	string devIPAddress2 = "192.168.2.232";
	IoTDeviceAddress* devAddress2 = new IoTDeviceAddress(devIPAddress2, 12346, 56700, false, false);
	unordered_set<void*>* myset2 = new unordered_set<void*>();
	myset2->insert(devAddress2);
	IoTSet<void*>* setDevAddress2 = new IoTSet<void*>(myset2);
	LifxLightBulb *llb2 = new LifxLightBulb(setDevAddress2, macAddress2);

	// Set of lightbulbs
	unordered_set<void*>* setLb = new unordered_set<void*>();
	setLb->insert(llb1);
	setLb->insert(llb2);
	IoTSet<void*>* lbSet = new IoTSet<void*>(setLb);

	// Set of rooms
	LabRoom *lr1 = new LabRoom();
	LabRoom *lr2 = new LabRoom();
	unordered_set<void*>* setLR = new unordered_set<void*>();
	setLR->insert(lr1);
	setLR->insert(lr2);
	IoTSet<void*>* lrSet = new IoTSet<void*>(setLR);

	pair<void*,void*>* pair1 = new pair<void*,void*>(lr1, llb1);
	pair<void*,void*>* pair2 = new pair<void*,void*>(lr2, llb2);
	unordered_multimap<void*,void*>* mmap = new unordered_multimap<void*,void*>();
	mmap->insert(*pair1);
	mmap->insert(*pair2);
	IoTRelation<void*,void*>* rlRel = new IoTRelation<void*,void*>(mmap);

	//void* args[1];
	//args[0] = (void*) lbSet;
	//Lifxtest *lt = new Lifxtest(args);
	Lifxtest *lt = new Lifxtest(lbSet, lrSet, rlRel);
	lt->init();

	//delete llb1;
	//delete llb2;
	delete devAddress1;
	delete devAddress2;
	delete myset1;
	delete myset2;
	delete setDevAddress1;
	delete setDevAddress2;
	delete setLb;
	delete lbSet;
	delete lr1;
	delete lr2;
	delete lrSet;
	delete pair1;
	delete pair2;

	return 0;
}*/


