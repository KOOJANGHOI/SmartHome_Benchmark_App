BASE = ../../..

include $(BASE)/common.mk

BOOFDIR := ../libs/boofcv_libs
BOOFJARS := $(BOOFDIR)/BoofCV-feature-0.21.jar:$(BOOFDIR)/BoofCV-io-0.21.jar:$(BOOFDIR)/BoofCV-visualize-0.21.jar:$(BOOFDIR)/BoofCV-ip-0.21.jar
JLAYERDIR := ../libs/jlayer_libs
JLAYERJARS := $(JLAYERDIR)/jl1.0.1.jar
JFLAGS = -d $(BIN_DIR) -cp $(BIN_DIR):$(PHONEJARS):$(BOOFJARS):$(JLAYERJARS):$(CHECKERJARS)
JARFLAGS = cf
INTFACE_DIR = iotcode/interfaces

# checker option
#
CHECKER_OPT = -processor iotchecker.IoTJavaChecker -AprintErrorStack

ASTUBS = -Astubs=../../../checker/astubs/ 

all: light camera labroom greenlawn sprinkler moisture weathergateway audioroom gpsgateway ihome homeroom alarm motion multipurpose waterleak doorlock
check-all: check-light check-camera check-labroom check-greenlawn check-sprinkler check-moisture check-weathergateway check-audioroom check-gpsgateway check-ihome check-homeroom check-alarm check-motion check-multipurpose check-waterleak

# Compile - without checker
#
PHONY += light
light:
	$(JAVAC) $(JFLAGS) LifxLightBulb/*.java
	cp LifxLightBulb/LifxLightBulb.config $(BIN_DIR)/iotcode/LifxLightBulb
	cd $(BIN_DIR)/iotcode/LifxLightBulb; $(JAR) $(JARFLAGS) LifxLightBulb.jar ../../iotcode/LifxLightBulb/*.class ../../iotcode/interfaces/LightBulb*.class

PHONY += camera
camera:
	$(JAVAC) $(JFLAGS) AmcrestCamera/*.java
	cp AmcrestCamera/AmcrestCamera.config $(BIN_DIR)/iotcode/AmcrestCamera
	cd $(BIN_DIR)/iotcode/AmcrestCamera; $(JAR) $(JARFLAGS) AmcrestCamera.jar ../../iotcode/AmcrestCamera/*.class ../../iotcode/interfaces/Camera*.class ../../iotcode/interfaces/Resolution*.class

PHONY += labroom
labroom:
	$(JAVAC) $(JFLAGS) LabRoom/*.java
	cp LabRoom/LabRoom.config $(BIN_DIR)/iotcode/LabRoom
	cd $(BIN_DIR)/iotcode/LabRoom; $(JAR) $(JARFLAGS) LabRoom.jar ../../iotcode/LabRoom/*.class ../../iotcode/interfaces/Room*.class

PHONY += greenlawn
greenlawn:
	$(JAVAC) $(JFLAGS) GreenLawn/*.java
	cp GreenLawn/GreenLawn.config $(BIN_DIR)/iotcode/GreenLawn
	cd $(BIN_DIR)/iotcode/GreenLawn; $(JAR) $(JARFLAGS) GreenLawn.jar ../../iotcode/GreenLawn/*.class ../../iotcode/interfaces/Lawn*.class

PHONY += sprinkler
sprinkler:
	$(JAVAC) $(JFLAGS) EspSprinkler/*.java
	cp EspSprinkler/EspSprinkler.config $(BIN_DIR)/iotcode/EspSprinkler
	cd $(BIN_DIR)/iotcode/EspSprinkler; $(JAR) $(JARFLAGS) EspSprinkler.jar ../../iotcode/EspSprinkler/*.class ../../iotcode/interfaces/Sprinkler*.class ../../iotcode/interfaces/ZoneState*.class

PHONY += moisture
moisture:
	$(JAVAC) $(JFLAGS) SpruceSensor/*.java
	cp SpruceSensor/SpruceSensor.config $(BIN_DIR)/iotcode/SpruceSensor
	#cd $(BIN_DIR)/iotcode/SpruceSensor; $(JAR) $(JARFLAGS) SpruceSensor.jar ../../iotcode/SpruceSensor/*.class ../../iotcode/interfaces/MoistureSensor*.class ../../iotcode/interfaces/Camera*.class ../../IrrigationController/MotionDetection*.class
	cd $(BIN_DIR)/iotcode/SpruceSensor; $(JAR) $(JARFLAGS) SpruceSensor.jar ../../iotcode/SpruceSensor/*.class ../../iotcode/interfaces/MoistureSensor*.class ../../iotcode/interfaces/Camera*.class

PHONY += weathergateway
weathergateway:
	$(JAVAC) $(JFLAGS) WeatherPhoneGateway/*.java
	cp WeatherPhoneGateway/WeatherPhoneGateway.config $(BIN_DIR)/iotcode/WeatherPhoneGateway
	cd $(BIN_DIR)/iotcode/WeatherPhoneGateway; $(JAR) $(JARFLAGS) WeatherPhoneGateway.jar ../../iotcode/WeatherPhoneGateway/*.class ../../iotcode/interfaces/WeatherGateway*.class

PHONY += audioroom
audioroom:
	$(JAVAC) $(JFLAGS) AudioRoom/*.java
	cp AudioRoom/AudioRoom.config $(BIN_DIR)/iotcode/AudioRoom
	cd $(BIN_DIR)/iotcode/AudioRoom; $(JAR) $(JARFLAGS) AudioRoom.jar ../../iotcode/AudioRoom/*.class ../../iotcode/interfaces/Room*.class

PHONY += gpsgateway
gpsgateway:
	$(JAVAC) $(JFLAGS) GPSPhoneGateway/*.java
	cp GPSPhoneGateway/GPSPhoneGateway.config $(BIN_DIR)/iotcode/GPSPhoneGateway
	cd $(BIN_DIR)/iotcode/GPSPhoneGateway; $(JAR) $(JARFLAGS) GPSPhoneGateway.jar ../../iotcode/GPSPhoneGateway/*.class ../../iotcode/interfaces/GPSGateway*.class

PHONY += ihome
ihome:
	$(JAVAC) $(JFLAGS) IHome/*.java
	cp IHome/IHome.config $(BIN_DIR)/iotcode/IHome
	cd $(BIN_DIR)/iotcode/IHome; $(JAR) $(JARFLAGS) IHome.jar ../../iotcode/IHome/*.class ../../iotcode/interfaces/Speaker*.class

PHONY += homeroom
homeroom:
	$(JAVAC) $(JFLAGS) HomeRoom/*.java
	cp HomeRoom/HomeRoom.config $(BIN_DIR)/iotcode/HomeRoom
	cd $(BIN_DIR)/iotcode/HomeRoom; $(JAR) $(JARFLAGS) HomeRoom.jar ../../iotcode/HomeRoom/*.class ../../iotcode/interfaces/Room*.class

PHONY += alarm
alarm:
	$(JAVAC) $(JFLAGS) EspAlarm/*.java
	cp EspAlarm/EspAlarm.config $(BIN_DIR)/iotcode/EspAlarm
	cd $(BIN_DIR)/iotcode/EspAlarm; $(JAR) $(JARFLAGS) EspAlarm.jar ../../iotcode/EspAlarm/*.class ../../iotcode/interfaces/Alarm*.class ../../iotcode/interfaces/ZoneState*.class

PHONY += motion
motion:
	$(JAVAC) $(JFLAGS) MotionSensor/*.java
	cp MotionSensor/MotionSensor.config $(BIN_DIR)/iotcode/MotionSensor
	#cd $(BIN_DIR)/iotcode/MotionSensor; $(JAR) $(JARFLAGS) MotionSensor.jar ../../iotcode/MotionSensor/*.class ../../iotcode/interfaces/SmartthingsSensor*.class ../../iotcode/interfaces/Camera*.class ../../IrrigationController/MotionDetection*.class
	cd $(BIN_DIR)/iotcode/MotionSensor; $(JAR) $(JARFLAGS) MotionSensor.jar ../../iotcode/MotionSensor/*.class ../../iotcode/interfaces/SmartthingsSensor*.class ../../iotcode/interfaces/Camera*.class

PHONY += multipurpose
multipurpose:
	$(JAVAC) $(JFLAGS) MultipurposeSensor/*.java
	cp MultipurposeSensor/MultipurposeSensor.config $(BIN_DIR)/iotcode/MultipurposeSensor
	#cd $(BIN_DIR)/iotcode/MultipurposeSensor; $(JAR) $(JARFLAGS) MultipurposeSensor.jar ../../iotcode/MultipurposeSensor/*.class ../../iotcode/interfaces/SmartthingsSensor*.class ../../iotcode/interfaces/Camera*.class ../../IrrigationController/MotionDetection*.class
	cd $(BIN_DIR)/iotcode/MultipurposeSensor; $(JAR) $(JARFLAGS) MultipurposeSensor.jar ../../iotcode/MultipurposeSensor/*.class ../../iotcode/interfaces/SmartthingsSensor*.class ../../iotcode/interfaces/Camera*.class

PHONY += waterleak
waterleak:
	$(JAVAC) $(JFLAGS) WaterLeakSensor/*.java
	cp WaterLeakSensor/WaterLeakSensor.config $(BIN_DIR)/iotcode/WaterLeakSensor
	#cd $(BIN_DIR)/iotcode/WaterLeakSensor; $(JAR) $(JARFLAGS) WaterLeakSensor.jar ../../iotcode/WaterLeakSensor/*.class ../../iotcode/interfaces/SmartthingsSensor*.class ../../iotcode/interfaces/Camera*.class ../../IrrigationController/MotionDetection*.class
	cd $(BIN_DIR)/iotcode/WaterLeakSensor; $(JAR) $(JARFLAGS) WaterLeakSensor.jar ../../iotcode/WaterLeakSensor/*.class ../../iotcode/interfaces/SmartthingsSensor*.class ../../iotcode/interfaces/Camera*.class

PHONY += doorlock
doorlock:
	$(JAVAC) $(JFLAGS) DoorlockSensor/*.java
	cp DoorlockSensor/DoorlockSensor.config $(BIN_DIR)/iotcode/DoorlockSensor
	cd $(BIN_DIR)/iotcode/DoorlockSensor; $(JAR) $(JARFLAGS) DoorlockSensor.jar ../../iotcode/DoorlockSensor/*.class ../../iotcode/interfaces/SmartthingsSensor*.class ../../iotcode/interfaces/Camera*.class ../../iotcode/interfaces/DoorLock*.class

# Compile - with checker
#
PHONY += check-light
check-light:
	$(JAVAC) $(JFLAGS) $(CHECKER_OPT) $(ASTUBS) LifxLightBulb/*.java
	cp LifxLightBulb/LifxLightBulb.config $(BIN_DIR)/iotcode/LifxLightBulb
	cd $(BIN_DIR)/iotcode/LifxLightBulb; $(JAR) $(JARFLAGS) LifxLightBulb.jar ../../iotcode/LifxLightBulb/*.class ../../iotcode/interfaces/LightBulb*.class

PHONY += check-camera
check-camera:
	$(JAVAC) $(JFLAGS) $(CHECKER_OPT) $(ASTUBS) AmcrestCamera/*.java
	cp AmcrestCamera/AmcrestCamera.config $(BIN_DIR)/iotcode/AmcrestCamera
	cd $(BIN_DIR)/iotcode/AmcrestCamera; $(JAR) $(JARFLAGS) AmcrestCamera.jar ../../iotcode/AmcrestCamera/*.class ../../iotcode/interfaces/Camera*.class ../../iotcode/interfaces/Resolution*.class

PHONY += check-labroom
check-labroom:
	$(JAVAC) $(JFLAGS) $(CHECKER_OPT) $(ASTUBS) LabRoom/*.java
	cp LabRoom/LabRoom.config $(BIN_DIR)/iotcode/LabRoom
	cd $(BIN_DIR)/iotcode/LabRoom; $(JAR) $(JARFLAGS) LabRoom.jar ../../iotcode/LabRoom/*.class ../../iotcode/interfaces/Room*.class

PHONY += check-greenlawn
check-greenlawn:
	$(JAVAC) $(JFLAGS) $(CHECKER_OPT) $(ASTUBS) GreenLawn/*.java
	cp GreenLawn/GreenLawn.config $(BIN_DIR)/iotcode/GreenLawn
	cd $(BIN_DIR)/iotcode/GreenLawn; $(JAR) $(JARFLAGS) GreenLawn.jar ../../iotcode/GreenLawn/*.class ../../iotcode/interfaces/Lawn*.class

PHONY += check-sprinkler
check-sprinkler:
	$(JAVAC) $(JFLAGS) $(CHECKER_OPT) $(ASTUBS) EspSprinkler/*.java
	cp EspSprinkler/EspSprinkler.config $(BIN_DIR)/iotcode/EspSprinkler
	cd $(BIN_DIR)/iotcode/EspSprinkler; $(JAR) $(JARFLAGS) EspSprinkler.jar ../../iotcode/EspSprinkler/*.class ../../iotcode/interfaces/Sprinkler*.class ../../iotcode/interfaces/ZoneState*.class

PHONY += check-moisture
check-moisture:
	$(JAVAC) $(JFLAGS) $(CHECKER_OPT) $(ASTUBS) SpruceSensor/*.java
	cp SpruceSensor/SpruceSensor.config $(BIN_DIR)/iotcode/SpruceSensor
	#cd $(BIN_DIR)/iotcode/SpruceSensor; $(JAR) $(JARFLAGS) SpruceSensor.jar ../../iotcode/SpruceSensor/*.class ../../iotcode/interfaces/MoistureSensor*.class ../../iotcode/interfaces/Camera*.class ../../IrrigationController/MotionDetection*.class
	cd $(BIN_DIR)/iotcode/SpruceSensor; $(JAR) $(JARFLAGS) SpruceSensor.jar ../../iotcode/SpruceSensor/*.class ../../iotcode/interfaces/MoistureSensor*.class ../../iotcode/interfaces/Camera*.class

PHONY += check-weathergateway
check-weathergateway:
	$(JAVAC) $(JFLAGS) $(CHECKER_OPT) $(ASTUBS) WeatherPhoneGateway/*.java
	cp WeatherPhoneGateway/WeatherPhoneGateway.config $(BIN_DIR)/iotcode/WeatherPhoneGateway
	cd $(BIN_DIR)/iotcode/WeatherPhoneGateway; $(JAR) $(JARFLAGS) WeatherPhoneGateway.jar ../../iotcode/WeatherPhoneGateway/*.class ../../iotcode/interfaces/WeatherGateway*.class

PHONY += check-audioroom
check-audioroom:
	$(JAVAC) $(JFLAGS) $(CHECKER_OPT) $(ASTUBS) AudioRoom/*.java
	cp AudioRoom/AudioRoom.config $(BIN_DIR)/iotcode/AudioRoom
	cd $(BIN_DIR)/iotcode/AudioRoom; $(JAR) $(JARFLAGS) AudioRoom.jar ../../iotcode/AudioRoom/*.class ../../iotcode/interfaces/Room*.class

PHONY += check-gpsgateway
check-gpsgateway:
	$(JAVAC) $(JFLAGS) $(CHECKER_OPT) $(ASTUBS) GPSPhoneGateway/*.java
	cp GPSPhoneGateway/GPSPhoneGateway.config $(BIN_DIR)/iotcode/GPSPhoneGateway
	cd $(BIN_DIR)/iotcode/GPSPhoneGateway; $(JAR) $(JARFLAGS) GPSPhoneGateway.jar ../../iotcode/GPSPhoneGateway/*.class ../../iotcode/interfaces/GPSGateway*.class

PHONY += check-ihome
check-ihome:
	$(JAVAC) $(JFLAGS) $(CHECKER_OPT) $(ASTUBS) IHome/*.java
	cp IHome/IHome.config $(BIN_DIR)/iotcode/IHome
	cd $(BIN_DIR)/iotcode/IHome; $(JAR) $(JARFLAGS) IHome.jar ../../iotcode/IHome/*.class ../../iotcode/interfaces/Speaker*.class

PHONY += check-homeroom
check-homeroom:
	$(JAVAC) $(JFLAGS) $(CHECKER_OPT) $(ASTUBS) HomeRoom/*.java
	cp HomeRoom/HomeRoom.config $(BIN_DIR)/iotcode/HomeRoom
	cd $(BIN_DIR)/iotcode/HomeRoom; $(JAR) $(JARFLAGS) HomeRoom.jar ../../iotcode/HomeRoom/*.class ../../iotcode/interfaces/Room*.class

PHONY += check-alarm
check-alarm:
	$(JAVAC) $(JFLAGS) $(CHECKER_OPT) $(ASTUBS) EspAlarm/*.java
	cp EspAlarm/EspAlarm.config $(BIN_DIR)/iotcode/EspAlarm
	cd $(BIN_DIR)/iotcode/EspAlarm; $(JAR) $(JARFLAGS) EspAlarm.jar ../../iotcode/EspAlarm/*.class ../../iotcode/interfaces/Alarm*.class ../../iotcode/interfaces/ZoneState*.class

PHONY += check-motion
check-motion:
	$(JAVAC) $(JFLAGS) $(CHECKER_OPT) $(ASTUBS) MotionSensor/*.java
	cp MotionSensor/MotionSensor.config $(BIN_DIR)/iotcode/MotionSensor
	cd $(BIN_DIR)/iotcode/MotionSensor; $(JAR) $(JARFLAGS) MotionSensor.jar ../../iotcode/MotionSensor/*.class ../../iotcode/interfaces/SmartthingsSensor*.class ../../iotcode/interfaces/Camera*.class ../../IrrigationController/MotionDetection*.class

PHONY += check-multipurpose
check-multipurpose:
	$(JAVAC) $(JFLAGS) $(CHECKER_OPT) $(ASTUBS) MultipurposeSensor/*.java
	cp MultipurposeSensor/MultipurposeSensor.config $(BIN_DIR)/iotcode/MultipurposeSensor
	cd $(BIN_DIR)/iotcode/MultipurposeSensor; $(JAR) $(JARFLAGS) MultipurposeSensor.jar ../../iotcode/MultipurposeSensor/*.class ../../iotcode/interfaces/SmartthingsSensor*.class ../../iotcode/interfaces/Camera*.class ../../IrrigationController/MotionDetection*.class

PHONY += check-waterleak
check-waterleak:
	$(JAVAC) $(JFLAGS) $(CHECKER_OPT) $(ASTUBS) WaterLeakSensor/*.java
	cp WaterLeakSensor/WaterLeakSensor.config $(BIN_DIR)/iotcode/WaterLeakSensor
	cd $(BIN_DIR)/iotcode/WaterLeakSensor; $(JAR) $(JARFLAGS) WaterLeakSensor.jar ../../iotcode/WaterLeakSensor/*.class ../../iotcode/interfaces/SmartthingsSensor*.class ../../iotcode/interfaces/Camera*.class ../../IrrigationController/MotionDetection*.class

PHONY += check-doorlock
check-doorlock:
	$(JAVAC) $(JFLAGS) $(CHECKER_OPT) $(ASTUBS) DoorlockSensor/*.java
	cp DoorlockSensor/DoorlockSensor.config $(BIN_DIR)/iotcode/DoorlockSensor
	cd $(BIN_DIR)/iotcode/DoorlockSensor; $(JAR) $(JARFLAGS) DoorlockSensor.jar ../../iotcode/DoorlockSensor/*.class ../../iotcode/interfaces/SmartthingsSensor*.class ../../iotcode/interfaces/Camera*.class

.PHONY: $(PHONY)
